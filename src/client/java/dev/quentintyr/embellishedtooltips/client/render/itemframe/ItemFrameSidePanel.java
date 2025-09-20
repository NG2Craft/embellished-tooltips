package dev.quentintyr.embellishedtooltips.client.render.itemframe;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.render.panel.SidePanelHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import java.awt.Point;

/**
 * Side panel renderer for item frame previews (random contained item).
 */
public final class ItemFrameSidePanel {
    private ItemFrameSidePanel() {
    }

    public static Vec2f renderPanel(TooltipContext ec, Vec2f tooltipPos, Point tooltipSize,
            Object pipelineOwner /* unused retained signature for compatibility */, int mouseX, int mouseY,
            int screenW, int screenH, boolean isTooltipOnLeft) {
        return SidePanelHelper.renderFixedPanel(ec, tooltipPos, new Point(48, 48), isTooltipOnLeft, mouseX, mouseY,
                screenW, screenH);
    }

    public static void renderPreview(DrawContext ctx, ItemStack frameStack, ItemStack previewStack, Vec2f center) {
        // 3D entity rendering only; baked model fallback removed as it rarely triggers
        // and complicates maintenance.
        ItemFrameGuiRenderer.render(ctx.getMatrices(), center.x, center.y, 46f, previewStack);
    }
}
