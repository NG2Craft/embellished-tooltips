package dev.quentintyr.embellishedtooltips.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

/**
 * A basic implementation of the TooltipContext interface.
 */
@Environment(EnvType.CLIENT)
public class BasicTooltipContext implements TooltipContext {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final List<Text> lines;
    private final ItemStack stack;
    private final int screenWidth;
    private final int screenHeight;
    private final MatrixStack matrixStack;

    /**
     * Creates a new basic tooltip context.
     *
     * @param x            The X coordinate of the tooltip.
     * @param y            The Y coordinate of the tooltip.
     * @param width        The width of the tooltip content.
     * @param height       The height of the tooltip content.
     * @param lines        The text lines of the tooltip.
     * @param stack        The item stack being shown in the tooltip.
     * @param screenWidth  The screen width.
     * @param screenHeight The screen height.
     * @param matrixStack  The matrix stack for rendering.
     */
    public BasicTooltipContext(int x, int y, int width, int height, List<Text> lines, ItemStack stack,
            int screenWidth, int screenHeight, MatrixStack matrixStack) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.lines = lines;
        this.stack = stack;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.matrixStack = matrixStack;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public List<Text> getLines() {
        return lines;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public int getScreenWidth() {
        return screenWidth;
    }

    @Override
    public int getScreenHeight() {
        return screenHeight;
    }

    @Override
    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
}
