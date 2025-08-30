package dev.quentintyr.embellishedtooltips.client.render.sidepanel;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;

/**
 * Handles rendering painting previews in side panels
 */
public class PaintingRenderer {
    
    /**
     * Renders a painting preview in the center of the side panel
     */
    public static void renderPaintingPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(center.x, center.y, 500.0F);
        
        // Get painting info
        String paintingId = getPaintingIdFromStack(stack);
        
        // Render frame
        int frameSize = 20;
        ms.translate(-frameSize / 2.0f, -frameSize / 2.0f, 0);
        
        // Brown frame
        ctx.fill(0, 0, frameSize, frameSize, 0xFF8B4513);
        
        // Inner painting area
        int innerSize = frameSize - 4;
        int innerOffset = 2;
        
        // Fill with a pattern based on painting ID
        int[] colors = getPaintingPattern(paintingId);
        int patternSize = 4; // 4x4 pattern
        
        for (int x = 0; x < patternSize; x++) {
            for (int y = 0; y < patternSize; y++) {
                int colorIndex = (x + y * patternSize) % colors.length;
                int cellSize = innerSize / patternSize;
                
                ctx.fill(
                    innerOffset + x * cellSize,
                    innerOffset + y * cellSize,
                    innerOffset + (x + 1) * cellSize,
                    innerOffset + (y + 1) * cellSize,
                    colors[colorIndex]
                );
            }
        }
        
        ms.pop();
    }
    
    /**
     * Extracts painting ID from item stack NBT
     */
    private static String getPaintingIdFromStack(ItemStack stack) {
        // Try to get painting entity data from NBT
        if (stack.hasNbt() && stack.getNbt().contains("EntityTag")) {
            var entityTag = stack.getNbt().getCompound("EntityTag");
            if (entityTag.contains("variant")) {
                return entityTag.getString("variant");
            }
        }
        
        // Default fallback
        return "minecraft:alban";
    }
    
    /**
     * Returns color patterns for different painting types
     */
    private static int[] getPaintingPattern(String paintingId) {
        // Return different color patterns for different paintings
        return switch (paintingId.toLowerCase()) {
            case "minecraft:kebab", "minecraft:aztec", "kebab", "aztec" -> 
                new int[]{0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00}; // Red, Green, Blue, Yellow
            case "minecraft:alban", "minecraft:courbet", "alban", "courbet" -> 
                new int[]{0xFF8B4513, 0xFFDEB887, 0xFF654321, 0xFFA0522D}; // Brown tones
            case "minecraft:sea", "minecraft:sunset", "sea", "sunset" -> 
                new int[]{0xFF0066CC, 0xFF87CEEB, 0xFF4682B4, 0xFF1E90FF}; // Blue tones
            case "minecraft:creebet", "minecraft:wanderer", "creebet", "wanderer" -> 
                new int[]{0xFF228B22, 0xFF32CD32, 0xFF90EE90, 0xFF00FF00}; // Green tones
            case "minecraft:graham", "minecraft:pool", "graham", "pool" -> 
                new int[]{0xFFFFB6C1, 0xFFFFC0CB, 0xFFFF69B4, 0xFFFF1493}; // Pink tones
            default -> 
                new int[]{0xFF800080, 0xFFDA70D6, 0xFF9370DB, 0xFFBA55D3}; // Purple tones (default)
        };
    }
}
