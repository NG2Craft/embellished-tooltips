package dev.quentintyr.embellishedtooltips.client.style.icon;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import net.minecraft.util.Identifier;

/**
 * Represents an icon that can be displayed on tooltips.
 */
@Environment(EnvType.CLIENT)
public class TooltipIcon {

    private final Identifier texture;
    private final int width;
    private final int height;
    private final int u;
    private final int v;

    /**
     * Creates a new tooltip icon.
     *
     * @param texture The texture identifier.
     * @param width   The width of the icon.
     * @param height  The height of the icon.
     * @param u       The U coordinate in the texture.
     * @param v       The V coordinate in the texture.
     */
    public TooltipIcon(Identifier texture, int width, int height, int u, int v) {
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
    }

    /**
     * Renders this icon.
     *
     * @param drawContext The DrawContext instance to render with.
     * @param context     The tooltip context.
     * @param x           The X coordinate to render at.
     * @param y           The Y coordinate to render at.
     */
    public void render(DrawContext drawContext, TooltipContext context, int x, int y) {
        // Render the icon at the specified position
        drawContext.drawTexture(texture, x, y, u, v, width, height);
    }

    /**
     * Gets the width of the icon.
     *
     * @return The width of the icon.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the icon.
     *
     * @return The height of the icon.
     */
    public int getHeight() {
        return height;
    }
}
