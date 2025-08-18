package dev.quentintyr.embellishedtooltips.client.style.frame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;

/**
 * Represents a frame rendering style for tooltips.
 */
@Environment(EnvType.CLIENT)
public abstract class TooltipFrame {

    /**
     * Renders the tooltip frame.
     *
     * @param drawContext The DrawContext instance to render with.
     * @param context     The tooltip context.
     */
    public abstract void render(DrawContext drawContext, TooltipContext context);

    /**
     * Gets the padding to apply to tooltip content due to the frame.
     *
     * @return The padding as [left, top, right, bottom].
     */
    public abstract int[] getPadding();
}
