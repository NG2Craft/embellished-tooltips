package com.obscuria.tooltips.client.style.icon;

import com.obscuria.tooltips.client.renderer.TooltipContext;
import org.joml.Vector3f;

public class DescentComplexIcon implements TooltipIcon {
   public void render(TooltipContext context, int x, int y) {
      float scale = (double)context.time() < 0.25D ? (1.0F - (float)Math.pow((double)(1.0F - context.time() * 4.0F), 3.0D)) * 1.5F : ((double)context.time() < 0.5D ? 1.5F - (1.0F - (float)Math.pow((double)(1.0F - (context.time() - 0.25F) * 4.0F), 3.0D)) * 0.25F : 1.25F);
      context.scale(scale, scale, scale);
      context.renderItem(new Vector3f(0.0F, 180.0F + 180.0F * (1.0F - (float)Math.pow((double)(1.0F - Math.min(1.0F, context.time() * 2.0F)), 3.0D)), 0.0F), new Vector3f(1.0F));
   }
}
