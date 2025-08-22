package dev.quentintyr.embellishedtooltips.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec2f;
import java.awt.Point;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.style.panel.TooltipPanel;
import dev.quentintyr.embellishedtooltips.client.style.TooltipStyle;
import dev.quentintyr.embellishedtooltips.client.style.frame.TooltipFrame;
import dev.quentintyr.embellishedtooltips.client.style.icon.TooltipIcon;
import dev.quentintyr.embellishedtooltips.client.style.panel.ColorRectPanel;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages tooltip styles and their configuration.
 */
@Environment(EnvType.CLIENT)
public class StyleManager {
    private static StyleManager instance;

    // Default style components (high-contrast visible colors for debugging)
    public static final TooltipPanel DEFAULT_PANEL = new ColorRectPanel(
            0xF0000000, // Background top - opaque black
            0xF0333333, // Background bottom - dark gray  
            0xFFFFFFFF, // Border top - white border for visibility
            0xFFAAAAAA, // Border bottom - light gray border
            0x80FFFF00  // Slot highlight - yellow with transparency
    );
    public static final TooltipFrame DEFAULT_FRAME = new TooltipFrame() {
        @Override
        public void render(TooltipContext context, Vec2f pos, Point size) {
            // Empty default frame
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
}
