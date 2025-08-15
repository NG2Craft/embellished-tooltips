package com.obscuria.tooltips.client.style.effect;

import com.obscuria.tooltips.client.renderer.TooltipContext;
import com.obscuria.tooltips.client.style.Effects;
import com.obscuria.tooltips.client.style.particle.EnderParticle;
import com.obscuria.tooltips.client.style.particle.TooltipParticle;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec2;

public class EnderEffect implements TooltipEffect {
   protected final int CENTER_COLOR;
   protected final int EDGE_COLOR;
   protected final List<TooltipParticle> particles = new ArrayList();
   protected float lastParticle = -0.2F;

   public EnderEffect(int centerColor, int edgeColor) {
      this.CENTER_COLOR = centerColor;
      this.EDGE_COLOR = edgeColor;
   }

   public void render(TooltipContext context, Vec2 pos, Point size) {
      if (context.time() - this.lastParticle >= 0.25F) {
         this.lastParticle = context.time();
         Vec2 center = new Vec2(pos.f_82470_ + 13.0F, pos.f_82471_ + 13.0F);
         this.particles.add(new EnderParticle(this.CENTER_COLOR, this.EDGE_COLOR, 3.0F, center, 13.0F));
      }

      context.renderParticles(this.particles);
   }

   public void reset() {
      this.lastParticle = -0.2F;
      this.particles.clear();
   }

   public Effects.Order order() {
      return Effects.Order.LAYER_5_FRONT;
   }

   public boolean canStackWith(TooltipEffect other) {
      return !(other instanceof EnderEffect);
   }
}
