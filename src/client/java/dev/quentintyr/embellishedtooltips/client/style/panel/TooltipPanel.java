package dev.quentintyr.embellishedtooltips.client.style.panel;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;

/**
 * Represents a panel rendering style for tooltips.
 */
@Environment(EnvType.CLIENT)
public abstract class TooltipPanel {

    /**
     * Renders the tooltip panel.
     *
     * @param drawContext The DrawContext instance to render with.
     * @param context     The tooltip context.
     */
    public abstract void render(DrawContext drawContext, TooltipContext context);

    /**
     * Gets the padding to apply to tooltip content.
     *
     * @return The padding as [left, top, right, bottom].
     */
    public abstract int[] getPadding();
}
