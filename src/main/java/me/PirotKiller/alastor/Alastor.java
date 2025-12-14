package me.PirotKiller.alastor;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Alastor implements ModInitializer {
    public static final String MOD_ID = "alastor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        me.PirotKiller.alastor.entity.ModEntities.registerModEntities();
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(
                me.PirotKiller.alastor.entity.ModEntities.ALASTOR,
                me.PirotKiller.alastor.entity.custom.AlastorEntity.createAttributes());
    }
}
