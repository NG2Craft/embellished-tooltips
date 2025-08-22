package dev.quentintyr.embellishedtooltips.client.style.icon;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;

/**
 * Represents an icon that can be displayed on tooltips.
 */
@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface TooltipIcon {

    /**
     * Renders the tooltip icon.
     *
     * @param context The tooltip context for rendering.
     * @param x       The x coordinate to render at.
     * @param y       The y coordinate to render at.
     */
    void render(TooltipContext context, int x, int y);

    /**
     * Reset any state for this icon.
     */
    default void reset() {
    }
}
