package org.agmas.holo.client.mixin;

import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import org.agmas.holo.client.render.PhoneHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.watermedia.youtube.WaterMediaYT;

@Mixin(WaterMediaYT.class)
public class AllowYoutubeMixin {
    @Inject(remap = false, method = "lambda$start$0", at = @At(value = "INVOKE", target = "Lorg/watermedia/api/media/MediaAPI;registerPlatform(Lorg/watermedia/api/media/platform/IPlatform;Ljava/lang/Class;)V"))
    private static void startyt(CallbackInfo ci) {
        PhoneHolder.canStart = true;
        Log.info(LogCategory.GENERAL, "okay youtube was loaded let's freaking go");
    }
}
