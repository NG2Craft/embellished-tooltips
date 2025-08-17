package dev.quentintyr.embellishedtooltips.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Represents the context for rendering a tooltip.
 */
@Environment(EnvType.CLIENT)
public interface TooltipContext {
    /**
     * Gets the width of the tooltip content.
     *
     * @return The width of the tooltip content.
     */
    int getWidth();

    /**
     * Gets the height of the tooltip content.
     *
     * @return The height of the tooltip content.
     */
    int getHeight();

    /**
     * Gets the X coordinate of the tooltip.
     *
     * @return The X coordinate of the tooltip.
     */
    int getX();

    /**
     * Gets the Y coordinate of the tooltip.
     *
     * @return The Y coordinate of the tooltip.
     */
    int getY();

    /**
     * Gets the text lines of the tooltip.
     *
     * @return The text lines of the tooltip.
     */
    List<Text> getLines();

    /**
     * Gets the item stack being shown in the tooltip.
     *
     * @return The item stack, or null if this tooltip is not for an item.
     */
    ItemStack getStack();

    /**
     * Gets the screen width.
     *
     * @return The screen width.
     */
    int getScreenWidth();

    /**
     * Gets the screen height.
     *
     * @return The screen height.
     */
    int getScreenHeight();

    /**
     * Gets the matrix stack for rendering.
     *
     * @return The matrix stack.
     */
    MatrixStack getMatrixStack();
}
