package dev.quentintyr.embellishedtooltips.client.style.effect;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.style.Effects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;

/**
 * Represents a visual effect that can be applied to a tooltip.
 */
@Environment(EnvType.CLIENT)
public abstract class TooltipEffect {

    /**
     * The layer this effect should be rendered on.
     */
    private final Effects layer;

    /**
     * Creates a new tooltip effect.
     *
     * @param layer The layer this effect should be rendered on.
     */
    protected TooltipEffect(Effects layer) {
        this.layer = layer;
    }

    /**
     * Gets the layer this effect should be rendered on.
     *
     * @return The layer this effect should be rendered on.
     */
    public Effects getLayer() {
        return this.layer;
    }

    /**
     * Renders this effect.
     *
     * @param drawContext The DrawContext instance to render with.
     * @param context     The tooltip context.
     */
    public abstract void render(DrawContext drawContext, TooltipContext context);
    
    /**
     * Determines if this effect can stack with another effect.
     *
     * @param other The other effect to check.
     * @return True if this effect can stack with the other effect, false otherwise.
     */
    public boolean canStackWith(TooltipEffect other) {
        // Default implementation: effects of the same class cannot stack
        return !this.getClass().equals(other.getClass());
    }
}
