// package dev.quentintyr.embellishedtooltips.client.style;

// import com.google.common.collect.ImmutableList;
// import dev.quentintyr.embellishedtooltips.client.style.effect.TooltipEffect;
// import dev.quentintyr.embellishedtooltips.client.style.frame.TooltipFrame;
// import dev.quentintyr.embellishedtooltips.client.style.icon.TooltipIcon;
// import dev.quentintyr.embellishedtooltips.client.style.panel.TooltipPanel;
// import java.util.ArrayList;
// import java.util.Iterator;
// import java.util.List;
// import java.util.Optional;
// import org.jetbrains.annotations.Nullable;

// public final class TooltipStylePreset {
// private final ImmutableList<TooltipEffect> EFFECTS;
// @Nullable
// private final TooltipPanel PANEL;
// @Nullable
// private final TooltipFrame FRAME;
// @Nullable
// private final TooltipIcon ICON;

// private TooltipStylePreset(List<TooltipEffect> effects, @Nullable
// TooltipPanel panel, @Nullable TooltipFrame frame,
// @Nullable TooltipIcon icon) {
// this.EFFECTS = ImmutableList.copyOf(effects);
// this.PANEL = panel;
// this.FRAME = frame;
// this.ICON = icon;
// }

// public Optional<TooltipPanel> getPanel() {
// return this.PANEL == null ? Optional.empty() : Optional.of(this.PANEL);
// }

// public Optional<TooltipFrame> getFrame() {
// return this.FRAME == null ? Optional.empty() : Optional.of(this.FRAME);
// }

// public Optional<TooltipIcon> getIcon() {
// return this.ICON == null ? Optional.empty() : Optional.of(this.ICON);
// }

// public ImmutableList<TooltipEffect> getEffects() {
// return this.EFFECTS;
// }

// public String toString() {
// return "[Panel:%s, Frame:%s, Icon:%s, Effects:%s]"
// .formatted(new Object[] { this.PANEL != null ?
// this.PANEL.getClass().getSimpleName() : "none",
// this.FRAME != null ? this.FRAME.getClass().getSimpleName() : "none",
// this.ICON != null ? this.ICON.getClass().getSimpleName() : "none",
// !this.EFFECTS.isEmpty() ? this.EFFECTS.stream().map((effect) -> {
// return effect.getClass().getSimpleName();
// }).toList() : "none" });
// }

// public static class Builder {
// private final List<TooltipEffect> effects = new ArrayList();
// @Nullable
// private TooltipPanel panel;
// @Nullable
// private TooltipFrame frame;
// @Nullable
// private TooltipIcon icon;

// public TooltipStylePreset.Builder withPanel(@Nullable TooltipPanel panel) {
// return this.withPanel(panel, true);
// }

// public TooltipStylePreset.Builder withFrame(@Nullable TooltipFrame frame) {
// return this.withFrame(frame, true);
// }

// public TooltipStylePreset.Builder withIcon(@Nullable TooltipIcon icon) {
// return this.withIcon(icon, true);
// }

// public TooltipStylePreset.Builder withPanel(@Nullable TooltipPanel panel,
// boolean override) {
// this.panel = this.panel != null && !override ? this.panel : panel;
// return this;
// }

// public TooltipStylePreset.Builder withFrame(@Nullable TooltipFrame frame,
// boolean override) {
// this.frame = this.frame != null && !override ? this.frame : frame;
// return this;
// }

// public TooltipStylePreset.Builder withIcon(@Nullable TooltipIcon icon,
// boolean override) {
// this.icon = this.icon != null && !override ? this.icon : icon;
// return this;
// }

// public TooltipStylePreset.Builder withEffects(@Nullable List<TooltipEffect>
// effects) {
// if (effects == null) {
// return this;
// } else {
// Iterator var2 = effects.iterator();

// while (true) {
// label25: while (var2.hasNext()) {
// TooltipEffect effect = (TooltipEffect) var2.next();
// Iterator var4 = this.effects.iterator();

// while (var4.hasNext()) {
// TooltipEffect loaded = (TooltipEffect) var4.next();
// if (!effect.canStackWith(loaded)) {
// continue label25;
// }
// }

// this.effects.add(effect);
// }

// return this;
// }
// }
// }

// public boolean isEmpty() {
// return this.panel == null && this.frame == null && this.icon == null &&
// this.effects.isEmpty();
// }

// public TooltipStylePreset build() {
// return new TooltipStylePreset(this.effects, this.panel, this.frame,
// this.icon);
// }
// }
// }
