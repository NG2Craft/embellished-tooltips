package dev.quentintyr.embellishedtooltips.client.render.sidepanel;

import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.render.TooltipStylePipeline;
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

public final class TooltipSidePanel {
    public static ArmorStandEntity renderStandRef;

    public static Vec2f renderSecondPanel(TooltipContext ec, Vec2f tooltipPos, Point tooltipSize,
            TooltipStylePipeline pipelineOwner, int mouseX, int mouseY, int screenW, int screenH) {
        final float gap = 6.0f;
        final Point panelSize = new Point(64, 64); // Square panel for better map display

        // Position panel to the right of cursor, similar to main tooltip positioning
        float panelX = mouseX + 12 + gap; // Same base offset as main tooltip, plus gap
        if (panelX + panelSize.x > screenW - 4) {
            // If doesn't fit on right, place on left of cursor
            panelX = mouseX - 16 - gap - panelSize.x;
        }
        panelX = Math.max(4, Math.min(panelX, screenW - panelSize.x - 4));

        float panelY = mouseY - 12; // Same Y offset as main tooltip
        panelY = Math.max(4, Math.min(panelY, screenH - panelSize.y - 4));

        Vec2f panelPos = new Vec2f(panelX, panelY);
        ec.drawManaged(() -> {
            if (TooltipStylePipeline.renderStyleRef != null)
                TooltipStylePipeline.renderStyleRef.renderBack(ec, panelPos, panelSize, false);
            StyleManager.getInstance().getDefaultStyle().renderBack(ec, panelPos, panelSize, false);
        });
        return new Vec2f(panelPos.x + panelSize.x / 2.0f, panelPos.y + panelSize.y / 2.0f);
    }

    public static void renderStand(DrawContext ctx, int x, int y) {
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

    public static void renderSpinningItem(DrawContext ctx, ItemStack stack, Vec2f center) {
        ItemRenderer3D.renderSpinningItem(ctx, stack, center);
    }

    public static void renderMapPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        MapRenderer.renderMapPreview(ctx, stack, center);
    }

    public static void renderPaintingPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        PaintingRenderer.renderPaintingPreview(ctx, stack, center);
    }
}
