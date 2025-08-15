package dev.quentintyr.embellishedtooltips.client.style.particle;

import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class TooltipParticle {
   protected final long START_TIME;
   protected final float MAX_LIFETIME;
   protected Vec2 position;

   public TooltipParticle(float lifetime) {
      this.position = Vec2.f_82462_;
      this.START_TIME = System.currentTimeMillis();
      this.MAX_LIFETIME = lifetime;
   }

   public abstract void renderParticle(TooltipContext var1, float var2);

   public final void render(TooltipContext context) {
      this.renderParticle(context, (float) (System.currentTimeMillis() - this.START_TIME) / 1000.0F);
   }

   public final boolean shouldRemove() {
      return (float) (System.currentTimeMillis() - this.START_TIME) / 1000.0F > this.MAX_LIFETIME;
   }
}
