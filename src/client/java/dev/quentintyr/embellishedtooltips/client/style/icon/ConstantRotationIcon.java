package com.obscuria.tooltips.client.style.icon;

import com.obscuria.tooltips.client.renderer.TooltipContext;

public class ConstantRotationIcon implements TooltipIcon {
   public void render(TooltipContext context, int x, int y) {
      context.context().m_280480_(context.stack(), x, y);
   }
}
