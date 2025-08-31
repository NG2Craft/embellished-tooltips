package dev.quentintyr.embellishedtooltips.client.render.map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;

import java.io.Closeable;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Caches 128x128 map images as GPU textures and rebuilds only when data changes
 */
class MapPreviewCache {

    private static final int MAX_ENTRIES = 32;

    private final LinkedHashMap<Integer, CacheEntry> cache = new LinkedHashMap<Integer, CacheEntry>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<Integer, CacheEntry> eldest) {
            if (size() > MAX_ENTRIES) {
                eldest.getValue().close();
                return true;
            }
            return false;
        }
    };

    Identifier getOrBuild(int mapId, MapState state) {
        long version = computeVersion(state);

        CacheEntry e = cache.get(mapId);
        if (e == null || e.version != version) {
            if (e != null)
                e.close();
            e = uploadTexture(mapId, state);
            cache.put(mapId, e);
        }
        return e.id;
    }

    void clear() {
        for (CacheEntry e : cache.values())
            e.close();
        cache.clear();
    }

    private long computeVersion(MapState state) {
        if (state.colors == null)
            return 0L;

        // Use Arrays.hashCode for efficient content-based hash calculation
        // This will detect changes when map data is updated
        return Arrays.hashCode(state.colors);
    }

    private CacheEntry uploadTexture(int mapId, MapState state) {
        NativeImage img = new NativeImage(128, 128, true);

        byte[] colors = state.colors;
        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            lut[i] = MapRenderer.getMapColor((byte) i);
        }

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                int idx = x + y * 128;
                int argb = 0x00000000;
                if (colors != null && idx < colors.length) {
                    int ci = colors[idx] & 0xFF;
                    if (ci != 0) {
                        argb = lut[ci];
                    }
                }
                img.setColor(x, y, argb);
            }
        }

        NativeImageBackedTexture tex = new NativeImageBackedTexture(img);
        Identifier id = Identifier.of(
                "embellishedtooltips",
                "map_preview/" + state.hashCode() + "_" + System.nanoTime());
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);

        return new CacheEntry(id, tex, computeVersion(state));
    }

    private static class CacheEntry implements Closeable {
        final Identifier id;
        final NativeImageBackedTexture texture;
        final long version;

        CacheEntry(Identifier id, NativeImageBackedTexture texture, long version) {
            this.id = id;
            this.texture = texture;
            this.version = version;
        }

        @Override
        public void close() {
            if (texture != null) {
                texture.close(); // releases GPU memory
                MinecraftClient.getInstance().getTextureManager().destroyTexture(id);
            }
        }
    }

    /**
     * Clears all cached textures and releases GPU memory
     */
    public void clearCache() {
        synchronized (cache) {
            cache.values().forEach(CacheEntry::close);
            cache.clear();
        }
    }

    /**
     * Removes a specific map from cache
     */
    public void invalidateMap(int mapId) {
        synchronized (cache) {
            CacheEntry entry = cache.remove(mapId);
            if (entry != null) {
                entry.close();
            }
        }
    }
}
