package dev.quentintyr.embellishedtooltips.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.quentintyr.embellishedtooltips.client.style.particle.TooltipParticle;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public final class TooltipContext {
   private final GuiGraphics CONTEXT;
   private ItemStack stack;
   private float seconds;

   public TooltipContext(GuiGraphics context) {
      this.stack = ItemStack.f_41583_;
      this.seconds = 0.0F;
      this.CONTEXT = context;
   }

   public void define(ItemStack stack, float seconds) {
      this.stack = stack;
      this.seconds = seconds;
   }

   public ItemStack stack() {
      return this.stack;
   }

   public float time() {
      return this.seconds;
   }

   public GuiGraphics context() {
      return this.CONTEXT;
   }

   public PoseStack pose() {
      return this.context().m_280168_();
   }

   public BufferSource bufferSource() {
      return this.context().m_280091_();
   }

   public void drawManaged(Runnable runnable) {
      this.context().m_286007_(runnable);
   }

   public void renderItem(Vector3f rot, Vector3f scale) {
      this.push(() -> {
         this.translate(0.0F, 0.0F, 500.0F);
         this.scale(scale.x, scale.y, scale.z);
         this.mul(Axis.f_252529_.m_252977_(rot.x));
         this.mul(Axis.f_252436_.m_252977_(rot.y));
         this.mul(Axis.f_252403_.m_252977_(rot.z));
         this.push(() -> {
            this.translate(-8.0F, -8.0F, -150.0F);
            this.context().m_280480_(this.stack, 0, 0);
         });
      });
   }

   public void renderParticles(List<TooltipParticle> particles) {
      List.copyOf(particles).forEach((particle) -> {
         if (particle.shouldRemove()) {
            particles.remove(particle);
         }

      });
      particles.forEach((particle) -> {
         particle.render(this);
      });
   }

   public void fill(int x, int y, int width, int height, int color) {
      this.context().m_280509_(x, y, x + width, y + height, color);
   }

   public void fillGradient(int x, int y, int width, int height, int start, int end) {
      this.context().m_280024_(x, y, x + width, y + height, start, end);
   }

   public void blit(ResourceLocation texture, int x, int y, int xTex, int yTex, int width, int height, int widthTex,
         int heightTex) {
      this.context().m_280163_(texture, x, y, (float) xTex, (float) yTex, width, height, widthTex, heightTex);
   }

   public void push(Runnable runnable) {
      this.context().m_280168_().m_85836_();

      try {
         runnable.run();
      } catch (Exception var3) {
      }

      this.context().m_280168_().m_85849_();
   }

   public void pushAndMul(Quaternionf quaternionf, Runnable before, Runnable after) {
      this.context().m_280168_().m_85836_();

      try {
         before.run();
      } catch (Exception var6) {
      }

      this.context().m_280168_().m_252781_(quaternionf);

      try {
         after.run();
      } catch (Exception var5) {
      }

      this.context().m_280168_().m_85849_();
   }

   public Vec2 lerp(Vec2 from, Vec2 to, float progress) {
      return new Vec2(Mth.m_14179_(progress, from.f_82470_, to.f_82470_),
            Mth.m_14179_(progress, from.f_82471_, to.f_82471_));
   }

   public float angle(Vec2 from, Vec2 to) {
      return (float) Math.atan2((double) (to.f_82471_ - from.f_82471_), (double) (to.f_82470_ - from.f_82470_));
   }

   public void flush() {
      this.context().m_280262_();
   }

   public int width() {
      return this.context().m_280182_();
   }

   public int height() {
      return this.context().m_280206_();
   }

   public void translate(float x, float y, float z) {
      this.context().m_280168_().m_252880_(x, y, z);
   }

   public void scale(float x, float y, float z) {
      this.context().m_280168_().m_85841_(x, y, z);
   }

   public void mul(Quaternionf quat) {
      this.context().m_280168_().m_252781_(quat);
   }
}
