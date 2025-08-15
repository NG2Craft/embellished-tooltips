package com.obscuria.tooltips.client.style.particle;

import com.mojang.math.Axis;
import com.obscuria.tooltips.client.renderer.TooltipContext;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class LineParticle extends TooltipParticle {
   protected final int START_COLOR;
   protected final int END_COLOR;
   protected Vec2 end;

   public LineParticle(int startColor, int endColor, float lifetime, Vec2 start, Vec2 end) {
      super(lifetime);
      this.START_COLOR = startColor;
      this.END_COLOR = endColor;
      this.position = start;
      this.end = end;
   }

   public void renderParticle(TooltipContext context, float lifetime) {
      float mod = 1.0F - (float)Math.pow((double)(1.0F - lifetime / this.MAX_LIFETIME), 3.0D);
      float scale = mod < 0.5F ? mod * 2.0F : (mod < 0.8F ? 1.0F : 1.0F - (mod - 0.8F) / 0.2F);
      context.push(() -> {
         context.translate(Mth.m_14179_(mod, this.position.f_82470_, this.end.f_82470_), Mth.m_14179_(mod, this.position.f_82471_, this.end.f_82471_), 0.0F);
         context.scale(scale, scale, scale);
         context.mul(Axis.f_252403_.m_252961_(context.angle(this.position, this.end) + 1.5707964F));
         context.push(() -> {
            context.translate(-0.5F, 0.5F, 0.0F);
            context.fillGradient(0, -5, 1, 5, this.END_COLOR, this.START_COLOR);
            context.fill(0, 0, 1, 1, this.START_COLOR);
            context.fillGradient(0, 1, 1, 5, this.START_COLOR, this.END_COLOR);
         });
      });
   }
}
