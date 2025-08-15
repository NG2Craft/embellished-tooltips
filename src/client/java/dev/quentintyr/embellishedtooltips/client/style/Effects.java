package dev.quentintyr.embellishedtooltips.client.style;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Effects {
   public static enum Order {
      LAYER_1_BACK,
      LAYER_2_BACK$TEXT,
      LAYER_3_TEXT$FRAME,
      LAYER_4_FRAME$ICON,
      LAYER_5_FRONT;

      // $FF: synthetic method
      private static Effects.Order[] $values() {
         return new Effects.Order[] { LAYER_1_BACK, LAYER_2_BACK$TEXT, LAYER_3_TEXT$FRAME, LAYER_4_FRAME$ICON,
               LAYER_5_FRONT };
      }
   }
}
