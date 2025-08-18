package dev.quentintyr.embellishedtooltips.client.style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.style.panel.TooltipPanel;
import dev.quentintyr.embellishedtooltips.client.style.frame.TooltipFrame;
import dev.quentintyr.embellishedtooltips.client.style.icon.TooltipIcon;
import dev.quentintyr.embellishedtooltips.client.style.effect.TooltipEffect;
import dev.quentintyr.embellishedtooltips.client.style.Effects;

/**
 * Represents a visual style for tooltips
 */
@Environment(EnvType.CLIENT)
public final class TooltipStyle {
    private final List<TooltipEffect> effects;
    private final TooltipPanel panel;
    private final TooltipFrame frame;
    private final TooltipIcon icon;

    private TooltipStyle(List<TooltipEffect> effects, TooltipPanel panel,
            TooltipFrame frame, TooltipIcon icon) {
        this.effects = effects != null ? new ArrayList<>(effects) : new ArrayList<>();
        this.panel = panel;
        this.frame = frame;
        this.icon = icon;
    }

    /**
     * Renders the background of the tooltip.
     *
     * @param drawContext The draw context to render with.
     * @param context     The tooltip context.
     */
    public void renderBack(DrawContext drawContext, TooltipContext context) {
        if (this.panel != null) {
            this.panel.render(drawContext, context);
        }
    }

    /**
     * Renders the front (frame and icon) of the tooltip.
     *
     * @param drawContext The draw context to render with.
     * @param context     The tooltip context.
     */
    public void renderFront(DrawContext drawContext, TooltipContext context) {
        // Render effects with FRONT layer
        renderEffects(Effects.FRONT, drawContext, context);

        // Render the frame
        if (this.frame != null) {
            this.frame.render(drawContext, context);
        }

        // Render effects with FRAME layer
        renderEffects(Effects.FRAME, drawContext, context);

        // Render the icon if present
        if (this.icon != null) {
            int iconX = context.getX() + 12;
            int iconY = context.getY() + 12;
            this.icon.render(drawContext, context, iconX, iconY);
        }
    }

    /**
     * Renders effects for a specific layer.
     *
     * @param layer       The layer to render effects for.
     * @param drawContext The draw context to render with.
     * @param context     The tooltip context.
     */
    public void renderEffects(Effects layer, DrawContext drawContext, TooltipContext context) {
        for (TooltipEffect effect : effects) {
            if (effect.getLayer() == layer) {
                effect.render(drawContext, context);
            }
        }
    }

    /**
     * Gets the panel component of this style.
     *
     * @return The panel component.
     */
    public TooltipPanel getPanel() {
        return panel;
    }

    /**
     * Gets the frame component of this style.
     *
     * @return The frame component.
     */
    public TooltipFrame getFrame() {
        return frame;
    }

    /**
     * Gets the icon component of this style.
     *
     * @return The icon component.
     */
    public TooltipIcon getIcon() {
        return icon;
    }

    /**
     * Gets the effects of this style.
     *
     * @return An unmodifiable list of the effects.
     */
    public List<TooltipEffect> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    /**
     * Builder for creating TooltipStyle instances
     */
    public static class Builder {
        private List<TooltipEffect> effects = new ArrayList<>();
        private TooltipPanel panel;
        private TooltipFrame frame;
        private TooltipIcon icon;

        public Builder() {
            // TODO: Replace with actual defaults when StyleManager is implemented
            this.panel = StyleManager.DEFAULT_PANEL;
            this.frame = StyleManager.DEFAULT_FRAME;
            this.icon = StyleManager.DEFAULT_ICON;
        }

        public TooltipStyle.Builder withPanel(TooltipPanel panel) {
            this.panel = panel;
            return this;
        }

        public TooltipStyle.Builder withFrame(TooltipFrame frame) {
            this.frame = frame;
            return this;
        }

        public TooltipStyle.Builder withIcon(TooltipIcon icon) {
            this.icon = icon;
            return this;
        }

        public TooltipStyle.Builder withEffects(List<TooltipEffect> effects) {
            this.effects.addAll(effects);
            return this;
        }

        public TooltipStyle build() {
            return new TooltipStyle(this.effects, this.panel, this.frame, this.icon);
        }
    }
}
