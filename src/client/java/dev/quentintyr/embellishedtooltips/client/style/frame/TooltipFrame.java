package com.obscuria.tooltips.client.style.frame;

import com.obscuria.tooltips.client.renderer.TooltipContext;
import java.awt.Point;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@FunctionalInterface
public interface TooltipFrame {
   void render(TooltipContext var1, Vec2 var2, Point var3);

   default void reset() {
   }
}
