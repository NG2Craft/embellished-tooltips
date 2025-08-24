package dev.quentintyr.embellishedtooltips.client.render;

import dev.quentintyr.embellishedtooltips.client.style.TooltipStyle;
import dev.quentintyr.embellishedtooltips.client.style.TooltipStylePreset;
import dev.quentintyr.embellishedtooltips.client.ResourceLoader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import org.joml.Quaternionf;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import dev.quentintyr.embellishedtooltips.client.StyleManager;
import net.minecraft.util.math.Vec2f;
import java.awt.Point;

/**
 * Handles the rendering of tooltips
 */
@Environment(EnvType.CLIENT)
public final class TooltipRenderer {
    @Nullable
    private static TooltipStyle renderStyle = null;
    @Nullable
    private static ArmorStandEntity renderStand;
    private static ItemStack lastStack;
    private static long tooltipStartMillis;
    private static float tooltipSeconds;
    // Track whether we rendered a tooltip last frame (for simple hover start
    // detection)
    private static boolean hoveredLastFrame;
    // Track last time we rendered to detect a pause (leaving tooltip) even if our
    // render method wasn't called to flip hoveredLastFrame
    private static long lastRenderMillis;

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

    /**
     * Renders a tooltip.
     * 
     * @param drawContext The draw context
     * @param stack       The item stack
     * @param font        The font renderer
     * @param components  The tooltip components
     * @param x           Mouse X position
     * @param y           Mouse Y position
     * @param positioner  The tooltip positioner
     * @return True if the tooltip was rendered, false otherwise
     */
    public static boolean render(DrawContext drawContext, ItemStack stack, TextRenderer font,
            List<TooltipComponent> components, int x, int y,
            TooltipPositioner positioner) {

        updateStyle(stack);

        if (renderStyle != null && components != null && !components.isEmpty()) {
            // Layout constants (tuned to Obscure spacing)
            final int leftGutter = 21; // flush with slot right edge (27) minus paddingX (6)
            final int paddingX = 6;
            final int paddingTop = 4; // align top text with slot background with a small cushion
            final int paddingBottom = 5; // match slot top offset (now 5px)
            final int lineGap = 2;
            final int titleExtra = 13; // gap below first line for rarity summary

            // Calculate tooltip size
            int tooltipWidth = 0;
            int tooltipHeight = 0;

            for (int i = 0; i < components.size(); i++) {
                TooltipComponent component = components.get(i);
                int width = component.getWidth(font);
                if (i == 0)
                    width += leftGutter; // first line shifted by gutter
                if (width > tooltipWidth) {
                    tooltipWidth = width;
                }
                tooltipHeight += component.getHeight();

                if (i < components.size() - 1) {
                    tooltipHeight += lineGap; // Gap between components
                }
            }

            // Include padding and extra gap under title
            tooltipWidth = Math.max(tooltipWidth, leftGutter) + paddingX * 2;
            tooltipHeight = paddingTop + tooltipHeight + paddingBottom + titleExtra;

            // Compute clamped position (vanilla-like)
            int screenW = drawContext.getScaledWindowWidth();
            int screenH = drawContext.getScaledWindowHeight();
            int posX = x + 12;
            if (posX + tooltipWidth > screenW)
                posX = x - 16 - tooltipWidth;
            posX = Math.max(4, Math.min(posX, screenW - tooltipWidth - 4));
            int posY = y - 12;
            if (posY + tooltipHeight + 6 > screenH)
                posY = screenH - tooltipHeight - 6;
            posY = Math.max(4, Math.min(posY, screenH - tooltipHeight - 4));

            // Reset/start animation timer only when hover begins or stack changes
            beginHoverIfNeeded(stack, !hoveredLastFrame);

            // Create tooltip context
            dev.quentintyr.embellishedtooltips.client.render.TooltipContext context = new dev.quentintyr.embellishedtooltips.client.render.TooltipContext(
                    drawContext);
            context.define(stack, tooltipSeconds);

            // Calculate position and size
            Vec2f pos = new Vec2f(posX, posY);
            Point size = new Point(tooltipWidth, tooltipHeight);

            // Default background to ensure panel even if selected style lacks one
            context.drawManaged(
                    () -> StyleManager.getInstance().getDefaultStyle().renderBack(context, pos, size, true));
            // Effect/back layers
            context.drawManaged(() -> renderStyle.renderEffects(
                    dev.quentintyr.embellishedtooltips.client.style.Effects.BACKGROUND, context, pos, size));
            context.drawManaged(() -> renderStyle.renderBack(context, pos, size, true));

            // Summary/rarity text to the right of icon gutter (forefront of text layer)
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(0, 0, 450.0F);
            Text rarity = getRarityName(stack);
            int rarityColor = getRarityColor(stack);
            int rarityY = posY + paddingTop + font.fontHeight + 1;
            // Align rarity with the same left padding as text content
            drawContext.drawText(font, rarity, posX + paddingX + leftGutter, rarityY, rarityColor, false);
            drawContext.getMatrices().pop();

            // Between background and text effects
            context.drawManaged(() -> renderStyle.renderEffects(
                    dev.quentintyr.embellishedtooltips.client.style.Effects.TEXT_BACKGROUND, context, pos, size));

            // Render text with offset and additional first-line spacing
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(0, 0, 450.0F);
            renderText(drawContext, font, components, posX + paddingX, posY + paddingTop, leftGutter);
            drawContext.getMatrices().pop();

            // Between text and frame effects
            context.drawManaged(() -> renderStyle.renderEffects(
                    dev.quentintyr.embellishedtooltips.client.style.Effects.TEXT_FRAME, context, pos, size));
            // Render front (frame, icon)
            context.drawManaged(() -> renderStyle.renderFront(context, pos, size));
            // Between frame and icon effects
            context.drawManaged(() -> renderStyle.renderEffects(
                    dev.quentintyr.embellishedtooltips.client.style.Effects.FRAME, context, pos, size));
            // Front effects
            context.drawManaged(() -> renderStyle
                    .renderEffects(dev.quentintyr.embellishedtooltips.client.style.Effects.FRONT, context, pos, size));

            // Render special cases for armor and tools
            if (stack.getItem() instanceof ArmorItem) {
                Vec2f center = renderSecondPanel(context, pos);
                equip(stack);
                renderStand(drawContext, (int) (center.x), (int) (center.y + 26));
            } else if (stack.getItem() instanceof ToolItem) {
                Vec2f center = renderSecondPanel(context, pos);
                // Render a 3D-ish spinning item like Obscure
                MatrixStack ms = drawContext.getMatrices();
                ms.push();
                ms.translate(center.x, center.y, 500.0F);
                ms.scale(2.75f, 2.75f, 2.75f);
                ms.multiply(new Quaternionf().rotationX((float) Math.toRadians(-30.0f)));
                // Spin slowly
                float spin = (float) (((System.currentTimeMillis() / 1000.0) % 360.0) * Math.toRadians(-20.0));
                ms.multiply(new Quaternionf().rotationY(spin));
                ms.multiply(new Quaternionf().rotationZ((float) Math.toRadians(-45.0f)));
                ms.push();
                ms.translate(-8.0F, -8.0F, -150.0F);
                drawContext.drawItem(stack, 0, 0);
                ms.pop();
                ms.pop();
            }

            hoveredLastFrame = true;
            return true;
        }

        hoveredLastFrame = false;
        return false;
    }

    /**
     * Simplified render method for Screen hook.
     * 
     * @param drawContext The draw context
     * @param stack       The item stack
     * @param x           Mouse X position
     * @param y           Mouse Y position
     * @return True if custom rendering should be used, false to fall back to
     *         default
     */
    public static boolean render(DrawContext drawContext, ItemStack stack, int x, int y) {
        updateStyle(stack);

        if (renderStyle != null) {
            // Create a basic tooltip with style
            MinecraftClient client = MinecraftClient.getInstance();
            TextRenderer font = client.textRenderer;

            // Get tooltip text from the item
            List<Text> lines = stack.getTooltip(client.player,
                    client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED
                            : TooltipContext.Default.BASIC);

            if (!lines.isEmpty()) {
                // Layout constants (mirror main path)
                final int leftGutter = 24; // flush alignment with slot
                final int paddingX = 6;
                final int paddingTop = 6;
                final int paddingBottom = 5; // match slot top offset (now 5px)
                final int lineGap = 2;
                final int titleExtra = 13;

                // Calculate tooltip size with left gutter on first line and include rarity
                // width
                int contentWidth = 0;
                for (int i = 0; i < lines.size(); i++) {
                    int width = font.getWidth(lines.get(i));
                    if (i == 0)
                        width += leftGutter; // first line shifts by gutter
                    contentWidth = Math.max(contentWidth, width);
                }
                // Include rarity summary width aligned to content start
                Text rarity2 = getRarityName(stack);
                contentWidth = Math.max(contentWidth, leftGutter + font.getWidth(rarity2));
                int tooltipWidth = Math.max(contentWidth, leftGutter) + paddingX * 2;
                int contentHeight = lines.size() * font.fontHeight + Math.max(0, lines.size() - 1) * lineGap;
                int tooltipHeight = paddingTop + contentHeight + paddingBottom + titleExtra;

                // Reset/start animation timer only when hover begins or stack changes
                beginHoverIfNeeded(stack, !hoveredLastFrame);

                // Create tooltip context
                dev.quentintyr.embellishedtooltips.client.render.TooltipContext context = new dev.quentintyr.embellishedtooltips.client.render.TooltipContext(
                        drawContext);
                context.define(stack, tooltipSeconds);

                // Calculate clamped position and size (vanilla-like)
                int screenW = drawContext.getScaledWindowWidth();
                int screenH = drawContext.getScaledWindowHeight();
                int posX = x + 12;
                if (posX + tooltipWidth > screenW)
                    posX = x - 16 - tooltipWidth;
                posX = Math.max(4, Math.min(posX, screenW - tooltipWidth - 4));
                int posY = y - 12;
                if (posY + tooltipHeight + 6 > screenH)
                    posY = screenH - tooltipHeight - 6;
                posY = Math.max(4, Math.min(posY, screenH - tooltipHeight - 4));
                Vec2f pos = new Vec2f(posX, posY);
                Point size = new Point(tooltipWidth, tooltipHeight);

                // Default background to ensure panel even if selected style lacks one
                context.drawManaged(
                        () -> StyleManager.getInstance().getDefaultStyle().renderBack(context, pos, size, true));
                // Effects and background
                context.drawManaged(() -> renderStyle.renderEffects(
                        dev.quentintyr.embellishedtooltips.client.style.Effects.BACKGROUND, context, pos, size));
                context.drawManaged(() -> renderStyle.renderBack(context, pos, size, true));

                // Rarity summary
                drawContext.getMatrices().push();
                drawContext.getMatrices().translate(0, 0, 450.0F);
                Text rarity = getRarityName(stack);
                int rarityColor = getRarityColor(stack);
                int rarityY = posY + paddingTop + font.fontHeight + 1;
                // Align rarity with the same left padding as text content
                drawContext.drawText(font, rarity, posX + paddingX + leftGutter, rarityY, rarityColor, false);
                drawContext.getMatrices().pop();

                // Between background and text effects
                context.drawManaged(() -> renderStyle.renderEffects(
                        dev.quentintyr.embellishedtooltips.client.style.Effects.TEXT_BACKGROUND, context, pos, size));

                // Text
                drawContext.getMatrices().push();
                drawContext.getMatrices().translate(0, 0, 450.0F);
                int currentY = posY + paddingTop;
                for (int i = 0; i < lines.size(); i++) {
                    int xOffset = (i == 0) ? leftGutter : 0;
                    drawContext.drawText(font, lines.get(i), posX + paddingX + xOffset, currentY, 0xFFFFFFFF, true);
                    currentY += font.fontHeight + lineGap + (i == 0 ? titleExtra : 0);
                }
                drawContext.getMatrices().pop();

                // Between text and frame effects
                context.drawManaged(() -> renderStyle.renderEffects(
                        dev.quentintyr.embellishedtooltips.client.style.Effects.TEXT_FRAME, context, pos, size));
                // Front (frame + icon) and effects
                context.drawManaged(() -> renderStyle.renderFront(context, pos, size));
                context.drawManaged(() -> renderStyle.renderEffects(
                        dev.quentintyr.embellishedtooltips.client.style.Effects.FRAME, context, pos, size));
                context.drawManaged(() -> renderStyle.renderEffects(
                        dev.quentintyr.embellishedtooltips.client.style.Effects.FRONT, context, pos, size));

                // Optional model panel when not in handled screens too
                if (stack.getItem() instanceof ArmorItem) {
                    Vec2f center = renderSecondPanel(context, pos);
                    equip(stack);
                    renderStand(drawContext, (int) (center.x), (int) (center.y + 26));
                } else if (stack.getItem() instanceof ToolItem) {
                    Vec2f center = renderSecondPanel(context, pos);
                    MatrixStack ms = drawContext.getMatrices();
                    ms.push();
                    ms.translate(center.x, center.y, 500.0F);
                    ms.scale(2.75f, 2.75f, 2.75f);
                    ms.multiply(new org.joml.Quaternionf().rotationX((float) Math.toRadians(-30.0f)));
                    float spin = (float) (((System.currentTimeMillis() / 1000.0) % 360.0) * Math.toRadians(-20.0));
                    ms.multiply(new org.joml.Quaternionf().rotationY(spin));
                    ms.multiply(new org.joml.Quaternionf().rotationZ((float) Math.toRadians(-45.0f)));
                    ms.push();
                    ms.translate(-8.0F, -8.0F, -150.0F);
                    drawContext.drawItem(stack, 0, 0);
                    ms.pop();
                    ms.pop();
                }

                hoveredLastFrame = true;
                return true;
            }
        }

        hoveredLastFrame = false;
        return false;
    }

    // No need to convert components to text; we render components directly

    /**
     * Renders the text components of a tooltip.
     *
     * @param drawContext The draw context
     * @param font        The font renderer
     * @param components  The tooltip components
     * @param x           The X position to render at
     * @param y           The Y position to render at
     */
    private static void renderText(DrawContext drawContext, TextRenderer font, List<TooltipComponent> components, int x,
            int y, int leftGutter) {
        final int lineGap = 2;
        final int titleExtra = 13;
        int currentY = y;

        // Use the matrix stack and vertex consumers from DrawContext
        MatrixStack matrices = drawContext.getMatrices();
        VertexConsumerProvider.Immediate vertexConsumers = drawContext.getVertexConsumers();

        for (TooltipComponent component : components) {
            // Push matrix state
            matrices.push();

            // Draw text first
            int xOffset = component == components.get(0) ? leftGutter : 0;
            component.drawText(font, x + xOffset, currentY, matrices.peek().getPositionMatrix(), vertexConsumers);

            // Draw items at the same x so glyph and item components align
            component.drawItems(font, x + xOffset, currentY, drawContext);

            // Pop matrix state
            matrices.pop();

            // Extra spacing after first line to accommodate summary area
            currentY += component.getHeight() + lineGap + (component == components.get(0) ? titleExtra : 0);
        }

        // Ensure all text is rendered
        try {
            vertexConsumers.drawCurrentLayer();
        } catch (Exception ignored) {
        }
    }

    /**
     * Updates the tooltip style based on the item stack.
     *
     * @param stack The item stack
     */
    private static void updateStyle(ItemStack stack) {
        // Build/resolve style for the given stack
        // Get style from ResourceLoader based on the actual ItemStack
        Optional<TooltipStylePreset> styleOpt = ResourceLoader.getStyleFor(stack);
        if (styleOpt.isPresent()) {
            TooltipStylePreset preset = styleOpt.get();
            TooltipStyle.Builder builder = new TooltipStyle.Builder();

            // Build style from preset components
            preset.getPanel().ifPresent(builder::withPanel);
            preset.getFrame().ifPresent(builder::withFrame);
            preset.getIcon().ifPresent(builder::withIcon);
            builder.withEffects(preset.getEffects());

            renderStyle = builder.build();
        } else {
            // Fall back to default style if no specific style matches
            renderStyle = StyleManager.getInstance().getDefaultStyle();
        }
    }

    /**
     * Equips an armor item on the armor stand entity.
     *
     * @param stack The armor item stack
     */
    private static void equip(ItemStack stack) {
        if (renderStand == null) {
            // Create armor stand entity
            renderStand = new ArmorStandEntity(MinecraftClient.getInstance().world, 0, 0, 0);
        }

        if (stack.getItem() instanceof ArmorItem armorItem) {
            // Equip the armor item on the armor stand
            renderStand.equipStack(armorItem.getSlotType(), stack);
        }
    }

    /**
     * Renders the armor stand.
     *
     * @param drawContext The draw context
     * @param x           The X position to render at
     * @param y           The Y position to render at
     */
    private static void renderStand(DrawContext drawContext, int x, int y) {
        if (renderStand != null) {
            MatrixStack matrixStack = drawContext.getMatrices();
            matrixStack.push();
            matrixStack.translate(x, y, 500.0F);
            matrixStack.scale(-30.0F, -30.0F, 30.0F);
            // Slight tilt and slow spin
            matrixStack.multiply(new Quaternionf().rotationX((float) Math.toRadians(25.0f)));
            float spin = (float) (((System.currentTimeMillis() / 1000.0) % 360.0) * Math.toRadians(20.0));
            matrixStack.multiply(new Quaternionf().rotationY(spin));

            // Create a render context
            VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders()
                    .getEntityVertexConsumers();

            // Render the armor stand
            DiffuseLighting.enableGuiDepthLighting();
            MinecraftClient.getInstance().getEntityRenderDispatcher().render(renderStand, 0, 0, 0, 0, 1.0f, matrixStack,
                    immediate, 15728880);
            immediate.draw();
            DiffuseLighting.disableGuiDepthLighting();

            matrixStack.pop();
        }
    }

    private static Text getRarityName(ItemStack stack) {
        try {
            String rarity = stack.getRarity().name().toLowerCase();
            // Use translation key consistent with Obscure: rarity.<rarity>.name
            return Text.translatable("rarity." + rarity + ".name");
        } catch (Exception e) {
            return Text.empty();
        }
    }

    private static int getRarityColor(ItemStack stack) {
        try {
            // Darker gray for common to match Obscure look
            if ("common".equalsIgnoreCase(stack.getRarity().name())) {
                return 0xFF6A6A6A;
            }
            var formatting = stack.getRarity().formatting;
            if (formatting != null && formatting.getColorValue() != null) {
                return 0xFF000000 | formatting.getColorValue();
            }
        } catch (Exception ignored) {
        }
        // Default to a soft dark gray for Common
        return 0xFF7A7A7A;
    }

    private static Vec2f renderSecondPanel(dev.quentintyr.embellishedtooltips.client.render.TooltipContext context,
            Vec2f pos) {
        float leftX = pos.x - 65.0f;
        // If this would go off-screen to the left, flip to right side of tooltip
        if (leftX < 0) {
            leftX = pos.x + 65.0f;
        }
        Vec2f panelPos = new Vec2f(leftX, pos.y);

        context.drawManaged(() -> {
            Point panelSize = new Point(36, 72);
            // If style has no panel, draw default panel to ensure visibility
            if (renderStyle != null) {
                renderStyle.renderBack(context, panelPos, panelSize, false);
            }
            // Always ensure a visible panel backdrop
            StyleManager.getInstance().getDefaultStyle().renderBack(context, panelPos, panelSize, false);
        });
        return new Vec2f(panelPos.x + 18.0f, panelPos.y + 36.0f);
    }
}
