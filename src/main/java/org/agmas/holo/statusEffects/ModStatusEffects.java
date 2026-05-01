package org.agmas.holo.statusEffects;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.agmas.holo.Holo;

public class ModStatusEffects {
    public static final DoomedStatusEffect DOOMED = new DoomedStatusEffect();
    public static void init() {
        Registry.register(Registries.STATUS_EFFECT, Identifier.of(Holo.MOD_ID, "doomed"), DOOMED);
    }
}
