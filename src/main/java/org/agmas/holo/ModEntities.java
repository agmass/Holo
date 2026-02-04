package org.agmas.holo;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.agmas.holo.block.entity.HologramControllerBlockEntity;

public class ModEntities {


    public static final BlockEntityType<HologramControllerBlockEntity> HOLOGRAM_CONTROLLER_BLOCK_ENTITY = blockEntityRegister(
            "hologram_controller_block_entity",
            FabricBlockEntityTypeBuilder.create(HologramControllerBlockEntity::new, ModBlocks.HOLOGRAM_CONTROLLER, ModBlocks.BATTLE_HOLOGRAM_CONTROLLER).build()
    );

    private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, Identifier.of(Holo.MOD_ID, id), type.build(id));
    }
    public static <T extends BlockEntityType<?>> T blockEntityRegister(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(Holo.MOD_ID, path), blockEntityType);
    }


    public static void init() {}

}
