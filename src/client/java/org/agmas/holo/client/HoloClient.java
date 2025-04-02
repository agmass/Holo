package org.agmas.holo.client;

import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;
import org.agmas.holo.ModEntities;
import org.agmas.holo.client.models.WardenHornsFeatureRenderer;
import org.agmas.holo.client.models.WardensHorns;
import org.agmas.holo.client.screen.DuelComputerScreen;
import org.agmas.holo.client.screen.TerminalChatScreen;
import org.agmas.holo.state.StateSaverAndLoader;
import org.agmas.holo.util.HologramType;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class HoloClient implements ClientModInitializer {

    public static HashMap<UUID, HologramType> playersInHolo = new HashMap<>();
    public static HashMap<UUID, Integer> shownEntities = new HashMap<>();
    private static KeyBinding keyBinding;
    private static KeyBinding terminalBind;
    public static HologramType hologramType = null;
    private static final ManagedShaderEffect GREYSCALE_SHADER = ShaderEffectManager.getInstance()
            .manage(Identifier.of("holo", "shaders/silent.json"));

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

        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
                    ClientPlayNetworking.registerReceiver(Holo.OPEN_BATTLE_COMPUTER_SCREEN, (client, handler, buf, responseSender) -> {
                        MinecraftClient.getInstance().execute(() -> {
                            minecraftClient.setScreen(new DuelComputerScreen());
                        });
                    });
                }));
        ClientPlayNetworking.registerGlobalReceiver(Holo.HUMAN_MODE, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            hologramType = null;
            playersInHolo.remove(uuid);
        });
        ClientPlayNetworking.registerGlobalReceiver(Holo.SEND_TERMINAL_AUTOCOMPLETE, (client, handler, buf, responseSender) -> {
            boolean complete = false;
            TerminalChatScreen.possibleSuggestions.clear();
            while (!complete) {
                try {
                    String s = buf.readString();
                    if (s == null) {
                        complete = true;
                        break;
                    }
                    TerminalChatScreen.possibleSuggestions.add(s);
                    TerminalChatScreen.refresh = true;

                } catch (Exception e) {
                    complete = true;
                }
            }
        });
        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
            if ((hologramType != null && hologramType.equals(HologramType.SILENT)) || MinecraftClient.getInstance().currentScreen instanceof TerminalChatScreen) {
                GREYSCALE_SHADER.render(tickDelta);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(Holo.TEMPORARILY_SHOW_ENTITY, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            shownEntities.put(uuid, 20*7);
        });
        ClientPlayNetworking.registerGlobalReceiver(Holo.HOLO_MODE, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            HologramType type = buf.readEnumConstant(HologramType.class);
            if (!playersInHolo.containsKey(uuid))
                playersInHolo.put(uuid, type);
            if (client.player != null) {
                if (uuid.equals(client.player.getUuid()))
                    hologramType = type;
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            playersInHolo.clear();
            shownEntities.clear();
            hologramType = null;
        }));



        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            shownEntities.keySet().removeIf((k)->{
                shownEntities.put(k, shownEntities.get(k)-1);
                return shownEntities.get(k) <= 0;
            });
            if (hologramType != null) {
                while (terminalBind.wasPressed()) {
                    PacketByteBuf data = PacketByteBufs.create();
                    client.execute(() -> {
                        ClientPlayNetworking.send(Holo.REQUEST_TERMINAL_AUTOCOMPLETE, data);
                    });
                    client.setScreen(new TerminalChatScreen(""));
                }
                while (keyBinding.wasPressed()) {
                    PacketByteBuf data = PacketByteBufs.create();
                    client.execute(() -> {
                        ClientPlayNetworking.send(Holo.SWAP_PACKET, data);
                    });
                }
            }
        });
    }
}
