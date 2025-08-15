package dev.quentintyr.embellishedtooltips.client.style.particle;

import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.lang3.RandomUtils;

public class EnderParticle extends TooltipParticle {
   protected final int CENTER_COLOR;
   protected final int EDGE_COLOR;
   protected Vec2 start;

   public EnderParticle(int centerColor, int edgeColor, float lifetime, Vec2 center, float range) {
      super(lifetime);
      this.CENTER_COLOR = centerColor;
      this.EDGE_COLOR = edgeColor;
      this.position = center;
      float rot = RandomUtils.nextFloat(0.0F, 360.0F);
      this.start = center
            .m_165910_(new Vec2((float) Math.cos((double) rot) * range, (float) Math.sin((double) rot) * range));
   }

   public void renderParticle(TooltipContext context, float lifetime) {
      float mod = 1.0F - (float) Math.pow((double) (1.0F - lifetime / this.MAX_LIFETIME), 3.0D);
      float scale = (mod < 0.4F ? (float) Math.pow((double) (mod / 0.4F), 3.0D)
            : (mod < 0.9F ? 1.0F - (float) Math.pow((double) ((mod - 0.4F) / 0.5F), 3.0D) : 0.0F)) * 1.2F;
      context.push(() -> {
         context.translate(
               Mth.m_14179_((float) Math.pow((double) mod, 4.0D), this.start.f_82470_, this.position.f_82470_),
               Mth.m_14179_((float) Math.pow((double) mod, 4.0D), this.start.f_82471_, this.position.f_82471_), 0.0F);
         context.scale(scale, scale, scale);
         context.push(() -> {
            context.translate(-0.5F, 0.5F, 0.0F);
            context.fill(0, 0, 1, 1, this.CENTER_COLOR);
            context.fill(1, 1, 1, 1, this.EDGE_COLOR);
            context.fill(-1, -1, 1, 1, this.EDGE_COLOR);
            context.fill(-1, 1, 1, 1, this.EDGE_COLOR);
            context.fill(1, -1, 1, 1, this.EDGE_COLOR);
         });
      });
   }
}
