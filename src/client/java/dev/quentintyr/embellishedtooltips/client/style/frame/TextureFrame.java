package dev.quentintyr.embellishedtooltips.client.style.frame;

import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;
import java.awt.Point;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public class TextureFrame implements TooltipFrame {
   protected final ResourceLocation TEXTURE;

   public TextureFrame(ResourceLocation texture) {
      this.TEXTURE = texture;
   }

   public void render(TooltipContext context, Vec2 pos, Point size) {
      context.blit(this.TEXTURE, (int) pos.f_82470_ + size.x / 2 - 30, (int) pos.f_82471_ - 10, 10, 0, 60, 16, 80, 32);
      context.blit(this.TEXTURE, (int) pos.f_82470_ + size.x / 2 - 30, (int) pos.f_82471_ + size.y - 6, 10, 16, 60, 16,
            80, 32);
      context.blit(this.TEXTURE, (int) pos.f_82470_ - 5, (int) pos.f_82471_ - 5, 0, 0, 10, 10, 80, 32);
      context.blit(this.TEXTURE, (int) pos.f_82470_ + size.x - 5, (int) pos.f_82471_ - 5, 70, 0, 10, 10, 80, 32);
      context.blit(this.TEXTURE, (int) pos.f_82470_ - 5, (int) pos.f_82471_ + size.y - 5, 0, 22, 10, 10, 80, 32);
      context.blit(this.TEXTURE, (int) pos.f_82470_ + size.x - 5, (int) pos.f_82471_ + size.y - 5, 70, 22, 10, 10, 80,
            32);
   }
}
