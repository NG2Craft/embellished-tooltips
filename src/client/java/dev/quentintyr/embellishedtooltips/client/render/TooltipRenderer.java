package dev.quentintyr.embellishedtooltips.client.render;

import dev.quentintyr.embellishedtooltips.client.style.Effects;
import dev.quentintyr.embellishedtooltips.client.style.StyleManager;
import dev.quentintyr.embellishedtooltips.client.style.TooltipStyle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import org.joml.Quaternionf;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import dev.quentintyr.embellishedtooltips.client.render.BasicTooltipContext;

/**
 * Handles the rendering of tooltips
 */
@Environment(EnvType.CLIENT)
public final class TooltipRenderer {
    @Nullable
    private static TooltipStyle renderStyle = null;
    @Nullable
    private static ArmorStandEntity renderStand;
    private static ItemStack renderStack;
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

        if (renderStyle != null && !components.isEmpty()) {
            // Calculate tooltip size
            int tooltipWidth = 0;
            int tooltipHeight = 0;

            for (TooltipComponent component : components) {
                int width = component.getWidth(font);
                if (width > tooltipWidth) {
                    tooltipWidth = width;
                }
                tooltipHeight += component.getHeight();

                if (component != components.get(components.size() - 1)) {
                    tooltipHeight += 2; // Gap between components
                }
            }

            // Add padding
            tooltipWidth += 8;
            tooltipHeight += 8;

            // Get position
            int posX = x;
            int posY = y;

            // Create tooltip context
            MatrixStack matrixStack = drawContext.getMatrices();
            BasicTooltipContext context = new BasicTooltipContext(
                    posX, posY, tooltipWidth, tooltipHeight,
                    componentsToText(components), stack,
                    MinecraftClient.getInstance().getWindow().getScaledWidth(),
                    MinecraftClient.getInstance().getWindow().getScaledHeight(),
                    matrixStack);

            // Render background
            renderStyle.renderBack(drawContext, context);

            // Render text
            renderText(drawContext, font, components, posX + 4, posY + 4);

            // Render front (frame, icon)
            renderStyle.renderFront(drawContext, context);

            // Render special cases for armor and tools
            if (stack.getItem() instanceof ArmorItem) {
                // TODO: Implement armor model rendering
                equip(stack);
                renderStand(drawContext, posX + tooltipWidth + 20, posY + tooltipHeight / 2);
            } else if (stack.getItem() instanceof ToolItem) {
                // TODO: Implement tool model rendering
            }

            return true;
        }

        return false;
    }

    /**
     * Converts tooltip components to text lines.
     *
     * @param components The tooltip components
     * @return The text lines
     */
    private static List<Text> componentsToText(List<TooltipComponent> components) {
        // This is a placeholder - in practice we'd need to extract text from components
        // based on their actual implementation
        return List.of(Text.literal("Item Description"));
    }

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
            int y) {
        int currentY = y;

        for (TooltipComponent component : components) {
            // Draw component using DrawContext which handles the rendering details
            component.drawItems(font, x, currentY, drawContext);
            // In Fabric 1.20.1, we need to use individual methods instead of drawText
            // directly
            currentY += component.getHeight() + 2; // Add spacing between components
        }
    }

    /**
     * Updates the tooltip style based on the item stack.
     *
     * @param stack The item stack
     */
    private static void updateStyle(ItemStack stack) {
        // Update the render data
        renderStack = stack;

        // Update tooltip timer
        long currentTimeMillis = System.currentTimeMillis();
        if (tooltipStartMillis == 0 || stack != renderStack) {
            tooltipStartMillis = currentTimeMillis;
        }
        tooltipSeconds = (currentTimeMillis - tooltipStartMillis) / 1000.0F;

        // Get style from StyleManager
        renderStyle = StyleManager.getInstance().getDefaultStyle();
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
            matrixStack.translate(x, y, 200.0F);
            matrixStack.scale(30.0F, 30.0F, 30.0F);
            matrixStack.multiply(new Quaternionf().rotationX((float) Math.toRadians(180.0f)));
            matrixStack.multiply(new Quaternionf().rotationY((float) Math.toRadians(135.0f)));

            // Create a render context
            VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders()
                    .getEntityVertexConsumers();

            // Render the armor stand
            DiffuseLighting.method_34742();
            MinecraftClient.getInstance().getEntityRenderDispatcher().render(
                    renderStand, 0, 0, 0,
                    0, 0, matrixStack, immediate, 15728880);
            immediate.draw();
            DiffuseLighting.enableGuiDepthLighting();

            matrixStack.pop();
        }
    }
}
