package dev.quentintyr.embellishedtooltips.client.style.panel;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec2f;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import java.awt.Point;

/**
 * Represents a panel rendering style for tooltips.
 */
@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface TooltipPanel {

    /**
     * Renders the tooltip panel.
     *
     * @param context The tooltip context for rendering.
     * @param pos     The position to render at.
     * @param size    The size of the panel.
     * @param slot    Whether to render the item slot highlight.
     */
    void render(TooltipContext context, Vec2f pos, Point size, boolean slot);

    /**
     * Reset any state for this panel.
     */
    default void reset() {
    }
}
