package org.agmas.holo.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.agmas.holo.state.HoloPlayerComponent;
import org.agmas.holo.util.HologramType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TridentEntity.class)
public class NoTridentDupingMixin extends PersistentProjectileEntity {
    protected NoTridentDupingMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapMethod(method = "tick")
    public void sendShellUpdate(Operation<Void> original) {


        if (HoloPlayerComponent.KEY.get(getOwner()).hologramType.equals(HologramType.BATTLE_DUEL)) {
            discard();
            return;
        }
        original.call();
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return null;
    }
}
