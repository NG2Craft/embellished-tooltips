package dev.quentintyr.embellishedtooltips.client.render.panel;

import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.render.TooltipStylePipeline;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Shared helpers for sizing + background rendering of side panels.
 * Centralizes the repeated math found in various *SidePanel classes.
 */
public final class SidePanelHelper {
    private SidePanelHelper() {
    }

    /**
     * Compute clamped panel top-left given desired size & placement relative to
     * mouse.
     */
    public static Point computePosition(boolean isTooltipOnLeft, int mouseX, int mouseY, int screenW, int screenH,
            Point size, int anchorY) {
        float panelX = isTooltipOnLeft ? (mouseX - 12 - size.x) : (mouseX + 12);
        panelX = Math.max(4, Math.min(panelX, screenW - size.x - 4));
        float panelY = anchorY; // allow caller alignment (tooltip top or mouse based)
        panelY = Math.max(4, Math.min(panelY, screenH - size.y - 4));
        return new Point((int) panelX, (int) panelY);
    }

    /**
     * Render layered backgrounds (active style then fallback default) and return
     * center.
     */
    public static Vec2f drawBackgroundAndGetCenter(TooltipContext ec, Point pos, Point size) {
        Objects.requireNonNull(ec, "TooltipContext");
        ec.drawManaged(() -> {
            if (TooltipStylePipeline.renderStyleRef != null) {
                TooltipStylePipeline.renderStyleRef.renderBack(ec, new Vec2f(pos.x, pos.y), size, false);
            }
            StyleManager.getInstance().getDefaultStyle().renderBack(ec, new Vec2f(pos.x, pos.y), size, false);
        });
        return new Vec2f(pos.x + size.x / 2f, pos.y + size.y / 2f);
    }

    /**
     * Convenience: full lifecycle for a simple fixed-size panel anchored to tooltip
     * top.
     */
    public static Vec2f renderFixedPanel(TooltipContext ec, Vec2f tooltipPos, Point size, boolean isTooltipOnLeft,
            int mouseX, int mouseY, int screenW, int screenH) {
        Point pos = computePosition(isTooltipOnLeft, mouseX, mouseY, screenW, screenH, size, (int) tooltipPos.y);
        return drawBackgroundAndGetCenter(ec, pos, size);
    }

    /**
     * Convenience for dynamic sized panels that need a lazy size calculation.
     */
    public static Vec2f renderDynamicPanel(TooltipContext ec, Vec2f tooltipPos, Supplier<Point> sizeSupplier,
            boolean isTooltipOnLeft, int mouseX, int mouseY, int screenW, int screenH, boolean anchorToMouse) {
        Point size = sizeSupplier.get();
        int anchorY = anchorToMouse ? mouseY - 12 : (int) tooltipPos.y;
        Point pos = computePosition(isTooltipOnLeft, mouseX, mouseY, screenW, screenH, size, anchorY);
        return drawBackgroundAndGetCenter(ec, pos, size);
    }
}
