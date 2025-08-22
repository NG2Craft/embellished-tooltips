package dev.quentintyr.embellishedtooltips.client.style;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.Vec2f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.style.panel.TooltipPanel;
import dev.quentintyr.embellishedtooltips.client.style.frame.TooltipFrame;
import dev.quentintyr.embellishedtooltips.client.style.icon.TooltipIcon;
import dev.quentintyr.embellishedtooltips.client.style.effect.TooltipEffect;

/**
 * Represents a visual style for tooltips.
 * This matches the original TooltipStyle from Obscure Tooltips.
 */
@Environment(EnvType.CLIENT)
public final class TooltipStyle {
    private final List<TooltipEffect> EFFECTS;
    private final TooltipPanel PANEL;
    private final TooltipFrame FRAME;
    private final TooltipIcon ICON;

    private TooltipStyle(List<TooltipEffect> effects, TooltipPanel panel, TooltipFrame frame, TooltipIcon icon) {
        this.EFFECTS = effects != null ? new ArrayList<>(effects) : new ArrayList<>();
        this.PANEL = panel;
        this.FRAME = frame;
        this.ICON = icon;
    }

    /**
     * Renders the background of the tooltip.
     *
     * @param renderer The tooltip context renderer.
     * @param pos      The position to render at.
     * @param size     The size of the tooltip.
     * @param slot     Whether to render the item slot.
     */
    public void renderBack(TooltipContext renderer, Vec2f pos, Point size, boolean slot) {
        renderer.pose().push();
        renderer.pose().translate(0.0F, 0.0F, -50.0F);
        if (this.PANEL != null) {
            this.PANEL.render(renderer, pos, size, slot);
        }
        renderer.pose().pop();
    }

    /**
     * Renders the front (frame and icon) of the tooltip.
     *
     * @param renderer The tooltip context renderer.
     * @param pos      The position to render at.
     * @param size     The size of the tooltip.
     */
    public void renderFront(TooltipContext renderer, Vec2f pos, Point size) {
        // Render frame
        renderer.push(() -> {
            renderer.translate(0.0F, 0.0F, -50.0F);
            if (this.FRAME != null) {
                this.FRAME.render(renderer, pos, size);
            }
        });

        // Render icon
        renderer.push(() -> {
            renderer.translate(pos.x + 12.0F, pos.y + 12.0F, 500.0F);
            renderer.push(() -> {
                if (this.ICON != null) {
                    this.ICON.render(renderer, -8, -8);
                }
            });
        });
    }

    /**
     * Resets the style state.
     */
    public void reset() {
        if (PANEL != null)
            PANEL.reset();
        if (FRAME != null)
            FRAME.reset();
        if (ICON != null)
            ICON.reset();
        // Effects reset will be implemented when effects are added
    }

    /**
     * Builder for creating TooltipStyle instances.
     */
    public static class Builder {
        private List<TooltipEffect> effects = new ArrayList<>();
        private TooltipPanel panel;
        private TooltipFrame frame;
        private TooltipIcon icon;

        public Builder withEffects(List<TooltipEffect> effects) {
            this.effects = effects != null ? new ArrayList<>(effects) : new ArrayList<>();
            return this;
        }

        public Builder withPanel(TooltipPanel panel) {
            this.panel = panel;
            return this;
        }

        public Builder withFrame(TooltipFrame frame) {
            this.frame = frame;
            return this;
        }

        public Builder withIcon(TooltipIcon icon) {
            this.icon = icon;
            return this;
        }

        public TooltipStyle build() {
            return new TooltipStyle(effects, panel, frame, icon);
        }
    }
}
