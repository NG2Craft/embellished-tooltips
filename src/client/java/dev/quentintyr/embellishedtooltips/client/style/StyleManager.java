package dev.quentintyr.embellishedtooltips.client.style;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.style.panel.TooltipPanel;
import dev.quentintyr.embellishedtooltips.client.style.frame.TooltipFrame;
import dev.quentintyr.embellishedtooltips.client.style.icon.TooltipIcon;
import dev.quentintyr.embellishedtooltips.client.style.panel.ColorRectPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages tooltip styles and their configuration.
 */
@Environment(EnvType.CLIENT)
public class StyleManager {
    private static StyleManager instance;

    // Default style components (matching original mod values)
    public static final TooltipPanel DEFAULT_PANEL = new ColorRectPanel(-267386864, -267386864, 1347420415, 1344798847,
            553648127);
    public static final TooltipFrame DEFAULT_FRAME = new TooltipFrame() {
        @Override
        public void render(DrawContext drawContext, TooltipContext context) {
            // Empty default frame
        }

        @Override
        public int[] getPadding() {
            return new int[] { 0, 0, 0, 0 };
        }
    };
    public static final TooltipIcon DEFAULT_ICON = null; // Will be DescentSimpleIcon when implemented

    // Registry of named styles
    private final Map<String, TooltipStyle> styles = new HashMap<>();
    private TooltipStyle defaultStyle;

    private StyleManager() {
        this.defaultStyle = createDefaultStyle();
        styles.put("default", defaultStyle);
    }

    /**
     * Gets the singleton instance of the StyleManager.
     *
     * @return The StyleManager instance.
     */
    public static StyleManager getInstance() {
        if (instance == null) {
            instance = new StyleManager();
        }
        return instance;
    }

    /**
     * Creates the default tooltip style.
     *
     * @return The default tooltip style.
     */
    private TooltipStyle createDefaultStyle() {
        return new TooltipStyle.Builder()
                .withPanel(DEFAULT_PANEL)
                .withFrame(DEFAULT_FRAME)
                .withIcon(DEFAULT_ICON)
                .build();
    }

    /**
     * Gets a style by its name.
     *
     * @param name The name of the style.
     * @return The style, or the default style if the named style doesn't exist.
     */
    public TooltipStyle getStyle(String name) {
        return styles.getOrDefault(name, defaultStyle);
    }

    /**
     * Gets the default style.
     *
     * @return The default style.
     */
    public TooltipStyle getDefaultStyle() {
        return defaultStyle;
    }

    /**
     * Registers a style with a name.
     *
     * @param name  The name of the style.
     * @param style The style to register.
     */
    public void registerStyle(String name, TooltipStyle style) {
        styles.put(name, style);
    }

    /**
     * Sets the default style.
     *
     * @param style The style to set as default.
     */
    public void setDefaultStyle(TooltipStyle style) {
        this.defaultStyle = style;
        styles.put("default", style);
    }

    // Inner classes for default implementations

    /**
     * Default implementation of TooltipPanel.
     */
    private static class DefaultTooltipPanel extends TooltipPanel {
        @Override
        public void render(DrawContext drawContext, TooltipContext context) {
            // Simple semi-transparent background
            int x = context.getX();
            int y = context.getY();
            int width = context.getWidth();
            int height = context.getHeight();

            drawContext.fill(x, y, x + width, y + height, 0xF0100010);
        }

        @Override
        public int[] getPadding() {
            return new int[] { 4, 4, 4, 4 }; // Left, top, right, bottom
        }
    }

    /**
     * Default implementation of TooltipFrame.
     */
    private static class DefaultTooltipFrame extends TooltipFrame {
        @Override
        public void render(DrawContext drawContext, TooltipContext context) {
            // Simple border
            int x = context.getX();
            int y = context.getY();
            int width = context.getWidth();
            int height = context.getHeight();

            // Outer border (dark)
            drawContext.fill(x - 1, y - 1, x + width + 1, y, 0xFF000000); // Top
            drawContext.fill(x - 1, y + height, x + width + 1, y + height + 1, 0xFF000000); // Bottom
            drawContext.fill(x - 1, y, x, y + height, 0xFF000000); // Left
            drawContext.fill(x + width, y, x + width + 1, y + height, 0xFF000000); // Right

            // Inner border (light)
            drawContext.fill(x, y, x + width, y + 1, 0xFFFFFFA0); // Top
            drawContext.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFA0); // Bottom
            drawContext.fill(x, y + 1, x + 1, y + height - 1, 0xFFFFFFA0); // Left
            drawContext.fill(x + width - 1, y + 1, x + width, y + height - 1, 0xFFFFFFA0); // Right
        }

        @Override
        public int[] getPadding() {
            return new int[] { 1, 1, 1, 1 }; // Left, top, right, bottom
        }
    }
}
