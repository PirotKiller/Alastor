package me.PirotKiller.alastor.entity;

import me.PirotKiller.alastor.Alastor;
import me.PirotKiller.alastor.entity.custom.AlastorEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<AlastorEntity> ALASTOR = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(Alastor.MOD_ID, "alastor"),
            EntityType.Builder.create(AlastorEntity::new, SpawnGroup.CREATURE)
                    .dimensions(1f, 2.5f).build());

    public static void registerModEntities() {
        Alastor.LOGGER.info("Registering Mod Entities for " + Alastor.MOD_ID);
    }
}
