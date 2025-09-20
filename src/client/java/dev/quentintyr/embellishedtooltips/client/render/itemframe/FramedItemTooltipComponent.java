package dev.quentintyr.embellishedtooltips.client.tooltip;

import dev.quentintyr.embellishedtooltips.tooltip.ItemFramePreviewTooltipData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;

/**
 * TooltipComponent implementation that renders a decorative frame and the item
 * inside.
 * Keeps logic lightweight to avoid performance penalties.
 */
public class FramedItemTooltipComponent implements TooltipComponent {
    private static final Identifier FRAME_TEX = new Identifier("embellished_tooltips", "textures/gui/item_frame.png");
    private static final int SIZE = 32; // full square
    private final ItemStack stack;

    public FramedItemTooltipComponent(ItemFramePreviewTooltipData data) {
        this.stack = data.stack();
    }

    @Override
    public int getHeight() {
        return SIZE + 4;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return SIZE + 4;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext ctx) {
        // Draw the frame (expect caller to have bound rendering state)
        ctx.drawTexture(FRAME_TEX, x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);

        MinecraftClient mc = MinecraftClient.getInstance();
        int itemX = x + (SIZE / 2) - 8;
        int itemY = y + (SIZE / 2) - 8;

        if (stack.getItem() instanceof FilledMapItem) {
            MapState state = FilledMapItem.getMapState(stack, mc.world);
            if (state != null) {
                ctx.getMatrices().push();
                ctx.getMatrices().translate(x + 2, y + 2, 400);
                float scale = (SIZE - 4) / 128f;
                ctx.getMatrices().scale(scale, scale, 1f);
                mc.gameRenderer.getMapRenderer().draw(ctx.getMatrices(), ctx.getVertexConsumers(), 0, state, false,
                        0xF000F0);
                ctx.getMatrices().pop();
                ctx.getVertexConsumers().draw();
                return;
            }
        }
        ctx.getMatrices().push();
        ctx.getMatrices().translate(0, 0, 400);
        // Use DrawContext convenience to render item + overlay count/foil
        ctx.drawItem(stack, itemX, itemY);
        ctx.drawItemInSlot(textRenderer, stack, itemX, itemY);
        ctx.getMatrices().pop();
    }
}
