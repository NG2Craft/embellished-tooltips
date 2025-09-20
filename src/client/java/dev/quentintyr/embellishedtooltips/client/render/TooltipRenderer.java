package dev.quentintyr.embellishedtooltips.client.render;

import dev.quentintyr.embellishedtooltips.client.ResourceLoader;
import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.config.ModConfig;
import dev.quentintyr.embellishedtooltips.client.render.item.ItemSidePanel;
import dev.quentintyr.embellishedtooltips.client.render.map.MapSidePanel;
import dev.quentintyr.embellishedtooltips.client.render.painting.PaintingSidePanel;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.item.Items;
import dev.quentintyr.embellishedtooltips.client.render.trim.TrimSidePanel;
import net.minecraft.item.ToolItem;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;

import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import dev.quentintyr.embellishedtooltips.client.render.itemframe.ItemFrameSidePanel;

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
    // Random preview support for item frame hover
    private static ItemStack randomPreviewStack = ItemStack.EMPTY;
    private static long lastRandomGenTime = 0L;
    private static List<net.minecraft.item.Item> randomPool;
    private static final long RANDOM_REFRESH_INTERVAL_MS = 2500L; // rotate every 2.5s while hovered
    private static final int RANDOM_POOL_MIN = 64; // ensure pool has enough variety

    private static boolean isItemFrame(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.isOf(Items.ITEM_FRAME);
    }

    private static ItemStack getRandomPreviewStack(ItemStack frameStack) {
        long now = System.currentTimeMillis();
        if (randomPool == null) {
            // Build a pool of candidate items (exclude AIR & ITEM_FRAME)
            List<net.minecraft.item.Item> list = new ArrayList<>();
            for (net.minecraft.item.Item item : Registries.ITEM) {
                if (item != Items.AIR && item != Items.ITEM_FRAME) {
                    list.add(item);
                }
            }
            if (list.size() < RANDOM_POOL_MIN) {
                randomPool = list; // small modpack fallback
            } else {
                // Shuffle once and keep
                Collections.shuffle(list, new Random(now));
                randomPool = list;
            }
        }
        boolean needNew = randomPreviewStack.isEmpty() || !lastStackIsFrame(frameStack)
                || (now - lastRandomGenTime) > RANDOM_REFRESH_INTERVAL_MS;
        if (needNew) {
            Random r = new Random(now);
            net.minecraft.item.Item base = randomPool.get(r.nextInt(randomPool.size()));
            randomPreviewStack = new ItemStack(base);
            lastRandomGenTime = now;
        }
        return randomPreviewStack;
    }

    private static boolean lastStackIsFrame(ItemStack current) {
        return lastStack != null && !lastStack.isEmpty() && lastStack.isOf(Items.ITEM_FRAME)
                && current.isOf(Items.ITEM_FRAME);
    }

    public static boolean render(DrawContext ctx, ItemStack stack, TextRenderer font,
            List<TooltipComponent> components, int mouseX, int mouseY,
            TooltipPositioner positioner) {
        // Only render for valid item stacks
        if (stack == null || stack.isEmpty()) {
            return false;
        }

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

        // Item frame preview will move to a side panel; no inline injection anymore

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

        // For trim smithing templates, drop only the verbose sections (Applies to /
        // Ingredients)
        if (isTrimTemplate(stack) && !lines.isEmpty()) {
            List<Text> filtered = new ArrayList<>(lines.size());
            boolean skipNext = false;
            for (Text t : lines) {
                if (skipNext) {
                    skipNext = false;
                    continue;
                }
                boolean isHeader = false;
                try {
                    var content = t.getContent();
                    if (content instanceof net.minecraft.text.TranslatableTextContent trans) {
                        String key = trans.getKey();
                        if (key.contains("smithing_template.applies_to")
                                || key.contains("smithing_template.ingredients")) {
                            isHeader = true;
                        }
                    }
                } catch (Throwable ignored) {
                }
                if (!isHeader) {
                    String s = t.getString();
                    if (s != null) {
                        String lower = s.toLowerCase();
                        if (lower.contains("applies to") || lower.contains("ingredients")) {
                            isHeader = true;
                        }
                    }
                }
                if (isHeader) {
                    skipNext = true;
                    continue;
                }
                filtered.add(t);
            }
            // Remove blank/whitespace-only lines and cap to the first three non-empty lines
            List<Text> compact = new ArrayList<>(3);
            for (Text t : filtered) {
                String s = null;
                try {
                    s = t.getString();
                } catch (Exception ignored) {
                }
                if (s == null || s.trim().isEmpty())
                    continue;
                compact.add(t);
                if (compact.size() >= 3)
                    break;
            }
            if (!compact.isEmpty())
                lines = compact;
            else
                lines = filtered;
        }

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

        // Item frame preview will move to a side panel; no inline component here

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
        final boolean isPainting = Registries.ITEM.getId(stack.getItem()).getPath().equals("painting");
        final boolean isItemFrameItem = isItemFrame(stack);

        if (isPainting) {
            System.out.println("Detected painting item: " + Registries.ITEM.getId(stack.getItem())); // Debug log
        }

        // Side panels are shown if:
        // - For armor: enableArmorPreview is true
        // - For tools: enableToolPreviews is true
        // - For maps: enableMapPreviews is true
        // - For paintings: enablePaintingPreviews is true
        final boolean hasSidePanel = (isArmor && config.rendering.enableArmorPreview) ||
                (isTool && config.rendering.enableToolPreviews) ||
                (isMap && config.rendering.enableMapPreviews) ||
                (isPainting && config.rendering.enablePaintingPreviews) ||
                (isItemFrameItem) ||
                (isTrimTemplate(stack) && config.rendering.enableArmorPreview);

        int rarityWidth = (isArmor || !config.rendering.showRarityText) ? 0 : font.getWidth(getRarityName(stack));
        TooltipSize content = renderSource.measure(font, L, isArmor, rarityWidth);

        int tooltipWidth = content.widthWithPadding(L);
        int tooltipHeight = content.heightWithPadding(L);
        // Grow tooltip to fit inline trim materials grid
        if (isTrimTemplate(stack)) {
            tooltipHeight += computeTrimMaterialsFooterHeight(tooltipWidth, L);
        }

        int screenW = ctx.getScaledWindowWidth();
        int screenH = ctx.getScaledWindowHeight();

        // Determine panel width based on item type (reconstructed after refactor)
        int panelWidth = 32;
        if (isMap) {
            panelWidth = 64;
        }
        if (isPainting) {
            java.awt.Point ps = dev.quentintyr.embellishedtooltips.client.render.painting.PaintingRenderer
                    .computePanelSize(stack, tooltipSeconds);
            panelWidth = Math.max(panelWidth, ps.x);
        }
        if (isItemFrameItem) {
            panelWidth = Math.max(panelWidth, 48);
        }

        // Get placement info including whether tooltip is on left side
        TooltipPlacement.PlacementResult placement = TooltipPlacement.placeWithSideInfo(mouseX, mouseY,
                tooltipWidth, tooltipHeight, screenW, screenH, hasSidePanel, panelWidth, 3);
        Point pos = placement.position;
        // Adjust Y position only for trim templates to align flush with trim render
        if (isTrimTemplate(stack)) {
            pos.y -= 2; // Move up by 2 pixels; adjust this value as needed for perfect alignment
        }
        boolean isTooltipOnLeft = placement.isTooltipOnLeft;

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

        // Render inline trim materials inside the tooltip (before front layers)
        if (isTrimTemplate(stack)) {
            renderTrimMaterials(ctx, posVec, size, L, content.contentHeight);
        }

        TooltipStylePipeline.renderFrontLayers(etx, posVec, size);

        if (hasSidePanel) {
            if (isArmor) {
                // For armor, show 3D armor preview
                Vec2f center = ItemSidePanel.renderArmorPanel(etx, posVec, size, null, mouseX, mouseY, screenW,
                        screenH, isTooltipOnLeft);
                equip(stack);
                ItemSidePanel.renderStandRef = renderStand;
                ItemSidePanel.renderStand(ctx, (int) center.x, (int) (center.y + 26));
            } else if (isTool) {
                // For tools, show spinning item preview
                Vec2f center = ItemSidePanel.renderToolPanel(etx, posVec, size, null, mouseX, mouseY, screenW,
                        screenH, isTooltipOnLeft);
                ItemSidePanel.renderSpinningItem(ctx, stack, center);
            } else if (isMap) {
                // For maps, show map preview
                Vec2f center = MapSidePanel.renderMapPanel(etx, posVec, size, null, mouseX, mouseY, screenW,
                        screenH, isTooltipOnLeft);
                MapSidePanel.renderMapPreview(ctx, stack, center);
            } else if (isPainting) {
                // For paintings, show painting preview with dynamic sizing
                Vec2f center = PaintingSidePanel.renderPaintingPanel(etx, posVec, size, null, mouseX, mouseY, screenW,
                        screenH, isTooltipOnLeft);
                PaintingSidePanel.renderPaintingPreview(etx, ctx, stack, center);
            } else if (isTrimTemplate(stack)) {
                Vec2f center = TrimSidePanel.renderTrimPanel(etx, posVec, size, null, mouseX, mouseY, screenW,
                        screenH, isTooltipOnLeft);
                TrimSidePanel.renderTrimPreview(ctx, stack, center);
            } else if (isItemFrameItem) {
                Vec2f center = ItemFrameSidePanel.renderPanel(etx, posVec, size, null, mouseX, mouseY, screenW,
                        screenH, isTooltipOnLeft);
                try {
                    ItemStack preview = getRandomPreviewStack(stack);
                    ItemFrameSidePanel.renderPreview(ctx, stack, preview, center);
                } catch (Throwable ignored) {
                }
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

    private static boolean isStackTrimMaterial(ItemStack stack) {
        try {
            var mc = MinecraftClient.getInstance();
            if (mc.world == null)
                return false;
            var reg = mc.world.getRegistryManager().get(net.minecraft.registry.RegistryKeys.TRIM_MATERIAL);
            if (reg == null)
                return false;
            var item = stack.getItem();
            for (net.minecraft.util.Identifier id : reg.getIds()) {
                var mat = reg.get(id);
                if (mat == null)
                    continue;
                try {
                    if (mat.ingredient().value() == item)
                        return true;
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean isTrimItem(ItemStack stack) {
        // Smithing template items for armor trims or items that are valid trim
        // materials
        try {
            if (stack.getItem() instanceof SmithingTemplateItem) {
                var id = Registries.ITEM.getId(stack.getItem());
                if (id != null && id.getPath().endsWith("_armor_trim_smithing_template"))
                    return true;
            }
        } catch (Exception ignored) {
        }
        return isStackTrimMaterial(stack);
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

    // ==== Inline trim materials helpers ====
    private static int computeTrimMaterialsFooterHeight(int tooltipWidth, TooltipLayout L) {
        try {
            int innerW = Math.max(1, tooltipWidth - L.paddingX * 2);
            // Layout constants
            final int chipW = 10, chipH = 9;
            final int gapX = 1, gapY = 1;
            final int padTop = 1;
            int count = getTrimMaterialsCount();
            if (count <= 0)
                return 0;
            int maxCols = Math.max(1, (innerW + gapX) / (chipW + gapX));
            int cols = Math.max(1, Math.min(count, maxCols));
            int rows = Math.max(1, (int) Math.ceil(count / (double) cols));
            int innerH = rows * chipH + (rows - 1) * gapY;
            return padTop + innerH; // bottom flush with L.paddingBottom
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static void renderTrimMaterials(DrawContext ctx, Vec2f tooltipPos, Point tooltipSize, TooltipLayout L,
            int contentHeight) {
        try {
            int innerW = Math.max(1, tooltipSize.x - L.paddingX * 2);
            final int chipW = 10, chipH = 10;
            final int gapX = 1, gapY = 1;
            final int padTop = 2;
            int count = getTrimMaterialsCount();
            if (count <= 0)
                return;

            int maxCols = Math.max(1, (innerW + gapX) / (chipW + gapX));
            int cols = Math.max(1, Math.min(count, maxCols));
            // Position directly beneath the rendered text area: paddingTop + contentHeight
            // + titleExtra
            int boxX = (int) (tooltipPos.x + L.paddingX);
            int boxY = (int) (tooltipPos.y + L.paddingTop + contentHeight + L.titleExtra);

            final int nudgeUp = 2;

            // Draw the chips
            // Use the same material order as TrimSidePanel
            var materials = TrimSidePanel.getMaterialEntriesForUI();
            int selected = TrimSidePanel.getSelectedMaterialIndexForUI();
            // compute total rows to set hover area
            int rows = (materials.size() + cols - 1) / cols;
            int areaW = cols * chipW + (cols - 1) * gapX;
            int areaH = rows * chipH + (rows - 1) * gapY + padTop;
            TrimSidePanel.setInlineMaterialsArea(boxX, boxY, areaW, areaH);
            TrimSidePanel.markInlineMaterialsActive();
            for (int i = 0; i < materials.size(); i++) {
                var mat = materials.get(i);
                var item = mat.value().ingredient().value();
                int r = i / cols;
                int c = i % cols;
                int cx = boxX + c * (chipW + gapX);
                int cy = boxY + (padTop - nudgeUp) + r * (chipH + gapY);
                if (i == selected) {
                    ctx.fill(cx, cy, cx + chipW, cy + chipH, 0xA0FFFFFF);
                }
                ctx.drawBorder(cx - 1, cy - 1, chipW + 2, chipH + 2, 0xFF3C3C3C);
                float s = chipW / 16.0f;
                var ms = ctx.getMatrices();
                ms.push();
                ms.translate(cx + 1, cy + 1, 400.0f);
                ms.scale(s, s, 1.0f);
                // Draw a slot-style background behind the selected chip, inside the same
                // transform
                if (i == selected) {
                    int slotColor = 0x20FFFFFF; // match vanilla slot color
                    // Larger slot highlight: fills more area, still avoids overlap
                    // Top bar (wider, 2px high)
                    ctx.fill(1, 1, 15, 2, slotColor);
                    // Body (fills almost all, leaves 1px border)
                    ctx.fill(0, 2, 16, 15, slotColor);
                    // Bottom bar (wider, 1px high)
                    ctx.fill(1, 15, 15, 16, slotColor);
                }
                ctx.drawItem(new ItemStack(item), 0, 0);
                ms.pop();
            }
        } catch (Exception ignored) {
        }
    }

    private static int getTrimMaterialsCount() {
        try {
            var mc = MinecraftClient.getInstance();
            if (mc.world == null)
                return 0;
            var reg = mc.world.getRegistryManager().get(net.minecraft.registry.RegistryKeys.TRIM_MATERIAL);
            return reg == null ? 0 : reg.getIds().size();
        } catch (Exception ignored) {
            return 0;
        }
    }

    // Helper to restrict behavior to smithing templates only
    private static boolean isTrimTemplate(ItemStack stack) {
        try {
            if (stack.getItem() instanceof SmithingTemplateItem) {
                var id = Registries.ITEM.getId(stack.getItem());
                return id != null && id.getPath().endsWith("_armor_trim_smithing_template");
            }
        } catch (Exception ignored) {
        }
        return false;
    }

}
