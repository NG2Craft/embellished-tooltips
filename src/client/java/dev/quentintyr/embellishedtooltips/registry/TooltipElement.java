package dev.quentintyr.embellishedtooltips.registry;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface TooltipElement<T> {
    T build(JsonObject var1);

    default T get() {
        return this.build((JsonObject) null);
    }
}
