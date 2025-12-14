package me.PirotKiller.alastor.client;

import me.PirotKiller.alastor.client.entity.ModEntities;
import me.PirotKiller.alastor.client.entity.client.AlastorModel;
import me.PirotKiller.alastor.client.entity.client.AlastorRenderer;
import me.PirotKiller.alastor.client.entity.custom.AlastorEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlastorClient implements ClientModInitializer {
    public static final String MOD_ID = "alastor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitializeClient() {
        FabricDefaultAttributeRegistry.register(ModEntities.ALASTOR, AlastorEntity.createAttributes());

        ModEntities.registerModEntities();

        EntityModelLayerRegistry.registerModelLayer(AlastorModel.ALASTOR,AlastorModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.ALASTOR, AlastorRenderer::new);
    }
}
