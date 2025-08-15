package com.obscuria.tooltips.client.style.frame;

import com.mojang.math.Axis;
import com.obscuria.tooltips.client.renderer.TooltipContext;
import java.awt.Point;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public class BonesFrame implements TooltipFrame {
   private static final ResourceLocation DECOR = new ResourceLocation("obscure_tooltips", "textures/tooltips/animated/bones.png");

   public void render(TooltipContext context, Vec2 pos, Point size) {
      int x = (int)pos.f_82470_;
      int y = (int)pos.f_82471_;
      int width = size.x;
      int height = size.y;
      float timeOffset = 2.0F + (float)Math.cos((double)context.time()) * 6.0F;
      float rotation1 = (float)Math.cos((double)(context.time() * 1.3F + timeOffset)) * 0.2F;
      float rotation2 = (float)Math.cos((double)(context.time() * 1.3F + 0.5F + timeOffset)) * 0.2F;
      float rotation3 = (float)Math.cos((double)(context.time() * 1.3F + 1.0F + timeOffset)) * 0.2F;
      context.push(() -> {
         context.translate(0.0F, 0.0F, 410.0F);
         context.blit(DECOR, x - 5, y - 5, 0, 0, 6, 6, 80, 32);
         context.blit(DECOR, x + width - 1, y - 5, 74, 0, 6, 6, 80, 32);
         context.blit(DECOR, x - 5, y + height - 1, 0, 26, 6, 6, 80, 32);
         context.blit(DECOR, x + width - 1, y + height - 1, 74, 26, 6, 6, 80, 32);
         context.blit(DECOR, x + width / 2 - 10, y - 10, 29, 1, 21, 10, 80, 32);
         context.pushAndMul(Axis.f_252403_.m_252961_(rotation3), () -> {
            context.translate((float)x + (float)width / 2.0F - 16.0F, (float)y - 1.0F, 0.0F);
         }, () -> {
            context.blit(DECOR, -3, -3, 11, 6, 3, 3, 80, 32);
         });
         context.pushAndMul(Axis.f_252403_.m_252961_(rotation2), () -> {
            context.translate((float)x + (float)width / 2.0F - 11.0F, (float)y - 1.0F, 0.0F);
         }, () -> {
            context.blit(DECOR, -6, -5, 14, 4, 6, 5, 80, 32);
         });
         context.pushAndMul(Axis.f_252403_.m_252961_(rotation1), () -> {
            context.translate((float)x + (float)width / 2.0F - 4.0F, (float)y - 1.0F, 0.0F);
         }, () -> {
            context.blit(DECOR, -9, -7, 20, 2, 9, 7, 80, 32);
         });
         context.pushAndMul(Axis.f_252403_.m_252961_(-rotation3), () -> {
            context.translate((float)x + (float)width / 2.0F + 17.0F, (float)y - 1.0F, 0.0F);
         }, () -> {
            context.blit(DECOR, 0, -3, 65, 6, 3, 3, 80, 32);
         });
         context.pushAndMul(Axis.f_252403_.m_252961_(-rotation2), () -> {
            context.translate((float)x + (float)width / 2.0F + 12.0F, (float)y - 1.0F, 0.0F);
         }, () -> {
            context.blit(DECOR, 0, -5, 59, 4, 6, 5, 80, 32);
         });
         context.pushAndMul(Axis.f_252403_.m_252961_(-rotation1), () -> {
            context.translate((float)x + (float)width / 2.0F + 5.0F, (float)y - 1.0F, 0.0F);
         }, () -> {
            context.blit(DECOR, 0, -7, 50, 2, 9, 7, 80, 32);
         });
         context.blit(DECOR, x + width / 2 - 1, y + height + 2, 38, 24, 3, 4, 80, 32);
         context.pushAndMul(Axis.f_252403_.m_252961_(-rotation3), () -> {
            context.translate((float)x + (float)width / 2.0F - 12.0F, (float)(y + height) + 1.0F, 0.0F);
         }, () -> {
            context.blit(DECOR, -3, 0, 21, 23, 3, 3, 80, 32);
         });
         context.pushAndMul(Axis.f_252403_.m_252961_(-rotation2), () -> {
            context.translate((float)x + (float)width / 2.0F - 7.0F, (float)(y + height) + 1.0F, 0.0F);
         }, () -> {
            context.blit(DECOR, -6, 0, 24, 23, 6, 5, 80, 32);
         });
         context.pushAndMul(Axis.f_252403_.m_252961_(-rotation1), () -> {
            context.translate((float)x + (float)width / 2.0F - 1.0F, (float)(y + height) + 1.0F, 0.0F);
         }, () -> {
            context.blit(DECOR, -8, 0, 30, 23, 8, 7, 80, 32);
         });
         context.pushAndMul(Axis.f_252403_.m_252961_(rotation3), () -> {
            context.translate((float)x + (float)width / 2.0F + 13.0F, (float)(y + height) + 1.0F, 0.0F);
         }, () -> {
            context.blit(DECOR, 0, 0, 55, 23, 3, 3, 80, 32);
         });
         context.pushAndMul(Axis.f_252403_.m_252961_(rotation2), () -> {
            context.translate((float)x + (float)width / 2.0F + 8.0F, (float)(y + height) + 1.0F, 0.0F);
         }, () -> {
            context.blit(DECOR, 0, 0, 49, 23, 6, 5, 80, 32);
         });
         context.pushAndMul(Axis.f_252403_.m_252961_(rotation1), () -> {
            context.translate((float)x + (float)width / 2.0F + 2.0F, (float)(y + height) + 1.0F, 0.0F);
         }, () -> {
            context.blit(DECOR, 0, 0, 41, 23, 8, 7, 80, 32);
         });
      });
   }
}
