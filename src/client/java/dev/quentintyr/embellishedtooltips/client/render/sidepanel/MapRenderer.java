package dev.quentintyr.embellishedtooltips.client.render.sidepanel;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;

/**
 * Handles rendering map previews in side panels
 */
public class MapRenderer {
    
    /**
     * Renders a map preview in the center of the side panel
     */
    public static void renderMapPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(center.x, center.y, 500.0F);
        
        // Render a simplified map grid
        int gridSize = 8;
        int cellSize = 3;
        int totalSize = gridSize * cellSize;
        
        ms.translate(-totalSize / 2.0f, -totalSize / 2.0f, 0);
        
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                // Generate a color based on position (simulate map data)
                int color = getMapColor(x, y, gridSize);
                
                ctx.fill(
                    (int) (x * cellSize), 
                    (int) (y * cellSize),
                    (int) ((x + 1) * cellSize), 
                    (int) ((y + 1) * cellSize), 
                    color
                );
            }
        }
        
        ms.pop();
    }
    
    /**
     * Generates a color for a map cell based on coordinates
     */
    private static int getMapColor(int x, int y, int gridSize) {
        // Simple hash-based color generation for map-like appearance
        int hash = (x * 31 + y * 17) % 100;
        
        if (hash < 30) {
            // Water - blue tones
            return 0xFF0066CC + (hash % 3) * 0x001111;
        } else if (hash < 50) {
            // Grass - green tones
            return 0xFF228B22 + (hash % 5) * 0x001100;
        } else if (hash < 70) {
            // Stone - gray tones
            return 0xFF808080 + (hash % 4) * 0x111111;
        } else if (hash < 85) {
            // Desert - sandy tones
            return 0xFFF4A460 + (hash % 3) * 0x110011;
        } else {
            // Snow - white tones
            return 0xFFFFFAFA - (hash % 3) * 0x101010;
        }
    }
}
