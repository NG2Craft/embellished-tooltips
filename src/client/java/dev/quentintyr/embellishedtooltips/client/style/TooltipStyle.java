package com.obscuria.tooltips.client.style;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.obscuria.tooltips.client.StyleManager;
import com.obscuria.tooltips.client.renderer.TooltipContext;
import com.obscuria.tooltips.client.style.effect.TooltipEffect;
import com.obscuria.tooltips.client.style.frame.TooltipFrame;
import com.obscuria.tooltips.client.style.icon.TooltipIcon;
import com.obscuria.tooltips.client.style.panel.TooltipPanel;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class TooltipStyle {
   private final ImmutableList<TooltipEffect> EFFECTS;
   private final TooltipPanel PANEL;
   private final TooltipFrame FRAME;
   private final TooltipIcon ICON;

   private TooltipStyle(List<TooltipEffect> effects, TooltipPanel panel, TooltipFrame frame, TooltipIcon icon) {
      this.EFFECTS = ImmutableList.copyOf(effects);
      this.PANEL = panel;
      this.FRAME = frame;
      this.ICON = icon;
   }

   public void renderBack(TooltipContext renderer, Vec2 pos, Point size, boolean slot) {
      renderer.pose().m_85836_();
      renderer.pose().m_252880_(0.0F, 0.0F, -50.0F);
      this.PANEL.render(renderer, pos, size, slot);
      renderer.pose().m_85849_();
   }

   public void renderFront(TooltipContext renderer, Vec2 pos, Point size) {
      this.renderEffects(Effects.Order.LAYER_3_TEXT$FRAME, renderer, pos, size);
      renderer.push(() -> {
         renderer.translate(0.0F, 0.0F, -50.0F);
         this.FRAME.render(renderer, pos, size);
      });
      this.renderEffects(Effects.Order.LAYER_4_FRAME$ICON, renderer, pos, size);
      renderer.push(() -> {
         renderer.translate(pos.f_82470_ + 12.0F, pos.f_82471_ + 12.0F, 500.0F);
         renderer.push(() -> {
            this.ICON.render(renderer, -8, -8);
         });
      });
   }

   public void renderEffects(Effects.Order order, TooltipContext renderer, Vec2 pos, Point size) {
      renderer.push(() -> {
         float var10003;
         switch(order) {
         case LAYER_1_BACK:
            var10003 = 0.0F;
            break;
         case LAYER_2_BACK$TEXT:
            var10003 = 100.0F;
            break;
         case LAYER_3_TEXT$FRAME:
            var10003 = 400.0F;
            break;
         case LAYER_4_FRAME$ICON:
            var10003 = 500.0F;
            break;
         case LAYER_5_FRONT:
            var10003 = 1000.0F;
            break;
         default:
            throw new IncompatibleClassChangeError();
         }

         renderer.translate(0.0F, 0.0F, var10003);
         UnmodifiableIterator var5 = this.EFFECTS.iterator();

         while(var5.hasNext()) {
            TooltipEffect effect = (TooltipEffect)var5.next();
            if (effect.order().equals(order)) {
               effect.render(renderer, pos, size);
            }
         }

      });
   }

   public void reset() {
      this.PANEL.reset();
      this.ICON.reset();
      this.FRAME.reset();
      this.EFFECTS.forEach(TooltipEffect::reset);
   }

   public static class Builder {
      private final List<TooltipEffect> effects = new ArrayList();
      private TooltipPanel panel;
      private TooltipFrame frame;
      private TooltipIcon icon;

      public Builder() {
         this.panel = StyleManager.DEFAULT_PANEL;
         this.frame = StyleManager.DEFAULT_FRAME;
         this.icon = StyleManager.DEFAULT_ICON;
      }

      public TooltipStyle.Builder withPanel(TooltipPanel panel) {
         this.panel = panel;
         return this;
      }

      public TooltipStyle.Builder withFrame(TooltipFrame frame) {
         this.frame = frame;
         return this;
      }

      public TooltipStyle.Builder withIcon(TooltipIcon icon) {
         this.icon = icon;
         return this;
      }

      public TooltipStyle.Builder withEffects(List<TooltipEffect> effects) {
         this.effects.addAll(effects);
         return this;
      }

      public TooltipStyle build() {
         return new TooltipStyle(this.effects, this.panel, this.frame, this.icon);
      }
   }
}
