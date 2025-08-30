package dev.quentintyr.embellishedtooltips.client.render;

import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.render.sidepanel.ItemRenderer3D;
import dev.quentintyr.embellishedtooltips.client.render.sidepanel.MapRenderer;
import dev.quentintyr.embellishedtooltips.client.render.sidepanel.PaintingRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import org.joml.Quaternionf;

import java.awt.Point;

final class TooltipSidePanel {
    static ArmorStandEntity renderStandRef;

    static Vec2f renderSecondPanel(TooltipContext ec, Vec2f tooltipPos, Point tooltipSize,
            TooltipStylePipeline pipelineOwner) {
        final float gap = 6.0f;
        final Point panelSize = new Point(36, 72);
        float leftX = tooltipPos.x - gap - panelSize.x;
        float placedX = leftX >= 4 ? leftX : tooltipPos.x + tooltipSize.x + gap;
        Vec2f panelPos = new Vec2f(placedX, tooltipPos.y);
        ec.drawManaged(() -> {
            if (TooltipStylePipeline.renderStyleRef != null)
                TooltipStylePipeline.renderStyleRef.renderBack(ec, panelPos, panelSize, false);
            StyleManager.getInstance().getDefaultStyle().renderBack(ec, panelPos, panelSize, false);
        });
        return new Vec2f(panelPos.x + panelSize.x / 2.0f, panelPos.y + panelSize.y / 2.0f);
    }

    static void renderStand(DrawContext ctx, int x, int y) {
        if (renderStandRef == null)
            return;
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(x, y, 500.0F);
        ms.scale(-30.0F, -30.0F, 30.0F);
        ms.multiply(new Quaternionf().rotationX((float) Math.toRadians(25.0f)));
        float spin = (float) (((System.currentTimeMillis() / 1000.0) % 360.0) * Math.toRadians(20.0));
        ms.multiply(new Quaternionf().rotationY(spin));

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders()
                .getEntityVertexConsumers();
        DiffuseLighting.enableGuiDepthLighting();
        MinecraftClient.getInstance().getEntityRenderDispatcher().render(renderStandRef, 0, 0, 0, 0, 1.0f, ms,
                immediate, 15728880);
        immediate.draw();
        DiffuseLighting.disableGuiDepthLighting();
        ms.pop();
    }

    static void renderSpinningItem(DrawContext ctx, ItemStack stack, Vec2f center) {
        ItemRenderer3D.renderSpinningItem(ctx, stack, center);
    }

    static void renderMapPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        MapRenderer.renderMapPreview(ctx, stack, center);
    }

    static void renderPaintingPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        PaintingRenderer.renderPaintingPreview(ctx, stack, center);
    }
}
