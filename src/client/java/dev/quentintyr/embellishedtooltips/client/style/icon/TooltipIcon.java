package dev.quentintyr.embellishedtooltips.client.style.icon;

import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@FunctionalInterface
public interface TooltipIcon {
   void render(TooltipContext var1, int var2, int var3);

   default void reset() {
   }
}
