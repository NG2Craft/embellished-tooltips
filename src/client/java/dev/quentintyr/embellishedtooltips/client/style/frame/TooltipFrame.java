package dev.quentintyr.embellishedtooltips.client.style.frame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;

/**
 * Represents a frame rendering style for tooltips.
 */
@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface TooltipFrame {

    /**
     * Renders the tooltip frame.
     *
     * @param drawContext The DrawContext instance to render with.
     * @param context     The tooltip context.
     */
    void render(DrawContext drawContext, TooltipContext context);

    /**
     * Reset any state for this frame.
     */
    default void reset() {
    }
}
