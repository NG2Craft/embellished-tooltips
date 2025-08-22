package dev.quentintyr.embellishedtooltips.registry;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

public final class FactoryHelper {
    public static int color(JsonObject params, String name) {
        return (int) Long.parseLong(params.get(name).getAsString(), 16);
    }

    public static Identifier key(JsonObject params, String name) {
        try {
            if (params.has(name)) {
                return new Identifier(params.get(name).getAsString());
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return new Identifier("null:null");
    }
}
