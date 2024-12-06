package org.agmas.holo;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.agmas.holo.item.HologramSpawnEgg;

public class ModItems {

    public static final Item HOLOGRAM_SPAWN_EGG = register(
            new HologramSpawnEgg(new FabricItemSettings().fireproof()),
            "hologram_spawn_egg"
    );


    public static Item register(Item item, String id) {
        // Create the identifier for the item.
        Identifier itemID = new Identifier(Holo.MOD_ID, id);

        // Register the item.
        Item registeredItem = Registry.register(Registries.ITEM, itemID, item);

        // Return the registered item!
        return registeredItem;
    }

    public static void initialize() {
    }
}
