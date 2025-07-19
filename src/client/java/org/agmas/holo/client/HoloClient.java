package org.agmas.holo.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;
import org.agmas.holo.client.models.WardenHornsFeatureRenderer;
import org.agmas.holo.client.models.WardensHorns;
import org.agmas.holo.client.screen.TerminalChatScreen;
import org.agmas.holo.util.HologramType;
import org.agmas.holo.util.payloads.*;
import org.ladysnake.satin.api.event.ShaderEffectRenderCallback;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HoloClient implements ClientModInitializer {

    public static HashMap<UUID, HologramType> playersInHolo = new HashMap<>();
    public static HashMap<UUID, Integer> shownEntities = new HashMap<>();
    private static KeyBinding keyBinding;
    public static KeyBinding terminalBind;
    public static HologramType hologramType = null;
    public static int HOLO_COLOR = new Color(191,191,255,128).getRGB();
    private static final ManagedShaderEffect GREYSCALE_SHADER = ShaderEffectManager.getInstance()
            .manage(Identifier.of("holo", "shaders/silent.json"));

    public static int hostHealth = 0;
    public static int power = 0;
    public static int maxPower = 555;
    public static int playersInDuel = 0;
    public static String holoName = "";
    int timesTriedToOpenTerminalAsABattleDuelHologram = 0;

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(WardensHorns.MODEL_LAYER, WardensHorns::getTexturedModelData);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType.equals(EntityType.PLAYER)) {
                registrationHelper.register(new WardenHornsFeatureRenderer((FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>) entityRenderer, context.getModelLoader()));
            }
        }));
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
        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
            if ((hologramType != null && hologramType.equals(HologramType.SILENT)) || MinecraftClient.getInstance().currentScreen instanceof TerminalChatScreen) {
                GREYSCALE_SHADER.render(tickDelta);
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
                if (!playersInHolo.containsKey(uuid))
                    playersInHolo.put(uuid, type);
                if (context.player() != null) {
                    if (uuid.equals(context.player().getUuid()))
                        hologramType = type;
                }
            } else {
                hologramType = null;
                playersInHolo.remove(uuid);
                MinecraftClient.getInstance().getWindow().setFramerateLimit(MinecraftClient.getInstance().options.getMaxFps().getValue());

            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            playersInHolo.clear();
            shownEntities.clear();
            hologramType = null;
            minecraftClient.getWindow().setFramerateLimit(minecraftClient.options.getMaxFps().getValue());
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