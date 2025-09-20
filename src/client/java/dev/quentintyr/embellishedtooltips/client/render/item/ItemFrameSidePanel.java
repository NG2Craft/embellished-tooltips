package dev.quentintyr.embellishedtooltips.client.render.item;

import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.render.TooltipStylePipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;

import java.awt.Point;

/**
 * Side panel renderer for item frame previews (random contained item).
 */
public final class ItemFrameSidePanel {
    private ItemFrameSidePanel() {
    }

    public static Vec2f renderPanel(TooltipContext ec, Vec2f tooltipPos, Point tooltipSize,
            TooltipStylePipeline pipelineOwner, int mouseX, int mouseY, int screenW, int screenH,
            boolean isTooltipOnLeft) {
        final Point panelSize = new Point(48, 48); // fixed panel size; content will scale inside
        float panelX;
        if (isTooltipOnLeft) {
            panelX = mouseX - 12 - panelSize.x;
        } else {
            panelX = mouseX + 12;
        }
        panelX = Math.max(4, Math.min(panelX, screenW - panelSize.x - 4));
        float panelY = tooltipPos.y; // align top with tooltip
        panelY = Math.max(4, Math.min(panelY, screenH - panelSize.y - 4));
        Vec2f panelPos = new Vec2f(panelX, panelY);
        ec.drawManaged(() -> {
            if (TooltipStylePipeline.renderStyleRef != null) {
                TooltipStylePipeline.renderStyleRef.renderBack(ec, panelPos, panelSize, false);
            }
            StyleManager.getInstance().getDefaultStyle().renderBack(ec, panelPos, panelSize, false);
        });
        return new Vec2f(panelPos.x + panelSize.x / 2f, panelPos.y + panelSize.y / 2f);
    }

    public static void renderPreview(DrawContext ctx, ItemStack frameStack, ItemStack previewStack, Vec2f center) {
        // Try true 3D entity rendering first
        try {
            ItemFrameGuiRenderer.render(ctx.getMatrices(), center.x, center.y, 46f, previewStack);
            return;
        } catch (Throwable ignored) {
            // Fallback to flat baked model approach
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        MatrixStack matrices = ctx.getMatrices();
        VertexConsumerProvider.Immediate buffers = ctx.getVertexConsumers();
        matrices.push();
        float topLeftX = center.x - 24f;
        float topLeftY = center.y - 24f;
        matrices.translate(topLeftX, topLeftY, 500);
        float framePixels = 44f;
        float frameScale = framePixels / 16f;
        float frameOffset = (48f - framePixels) / 2f;
        matrices.push();
        matrices.translate(frameOffset, frameOffset, 0);
        matrices.scale(frameScale, frameScale, frameScale);
        ItemStack vanillaFrame = new ItemStack(Items.ITEM_FRAME);
        BakedModel frameModel = mc.getItemRenderer().getModel(vanillaFrame, mc.world, mc.player, 0);
        mc.getItemRenderer().renderItem(vanillaFrame, ModelTransformationMode.GUI, false, matrices, buffers,
                LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, frameModel);
        matrices.pop();
        float itemPixels = 30f;
        float itemScale = itemPixels / 16f;
        float itemOffset = (48f - itemPixels) / 2f;
        matrices.push();
        matrices.translate(itemOffset, itemOffset, 50.0f);
        matrices.scale(itemScale, itemScale, itemScale);
        BakedModel itemModel = mc.getItemRenderer().getModel(previewStack, mc.world, mc.player, 0);
        mc.getItemRenderer().renderItem(previewStack, ModelTransformationMode.GUI, false, matrices, buffers,
                LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, itemModel);
        matrices.pop();
        matrices.pop();
        buffers.draw();
    }
}
