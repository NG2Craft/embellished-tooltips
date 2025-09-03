package dev.quentintyr.embellishedtooltips.client.render.map;

import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.render.TooltipStylePipeline;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;

import java.awt.Point;

public final class MapSidePanel {

    public static Vec2f renderMapPanel(TooltipContext ec, Vec2f tooltipPos, Point tooltipSize,
            TooltipStylePipeline pipelineOwner, int mouseX, int mouseY, int screenW, int screenH,
            boolean isTooltipOnLeft) {
        final Point panelSize = new Point(64, 64); // Square panel for map display

        float panelX, panelY;

        // Position panel based on tooltip side
        if (isTooltipOnLeft) {
            // Tooltip is on left, so panel goes to the left of cursor
            panelX = mouseX - 12 - panelSize.x; // Panel to the left of cursor
        } else {
            // Tooltip is on right, so panel goes to the right of cursor
            panelX = mouseX + 12; // Panel to the right of cursor
        }
        // Ensure panel stays within screen bounds
        panelX = Math.max(4, Math.min(panelX, screenW - panelSize.x - 4));

    // Align the top of the side panel with the top of the main tooltip
    panelY = tooltipPos.y;
    panelY = Math.max(4, Math.min(panelY, screenH - panelSize.y - 4));

        Vec2f panelPos = new Vec2f(panelX, panelY);
        ec.drawManaged(() -> {
            if (TooltipStylePipeline.renderStyleRef != null)
                TooltipStylePipeline.renderStyleRef.renderBack(ec, panelPos, panelSize, false);
            StyleManager.getInstance().getDefaultStyle().renderBack(ec, panelPos, panelSize, false);
        });
        return new Vec2f(panelPos.x + panelSize.x / 2.0f, panelPos.y + panelSize.y / 2.0f);
    }

    public static void renderMapPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        MapRenderer.renderMapPreview(ctx, stack, center);
    }
}
