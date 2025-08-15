package com.obscuria.tooltips.client;

import com.obscuria.tooltips.client.style.TooltipStyle;
import com.obscuria.tooltips.client.style.TooltipStylePreset;
import com.obscuria.tooltips.client.style.frame.TooltipFrame;
import com.obscuria.tooltips.client.style.icon.DescentSimpleIcon;
import com.obscuria.tooltips.client.style.icon.TooltipIcon;
import com.obscuria.tooltips.client.style.panel.ColorRectPanel;
import com.obscuria.tooltips.client.style.panel.TooltipPanel;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public class StyleManager {
   public static final TooltipPanel DEFAULT_PANEL = new ColorRectPanel(-267386864, -267386864, 1347420415, 1344798847, 553648127);
   public static final TooltipFrame DEFAULT_FRAME = (renderer, pos, size) -> {
   };
   public static final TooltipIcon DEFAULT_ICON = new DescentSimpleIcon();

   public static Optional<TooltipStyle> getStyleFor(ItemStack stack) {
      TooltipStylePreset preset = (TooltipStylePreset)ResourceLoader.getStyleFor(stack).orElse((Object)null);
      return preset == null ? defaultStyle() : Optional.of((new TooltipStyle.Builder()).withPanel((TooltipPanel)preset.getPanel().orElse(DEFAULT_PANEL)).withFrame((TooltipFrame)preset.getFrame().orElse(DEFAULT_FRAME)).withIcon((TooltipIcon)preset.getIcon().orElse(DEFAULT_ICON)).withEffects(preset.getEffects()).build());
   }

   public static Optional<TooltipStyle> defaultStyle() {
      return Optional.of((new TooltipStyle.Builder()).withPanel(DEFAULT_PANEL).withFrame(DEFAULT_FRAME).withIcon(DEFAULT_ICON).build());
   }
}
