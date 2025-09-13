package dev.quentintyr.embellishedtooltips.client.style.effect;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.style.Effects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec2f;
import java.awt.Point;

/**
 * Represents a visual effect that can be applied to a tooltip.
 */
@Environment(EnvType.CLIENT)
public interface TooltipEffect {

    /**
     * Gets the layer this effect should be rendered on.
     *
     * @return The layer this effect should be rendered on.
     */
    Effects getLayer();

    /**
     * Renders this effect.
     *
     * @param context The tooltip context for rendering.
     * @param pos     The position to render at.
     * @param size    The size of the tooltip.
     * @param slot    Whether to render the item slot highlight.
     */
    void render(TooltipContext context, Vec2f pos, Point size, boolean slot);

    /**
     * Determines if this effect can stack with another effect.
     *
     * @param other The other effect to check.
     * @return True if this effect can stack with the other effect, false otherwise.
     */
    default boolean canStackWith(TooltipEffect other) {
        // Default implementation: effects of the same class cannot stack
        return !this.getClass().equals(other.getClass());
    }

    /**
     * Reset any state for this effect.
     */
    default void reset() {
        // Default implementation does nothing
    }
}
