package dev.quentintyr.embellishedtooltips.client.tooltip;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Renders a miniature 3D item frame + contained item inside a tooltip using an
 * ItemFrameEntity.
 * This avoids trying to perfectly replicate vanilla transforms manually.
 */
public class ItemFrame3DTooltipComponent implements TooltipComponent {
    private static final int WIDTH = 48; // matches side panel, internal scale controls fill
    private static final int HEIGHT = 48;
    private final ItemStack displayed;

    public ItemFrame3DTooltipComponent(ItemStack displayed) {
        this.displayed = displayed;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return WIDTH;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        MatrixStack matrices = ctx.getMatrices();
        VertexConsumerProvider.Immediate buffers = ctx.getVertexConsumers();
        matrices.push();
        // Translate to panel origin then center content (panel 48x48)
        matrices.translate(x, y, 400);

        // Draw a flat item frame background using the vanilla item frame item model
        // (front face only)
        ItemStack frameStack = new ItemStack(Items.ITEM_FRAME);
        BakedModel frameModel = mc.getItemRenderer().getModel(frameStack, mc.world, mc.player, 0);
        matrices.push();
        float scale = 2.95f; // ~47.2px frame footprint
        float pixelSize = 16f * scale; // scaled model size in panel pixels
        float offset = (48f - pixelSize) / 2f; // center offset (can be negative if overflow)
        matrices.translate(offset, offset, 0);
        matrices.scale(scale, scale, scale);
        mc.getItemRenderer().renderItem(frameStack, ModelTransformationMode.GUI, false, matrices, buffers,
                LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, frameModel);
        matrices.pop();

        // Render displayed item with slight inset/scale so it appears inside frame
        matrices.push();
        // Recompute basePixels (46.4) and center item inside (target item size ~ 30px)
        float basePixels = 16f * scale;
        float itemScale = 2.0f; // 16 * 2 = 32px item inside 47px frame area
        float inset = (basePixels - (16f * itemScale)) / 2f / scale; // model units
        matrices.translate(offset / scale + inset, offset / scale + inset, 50.0f);
        matrices.scale(itemScale, itemScale, itemScale);
        BakedModel itemModel = mc.getItemRenderer().getModel(displayed, mc.world, mc.player, 0);
        mc.getItemRenderer().renderItem(displayed, ModelTransformationMode.GUI, false, matrices, buffers,
                LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, itemModel);
        matrices.pop();

        matrices.pop();
        buffers.draw();
    }
}
