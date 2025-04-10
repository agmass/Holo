package org.agmas.holo;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.agmas.holo.item.HologramSpawnEgg;
import org.agmas.holo.util.HologramType;

public class ModItems {

    public static final Item HOLOGRAM_SPAWN_EGG = register(
            new HologramSpawnEgg(new FabricItemSettings().fireproof(), HologramType.NORMAL),
            "hologram_spawn_egg"
    );

    public static final Item BATTLE_HOLOGRAM_SPAWN_EGG = register(
            new HologramSpawnEgg(new FabricItemSettings().fireproof(), HologramType.BATTLE),
            "battle_hologram_spawn_egg"
    );


    public static final Item SCOUT_HOLOGRAM_SPAWN_EGG = register(
            new HologramSpawnEgg(new FabricItemSettings().fireproof(), HologramType.SCOUT),
            "scout_hologram_spawn_egg"
    );


    public static final Item SILENT_HOLOGRAM_SPAWN_EGG = register(
            new HologramSpawnEgg(new FabricItemSettings().fireproof(), HologramType.SILENT),
            "silent_hologram_spawn_egg"
    );


    public static final Item ENDER_HOLOGRAM_SPAWN_EGG = register(
            new HologramSpawnEgg(new FabricItemSettings().fireproof(), HologramType.ENDER),
            "ender_hologram_spawn_egg"
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
