package dev.quentintyr.embellishedtooltips.client.render.painting;

import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.render.TooltipStylePipeline;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;

import java.awt.Point;

public final class PaintingSidePanel {

    public static Vec2f renderPaintingPanel(TooltipContext ec, Vec2f tooltipPos, Point tooltipSize,
            TooltipStylePipeline pipelineOwner, int mouseX, int mouseY, int screenW, int screenH,
            boolean isTooltipOnLeft) {
        final float gap = 6.0f;

        // Dynamic panel size for paintings - wider to accommodate different aspect
        // ratios
        final Point panelSize = new Point(80, 64); // Wider panel for better painting display

        float panelX, panelY;

        if (isTooltipOnLeft) {
            panelX = mouseX - 16 - gap - panelSize.x;
            panelX = Math.max(4, panelX);
        } else {
            panelX = mouseX + 12 + gap;
            if (panelX + panelSize.x > screenW - 4) {
                panelX = screenW - panelSize.x - 4;
            }
        }

        panelY = mouseY - 12;
        panelY = Math.max(4, Math.min(panelY, screenH - panelSize.y - 4));

        Vec2f panelPos = new Vec2f(panelX, panelY);
        ec.drawManaged(() -> {
            if (TooltipStylePipeline.renderStyleRef != null)
                TooltipStylePipeline.renderStyleRef.renderBack(ec, panelPos, panelSize, false);
            StyleManager.getInstance().getDefaultStyle().renderBack(ec, panelPos, panelSize, false);
        });
        return new Vec2f(panelPos.x + panelSize.x / 2.0f, panelPos.y + panelSize.y / 2.0f);
    }

    public static void renderPaintingPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        PaintingRenderer.renderPaintingPreview(ctx, stack, center);
    }
}
