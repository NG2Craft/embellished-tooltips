package com.obscuria.tooltips.client.style.panel;

import com.obscuria.tooltips.client.renderer.TooltipContext;
import java.awt.Point;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@FunctionalInterface
public interface TooltipPanel {
   void render(TooltipContext var1, Vec2 var2, Point var3, boolean var4);

   default void reset() {
   }
}
