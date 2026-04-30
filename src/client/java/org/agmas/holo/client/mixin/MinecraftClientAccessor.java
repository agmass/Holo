package org.agmas.holo.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.WindowProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("thread")
    Thread holo$getThread();
}
