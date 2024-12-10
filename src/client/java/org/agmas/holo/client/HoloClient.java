package org.agmas.holo.client;

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
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import org.agmas.holo.Holo;
import org.agmas.holo.ModEntities;
import org.agmas.holo.client.models.WardenHornsFeatureRenderer;
import org.agmas.holo.client.models.WardensHorns;
import org.agmas.holo.state.StateSaverAndLoader;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.UUID;

public class HoloClient implements ClientModInitializer {

    public static ArrayList<UUID> playersInHolo = new ArrayList<>();
    private static KeyBinding keyBinding;

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
        ClientPlayNetworking.registerGlobalReceiver(Holo.HUMAN_MODE, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            playersInHolo.remove(uuid);
        });
        ClientPlayNetworking.registerGlobalReceiver(Holo.HOLO_MODE, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            if (!playersInHolo.contains(uuid))
                playersInHolo.add(uuid);
        });
        ClientPlayConnectionEvents.DISCONNECT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            playersInHolo.clear();
        }));



        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                PacketByteBuf data = PacketByteBufs.create();
                client.execute(() -> {
                    ClientPlayNetworking.send(Holo.SWAP_PACKET, data);
                });
            }
        });
    }
}
