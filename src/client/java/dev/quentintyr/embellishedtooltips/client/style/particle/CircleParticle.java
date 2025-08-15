package dev.quentintyr.embellishedtooltips.client.style.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class CircleParticle extends TooltipParticle {
   protected final int START_COLOR;
   protected final int END_COLOR;
   protected final float RADIUS;
   protected Vec2 end;

   public CircleParticle(float lifetime, Vec2 start, Vec2 end, int startColor, int endColor, float radius) {
      super(lifetime);
      this.START_COLOR = startColor;
      this.END_COLOR = endColor;
      this.RADIUS = radius;
      this.position = start;
      this.end = end;
   }

   public void renderParticle(TooltipContext context, float lifetime) {
      float mod = 1.0F - (float) Math.pow((double) (1.0F - lifetime / this.MAX_LIFETIME), 3.0D);
      float scale = mod < 0.5F ? mod * 2.0F : (mod < 0.8F ? 1.0F : 1.0F - (mod - 0.8F) / 0.2F);
      context.push(() -> {
         context.translate(Mth.m_14179_(mod, this.position.f_82470_, this.end.f_82470_),
               Mth.m_14179_(mod, this.position.f_82471_, this.end.f_82471_), 0.0F);
         context.scale(scale, scale, scale);
         context.push(() -> {
            for (float i = 0.0F; i < 2.0F; i += 0.2F) {
               double d1 = 3.141592653589793D * (double) (i + 0.1F);
               double d2 = 3.141592653589793D * (double) (i - 0.1F);
               Vector2f first = new Vector2f((float) (Math.cos(d1) * (double) this.RADIUS),
                     (float) (Math.sin(d1) * (double) this.RADIUS));
               Vector2f second = new Vector2f((float) (Math.cos(d2) * (double) this.RADIUS),
                     (float) (Math.sin(d2) * (double) this.RADIUS));
               Matrix4f matrix4f = context.pose().m_85850_().m_252922_();
               VertexConsumer vertexconsumer = context.context().m_280091_().m_6299_(RenderType.m_286086_());
               vertexconsumer.m_252986_(matrix4f, 0.0F, 0.0F, 0.0F).m_193479_(this.START_COLOR).m_5752_();
               vertexconsumer.m_252986_(matrix4f, 0.0F, 0.0F, 0.0F).m_193479_(this.START_COLOR).m_5752_();
               vertexconsumer.m_252986_(matrix4f, first.x, first.y, 0.0F).m_193479_(this.END_COLOR).m_5752_();
               vertexconsumer.m_252986_(matrix4f, second.x, second.y, 0.0F).m_193479_(this.END_COLOR).m_5752_();
            }

         });
      });
   }
}
