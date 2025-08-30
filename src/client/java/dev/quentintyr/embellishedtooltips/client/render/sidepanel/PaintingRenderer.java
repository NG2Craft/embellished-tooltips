package dev.quentintyr.embellishedtooltips.client.render.sidepanel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
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
        MatrixStack matrices = ctx.getMatrices();
        matrices.push();

        // Standard painting preview size - larger for better detail
        int paintingSize = 56;
        float x = center.x - paintingSize / 2f;
        float y = center.y - paintingSize / 2f;

        matrices.translate(x, y, 500.0F);

        try {
            // Always render something visible for debugging
            renderPaintingFrame(ctx, paintingSize);

            // Try to get the painting variant from the item
            String paintingId = getPaintingIdFromStack(stack);
            if (paintingId != null) {
                renderPaintingContent(ctx, paintingSize, paintingId);
            } else {
                renderPaintingPlaceholder(ctx, paintingSize);
            }
        } catch (Exception e) {
            // Fallback to placeholder
            renderPaintingPlaceholder(ctx, paintingSize);
        }

        matrices.pop();
    }

    /**
     * Gets the painting variant ID from an item stack
     */
    private static String getPaintingIdFromStack(ItemStack stack) {
        // Try to get painting variant from NBT data
        try {
            if (stack.hasNbt()) {
                var nbt = stack.getNbt();

                // Check for EntityTag.variant (from placing the painting)
                if (nbt.contains("EntityTag")) {
                    var entityTag = nbt.getCompound("EntityTag");
                    if (entityTag.contains("variant")) {
                        return entityTag.getString("variant");
                    }
                }

                // Check for direct variant property
                if (nbt.contains("variant")) {
                    return nbt.getString("variant");
                }
            }
        } catch (Exception e) {
            // Ignore NBT errors
        }

        // For now, show different paintings based on the item's hash to demonstrate
        // variety
        int hash = Math.abs(stack.toString().hashCode()) % 12;
        String[] variants = {
                "kebab", "aztec", "alban", "aztec2", "bomb", "plant",
                "wasteland", "pool", "courbet", "sea", "sunset", "creebet"
        };
        return "minecraft:" + variants[hash];
    }

    /**
     * Renders the frame around a painting preview
     */
    private static void renderPaintingFrame(DrawContext ctx, int size) {
        // Draw wooden frame with more contrast
        ctx.fill(0, 0, size, size, 0xFF8B4513); // Dark brown outer frame
        ctx.fill(2, 2, size - 2, size - 2, 0xFFD2B48C); // Light brown inner frame
        ctx.fill(4, 4, size - 4, size - 4, 0xFFF5DEB3); // Canvas background
    }

    /**
     * Renders the content of a painting based on its ID
     */
    private static void renderPaintingContent(DrawContext ctx, int size, String paintingId) {
        // Draw content within the frame
        int innerSize = size - 8;
        int startX = 4;
        int startY = 4;

        // Normalize painting ID (remove minecraft: prefix if present)
        String normalizedId = paintingId.toLowerCase();
        if (normalizedId.startsWith("minecraft:")) {
            normalizedId = normalizedId.substring(10);
        }

        // Create recognizable patterns based on actual Minecraft painting variants
        switch (normalizedId) {
            case "kebab" -> {
                // Kebab painting - meat on a stick pattern
                ctx.fill(startX + innerSize / 4, startY + 2, startX + 3 * innerSize / 4, startY + innerSize - 2,
                        0xFF8B4513);
                ctx.fill(startX + innerSize / 3, startY + innerSize / 4, startX + 2 * innerSize / 3,
                        startY + 3 * innerSize / 4, 0xFFB22222);
                ctx.fill(startX + innerSize / 3 + 2, startY + innerSize / 4 + 2, startX + 2 * innerSize / 3 - 2,
                        startY + 3 * innerSize / 4 - 2, 0xFFDC143C);
            }
            case "aztec" -> {
                // Aztec pattern - geometric design
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF8B4513);
                ctx.fill(startX + innerSize / 4, startY + innerSize / 4, startX + 3 * innerSize / 4,
                        startY + 3 * innerSize / 4, 0xFFFFD700);
                ctx.fill(startX + innerSize / 3, startY + innerSize / 3, startX + 2 * innerSize / 3,
                        startY + 2 * innerSize / 3, 0xFFFF4500);
            }
            case "alban" -> {
                // Alban painting - portrait-like with earth tones
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF8B7355);
                ctx.fill(startX + 2, startY + 2, startX + innerSize - 2, startY + innerSize / 2, 0xFFDEB887);
                ctx.fill(startX + innerSize / 4, startY + innerSize / 3, startX + 3 * innerSize / 4,
                        startY + 2 * innerSize / 3, 0xFFF5DEB3);
            }
            case "aztec2" -> {
                // Aztec2 - another geometric pattern
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF654321);
                ctx.fill(startX + 1, startY + 1, startX + innerSize - 1, startY + innerSize - 1, 0xFFCD853F);
                // Diamond pattern
                int centerX = startX + innerSize / 2;
                int centerY = startY + innerSize / 2;
                for (int i = 0; i < innerSize / 4; i++) {
                    ctx.drawHorizontalLine(centerX - i, centerX + i, centerY - innerSize / 4 + i, 0xFFFFD700);
                    ctx.drawHorizontalLine(centerX - i, centerX + i, centerY + innerSize / 4 - i, 0xFFFFD700);
                }
            }
            case "bomb" -> {
                // Bomb painting - dark with explosive elements
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF2F2F2F);
                ctx.fill(startX + innerSize / 3, startY + innerSize / 3, startX + 2 * innerSize / 3,
                        startY + 2 * innerSize / 3, 0xFF000000);
                ctx.fill(startX + innerSize / 2 - 2, startY + 2, startX + innerSize / 2 + 2, startY + innerSize / 3,
                        0xFFFF0000);
            }
            case "plant" -> {
                // Plant painting - green nature scene
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF87CEEB); // Sky blue background
                ctx.fill(startX, startY + 2 * innerSize / 3, startX + innerSize, startY + innerSize, 0xFF228B22); // Ground
                ctx.fill(startX + innerSize / 2 - 2, startY + innerSize / 2, startX + innerSize / 2 + 2,
                        startY + innerSize - 2, 0xFF8B4513); // Stem
                ctx.fill(startX + innerSize / 4, startY + innerSize / 4, startX + 3 * innerSize / 4,
                        startY + innerSize / 2, 0xFF32CD32); // Leaves
            }
            case "wasteland" -> {
                // Wasteland - post-apocalyptic scene
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF8B7355); // Desert background
                ctx.fill(startX, startY + 3 * innerSize / 4, startX + innerSize, startY + innerSize, 0xFFD2B48C); // Sand
                // Add some "wasteland" elements
                for (int i = 0; i < 4; i++) {
                    int px = startX + (i * innerSize / 5) + 2;
                    int py = startY + innerSize / 2 + (i % 2) * 4;
                    ctx.fill(px, py, px + 2, py + innerSize / 4, 0xFF654321);
                }
            }
            case "pool" -> {
                // Pool painting - water scene
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF87CEEB); // Light blue background
                ctx.fill(startX + innerSize / 6, startY + innerSize / 3, startX + 5 * innerSize / 6,
                        startY + 2 * innerSize / 3, 0xFF4169E1); // Pool water
                ctx.fill(startX + innerSize / 4, startY + innerSize / 2, startX + 3 * innerSize / 4,
                        startY + 3 * innerSize / 5, 0xFF0000FF); // Deeper water
            }
            case "courbet" -> {
                // Courbet - landscape painting
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF87CEEB); // Sky
                ctx.fill(startX, startY + innerSize / 2, startX + innerSize, startY + innerSize, 0xFF228B22); // Ground
                ctx.fill(startX + innerSize / 4, startY + innerSize / 3, startX + 3 * innerSize / 4,
                        startY + 2 * innerSize / 3, 0xFF8B4513); // Earth tones
            }
            case "sea" -> {
                // Sea painting - ocean scene
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF87CEEB); // Sky
                ctx.fill(startX, startY + innerSize / 3, startX + innerSize, startY + innerSize, 0xFF4169E1); // Sea
                // Add waves
                for (int i = 0; i < innerSize; i += 4) {
                    ctx.fill(startX + i, startY + innerSize / 2, startX + i + 2, startY + innerSize / 2 + 2,
                            0xFF1E90FF);
                }
            }
            case "sunset" -> {
                // Sunset painting - warm colors
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize / 3, 0xFFFFD700); // Golden sky
                ctx.fill(startX, startY + innerSize / 3, startX + innerSize, startY + 2 * innerSize / 3, 0xFFFF8C00); // Orange
                ctx.fill(startX, startY + 2 * innerSize / 3, startX + innerSize, startY + innerSize, 0xFFFF4500); // Red
                                                                                                                  // horizon
            }
            case "creebet" -> {
                // Creeper-themed painting
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF00FF00); // Green background
                ctx.fill(startX + innerSize / 4, startY + innerSize / 4, startX + 3 * innerSize / 4,
                        startY + 3 * innerSize / 4, 0xFF228B22); // Darker green
                ctx.fill(startX + innerSize / 3, startY + innerSize / 3, startX + 2 * innerSize / 3,
                        startY + 2 * innerSize / 3, 0xFF006400); // Dark green center
            }
            case "wanderer" -> {
                // Wanderer painting - mysterious figure
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF483D8B); // Dark slate blue
                ctx.fill(startX + innerSize / 3, startY + innerSize / 4, startX + 2 * innerSize / 3,
                        startY + 3 * innerSize / 4, 0xFF2F2F2F); // Dark figure
                ctx.fill(startX + innerSize / 2 - 1, startY + innerSize / 3, startX + innerSize / 2 + 1,
                        startY + innerSize / 2, 0xFFFFFFFF); // Light element
            }
            case "graham" -> {
                // Graham painting - portrait style
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF8B7355); // Brown background
                ctx.fill(startX + innerSize / 4, startY + innerSize / 6, startX + 3 * innerSize / 4,
                        startY + 5 * innerSize / 6, 0xFFF5DEB3); // Portrait area
                ctx.fill(startX + innerSize / 3, startY + innerSize / 4, startX + 2 * innerSize / 3,
                        startY + 3 * innerSize / 4, 0xFFDEB887); // Face area
            }
            default -> {
                // Generic colorful pattern for unknown paintings
                int hash = normalizedId.hashCode();
                int color1 = 0xFF000000 | (Math.abs(hash) & 0xFFFFFF);
                int color2 = 0xFF000000 | (Math.abs(hash >> 8) & 0xFFFFFF);
                int color3 = 0xFF000000 | (Math.abs(hash >> 16) & 0xFFFFFF);

                // Ensure colors are not too dark
                color1 = ensureColorBrightness(color1);
                color2 = ensureColorBrightness(color2);
                color3 = ensureColorBrightness(color3);

                ctx.fill(startX, startY, startX + innerSize, startY + innerSize / 3, color1);
                ctx.fill(startX, startY + innerSize / 3, startX + innerSize, startY + 2 * innerSize / 3, color2);
                ctx.fill(startX, startY + 2 * innerSize / 3, startX + innerSize, startY + innerSize, color3);
            }
        }
    }

    /**
     * Ensures a color has minimum brightness for visibility
     */
    private static int ensureColorBrightness(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Calculate brightness
        double brightness = (r * 0.299 + g * 0.587 + b * 0.114) / 255.0;

        // If too dark, brighten it
        if (brightness < 0.3) {
            float factor = 0.3f / (float) brightness;
            r = Math.min(255, (int) (r * factor));
            g = Math.min(255, (int) (g * factor));
            b = Math.min(255, (int) (b * factor));
        }

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Renders a placeholder for paintings when no specific pattern is available
     */
    private static void renderPaintingPlaceholder(DrawContext ctx, int size) {
        // Draw a generic painting with a question mark
        int innerSize = size - 8;
        int startX = 4;
        int startY = 4;

        // Fill with a neutral color
        ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFFCCCCCC);

        // Draw a question mark
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer font = mc.textRenderer;
        String questionMark = "?";
        int textWidth = font.getWidth(questionMark);
        int textX = startX + (innerSize - textWidth) / 2;
        int textY = startY + (innerSize - font.fontHeight) / 2;

        ctx.drawText(font, questionMark, textX, textY, 0xFF000000, false);
    }
}
