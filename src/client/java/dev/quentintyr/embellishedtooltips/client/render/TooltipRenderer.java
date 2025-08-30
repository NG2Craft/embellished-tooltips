package dev.quentintyr.embellishedtooltips.client.render;

import dev.quentintyr.embellishedtooltips.client.ResourceLoader;
import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.config.ModConfig;
import dev.quentintyr.embellishedtooltips.client.render.sidepanel.TooltipSidePanel;
import dev.quentintyr.embellishedtooltips.client.style.TooltipStyle;
import dev.quentintyr.embellishedtooltips.client.style.TooltipStylePreset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.item.TooltipContext; // vanilla context used only for ItemStack.getTooltip
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.Vec2f;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;

import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class TooltipRenderer {

    @Nullable
    private static TooltipStyle renderStyle = null;
    @Nullable
    private static ArmorStandEntity renderStand;
    private static ItemStack lastStack;
    private static long tooltipStartMillis;
    private static float tooltipSeconds;
    private static boolean hoveredLastFrame;
    private static long lastRenderMillis;

    public static boolean render(DrawContext ctx, ItemStack stack, TextRenderer font,
            List<TooltipComponent> components, int mouseX, int mouseY,
            TooltipPositioner positioner) {

        // Check if custom tooltips are enabled
        ModConfig config = ModConfig.getInstance();
        if (!config.rendering.enableCustomTooltips) {
            hoveredLastFrame = false;
            return false;
        }

        updateStyle(stack);
        if (renderStyle == null || components == null || components.isEmpty()) {
            hoveredLastFrame = false;
            return false;
        }

        List<TooltipComponent> compsToRender;
        if (stack.getItem() instanceof ArmorItem && !components.isEmpty() && config.rendering.enableEnchantmentLines) {
            List<TooltipComponent> list = new ArrayList<>();
            list.add(components.get(0)); // title
            for (Text enchText : buildEnchantmentTexts(stack))
                list.add(TooltipComponent.of(enchText.asOrderedText()));
            compsToRender = list;
        } else {
            compsToRender = components;
        }

        return renderCore(ctx, stack, font, new ComponentsSource(compsToRender), mouseX, mouseY);
    }

    public static boolean render(DrawContext ctx, ItemStack stack, int mouseX, int mouseY) {
        // Check if custom tooltips are enabled
        ModConfig config = ModConfig.getInstance();

        if (!config.rendering.enableCustomTooltips) {
            hoveredLastFrame = false;
            return false;
        }

        updateStyle(stack);
        if (renderStyle == null) {
            hoveredLastFrame = false;
            return false;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer font = mc.textRenderer;

        List<Text> lines = stack.getTooltip(
                mc.player,
                mc.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC); // vanilla
                                                                                                                   // API
                                                                                                                   // [2]

        if (lines.isEmpty()) {
            hoveredLastFrame = false;
            return false;
        }

        List<TooltipComponent> comps = new ArrayList<>(lines.size());
        for (Text t : lines) {
            OrderedText ot = t.asOrderedText();
            comps.add(TooltipComponent.of(ot)); // 1.20.1 OK [2]
        }

        List<TooltipComponent> compsToRender;
        if (stack.getItem() instanceof ArmorItem && !comps.isEmpty()) {
            List<TooltipComponent> list = new ArrayList<>();
            list.add(comps.get(0)); // title
            for (Text enchText : buildEnchantmentTexts(stack))
                list.add(TooltipComponent.of(enchText.asOrderedText()));
            compsToRender = list;
        } else {
            compsToRender = comps;
        }

        return renderCore(ctx, stack, font, new ComponentsSource(compsToRender), mouseX, mouseY);
    }

    private static boolean renderCore(DrawContext ctx, ItemStack stack, TextRenderer font,
            RenderSource renderSource, int mouseX, int mouseY) {

        beginHoverIfNeeded(stack, !hoveredLastFrame);

        ModConfig config = ModConfig.getInstance();
        final TooltipLayout L = TooltipLayout.defaults();
        final boolean isArmor = stack.getItem() instanceof ArmorItem;
        final boolean isTool = stack.getItem() instanceof ToolItem;
        final boolean isMap = stack.getItem() instanceof FilledMapItem;
        final boolean isPainting = stack.getItem() instanceof DecorationItem;

        // Side panels are shown if:
        // - For armor: enableArmorPreview is true
        // - For tools: enableToolPreviews is true
        // - For maps: enableMapPreviews is true
        // - For paintings: enablePaintingPreviews is true
        final boolean hasSidePanel = (isArmor && config.rendering.enableArmorPreview) ||
                (isTool && config.rendering.enableToolPreviews) ||
                (isMap && config.rendering.enableMapPreviews) ||
                (isPainting && config.rendering.enablePaintingPreviews);

        int rarityWidth = (isArmor || !config.rendering.showRarityText) ? 0 : font.getWidth(getRarityName(stack));
        TooltipSize content = renderSource.measure(font, L, isArmor, rarityWidth);

        int tooltipWidth = content.widthWithPadding(L);
        int tooltipHeight = content.heightWithPadding(L);

        int screenW = ctx.getScaledWindowWidth(); // 1.20.1 [1]
        int screenH = ctx.getScaledWindowHeight(); // 1.20.1 [1]
        Point pos = TooltipPlacement.place(mouseX, mouseY, tooltipWidth, tooltipHeight, screenW, screenH, hasSidePanel,
                64, 12);
        Vec2f posVec = new Vec2f(pos.x, pos.y);
        Point size = new Point(tooltipWidth, tooltipHeight);

        // Use the CUSTOM TooltipContext class (fully qualified to avoid clash)
        dev.quentintyr.embellishedtooltips.client.render.TooltipContext etx = new dev.quentintyr.embellishedtooltips.client.render.TooltipContext(
                ctx);
        etx.define(stack, tooltipSeconds);

        // Apply tooltip scaling
        MatrixStack ms = ctx.getMatrices();
        boolean isScaled = config.rendering.tooltipScale != 1.0f;
        if (isScaled) {
            ms.push();
            ms.scale(config.rendering.tooltipScale, config.rendering.tooltipScale, 1.0f);
            // Adjust position for scaling
            posVec = new Vec2f(pos.x / config.rendering.tooltipScale, pos.y / config.rendering.tooltipScale);
            size = new Point((int) (tooltipWidth / config.rendering.tooltipScale),
                    (int) (tooltipHeight / config.rendering.tooltipScale));
        }

        TooltipStylePipeline.renderStyleRef = renderStyle;
        TooltipStylePipeline.renderBackLayers(etx, posVec, size);

        if (config.rendering.showStatIcons) {
            TooltipSummaryRow.render(
                    ctx, font, stack,
                    pos.x + L.paddingX + L.leftGutter + L.firstLineXOffset,
                    pos.y + L.paddingTop + L.firstLineYOffset + font.fontHeight + 1);
        }

        TooltipStylePipeline.renderBetweenTextEffects(etx, posVec, size);

        ms.push();
        ms.translate(0, 0, 450.0F);
        renderSource.renderText(ctx, font, pos.x + L.paddingX, pos.y + L.paddingTop, L);
        ms.pop();

        TooltipStylePipeline.renderFrontLayers(etx, posVec, size);

        if (hasSidePanel) {
            Vec2f center = TooltipSidePanel.renderSecondPanel(etx, posVec, size, null, mouseX, mouseY, screenW,
                    screenH);
            if (isArmor) {
                // For armor, show 3D armor preview (enableArmorPreview was already checked)
                equip(stack);
                TooltipSidePanel.renderStandRef = renderStand;
                TooltipSidePanel.renderStand(ctx, (int) center.x, (int) (center.y + 26));
            } else if (isTool) {
                // For tools, show spinning item preview
                TooltipSidePanel.renderSpinningItem(ctx, stack, center);
            } else if (isMap) {
                // For maps, show map preview
                TooltipSidePanel.renderMapPreview(ctx, stack, center);
            } else if (isPainting) {
                // For paintings, show painting preview
                TooltipSidePanel.renderPaintingPreview(ctx, stack, center);
            }
        }

        // Close scaling transformation if it was applied
        if (isScaled) {
            ms.pop();
        }

        hoveredLastFrame = true;
        return true;
    }

    private static List<Text> buildEnchantmentTexts(ItemStack stack) {
        List<Text> out = new ArrayList<>();
        try {
            var map = EnchantmentHelper.get(stack);
            if (map == null || map.isEmpty())
                return out;
            map.entrySet().stream()
                    .sorted((a, b) -> {
                        var ka = Registries.ENCHANTMENT.getId(a.getKey());
                        var kb = Registries.ENCHANTMENT.getId(b.getKey());
                        String sa = ka == null ? "" : ka.toString();
                        String sb = kb == null ? "" : kb.toString();
                        return sa.compareTo(sb);
                    })
                    .forEach(e -> {
                        Enchantment ench = e.getKey();
                        int level = e.getValue();
                        Text name = Text.translatable(ench.getTranslationKey());
                        Text lvl = Text.translatable("enchantment.level." + level);
                        out.add(Text.empty().append(name).append(" ").append(lvl).formatted(Formatting.GRAY));
                    });
        } catch (Exception ignored) {
        }
        return out;
    }

    private static void beginHoverIfNeeded(ItemStack stack, boolean firstFrameThisHover) {
        long now = System.currentTimeMillis();
        boolean stackChanged = (lastStack == null) || (!ItemStack.areEqual(stack, lastStack));

        // Reset if we haven't rendered for more than the configured timeout (indicates
        // user stopped hovering)
        ModConfig config = ModConfig.getInstance();
        boolean hasBeenAway = (now - lastRenderMillis) > config.animations.reHoverTimeoutMs;

        if (firstFrameThisHover || stackChanged || tooltipStartMillis == 0L || hasBeenAway) {
            tooltipStartMillis = now;
            if (renderStyle != null) {
                try {
                    renderStyle.reset();
                } catch (Exception ignored) {
                }
            }
        }
        tooltipSeconds = (now - tooltipStartMillis) / 1000.0F;
        lastStack = stack.copy();
        lastRenderMillis = now;
    }

    private static void updateStyle(ItemStack stack) {
        Optional<TooltipStylePreset> styleOpt = ResourceLoader.getStyleFor(stack);
        if (styleOpt.isPresent()) {
            TooltipStylePreset preset = styleOpt.get();
            TooltipStyle.Builder b = new TooltipStyle.Builder();
            preset.getPanel().ifPresent(b::withPanel);
            preset.getFrame().ifPresent(b::withFrame);
            preset.getIcon().ifPresent(b::withIcon);
            b.withEffects(preset.getEffects());
            renderStyle = b.build();
        } else {
            renderStyle = StyleManager.getInstance().getDefaultStyle();
        }
    }

    private static void equip(ItemStack stack) {
        if (renderStand == null) {
            renderStand = new ArmorStandEntity(MinecraftClient.getInstance().world, 0, 0, 0);
        }
        if (stack.getItem() instanceof ArmorItem armorItem) {
            renderStand.equipStack(armorItem.getSlotType(), stack);
        }
    }

    static Text getRarityName(ItemStack stack) {
        try {
            String rarity = stack.getRarity().name().toLowerCase();
            return Text.translatable("rarity." + rarity + ".name");
        } catch (Exception e) {
            return Text.empty();
        }
    }

    static int getRarityColor(ItemStack stack) {
        try {
            if ("common".equalsIgnoreCase(stack.getRarity().name()))
                return 0xFF6A6A6A;
            var formatting = stack.getRarity().formatting;
            if (formatting != null && formatting.getColorValue() != null) {
                return 0xFF000000 | formatting.getColorValue();
            }
        } catch (Exception ignored) {
        }
        return 0xFF7A7A7A;
    }

    /**
     * Renders a preview of a map in the side panel
     */
    private static void renderMapPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null)
            return;

        MatrixStack matrices = ctx.getMatrices();
        matrices.push();

        // Center the map preview
        int mapSize = 64; // Size of the map preview
        float x = center.x - mapSize / 2f;
        float y = center.y - mapSize / 2f;

        matrices.translate(x, y, 0);

        try {
            // Try to get map data and render it
            Integer mapId = FilledMapItem.getMapId(stack);
            if (mapId != null) {
                MapState mapState = FilledMapItem.getMapState(mapId, mc.world);
                if (mapState != null && mapState.colors != null) {
                    // Render actual map data
                    renderMapData(ctx, mapState, mapSize);
                } else {
                    // Draw a placeholder if no map data
                    renderMapPlaceholder(ctx, mapSize);
                }
            } else {
                // Draw empty map placeholder
                renderMapPlaceholder(ctx, mapSize);
            }
        } catch (Exception e) {
            // Fallback to placeholder if rendering fails
            renderMapPlaceholder(ctx, mapSize);
        }

        matrices.pop();
    }

    /**
     * Renders actual map data as a colored grid
     */
    private static void renderMapData(DrawContext ctx, MapState mapState, int size) {
        // Draw map background
        ctx.fill(0, 0, size, size, 0xFF8B4513); // Brown frame
        ctx.fill(2, 2, size - 2, size - 2, 0xFFF5DEB3); // Map background

        // Draw a simplified version of the map data
        if (mapState.colors != null) {
            int mapDataSize = 128; // Minecraft maps are 128x128
            int pixelSize = (size - 4) / 16; // Divide into 16x16 grid for performance

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    // Sample the map data at intervals
                    int mapX = (x * mapDataSize) / 16;
                    int mapY = (y * mapDataSize) / 16;
                    int index = mapX + mapY * mapDataSize;

                    if (index < mapState.colors.length) {
                        byte colorIndex = mapState.colors[index];
                        if (colorIndex != 0) {
                            // Convert map color to RGB (simplified)
                            int color = getMapColor(colorIndex);
                            ctx.fill(2 + x * pixelSize, 2 + y * pixelSize,
                                    2 + (x + 1) * pixelSize, 2 + (y + 1) * pixelSize, color);
                        }
                    }
                }
            }
        }
    }

    /**
     * Converts a map color index to RGB color
     */
    private static int getMapColor(byte colorIndex) {
        // Simplified color mapping based on Minecraft's map colors
        int baseColor = colorIndex / 4;
        int shade = colorIndex % 4;

        int[] baseColors = {
                0x000000, 0x7FB238, 0xF7E9A3, 0xC7C7C7, 0xFF0000, 0xA0A0FF, 0xA7A7A7, 0x007C00,
                0xFFFFFF, 0xA4A4A4, 0x740085, 0x0000FF, 0x8B4513, 0xFFFF00, 0xFF00FF, 0x00FF00
        };

        if (baseColor >= baseColors.length)
            baseColor = 0;
        int color = baseColors[baseColor];

        // Apply shading
        float brightness = 1.0f - (shade * 0.15f);
        int r = (int) ((color >> 16 & 0xFF) * brightness);
        int g = (int) ((color >> 8 & 0xFF) * brightness);
        int b = (int) ((color & 0xFF) * brightness);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Renders a placeholder for maps when actual map data is not available
     */
    private static void renderMapPlaceholder(DrawContext ctx, int size) {
        // Draw a brown square to represent an empty/unknown map
        ctx.fill(0, 0, size, size, 0xFF8B4513); // Brown background
        ctx.fill(2, 2, size - 2, size - 2, 0xFFD2B48C); // Lighter brown interior

        // Draw a simple grid pattern
        for (int i = 8; i < size - 8; i += 8) {
            ctx.drawHorizontalLine(4, size - 4, i, 0xFF654321); // Darker brown lines
            ctx.drawVerticalLine(i, 4, size - 4, 0xFF654321);
        }
    }

    /**
     * Renders a preview of a painting in the side panel
     */
    private static void renderPaintingPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        MatrixStack matrices = ctx.getMatrices();
        matrices.push();

        // Standard painting preview size
        int paintingSize = 48;
        float x = center.x - paintingSize / 2f;
        float y = center.y - paintingSize / 2f;

        matrices.translate(x, y, 0);

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
            if (stack.hasNbt() && stack.getNbt().contains("variant")) {
                return stack.getNbt().getString("variant");
            }
        } catch (Exception e) {
            // Ignore NBT errors
        }

        // Default to a recognizable painting pattern
        return "kebab";
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

        // Create a recognizable pattern based on painting ID
        switch (paintingId.toLowerCase()) {
            case "kebab":
                // Draw a simple kebab-like pattern
                ctx.fill(startX + innerSize / 4, startY + 2, startX + 3 * innerSize / 4, startY + innerSize - 2,
                        0xFF8B4513);
                ctx.fill(startX + innerSize / 3, startY + innerSize / 4, startX + 2 * innerSize / 3,
                        startY + 3 * innerSize / 4, 0xFFFF0000);
                break;
            case "plant":
                // Draw a plant-like pattern
                ctx.fill(startX + innerSize / 2 - 2, startY + innerSize / 2, startX + innerSize / 2 + 2,
                        startY + innerSize - 2, 0xFF228B22);
                ctx.fill(startX + innerSize / 4, startY + 2, startX + 3 * innerSize / 4, startY + innerSize / 3,
                        0xFF32CD32);
                break;
            case "void":
                // Draw "The Void" pattern - dark with some highlights
                ctx.fill(startX, startY, startX + innerSize, startY + innerSize, 0xFF000011);
                ctx.fill(startX + 2, startY + 2, startX + innerSize - 2, startY + innerSize - 2, 0xFF1a1a2e);
                // Add some "void" particles
                for (int i = 0; i < 8; i++) {
                    int px = startX + (i * innerSize / 8) + 2;
                    int py = startY + ((i * 7) % innerSize) + 2;
                    ctx.fill(px, py, px + 2, py + 2, 0xFF4a4a8a);
                }
                break;
            default:
                // Generic colorful pattern
                int hash = paintingId.hashCode();
                int color1 = 0xFF000000 | (Math.abs(hash) & 0xFFFFFF);
                int color2 = 0xFF000000 | (Math.abs(hash >> 8) & 0xFFFFFF);
                int color3 = 0xFF000000 | (Math.abs(hash >> 16) & 0xFFFFFF);

                ctx.fill(startX, startY, startX + innerSize, startY + innerSize / 3, color1);
                ctx.fill(startX, startY + innerSize / 3, startX + innerSize, startY + 2 * innerSize / 3, color2);
                ctx.fill(startX, startY + 2 * innerSize / 3, startX + innerSize, startY + innerSize, color3);
                break;
        }
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
