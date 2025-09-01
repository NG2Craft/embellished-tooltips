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
        final boolean isPainting = Registries.ITEM.getId(stack.getItem()).getPath().equals("painting");

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
                (isPainting && config.rendering.enablePaintingPreviews);

        int rarityWidth = (isArmor || !config.rendering.showRarityText) ? 0 : font.getWidth(getRarityName(stack));
        TooltipSize content = renderSource.measure(font, L, isArmor, rarityWidth);

        int tooltipWidth = content.widthWithPadding(L);
        int tooltipHeight = content.heightWithPadding(L);

        int screenW = ctx.getScaledWindowWidth(); // 1.20.1 [1]
        int screenH = ctx.getScaledWindowHeight(); // 1.20.1 [1]

        // Determine panel width based on item type
        int panelWidth = 32; // Default for tools/armor
        if (isMap) {
            panelWidth = 64; // Maps use 64x64 square panels
        }

        // Get placement info including whether tooltip is on left side
        TooltipPlacement.PlacementResult placement = TooltipPlacement.placeWithSideInfo(mouseX, mouseY,
                tooltipWidth, tooltipHeight, screenW, screenH, hasSidePanel, panelWidth, 3);
        Point pos = placement.position;
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
                // For paintings, show painting preview
                Vec2f center = PaintingSidePanel.renderPaintingPanel(etx, posVec, size, null, mouseX, mouseY, screenW,
                        screenH, isTooltipOnLeft);
                PaintingSidePanel.renderPaintingPreview(ctx, stack, center);
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

}
