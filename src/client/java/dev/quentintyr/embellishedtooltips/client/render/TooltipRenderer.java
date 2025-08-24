package dev.quentintyr.embellishedtooltips.client.render;

import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.ResourceLoader;
import dev.quentintyr.embellishedtooltips.client.style.TooltipStyle;
import dev.quentintyr.embellishedtooltips.client.style.TooltipStylePreset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import org.joml.Quaternionf;
import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

//TODO add custom icons for tools
//TODO 3d renders, texture icons should be toggleable in a config screen
//also compatible with mod menu

@Environment(EnvType.CLIENT)
public final class TooltipRenderer {

    // Style/state -------------------------------------------------------------

    @Nullable
    private static TooltipStyle renderStyle = null;
    @Nullable
    private static ArmorStandEntity renderStand;
    private static ItemStack lastStack;
    private static long tooltipStartMillis;
    private static float tooltipSeconds;
    private static boolean hoveredLastFrame;
    private static long lastRenderMillis;

    // Textures ---------------------------------------------------------------

    private static final Identifier TEX_ARMOR = new Identifier("embellished_tooltips", "textures/stats/armor.png");
    private static final Identifier TEX_ARMOR_TOUGH = new Identifier("embellished_tooltips",
            "textures/stats/armor_toughness.png");
    private static final Identifier TEX_DURABILITY = new Identifier("embellished_tooltips",
            "textures/stats/durability.png");

    // Public API -------------------------------------------------------------

    public static boolean render(DrawContext ctx, ItemStack stack, TextRenderer font,
            List<TooltipComponent> components, int mouseX, int mouseY,
            TooltipPositioner positioner) {

        updateStyle(stack); // style per-stack [2]
        if (renderStyle == null || components == null || components.isEmpty()) {
            hoveredLastFrame = false;
            return false;
        }

        List<TooltipComponent> compsToRender = (stack.getItem() instanceof ArmorItem && !components.isEmpty())
                ? Collections.singletonList(components.get(0))
                : components;

        return renderCore(ctx, stack, font, new ComponentsSource(compsToRender), mouseX, mouseY);
    }

    public static boolean render(DrawContext ctx, ItemStack stack, int mouseX, int mouseY) {
        updateStyle(stack); // style per-stack [2]
        if (renderStyle == null) {
            hoveredLastFrame = false;
            return false;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer font = mc.textRenderer;

        List<Text> lines = stack.getTooltip(
                mc.player,
                mc.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC); // official
                                                                                                                   // tooltip
                                                                                                                   // source
                                                                                                                   // [2]

        if (lines.isEmpty()) {
            hoveredLastFrame = false;
            return false;
        }

        List<TooltipComponent> comps = new ArrayList<>(lines.size());
        for (Text t : lines) {
            OrderedText ot = t.asOrderedText();
            comps.add(TooltipComponent.of(ot)); // 1.20.1-safe [4]
        }

        List<TooltipComponent> compsToRender = (stack.getItem() instanceof ArmorItem && !comps.isEmpty())
                ? Collections.singletonList(comps.get(0))
                : comps;

        return renderCore(ctx, stack, font, new ComponentsSource(compsToRender), mouseX, mouseY);
    }

    // Core renderer (shared) -------------------------------------------------

    private static boolean renderCore(DrawContext ctx, ItemStack stack, TextRenderer font,
            RenderSource renderSource, int mouseX, int mouseY) {
        // Timing
        beginHoverIfNeeded(stack, !hoveredLastFrame); // hover reset logic [2]

        // Layout
        final TooltipLayout L = TooltipLayout.defaults();
        final boolean isArmor = stack.getItem() instanceof ArmorItem;
        final boolean hasSidePanel = isArmor || (stack.getItem() instanceof ToolItem);

        // Measure content
        int rarityWidth = isArmor ? 0 : font.getWidth(getRarityName(stack)); // rarity text width when not armor [2]
        Size contentSize = renderSource.measure(font, L, isArmor, rarityWidth);

        int tooltipWidth = contentSize.widthWithPadding(L);
        int tooltipHeight = contentSize.heightWithPadding(L);

        // Placement/clamp using DrawContext screen size [1]
        int screenW = ctx.getScaledWindowWidth();
        int screenH = ctx.getScaledWindowHeight();
        Point pos = Placement.place(mouseX, mouseY, tooltipWidth, tooltipHeight, screenW, screenH, hasSidePanel, 36,
                12);
        Vec2f posVec = new Vec2f(pos.x, pos.y);
        Point size = new Point(tooltipWidth, tooltipHeight);

        // Context
        dev.quentintyr.embellishedtooltips.client.render.TooltipContext ec = new dev.quentintyr.embellishedtooltips.client.render.TooltipContext(
                ctx);
        ec.define(stack, tooltipSeconds);

        // Layers: default back to guarantee visibility then style layers [2]
        StylePipeline.renderBackLayers(ec, posVec, size);

        // Summary row beneath first line (uses first-line vertical offset so it follows
        // the lowered baseline)
        renderSummaryRow(
                ctx, font, stack,
                pos.x + L.paddingX + L.leftGutter + L.firstLineXOffset,
                pos.y + L.paddingTop + L.firstLineYOffset + font.fontHeight + 1);

        // Between background and text effects
        StylePipeline.renderBetweenTextEffects(ec, posVec, size);

        // Text (with first-line left gutter and extra title spacing; first line lowered
        // by firstLineYOffset)
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(0, 0, 450.0F);
        renderSource.renderText(ctx, font, pos.x + L.paddingX, pos.y + L.paddingTop, L);
        ms.pop();

        // Foreground/frame/effects
        StylePipeline.renderFrontLayers(ec, posVec, size);

        // Optional side panel (armor stand or spinning tool)
        if (hasSidePanel) {
            Vec2f center = SidePanelRenderer.renderSecondPanel(ec, posVec, size);
            if (isArmor) {
                equip(stack);
                SidePanelRenderer.renderStand(ctx, (int) center.x, (int) (center.y + 26));
            } else { // tool
                SidePanelRenderer.renderSpinningItem(ctx, stack, center);
            }
        }

        hoveredLastFrame = true;
        return true;
    }

    // Layout and placement ---------------------------------------------------

    private static final class TooltipLayout {
        final int leftGutter;
        final int paddingX;
        final int paddingTop;
        final int paddingBottom;
        final int lineGap;
        final int titleExtra;
        final int firstLineYOffset;
        final int firstLineXOffset;

        private TooltipLayout(int leftGutter, int paddingX, int paddingTop, int paddingBottom,
                int lineGap, int titleExtra, int firstLineYOffset, int firstLineXOffset) {
            this.leftGutter = leftGutter;
            this.paddingX = paddingX;
            this.paddingTop = paddingTop;
            this.paddingBottom = paddingBottom;
            this.lineGap = lineGap;
            this.titleExtra = titleExtra;
            this.firstLineYOffset = firstLineYOffset;
            this.firstLineXOffset = firstLineXOffset;
        }

        static TooltipLayout defaults() {
            return new TooltipLayout(
                    21, // leftGutter (keep)
                    6, // paddingX
                    4, // paddingTop
                    5, // paddingBottom
                    2, // lineGap
                    13, // titleExtra
                    3, // firstLineYOffset
                    3 // firstLineXOffset: push title a bit to the right to clear the panel edge
            );
        }
    }

    private static final class Size {
        final int contentWidth;
        final int contentHeight;

        Size(int w, int h) {
            this.contentWidth = w;
            this.contentHeight = h;
        }

        int widthWithPadding(TooltipLayout L) {
            return Math.max(contentWidth, L.leftGutter) + L.paddingX * 2;
        }

        int heightWithPadding(TooltipLayout L) {
            return L.paddingTop + contentHeight + L.paddingBottom + L.titleExtra;
        }
    }

    private interface RenderSource {
        Size measure(TextRenderer font, TooltipLayout L, boolean isArmor, int rarityWidth);

        void renderText(DrawContext ctx, TextRenderer font, int x, int y, TooltipLayout L);
    }

    private static final class ComponentsSource implements RenderSource {
        private final List<TooltipComponent> components;

        ComponentsSource(List<TooltipComponent> components) {
            this.components = components;
        }

        @Override
        public Size measure(TextRenderer font, TooltipLayout L, boolean isArmor, int rarityWidth) {
            int w = 0, h = 0;
            for (int i = 0; i < components.size(); i++) {
                TooltipComponent c = components.get(i);
                int cw = c.getWidth(font);
                if (i == 0)
                    cw += L.leftGutter; // [12]
                w = Math.max(w, cw);
                h += c.getHeight();
                if (i < components.size() - 1)
                    h += L.lineGap;
            }
            if (isArmor) {
                // Use capped stats width so long numbers don't stretch the tooltip
                int cappedStatsWidth = ArmorRow.width(font); // based on cap example [10]
                w = Math.max(w, L.leftGutter + cappedStatsWidth);
            } else {
                w = Math.max(w, L.leftGutter + rarityWidth);
            }
            return new Size(w, h);
        }

        @Override
        public void renderText(DrawContext ctx, TextRenderer font, int x, int y, TooltipLayout L) {
            MatrixStack matrices = ctx.getMatrices();
            VertexConsumerProvider.Immediate vcp = ctx.getVertexConsumers();
            int cy = y;
            for (int i = 0; i < components.size(); i++) {
                TooltipComponent c = components.get(i);
                int xOff = (i == 0) ? (L.leftGutter + L.firstLineXOffset) : 0; // add X offset on first line
                int yOff = (i == 0) ? L.firstLineYOffset : 0;
                matrices.push();
                c.drawText(font, x + xOff, cy + yOff, matrices.peek().getPositionMatrix(), vcp); // [4]
                c.drawItems(font, x + xOff, cy + yOff, ctx); // [4]
                matrices.pop();
                cy += c.getHeight() + L.lineGap + (i == 0 ? L.titleExtra : 0);
            }
            try {
                vcp.drawCurrentLayer();
            } catch (Exception ignored) {
            }
        }
    }

    private static final class Placement {
        static Point place(int mouseX, int mouseY, int width, int height,
                int screenW, int screenH, boolean hasSide, int panelW, int gap) {
            int x, y;
            if (hasSide) {
                final int panelRightX = mouseX + 12;
                int posXRight = panelRightX + panelW + gap;
                x = (posXRight + width <= screenW - 4) ? posXRight : Math.max(4, mouseX - 16 - width - gap - panelW);
            } else {
                x = mouseX + 12;
                if (x + width > screenW)
                    x = mouseX - 16 - width;
                x = Math.max(4, Math.min(x, screenW - width - 4));
            }
            y = mouseY - 12;
            if (y + height + 6 > screenH)
                y = screenH - height - 6;
            y = Math.max(4, Math.min(y, screenH - height - 4));
            return new Point(x, y);
        }
    }

    // Style pipeline ---------------------------------------------------------

    private static final class StylePipeline {
        static void renderBackLayers(dev.quentintyr.embellishedtooltips.client.render.TooltipContext ec, Vec2f pos,
                Point size) {
            // guarantee a back even if style has none
            ec.drawManaged(() -> StyleManager.getInstance().getDefaultStyle().renderBack(ec, pos, size, true)); // default
                                                                                                                // [2]
            if (renderStyle != null) {
                ec.drawManaged(() -> renderStyle.renderEffects(
                        dev.quentintyr.embellishedtooltips.client.style.Effects.BACKGROUND, ec, pos, size)); // [2]
                ec.drawManaged(() -> renderStyle.renderBack(ec, pos, size, true)); // back [2]
            }
        }

        static void renderBetweenTextEffects(dev.quentintyr.embellishedtooltips.client.render.TooltipContext ec,
                Vec2f pos, Point size) {
            if (renderStyle != null) {
                ec.drawManaged(() -> renderStyle.renderEffects(
                        dev.quentintyr.embellishedtooltips.client.style.Effects.TEXT_BACKGROUND, ec, pos, size)); // [2]
            }
        }

        static void renderFrontLayers(dev.quentintyr.embellishedtooltips.client.render.TooltipContext ec, Vec2f pos,
                Point size) {
            if (renderStyle != null) {
                ec.drawManaged(() -> renderStyle.renderEffects(
                        dev.quentintyr.embellishedtooltips.client.style.Effects.TEXT_FRAME, ec, pos, size)); // [2]
                ec.drawManaged(() -> renderStyle.renderFront(ec, pos, size)); // frame+icon [2]
                ec.drawManaged(() -> renderStyle
                        .renderEffects(dev.quentintyr.embellishedtooltips.client.style.Effects.FRAME, ec, pos, size)); // [2]
                ec.drawManaged(() -> renderStyle
                        .renderEffects(dev.quentintyr.embellishedtooltips.client.style.Effects.FRONT, ec, pos, size)); // [2]
            }
        }
    }

    // Summary row (armor or rarity) -----------------------------------------

    private static void renderSummaryRow(DrawContext ctx, TextRenderer font, ItemStack stack, int rowX, int rowY) {
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(0, 0, 450.0F);
        if (stack.getItem() instanceof ArmorItem armorItem) {
            int armor = armorItem.getProtection();
            float tough = armorItem.getToughness();
            int maxDur = stack.getMaxDamage();
            int curDur = Math.max(0, maxDur - stack.getDamage());
            ArmorRow.draw(ctx, font, rowX, rowY, armor, tough, maxDur, curDur);
        } else {
            Text rarity = getRarityName(stack);
            int color = getRarityColor(stack);
            ctx.drawText(font, rarity, rowX, rowY, color, false);
        }
        ms.pop();
    }

    private static final class ArmorRow {
        private static final String DURABILITY_CAP = "0000/0000";

        static int width(TextRenderer font) {
            int icon = 10, gapNum = 1, spacing = 3;
            // Provision space for durability as "0000/0000" (covers 1561-style values)
            return (icon + gapNum + font.getWidth("00")) + spacing
                    + (icon + gapNum + font.getWidth("00")) + spacing
                    + (icon + gapNum + font.getWidth(DURABILITY_CAP));
        }

        static void draw(DrawContext ctx, TextRenderer font, int x, int y, int armor, float tough, int maxDur,
                int curDur) {
            final int icon = 10, spacing = 3, gapNum = 1;
            final int color = 0xFF6A6A6A;
            int cursor = x;

            ctx.drawTexture(TEX_ARMOR, cursor, y - 2, 0, 0, icon, icon, icon, icon);
            cursor += icon + gapNum;
            String armorStr = Integer.toString(armor);
            ctx.drawText(font, Text.literal(armorStr), cursor, y, color, false);
            cursor += font.getWidth(armorStr) + spacing;

            ctx.drawTexture(TEX_ARMOR_TOUGH, cursor, y - 2, 0, 0, icon, icon, icon, icon);
            cursor += icon + gapNum;
            String toughStr = (tough % 1.0f == 0.0f) ? Integer.toString((int) tough) : String.format("%.1f", tough);
            ctx.drawText(font, Text.literal(toughStr), cursor, y, color, false);
            cursor += font.getWidth(toughStr) + spacing;

            ctx.drawTexture(TEX_DURABILITY, cursor, y - 2, 0, 0, icon, icon, icon, icon);
            cursor += icon + gapNum;
            String duraStr = maxDur + "/" + curDur;
            ctx.drawText(font, Text.literal(duraStr), cursor, y, color, false);
        }
    }

    // Side panel & models ----------------------------------------------------

    private static final class SidePanelRenderer {
        static Vec2f renderSecondPanel(dev.quentintyr.embellishedtooltips.client.render.TooltipContext ec,
                Vec2f tooltipPos, Point tooltipSize) {
            final float gap = 6.0f;
            final Point panelSize = new Point(36, 72);

            float leftX = tooltipPos.x - gap - panelSize.x;
            float placedX = leftX >= 4 ? leftX : tooltipPos.x + tooltipSize.x + gap;
            Vec2f panelPos = new Vec2f(placedX, tooltipPos.y);

            ec.drawManaged(() -> {
                if (renderStyle != null)
                    renderStyle.renderBack(ec, panelPos, panelSize, false); // style panel [2]
                StyleManager.getInstance().getDefaultStyle().renderBack(ec, panelPos, panelSize, false); // fallback [2]
            });
            return new Vec2f(panelPos.x + panelSize.x / 2.0f, panelPos.y + panelSize.y / 2.0f);
        }

        static void renderStand(DrawContext ctx, int x, int y) {
            if (renderStand == null)
                return;
            MatrixStack ms = ctx.getMatrices();
            ms.push();
            ms.translate(x, y, 500.0F);
            ms.scale(-30.0F, -30.0F, 30.0F);
            ms.multiply(new Quaternionf().rotationX((float) Math.toRadians(25.0f)));
            float spin = (float) (((System.currentTimeMillis() / 1000.0) % 360.0) * Math.toRadians(20.0));
            ms.multiply(new Quaternionf().rotationY(spin));

            VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders()
                    .getEntityVertexConsumers();
            DiffuseLighting.enableGuiDepthLighting();
            MinecraftClient.getInstance().getEntityRenderDispatcher().render(renderStand, 0, 0, 0, 0, 1.0f, ms,
                    immediate, 15728880);
            immediate.draw();
            DiffuseLighting.disableGuiDepthLighting();
            ms.pop();
        }

        static void renderSpinningItem(DrawContext ctx, ItemStack stack, Vec2f center) {
            MatrixStack ms = ctx.getMatrices();
            ms.push();
            ms.translate(center.x, center.y, 500.0F);
            ms.scale(2.75f, 2.75f, 2.75f);
            ms.multiply(new Quaternionf().rotationX((float) Math.toRadians(-30.0f)));
            float spin = (float) (((System.currentTimeMillis() / 1000.0) % 360.0) * Math.toRadians(-20.0));
            ms.multiply(new Quaternionf().rotationY(spin));
            ms.multiply(new Quaternionf().rotationZ((float) Math.toRadians(-45.0f)));
            ms.push();
            ms.translate(-8.0F, -8.0F, -150.0F);
            ctx.drawItem(stack, 0, 0);
            ms.pop();
            ms.pop();
        }
    }

    // Shared helpers ---------------------------------------------------------

    private static void beginHoverIfNeeded(ItemStack stack, boolean firstFrameThisHover) {
        long now = System.currentTimeMillis();
        boolean stackChanged = (lastStack == null) || (!ItemStack.areEqual(stack, lastStack));
        boolean reenteredByGap = lastRenderMillis == 0L || (now - lastRenderMillis) > 150L;
        if (firstFrameThisHover || stackChanged || tooltipStartMillis == 0L || reenteredByGap) {
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

    private static Text getRarityName(ItemStack stack) {
        try {
            String rarity = stack.getRarity().name().toLowerCase();
            return Text.translatable("rarity." + rarity + ".name");
        } catch (Exception e) {
            return Text.empty();
        }
    }

    private static int getRarityColor(ItemStack stack) {
        try {
            if ("common".equalsIgnoreCase(stack.getRarity().name())) {
                return 0xFF6A6A6A;
            }
            var formatting = stack.getRarity().formatting;
            if (formatting != null && formatting.getColorValue() != null) {
                return 0xFF000000 | formatting.getColorValue();
            }
        } catch (Exception ignored) {
        }
        return 0xFF7A7A7A;
    }
}
