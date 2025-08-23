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
            // Calculate tooltip size
            int tooltipWidth = 0;
            int tooltipHeight = 0;
            final int leftGutter = 26; // like Obscure: space for icon + summary

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
                    tooltipHeight += 2; // Gap between components
                }
            }

            // Include padding
            tooltipWidth = Math.max(tooltipWidth, leftGutter) + 8;
            // Extra 13px under first line for summary field spacing
            tooltipHeight = 14 + tooltipHeight + 8;

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
            drawContext.drawText(font, rarity, posX + leftGutter, posY + 13, 0xFF4ECDC4, false);
            drawContext.getMatrices().pop();

            // Between background and text effects
            context.drawManaged(() -> renderStyle.renderEffects(
                    dev.quentintyr.embellishedtooltips.client.style.Effects.TEXT_BACKGROUND, context, pos, size));

            // Render text with offset and additional first-line spacing
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(0, 0, 450.0F);
            renderText(drawContext, font, components, posX + 4, posY + 4, leftGutter);
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

            return true;
        }

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
                // Calculate tooltip size with left gutter and first-line gap
                int tooltipWidth = 0;
                for (Text line : lines) {
                    tooltipWidth = Math.max(tooltipWidth, font.getWidth(line));
                }
                final int leftGutter = 26;
                tooltipWidth = Math.max(tooltipWidth, leftGutter) + 8;
                int tooltipHeight = 14 + (lines.size() * font.fontHeight + (lines.size() - 1) * 2) + 8;

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
                drawContext.drawText(font, rarity, posX + leftGutter, posY + 13, 0xFF4ECDC4, false);
                drawContext.getMatrices().pop();

                // Between background and text effects
                context.drawManaged(() -> renderStyle.renderEffects(
                        dev.quentintyr.embellishedtooltips.client.style.Effects.TEXT_BACKGROUND, context, pos, size));

                // Text
                drawContext.getMatrices().push();
                drawContext.getMatrices().translate(0, 0, 450.0F);
                int currentY = posY + 4;
                for (int i = 0; i < lines.size(); i++) {
                    int xOffset = (i == 0) ? leftGutter : 0;
                    drawContext.drawText(font, lines.get(i), posX + 4 + xOffset, currentY, 0xFFFFFFFF, true);
                    currentY += font.fontHeight + 2 + (i == 0 ? 13 : 0);
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

                return true;
            }
        }

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

            // Draw items
            component.drawItems(font, x, currentY, drawContext);

            // Pop matrix state
            matrices.pop();

            // Extra 13px spacing after first line to accommodate summary area
            currentY += component.getHeight() + 2 + (component == components.get(0) ? 13 : 0);
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
        // Update tooltip timer
        long currentTimeMillis = System.currentTimeMillis();
        boolean stackChanged = (lastStack == null)
                || (!ItemStack.areEqual(stack, lastStack));
        if (tooltipStartMillis == 0 || stackChanged) {
            tooltipStartMillis = currentTimeMillis;
        }
        tooltipSeconds = (currentTimeMillis - tooltipStartMillis) / 1000.0F;

        // Track last seen stack after computing change
        lastStack = stack.copy();

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
