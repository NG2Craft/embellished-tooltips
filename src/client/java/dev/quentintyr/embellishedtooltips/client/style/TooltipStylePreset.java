package dev.quentintyr.embellishedtooltips.client.style;

import com.google.common.collect.ImmutableList;
import dev.quentintyr.embellishedtooltips.client.style.effect.TooltipEffect;
import dev.quentintyr.embellishedtooltips.client.style.frame.TooltipFrame;
import dev.quentintyr.embellishedtooltips.client.style.icon.TooltipIcon;
import dev.quentintyr.embellishedtooltips.client.style.panel.TooltipPanel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public final class TooltipStylePreset {
    private final ImmutableList<TooltipEffect> EFFECTS;
    @Nullable
    private final TooltipPanel PANEL;
    @Nullable
    private final TooltipFrame FRAME;
    @Nullable
    private final TooltipIcon ICON;

    private TooltipStylePreset(List<TooltipEffect> effects, @Nullable TooltipPanel panel, @Nullable TooltipFrame frame,
            @Nullable TooltipIcon icon) {
        this.EFFECTS = ImmutableList.copyOf(effects);
        this.PANEL = panel;
        this.FRAME = frame;
        this.ICON = icon;
    }

    /**
     * Returns the panel component of this style preset, if present.
     * 
     * @return Optional containing the panel, or empty if no panel is set
     */
    public Optional<TooltipPanel> getPanel() {
        return Optional.ofNullable(this.PANEL);
    }

    /**
     * Returns the frame component of this style preset, if present.
     * 
     * @return Optional containing the frame, or empty if no frame is set
     */
    public Optional<TooltipFrame> getFrame() {
        return Optional.ofNullable(this.FRAME);
    }

    /**
     * Returns the icon component of this style preset, if present.
     * 
     * @return Optional containing the icon, or empty if no icon is set
     */
    public Optional<TooltipIcon> getIcon() {
        return Optional.ofNullable(this.ICON);
    }

    /**
     * Returns an immutable list of all effects in this style preset.
     * 
     * @return immutable list of tooltip effects, never null
     */
    public ImmutableList<TooltipEffect> getEffects() {
        return this.EFFECTS;
    }

    /**
     * Returns a human-readable string representation of this tooltip style preset.
     * Shows the class names of all components (panel, frame, icon) and effects.
     * 
     * @return formatted string showing all preset components
     */
    @Override
    public String toString() {
        String panelName = this.PANEL != null ? this.PANEL.getClass().getSimpleName() : "none";
        String frameName = this.FRAME != null ? this.FRAME.getClass().getSimpleName() : "none";
        String iconName = this.ICON != null ? this.ICON.getClass().getSimpleName() : "none";

        String effectNames = this.EFFECTS.isEmpty() ? "none"
                : this.EFFECTS.stream()
                        .map(effect -> effect.getClass().getSimpleName())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("none");

        return "[Panel:%s, Frame:%s, Icon:%s, Effects:%s]"
                .formatted(panelName, frameName, iconName, effectNames);
    }

    /**
     * Builder class for creating TooltipStylePreset instances.
     * Provides a fluent interface for configuring tooltip components.
     */
    public static class Builder {
        private final List<TooltipEffect> effects = new ArrayList<>();
        @Nullable
        private TooltipPanel panel;
        @Nullable
        private TooltipFrame frame;
        @Nullable
        private TooltipIcon icon;

        /**
         * Sets the panel for this tooltip style, overriding any existing panel.
         * 
         * @param panel the panel to use, or null for no panel
         * @return this builder for method chaining
         */
        public TooltipStylePreset.Builder withPanel(@Nullable TooltipPanel panel) {
            return this.withPanel(panel, true);
        }

        /**
         * Sets the frame for this tooltip style, overriding any existing frame.
         * 
         * @param frame the frame to use, or null for no frame
         * @return this builder for method chaining
         */
        public TooltipStylePreset.Builder withFrame(@Nullable TooltipFrame frame) {
            return this.withFrame(frame, true);
        }

        /**
         * Sets the icon for this tooltip style, overriding any existing icon.
         * 
         * @param icon the icon to use, or null for no icon
         * @return this builder for method chaining
         */
        public TooltipStylePreset.Builder withIcon(@Nullable TooltipIcon icon) {
            return this.withIcon(icon, true);
        }

        /**
         * Sets the panel for this tooltip style with optional override behavior.
         * 
         * @param panel    the panel to use, or null for no panel
         * @param override if true, replaces existing panel; if false, keeps existing if
         *                 present
         * @return this builder for method chaining
         */
        public TooltipStylePreset.Builder withPanel(@Nullable TooltipPanel panel, boolean override) {
            this.panel = this.panel != null && !override ? this.panel : panel;
            return this;
        }

        /**
         * Sets the frame for this tooltip style with optional override behavior.
         * 
         * @param frame    the frame to use, or null for no frame
         * @param override if true, replaces existing frame; if false, keeps existing if
         *                 present
         * @return this builder for method chaining
         */
        public TooltipStylePreset.Builder withFrame(@Nullable TooltipFrame frame, boolean override) {
            this.frame = this.frame != null && !override ? this.frame : frame;
            return this;
        }

        /**
         * Sets the icon for this tooltip style with optional override behavior.
         * 
         * @param icon     the icon to use, or null for no icon
         * @param override if true, replaces existing icon; if false, keeps existing if
         *                 present
         * @return this builder for method chaining
         */
        public TooltipStylePreset.Builder withIcon(@Nullable TooltipIcon icon, boolean override) {
            this.icon = this.icon != null && !override ? this.icon : icon;
            return this;
        }

        /**
         * Adds effects to this builder, checking for compatibility with existing
         * effects.
         * Effects that cannot stack with already loaded effects will be skipped.
         * 
         * @param effects the list of effects to add, or null to skip
         * @return this builder for method chaining
         */
        public TooltipStylePreset.Builder withEffects(@Nullable List<TooltipEffect> effects) {
            if (effects == null || effects.isEmpty()) {
                return this;
            }

            // Pre-filter effects for better performance with large effect lists
            for (TooltipEffect newEffect : effects) {
                boolean canAdd = this.effects.stream()
                        .allMatch(existingEffect -> newEffect.canStackWith(existingEffect));

                if (canAdd) {
                    this.effects.add(newEffect);
                }
            }

            return this;
        }

        /**
         * Checks if this builder has no configured components.
         * 
         * @return true if no panel, frame, icon, or effects are set; false otherwise
         */
        public boolean isEmpty() {
            return this.panel == null && this.frame == null && this.icon == null &&
                    this.effects.isEmpty();
        }

        /**
         * Builds the final TooltipStylePreset with all configured components.
         * 
         * @return a new immutable TooltipStylePreset instance
         */
        public TooltipStylePreset build() {
            return new TooltipStylePreset(this.effects, this.panel, this.frame, this.icon);
        }
    }
}
