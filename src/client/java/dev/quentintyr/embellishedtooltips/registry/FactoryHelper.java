package dev.quentintyr.embellishedtooltips.registry;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public final class FactoryHelper {
   public static int color(JsonObject params, String name) {
      return (int) Long.parseLong(params.get(name).getAsString(), 16);
   }

   public static ResourceLocation key(JsonObject params, String name) {
      try {
         if (params.has(name)) {
            return new ResourceLocation(params.get(name).getAsString());
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return new ResourceLocation("null:null");
   }
}
