package dev.quentintyr.embellishedtooltips.client.render.painting;

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
     * Gets painting dimensions based on the variant ID
     * Returns [width, height] in blocks (each block = 16 pixels)
     */
    private static int[] getPaintingDimensions(String paintingId) {
        // Normalize painting ID (remove minecraft: prefix if present)
        String normalizedId = paintingId.toLowerCase();
        if (normalizedId.startsWith("minecraft:")) {
            normalizedId = normalizedId.substring(10);
        }

        // Official Minecraft painting sizes in blocks (width x height)
        return switch (normalizedId) {
            // 1x1 paintings (16x16 pixels)
            case "kebab", "aztec", "alban", "aztec2", "bomb", "plant", "wasteland" -> new int[] { 1, 1 };

            // 1x2 paintings (16x32 pixels)
            case "pool", "courbet", "sea", "sunset", "creebet" -> new int[] { 1, 2 };

            // 2x1 paintings (32x16 pixels)
            case "wanderer", "graham" -> new int[] { 2, 1 };

            // 2x2 paintings (32x32 pixels)
            case "match", "bust", "stage", "void", "skull_and_roses", "wither" -> new int[] { 2, 2 };

            // 4x2 paintings (64x32 pixels)
            case "fighters" -> new int[] { 4, 2 };

            // 4x3 paintings (64x48 pixels)
            case "pointer", "pigscene", "burning_skull" -> new int[] { 4, 3 };

            // 4x4 paintings (64x64 pixels)
            case "skeleton", "donkey_kong" -> new int[] { 4, 4 };

            // Default to 1x1 if unknown
            default -> new int[] { 1, 1 };
        };
    }

    /**
     * Renders a painting preview in the center of the side panel
     */
    public static void renderPaintingPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        MatrixStack matrices = ctx.getMatrices();
        matrices.push();

        try {
            // Get the painting variant and its dimensions
            String paintingId = getPaintingIdFromStack(stack);
            int[] dimensions = getPaintingDimensions(paintingId);
            int paintingWidthBlocks = dimensions[0];
            int paintingHeightBlocks = dimensions[1];

            // Calculate display size - scale to fit in available space while maintaining
            // aspect ratio
            int maxDisplaySize = 72; // Maximum size to fit in the larger 80x64 panel
            float aspectRatio = (float) paintingWidthBlocks / paintingHeightBlocks;

            int displayWidth, displayHeight;
            if (aspectRatio > 1.0f) {
                // Wider than tall
                displayWidth = Math.min(maxDisplaySize, paintingWidthBlocks * 16);
                displayHeight = (int) (displayWidth / aspectRatio);
            } else {
                // Taller than wide or square
                displayHeight = Math.min(maxDisplaySize, paintingHeightBlocks * 16);
                displayWidth = (int) (displayHeight * aspectRatio);
            }

            float x = center.x - displayWidth / 2f;
            float y = center.y - displayHeight / 2f;

            matrices.translate(x, y, 500.0F);

            System.out.println("Rendering painting '" + paintingId + "' with dimensions " +
                    paintingWidthBlocks + "x" + paintingHeightBlocks + " blocks, display size " +
                    displayWidth + "x" + displayHeight + " pixels"); // Debug log

            // Always render the frame first
            renderPaintingFrame(ctx, displayWidth, displayHeight);

            if (paintingId != null) {
                renderPaintingContent(ctx, displayWidth, displayHeight, paintingId);
            } else {
                renderPaintingPlaceholder(ctx, displayWidth, displayHeight);
            }
        } catch (Exception e) {
            System.err.println("Error rendering painting: " + e.getMessage());
            // Fallback to square placeholder
            int fallbackSize = 32;
            float x = center.x - fallbackSize / 2f;
            float y = center.y - fallbackSize / 2f;
            matrices.translate(x, y, 500.0F);
            renderPaintingPlaceholder(ctx, fallbackSize, fallbackSize);
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

                // Check for painting-specific NBT structure
                if (nbt.contains("Painting")) {
                    var painting = nbt.getCompound("Painting");
                    if (painting.contains("variant")) {
                        return painting.getString("variant");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading painting NBT: " + e.getMessage());
        }

        // If no specific variant found, use a deterministic selection based on item
        // data
        // This ensures paintings look consistent even without NBT data
        int hash = Math.abs(stack.hashCode()) % 26; // 26 standard Minecraft paintings
        String[] variants = {
                "kebab", "aztec", "alban", "aztec2", "bomb", "plant",
                "wasteland", "pool", "courbet", "sea", "sunset", "creebet",
                "wanderer", "graham", "match", "bust", "stage", "void",
                "skull_and_roses", "wither", "fighters", "pointer", "pigscene",
                "burning_skull", "skeleton", "donkey_kong"
        };
        return "minecraft:" + variants[hash];
    }

    /**
     * Renders the frame around a painting preview
     */
    private static void renderPaintingFrame(DrawContext ctx, int width, int height) {
        // Draw wooden frame with more contrast
        ctx.fill(0, 0, width, height, 0xFF8B4513); // Dark brown outer frame
        ctx.fill(2, 2, width - 2, height - 2, 0xFFD2B48C); // Light brown inner frame
        ctx.fill(4, 4, width - 4, height - 4, 0xFFF5DEB3); // Canvas background
    }

    /**
     * Renders the content of a painting based on its ID
     */
    private static void renderPaintingContent(DrawContext ctx, int width, int height, String paintingId) {
        // Draw content within the frame
        int innerWidth = width - 8;
        int innerHeight = height - 8;
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
                ctx.fill(startX + innerWidth / 4, startY + 2, startX + 3 * innerWidth / 4, startY + innerHeight - 2,
                        0xFF8B4513);
                ctx.fill(startX + innerWidth / 3, startY + innerHeight / 4, startX + 2 * innerWidth / 3,
                        startY + 3 * innerHeight / 4, 0xFFB22222);
                ctx.fill(startX + innerWidth / 3 + 2, startY + innerHeight / 4 + 2, startX + 2 * innerWidth / 3 - 2,
                        startY + 3 * innerHeight / 4 - 2, 0xFFDC143C);
            }
            case "aztec" -> {
                // Aztec pattern - geometric design
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF8B4513);
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 4, startX + 3 * innerWidth / 4,
                        startY + 3 * innerHeight / 4, 0xFFFFD700);
                ctx.fill(startX + innerWidth / 3, startY + innerHeight / 3, startX + 2 * innerWidth / 3,
                        startY + 2 * innerHeight / 3, 0xFFFF4500);
            }
            case "alban" -> {
                // Alban painting - portrait-like with earth tones
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF8B7355);
                ctx.fill(startX + 2, startY + 2, startX + innerWidth - 2, startY + innerHeight / 2, 0xFFDEB887);
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 3, startX + 3 * innerWidth / 4,
                        startY + 2 * innerHeight / 3, 0xFFF5DEB3);
            }
            case "aztec2" -> {
                // Aztec2 - another geometric pattern
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF654321);
                ctx.fill(startX + 1, startY + 1, startX + innerWidth - 1, startY + innerHeight - 1, 0xFFCD853F);
                // Diamond pattern
                int centerX = startX + innerWidth / 2;
                int centerY = startY + innerHeight / 2;
                int diamondSize = Math.min(innerWidth, innerHeight) / 4;
                for (int i = 0; i < diamondSize; i++) {
                    ctx.drawHorizontalLine(centerX - i, centerX + i, centerY - diamondSize + i, 0xFFFFD700);
                    ctx.drawHorizontalLine(centerX - i, centerX + i, centerY + diamondSize - i, 0xFFFFD700);
                }
            }
            case "bomb" -> {
                // Bomb painting - dark with explosive elements
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF2F2F2F);
                ctx.fill(startX + innerWidth / 3, startY + innerHeight / 3, startX + 2 * innerWidth / 3,
                        startY + 2 * innerHeight / 3, 0xFF000000);
                ctx.fill(startX + innerWidth / 2 - 2, startY + 2, startX + innerWidth / 2 + 2, startY + innerHeight / 3,
                        0xFFFF0000);
            }
            case "plant" -> {
                // Plant painting - green nature scene
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF87CEEB); // Sky blue background
                ctx.fill(startX, startY + 2 * innerHeight / 3, startX + innerWidth, startY + innerHeight, 0xFF228B22); // Ground
                ctx.fill(startX + innerWidth / 2 - 2, startY + innerHeight / 2, startX + innerWidth / 2 + 2,
                        startY + innerHeight - 2, 0xFF8B4513); // Stem
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 4, startX + 3 * innerWidth / 4,
                        startY + innerHeight / 2, 0xFF32CD32); // Leaves
            }
            case "wasteland" -> {
                // Wasteland - post-apocalyptic scene
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF8B7355); // Desert background
                ctx.fill(startX, startY + 3 * innerHeight / 4, startX + innerWidth, startY + innerHeight, 0xFFD2B48C); // Sand
                // Add some "wasteland" elements
                for (int i = 0; i < 4; i++) {
                    int px = startX + (i * innerWidth / 5) + 2;
                    int py = startY + innerHeight / 2 + (i % 2) * 4;
                    ctx.fill(px, py, px + 2, py + innerHeight / 4, 0xFF654321);
                }
            }
            case "pool" -> {
                // Pool painting - water scene (1x2 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF87CEEB); // Light blue
                                                                                                 // background
                ctx.fill(startX + innerWidth / 6, startY + innerHeight / 3, startX + 5 * innerWidth / 6,
                        startY + 2 * innerHeight / 3, 0xFF4169E1); // Pool water
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 2, startX + 3 * innerWidth / 4,
                        startY + 3 * innerHeight / 5, 0xFF0000FF); // Deeper water
            }
            case "courbet" -> {
                // Courbet - landscape painting (1x2 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF87CEEB); // Sky
                ctx.fill(startX, startY + innerHeight / 2, startX + innerWidth, startY + innerHeight, 0xFF228B22); // Ground
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 3, startX + 3 * innerWidth / 4,
                        startY + 2 * innerHeight / 3, 0xFF8B4513); // Earth tones
            }
            case "sea" -> {
                // Sea painting - ocean scene (1x2 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF87CEEB); // Sky
                ctx.fill(startX, startY + innerHeight / 3, startX + innerWidth, startY + innerHeight, 0xFF4169E1); // Sea
                // Add waves
                for (int i = 0; i < innerWidth; i += 4) {
                    ctx.fill(startX + i, startY + innerHeight / 2, startX + i + 2, startY + innerHeight / 2 + 2,
                            0xFF1E90FF);
                }
            }
            case "sunset" -> {
                // Sunset painting - warm colors (1x2 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight / 3, 0xFFFFD700); // Golden sky
                ctx.fill(startX, startY + innerHeight / 3, startX + innerWidth, startY + 2 * innerHeight / 3,
                        0xFFFF8C00); // Orange
                ctx.fill(startX, startY + 2 * innerHeight / 3, startX + innerWidth, startY + innerHeight, 0xFFFF4500); // Red
                                                                                                                       // horizon
            }
            case "creebet" -> {
                // Creeper-themed painting (1x2 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF00FF00); // Green background
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 4, startX + 3 * innerWidth / 4,
                        startY + 3 * innerHeight / 4, 0xFF228B22); // Darker green
                ctx.fill(startX + innerWidth / 3, startY + innerHeight / 3, startX + 2 * innerWidth / 3,
                        startY + 2 * innerHeight / 3, 0xFF006400); // Dark green center
            }
            case "wanderer" -> {
                // Wanderer painting - mysterious figure (2x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF483D8B); // Dark slate blue
                ctx.fill(startX + innerWidth / 3, startY + innerHeight / 4, startX + 2 * innerWidth / 3,
                        startY + 3 * innerHeight / 4, 0xFF2F2F2F); // Dark figure
                ctx.fill(startX + innerWidth / 2 - 1, startY + innerHeight / 3, startX + innerWidth / 2 + 1,
                        startY + innerHeight / 2, 0xFFFFFFFF); // Light element
            }
            case "graham" -> {
                // Graham painting - portrait style (2x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF8B7355); // Brown background
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 6, startX + 3 * innerWidth / 4,
                        startY + 5 * innerHeight / 6, 0xFFF5DEB3); // Portrait area
                ctx.fill(startX + innerWidth / 3, startY + innerHeight / 4, startX + 2 * innerWidth / 3,
                        startY + 3 * innerHeight / 4, 0xFFDEB887); // Face area
            }
            case "match" -> {
                // Match painting - flame and matchstick (1x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF2F2F2F); // Dark background
                ctx.fill(startX + innerWidth / 2 - 1, startY + 2, startX + innerWidth / 2 + 1, startY + innerHeight - 4,
                        0xFF8B4513); // Brown stick
                ctx.fill(startX + innerWidth / 2 - 2, startY + 2, startX + innerWidth / 2 + 2, startY + 8,
                        0xFFFF4500); // Flame
            }
            case "bust" -> {
                // Bust painting - sculpture-like (1x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF696969); // Gray background
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 6, startX + 3 * innerWidth / 4,
                        startY + 5 * innerHeight / 6, 0xFFF5F5DC); // Bust color
                ctx.fill(startX + innerWidth / 3, startY + innerHeight / 4, startX + 2 * innerWidth / 3,
                        startY + 3 * innerHeight / 4, 0xFFD3D3D3); // Sculpture details
            }
            case "stage" -> {
                // Stage painting - performance scene (1x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF8B0000); // Dark red curtain
                ctx.fill(startX + innerWidth / 6, startY + innerHeight / 3, startX + 5 * innerWidth / 6,
                        startY + innerHeight, 0xFF2F2F2F); // Stage area
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 2, startX + 3 * innerWidth / 4,
                        startY + 3 * innerHeight / 4, 0xFFFFD700); // Golden spotlight
            }
            case "void" -> {
                // Void painting - dark and mysterious (1x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF000000); // Black void
                // Add some subtle highlights
                for (int i = 0; i < 3; i++) {
                    int px = startX + (i * innerWidth / 4) + 4;
                    int py = startY + (i * innerHeight / 4) + 4;
                    ctx.fill(px, py, px + 2, py + 2, 0xFF1A1A1A); // Very dark gray dots
                }
            }
            case "skull_and_roses" -> {
                // Skull and roses - gothic painting (1x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF2F2F2F); // Dark background
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 6, startX + 3 * innerWidth / 4,
                        startY + 2 * innerHeight / 3, 0xFFF5F5F5); // Skull area
                ctx.fill(startX + 2, startY + 2 * innerHeight / 3, startX + innerWidth - 2, startY + innerHeight - 2,
                        0xFFDC143C); // Roses at bottom
            }
            case "wither" -> {
                // Wither painting - menacing (1x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF2F2F2F); // Dark background
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 4, startX + 3 * innerWidth / 4,
                        startY + 3 * innerHeight / 4, 0xFF1A1A1A); // Wither body
                ctx.fill(startX + innerWidth / 3, startY + innerHeight / 6, startX + 2 * innerWidth / 3,
                        startY + innerHeight / 3, 0xFF404040); // Wither heads
            }
            case "fighters" -> {
                // Fighters painting - battle scene (1x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF8B4513); // Brown background
                ctx.fill(startX + 2, startY + innerHeight / 2, startX + innerWidth / 2, startY + innerHeight - 2,
                        0xFF000080); // Blue fighter
                ctx.fill(startX + innerWidth / 2, startY + innerHeight / 2, startX + innerWidth - 2,
                        startY + innerHeight - 2,
                        0xFF800000); // Red fighter
            }
            case "pointer" -> {
                // Pointer painting - pointing hand (1x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFFFFE4B5); // Cream background
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 3, startX + 3 * innerWidth / 4,
                        startY + 2 * innerHeight / 3, 0xFFDEB887); // Hand color
                ctx.fill(startX + innerWidth / 2, startY + innerHeight / 4, startX + innerWidth - 4,
                        startY + innerHeight / 2,
                        0xFFCD853F); // Pointing finger
            }
            case "pigscene" -> {
                // Pig scene painting - pastoral (1x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF87CEEB); // Sky blue
                ctx.fill(startX, startY + 2 * innerHeight / 3, startX + innerWidth, startY + innerHeight, 0xFF228B22); // Grass
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 2, startX + 3 * innerWidth / 4,
                        startY + 2 * innerHeight / 3, 0xFFFFB6C1); // Pink pig
            }
            case "burning_skull" -> {
                // Burning skull - fiery and dark (1x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF8B0000); // Dark red background
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 6, startX + 3 * innerWidth / 4,
                        startY + 2 * innerHeight / 3, 0xFFF5F5F5); // Skull
                ctx.fill(startX + 2, startY + 2, startX + innerWidth - 2, startY + innerHeight / 4,
                        0xFFFF4500); // Flames
            }
            case "skeleton" -> {
                // Skeleton painting - spooky (1x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF2F2F2F); // Dark background
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 6, startX + 3 * innerWidth / 4,
                        startY + 5 * innerHeight / 6, 0xFFF5F5DC); // Bone color
                ctx.fill(startX + innerWidth / 3, startY + innerHeight / 4, startX + 2 * innerWidth / 3,
                        startY + 3 * innerHeight / 4, 0xFFFFFFFF); // Highlighted bones
            }
            case "donkey_kong" -> {
                // Donkey Kong painting - retro arcade (1x1 aspect ratio)
                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFF000080); // Navy background
                ctx.fill(startX + innerWidth / 6, startY + innerHeight / 4, startX + 5 * innerWidth / 6,
                        startY + 3 * innerHeight / 4, 0xFF8B4513); // Brown ladder/platform
                ctx.fill(startX + innerWidth / 4, startY + innerHeight / 6, startX + 3 * innerWidth / 4,
                        startY + innerHeight / 2, 0xFFFF0000); // Red elements
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

                ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight / 3, color1);
                ctx.fill(startX, startY + innerHeight / 3, startX + innerWidth, startY + 2 * innerHeight / 3, color2);
                ctx.fill(startX, startY + 2 * innerHeight / 3, startX + innerWidth, startY + innerHeight, color3);
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
    private static void renderPaintingPlaceholder(DrawContext ctx, int width, int height) {
        // Draw a generic painting with a question mark
        int innerWidth = width - 8;
        int innerHeight = height - 8;
        int startX = 4;
        int startY = 4;

        // Fill with a neutral color
        ctx.fill(startX, startY, startX + innerWidth, startY + innerHeight, 0xFFCCCCCC);

        // Draw a question mark
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer font = mc.textRenderer;
        String questionMark = "?";
        int textWidth = font.getWidth(questionMark);
        int textX = startX + (innerWidth - textWidth) / 2;
        int textY = startY + (innerHeight - font.fontHeight) / 2;

        ctx.drawText(font, questionMark, textX, textY, 0xFF000000, false);
    }
}
