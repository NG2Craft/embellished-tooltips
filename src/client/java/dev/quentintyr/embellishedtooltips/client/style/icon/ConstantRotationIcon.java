package dev.quentintyr.embellishedtooltips.client.style.icon;

import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;

public class ConstantRotationIcon implements TooltipIcon {
   public void render(TooltipContext context, int x, int y) {
      context.context().m_280480_(context.stack(), x, y);
   }
}
