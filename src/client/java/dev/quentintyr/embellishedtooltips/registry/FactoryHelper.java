package dev.quentintyr.embellishedtooltips.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

public final class FactoryHelper {

    private static final String MODID = "embellished_tooltips";

    public static int color(JsonObject params, String name) {
        if (params == null || !params.has(name))
            return 0xFFFFFFFF;
        try {
            String s = params.get(name).getAsString().replaceFirst("^#", "");
            // Allow 6 or 8 hex digits; default opaque if 6
            long v = Long.parseLong(s, 16);
            if (s.length() <= 6)
                return 0xFF000000 | (int) v;
            return (int) v;
        } catch (Exception e) {
            e.printStackTrace();
            return 0xFFFFFFFF;
        }
    }

    public static Identifier key(JsonObject params, String name) {
        try {
            if (params != null) {
                JsonElement el = params.get(name);
                if (el != null && !el.isJsonNull()) {
                    String raw = el.getAsString();
                    if (raw != null) {
                        String s = raw.trim();
                        // If already "ns:path", use as-is
                        if (s.indexOf(':') >= 0) {
                            return new Identifier(s);
                        }
                        // Otherwise assume mod namespace + path
                        return new Identifier(MODID, s);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Avoid returning "null:null"; provide valid sentinel
        return new Identifier(MODID, "invalid");
    }
}
