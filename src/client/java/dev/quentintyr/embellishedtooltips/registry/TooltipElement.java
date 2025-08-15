package com.obscuria.tooltips.registry;

import com.google.gson.JsonObject;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@FunctionalInterface
public interface TooltipElement<T> {
   T build(JsonObject var1);

   default T get() {
      return this.build((JsonObject)null);
   }
}
