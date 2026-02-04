package org.agmas.holo.client;

import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.platform.VeilEventPlatform;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.agmas.holidaylib.client.HolidaylibClient;
import org.agmas.holidaylib.client.events.ModifyPlayerRenderLayer;
import org.agmas.holidaylib.client.events.ModifyPlayerSkinTint;
import org.agmas.holo.Holo;
import org.agmas.holo.ModEntities;
import org.agmas.holo.client.blockEntities.HologramControllerBlockEntityRenderer;
import org.agmas.holo.client.config.HoloConfig;
import org.agmas.holo.client.models.HoloLightRenderer;
import org.agmas.holo.client.models.WardenHornsFeatureRenderer;
import org.agmas.holo.client.models.WardensHorns;
import org.agmas.holo.client.screen.TerminalChatScreen;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HologramType;
import org.agmas.holo.util.payloads.*;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.text.Format;
import java.util.*;
import java.util.List;

public class HoloClient implements ClientModInitializer {

    public static HashMap<UUID, HologramType> playersInHolo = new HashMap<>();
    public static HashMap<UUID, Integer> shownEntities = new HashMap<>();
    private static KeyBinding keyBinding;
    public static KeyBinding terminalBind;
    public static HologramType hologramType = null;
    public static Color HOLO_COLOR = new Color(128,128,255,255);
    public static Color SILENT_HOLO_COLOR = new Color(36, 36, 122,255);
    public static Color BATTLE_HOLO_COLOR = new Color(255, 144, 166,255);
    public static Color SCOUT_HOLO_COLOR = new Color(255, 240, 144,255);
    private static final Identifier CUSTOM_POST_SHADER = Identifier.of("holo", "silent");

    public static int hostHealth = 0;
    public static int power = 0;
    public static int maxPower = 555;
    public static int playersInDuel = 0;
    public static String holoName = "";
    int timesTriedToOpenTerminalAsABattleDuelHologram = 0;

    float ticks = 0;
    float noiseTime = 0;
    public LightRenderHandle<PointLightData> annoyingLightThatStopsShadersFromRecompiling;
    public LightRenderHandle<PointLightData> clientLight;

    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(ModEntities.HOLOGRAM_CONTROLLER_BLOCK_ENTITY, HologramControllerBlockEntityRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(WardensHorns.MODEL_LAYER, WardensHorns::getTexturedModelData);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType.equals(EntityType.PLAYER)) {
                registrationHelper.register(new WardenHornsFeatureRenderer((FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>) entityRenderer, context.getModelLoader()));
                registrationHelper.register(new HoloLightRenderer((FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>) entityRenderer, context.getModelLoader()));
            }
        }));

        ModifyPlayerRenderLayer.EVENT.register((player,texture)->{

            if (HoloClient.playersInHolo.containsKey(player.getUuid()) || HoloPlayerComponent.KEY.get(player).inHoloMode) {
                ModifyPlayerRenderLayer.Entry entry = new ModifyPlayerRenderLayer.Entry();
                entry.layer = HolidaylibClient.shaderFallbackLayer(VeilRenderType.get(Identifier.of("holo","scanline"),texture), HolidaylibClient.translucentMaskedEmissiveRenderLayer(texture));
                entry.shaderIdentifier = Identifier.of("holo", "scanline");
                entry.priority = 1500;
                return entry;
            }
            return null;
        });
        ModifyPlayerSkinTint.EVENT.register((player)->{
            if (HoloClient.playersInHolo.containsKey(player.getUuid()) || HoloPlayerComponent.KEY.get(player).inHoloMode) {
                HologramType type = HoloPlayerComponent.KEY.get(player).hologramType;
                if (HoloClient.playersInHolo.containsKey(player.getUuid())) {
                    type = HoloClient.playersInHolo.get(player.getUuid());
                }
                Color color;
                switch (type) {
                    case SILENT -> color = SILENT_HOLO_COLOR;
                    case BATTLE, BATTLE_DUEL -> color = BATTLE_HOLO_COLOR;
                    case SCOUT -> color = SCOUT_HOLO_COLOR;
                    default -> color = HOLO_COLOR;
                }
                return HolidaylibClient.shaderFallbackColor(color, new Color(color.getRed(),color.getBlue(),color.getGreen(),128));
            }
            return null;
        });

        AutoConfig.register(HoloConfig.class, JanksonConfigSerializer::new);


        ClientPlayConnectionEvents.DISCONNECT.register((clientPlayNetworkHandler,client)->{
            HoloConfig config = AutoConfig.getConfigHolder(HoloConfig.class).getConfig();
            if (config.useVeilLights) {
            clientLight.free();
            annoyingLightThatStopsShadersFromRecompiling.free();
            }
        });
        WorldRenderEvents.AFTER_ENTITIES.register((t)->{
            HoloConfig config = AutoConfig.getConfigHolder(HoloConfig.class).getConfig();
            if (config.useVeilLights) {
                if (clientLight == null) {
                    PointLightData pointLightData = new PointLightData();
                    clientLight = VeilRenderSystem.renderer().getLightRenderer().addLight(pointLightData);
                    clientLight.getLightData().setColor(Colors.WHITE);
                    clientLight.getLightData().setRadius(100000f);
                    clientLight.getLightData().setBrightness(0.0001f);
                }
                HoloLightRenderer.changeLightWithPlayer(MinecraftClient.getInstance().player, clientLight, t.tickCounter().getTickDelta(true));
                if (annoyingLightThatStopsShadersFromRecompiling == null) {
                    PointLightData pointLightData = new PointLightData();
                    annoyingLightThatStopsShadersFromRecompiling = VeilRenderSystem.renderer().getLightRenderer().addLight(pointLightData);
                    annoyingLightThatStopsShadersFromRecompiling.getLightData().setColor(Colors.WHITE);
                    annoyingLightThatStopsShadersFromRecompiling.getLightData().setRadius(100000f);
                    annoyingLightThatStopsShadersFromRecompiling.getLightData().setBrightness(0.0001f);
                }
                Vec3d d = MinecraftClient.getInstance().player.getCameraPosVec(0f).add(MinecraftClient.getInstance().player.getRotationVecClient().multiply(0.25f));
                annoyingLightThatStopsShadersFromRecompiling.getLightData().setPosition(d.x, d.y, d.z);
            }
            ticks += MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration();
            ShaderProgram shader = VeilRenderSystem.setShader(Identifier.of(Holo.MOD_ID, "scanline"));

            if (shader == null) {
                return;
            }

            shader.getOrCreateUniform("STime").setFloat(ticks);
            if (ticks >= 60) {
                ticks = 0;
            }
        });
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.holo.swap", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_B, // The keycode of the key
                "category.holo.hologramcontrols" // The translation key of the keybinding's category.
        ));
        terminalBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.holo.openTerminal", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_U, // The keycode of the key
                "category.holo.hologramcontrols" // The translation key of the keybinding's category.
        ));

        ClientPlayNetworking.registerGlobalReceiver(Holo.SEND_TERMINAL_AUTOCOMPLETE, (packet,context) -> {
            TerminalChatScreen.possibleSuggestions.clear();
            TerminalChatScreen.possibleSuggestions.addAll(packet.commands());
            TerminalChatScreen.refresh = true;
        });
        VeilEventPlatform.INSTANCE.preVeilPostProcessing((pipelineName, pipeline, context) -> {
            if (CUSTOM_POST_SHADER.equals(pipelineName)) {
                ShaderProgram shader = context.getShader(CUSTOM_POST_SHADER);
                if (shader != null) {
                    if (MinecraftClient.getInstance().currentScreen instanceof TerminalChatScreen) {
                        shader.getOrCreateUniform("shouldRender").setInt(1);
                    } else if (hologramType != null && hologramType.equals(HologramType.SILENT)) {
                        shader.getOrCreateUniform("shouldRender").setInt(2);
                    } else {
                        shader.getOrCreateUniform("shouldRender").setInt(0);
                    }
                    shader.getOrCreateUniform("noiseTime").setFloat(noiseTime);
                    noiseTime -= MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration()/20;
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(Holo.HOLO_STATUS_INFO, (packet,context) -> {
            hostHealth = packet.hostHealth();
            power = packet.power();
            maxPower = packet.maxPower();
            holoName = packet.holoName();
            playersInDuel = packet.playersAliveInDuel();
        });
        ClientPlayNetworking.registerGlobalReceiver(Holo.TEMPORARILY_SHOW_ENTITY, (packet,context) -> {
            shownEntities.put(packet.entity(), 20 * 7);
        });
        ClientPlayNetworking.registerGlobalReceiver(Holo.HOLO_MODE, (packet,context) -> {
            UUID uuid = packet.player();
            HologramType type = packet.hologramType();
            if (type != HologramType.HUMAN) {
                playersInHolo.put(uuid, type);
                if (context.player() != null) {
                    if (uuid.equals(context.player().getUuid())) {
                        VeilRenderSystem.renderer().getPostProcessingManager().add(CUSTOM_POST_SHADER);
                        hologramType = type;
                        noiseTime = 1;
                        context.player().playSound(Holo.holo_switch,1,new Random().nextFloat(0.9f,1.1f));
                    }
                }
            } else {
                if (context.player() != null) {
                    if (uuid.equals(context.player().getUuid())) {
                        VeilRenderSystem.renderer().getPostProcessingManager().remove(CUSTOM_POST_SHADER);
                        hologramType = null;
                        MinecraftClient.getInstance().getWindow().setFramerateLimit(MinecraftClient.getInstance().options.getMaxFps().getValue());
                    }
                }
                playersInHolo.remove(uuid);

            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            playersInHolo.clear();
            shownEntities.clear();
            hologramType = null;
            //minecraftClient.getWindow().setFramerateLimit(minecraftClient.options.getMaxFps().getValue());
        }));


        List<String> angrierBattleDuelWarning = List.of("You can't use the terminal in a duel hologram.", "You CAN'T use the TERMINAL in a DUEL HOLOGRAM!", "STOP TRYING TO USE THE TERMINAL!!", "Are you stupid", "Fine. I'll let you use the terminal if you click the button 100 times in a row.");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            shownEntities.keySet().removeIf((k)->{
                shownEntities.put(k, shownEntities.get(k)-1);
                return shownEntities.get(k) <= 0;
            });
            if (hologramType != null) {
                if (hologramType != HologramType.BATTLE_DUEL) {
                    while (terminalBind.wasPressed()) {
                        PacketByteBuf data = PacketByteBufs.create();
                        client.execute(() -> {
                            ClientPlayNetworking.send(new RequestTerminalAutocompleteC2SPacket());
                        });
                        client.setScreen(new TerminalChatScreen(""));
                    }
                } else {

                    while (terminalBind.wasPressed()) {
                        if (timesTriedToOpenTerminalAsABattleDuelHologram < angrierBattleDuelWarning.size()) {
                            if (client.player != null) {
                                client.player.sendMessage(Text.literal(angrierBattleDuelWarning.get(timesTriedToOpenTerminalAsABattleDuelHologram)).formatted(Formatting.RED));
                            }
                        }
                        else  if (timesTriedToOpenTerminalAsABattleDuelHologram == 100) {
                            client.player.sendMessage(Text.literal("Did you really think that would work? ...... I'm setting your framerate to ten.").formatted(Formatting.DARK_RED));
                            client.getWindow().setFramerateLimit(10);
                        } else {
                            client.player.sendMessage(Text.literal("You clicked the button " + timesTriedToOpenTerminalAsABattleDuelHologram + " times.").formatted(Formatting.RED));
                        }
                        timesTriedToOpenTerminalAsABattleDuelHologram++;
                    }
                }
                while (keyBinding.wasPressed()) {
                    PacketByteBuf data = PacketByteBufs.create();
                    client.execute(() -> {
                        ClientPlayNetworking.send(new SwapC2SPacket());
                    });
                }
            }
        });
    }
}