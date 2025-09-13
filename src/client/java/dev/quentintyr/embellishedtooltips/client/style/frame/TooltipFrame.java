package dev.quentintyr.embellishedtooltips.client.style.frame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec2f;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import java.awt.Point;

/**
 * Represents a frame rendering style for tooltips.
 */
@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface TooltipFrame {

    /**
     * Renders the tooltip frame.
     *
     * @param context The tooltip context for rendering.
     * @param pos     The position to render at.
     * @param size    The size of the frame.
     */
    void render(TooltipContext context, Vec2f pos, Point size);

    /**
     * Reset any state for this frame.
     */
    default void reset() {
    }
}
