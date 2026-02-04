package org.agmas.holo.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AllayEntity.class)
public class NoAllayMixin {
    @WrapMethod(method = "interactMob")
    public ActionResult sendShellUpdate(PlayerEntity player, Hand hand, Operation<ActionResult> original) {


        if (HoloPlayerComponent.KEY.get(player).hologramType.equals(HologramType.BATTLE_DUEL))
            return ActionResult.CONSUME;
        return original.call(player,hand);
    }
}
