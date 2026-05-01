package org.agmas.holo.statusEffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

import java.awt.*;

public class DoomedStatusEffect extends StatusEffect {
    public DoomedStatusEffect() {
        super(
                StatusEffectCategory.BENEFICIAL,
                new Color(255,100,100).getRGB());
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        return true;
    }
}