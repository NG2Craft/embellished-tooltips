package dev.quentintyr.embellishedtooltips.client.render.sidepanel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;

/**
 * Handles rendering map previews in side panels
 */
public class MapRenderer {

    // Small cache instance; consider clearing it on screen close/world change
    private static final MapPreviewCache MAP_CACHE = new MapPreviewCache();

    /**
     * Renders a map preview in the center of the side panel
     */
    public static void renderMapPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        MatrixStack matrices = ctx.getMatrices();
        matrices.push();

        int mapSize = 56; // Larger size to better fill the 64x64 square panel
        float x = center.x - mapSize / 2f;
        float y = center.y - mapSize / 2f;

        matrices.translate(x, y, 450.0F); // Render above tooltip background but below effects

        try {
            Integer mapId = FilledMapItem.getMapId(stack);
            if (mapId != null) {
                MinecraftClient mc = MinecraftClient.getInstance();
                MapState mapState = FilledMapItem.getMapState(mapId, mc.world);
                if (mapState != null && mapState.colors != null) {
                    renderMapData(ctx, mapId, mapState, mapSize);
                } else {
                    renderMapPlaceholder(ctx, mapSize);
                }
            } else {
                renderMapPlaceholder(ctx, mapSize);
            }
        } catch (Exception e) {
            renderMapPlaceholder(ctx, mapSize);
        }

        matrices.pop();
    }

    /**
     * Draws frame + cached 128x128 map texture
     */
    private static void renderMapData(DrawContext ctx, int mapId, MapState mapState, int size) {
        // Background + frame
        ctx.fill(0, 0, size, size, 0xFFF7F3D0);
        ctx.fill(1, 1, size - 1, size - 1, 0xFFF5F1CE);

        // Border lines
        ctx.fill(0, 0, size, 1, 0xFF8B7355); // top
        ctx.fill(0, size - 1, size, size, 0xFF8B7355); // bottom
        ctx.fill(0, 0, 1, size, 0xFF8B7355); // left
        ctx.fill(size - 1, 0, size, size, 0xFF8B7355); // right

        int inner = size - 4;
        int dstX = 2, dstY = 2, dstW = inner, dstH = inner;

        Identifier texId = MAP_CACHE.getOrBuild(mapId, mapState);
        ctx.drawTexture(texId, dstX, dstY, dstW, dstH, 0, 0, 128, 128, 128, 128);
    }

    /**
     * Converts a map color index to ARGB (used only during texture build)
     * Uses Minecraft's official map color palette for accurate rendering
     */
    static int getMapColor(byte colorIndex) {
        int colorId = colorIndex & 0xFF;
        
        // Handle transparent/air color
        if (colorId == 0) {
            return 0x00000000; // Transparent
        }
        
        int baseColor = colorId / 4;
        int shade = colorId % 4;

        // Minecraft's official map color palette (RGB values)
        int[] baseColors = {
                0x000000, // 0: NONE (should not be used)
                0x7FB238, // 1: GRASS
                0xF7E9A3, // 2: SAND  
                0xC7C7C7, // 3: WOOL
                0x4040FF, // 4: FIRE/LAVA (swapped - was showing as water)
                0xA0A0FF, // 5: ICE
                0xA7A7A7, // 6: METAL
                0x007C00, // 7: PLANT
                0xFFFFFF, // 8: SNOW
                0xA4A4A4, // 9: CLAY
                0x976D4D, // 10: DIRT
                0x707070, // 11: STONE
                0xFF0000, // 12: WATER (swapped - was showing as lava)
                0x8F7748, // 13: WOOD
                0xFFFCF5, // 14: QUARTZ
                0xD87F33, // 15: COLOR_ORANGE
                0xB24CD8, // 16: COLOR_MAGENTA
                0x6699D8, // 17: COLOR_LIGHT_BLUE
                0xE5E533, // 18: COLOR_YELLOW
                0x7FCC19, // 19: COLOR_LIGHT_GREEN
                0xF27FA5, // 20: COLOR_PINK
                0x4C4C4C, // 21: COLOR_GRAY
                0x999999, // 22: COLOR_LIGHT_GRAY
                0x4C7F99, // 23: COLOR_CYAN
                0x7F3FB2, // 24: COLOR_PURPLE
                0x334CB2, // 25: COLOR_BLUE
                0x664C33, // 26: COLOR_BROWN
                0x667F33, // 27: COLOR_GREEN
                0x993333, // 28: COLOR_RED
                0x191919, // 29: COLOR_BLACK
                0xFAEE4D, // 30: GOLD
                0x5CDBD5, // 31: DIAMOND
                0x4A80FF, // 32: LAPIS
                0x00D93A, // 33: EMERALD
                0x815631, // 34: PODZOL
                0x700200, // 35: NETHER
                0xD1B1A1, // 36: TERRACOTTA_WHITE
                0x9F5224, // 37: TERRACOTTA_ORANGE
                0x95576C, // 38: TERRACOTTA_MAGENTA
                0x706C8A, // 39: TERRACOTTA_LIGHT_BLUE
                0xBA8524, // 40: TERRACOTTA_YELLOW
                0x677535, // 41: TERRACOTTA_LIGHT_GREEN
                0xA04D4E, // 42: TERRACOTTA_PINK
                0x392923, // 43: TERRACOTTA_GRAY
                0x876B62, // 44: TERRACOTTA_LIGHT_GRAY
                0x575C5C, // 45: TERRACOTTA_CYAN
                0x7A4958, // 46: TERRACOTTA_PURPLE
                0x4C3E5C, // 47: TERRACOTTA_BLUE
                0x4C3223, // 48: TERRACOTTA_BROWN
                0x4C522A, // 49: TERRACOTTA_GREEN
                0x8E3C2E, // 50: TERRACOTTA_RED
                0x251610, // 51: TERRACOTTA_BLACK
                0xBD3031, // 52: CRIMSON_NYLIUM
                0x943F61, // 53: CRIMSON_STEM
                0x5C191D, // 54: CRIMSON_HYPHAE
                0x167E86, // 55: WARPED_NYLIUM
                0x3A8E8C, // 56: WARPED_STEM
                0x562C3E, // 57: WARPED_HYPHAE
                0x14B485, // 58: WARPED_WART_BLOCK
                0x646464, // 59: DEEPSLATE
                0x7A7A7A, // 60: RAW_IRON
                0x7AFDD4  // 61: GLOW_LICHEN
        };

        if (baseColor >= baseColors.length || baseColor <= 0) {
            return 0xFF000000; // Black for invalid colors
        }
        
        int color = baseColors[baseColor];

        // Apply brightness multiplier based on shade
        float brightness = switch (shade) {
            case 0 -> 0.71f; // Darkest
            case 1 -> 0.86f; // Dark
            case 2 -> 1.0f;  // Normal (brightest)
            case 3 -> 0.53f; // Very dark
            default -> 1.0f;
        };

        int r = Math.min(255, (int) ((color >> 16 & 0xFF) * brightness));
        int g = Math.min(255, (int) ((color >> 8 & 0xFF) * brightness));
        int b = Math.min(255, (int) ((color & 0xFF) * brightness));

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Placeholder for when actual map data is not available
     */
    private static void renderMapPlaceholder(DrawContext ctx, int size) {
        ctx.fill(0, 0, size, size, 0xFFF7F3D0);
        ctx.fill(1, 1, size - 1, size - 1, 0xFFF5F1CE);

        ctx.fill(0, 0, size, 1, 0xFF8B7355); // top
        ctx.fill(0, size - 1, size, size, 0xFF8B7355); // bottom
        ctx.fill(0, 0, 1, size, 0xFF8B7355); // left
        ctx.fill(size - 1, 0, size, size, 0xFF8B7355); // right

        int gridSpacing = 8;
        for (int i = gridSpacing; i < size - gridSpacing; i += gridSpacing) {
            ctx.fill(4, i, size - 4, i + 1, 0xFFE0DCC0); // horizontal grid line
            ctx.fill(i, 4, i + 1, size - 4, 0xFFE0DCC0); // vertical grid line
        }
    }
}
