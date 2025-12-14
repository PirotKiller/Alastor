package me.PirotKiller.alastor.client.entity.client;

import me.PirotKiller.alastor.Alastor;
import me.PirotKiller.alastor.entity.custom.AlastorEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class AlastorRenderer extends MobEntityRenderer<AlastorEntity, AlastorModel<AlastorEntity>> {
    public AlastorRenderer(EntityRendererFactory.Context context) {
        super(context, new AlastorModel<>(context.getPart(AlastorModel.ALASTOR)), 0.75f);
    }

    @Override
    public Identifier getTexture(AlastorEntity entity) {
        if (entity.isDemon()) {
            return Identifier.of(Alastor.MOD_ID, "textures/entity/alastor/alastor_demon.png");
        }
        return Identifier.of(Alastor.MOD_ID, "textures/entity/alastor/alastor.png");
    }

    @Override
    public void render(AlastorEntity livingEntity, float f, float g, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int i) {
        if (livingEntity.isBaby()) {
            matrixStack.scale(0.5f, 0.5f, 0.5f);
        } else {
            matrixStack.scale(1f, 1f, 1f);

        }
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
