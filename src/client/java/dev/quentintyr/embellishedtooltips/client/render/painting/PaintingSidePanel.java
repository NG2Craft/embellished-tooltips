package dev.quentintyr.embellishedtooltips.client.render.painting;

import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.render.TooltipStylePipeline;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;

import java.awt.Point;

/**
 * Side panel placement and background rendering for painting previews.
 */
public final class PaintingSidePanel {

    private PaintingSidePanel() {
    }

    /**
     * Renders the panel background and returns the visual center for content.
     */
    public static Vec2f renderPaintingPanel(
            TooltipContext ec,
            Vec2f tooltipPos,
            Point tooltipSize,
            TooltipStylePipeline pipelineOwner,
            int mouseX,
            int mouseY,
            int screenW,
            int screenH,
            boolean isTooltipOnLeft) {

        // Dynamic panel size based on painting variant; default falls back to ~64x64
        final Point panelSize = PaintingRenderer.computePanelSize(ec.stack());

        float panelX, panelY;

        if (isTooltipOnLeft) {
            panelX = mouseX - 12 - panelSize.x; // place to the left of cursor
        } else {
            panelX = mouseX + 12; // place to the right of cursor
        }

        panelX = Math.max(4, Math.min(panelX, screenW - panelSize.x - 4));
        panelY = mouseY - 12;
        panelY = Math.max(4, Math.min(panelY, screenH - panelSize.y - 4));

        Vec2f panelPos = new Vec2f(panelX, panelY);

        // Render panel background using current style (and default as fallback)
        ec.drawManaged(() -> {
            if (TooltipStylePipeline.renderStyleRef != null)
                TooltipStylePipeline.renderStyleRef.renderBack(ec, panelPos, panelSize, false);
            StyleManager.getInstance().getDefaultStyle().renderBack(ec, panelPos, panelSize, false);
        });

        return new Vec2f(panelPos.x + panelSize.x / 2.0f, panelPos.y + panelSize.y / 2.0f);
    }

    /**
     * Draw the painting preview centered inside the panel.
     */
    public static void renderPaintingPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        // Use same dynamic panel sizing here to ensure correct centering
        Point panelSize = PaintingRenderer.computePanelSize(stack);
        PaintingRenderer.renderPaintingPreview(ctx, stack, center, panelSize);
    }
}
