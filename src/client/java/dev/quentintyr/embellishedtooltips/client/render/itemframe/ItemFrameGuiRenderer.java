package dev.quentintyr.embellishedtooltips.client.render.itemframe;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

/**
 * Renders a real ItemFrameEntity (with depth & model thickness) in GUI space.
 * Attempts to emulate in-world appearance while ignoring player camera.
 */
public final class ItemFrameGuiRenderer {
    private ItemFrameGuiRenderer() {
    }

    /**
     * Render a 3D item frame entity with the supplied contained item at panel
     * center.
     *
     * @param matrices  current matrix stack
     * @param x         center screen x (GUI pixels)
     * @param y         center screen y (GUI pixels)
     * @param size      target frame outer size in pixels (approximate)
     * @param contained item stack to display in frame
     */
    public static void render(MatrixStack matrices, float x, float y, float size, ItemStack contained) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null)
            return;
        matrices.push();
        matrices.translate(x, y, 700); // draw above tooltip text layers

        float baseScale = size; // frame logical size is 16
        matrices.scale(baseScale, -baseScale, baseScale); // invert Y for GUI space

        matrices.translate(0.5f, 0.5f, 0.5f); // pivot to center
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(32f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15f));
        matrices.translate(-0.5f, -0.5f, -0.5f); // restore pivot
        matrices.translate(-0.03f, 0.04f, 0f); // empirical recenter after perspective

        ItemFrameEntity frame = new ItemFrameEntity(mc.world, mc.player.getBlockPos(), Direction.SOUTH);
        frame.setHeldItemStack(contained, false);

        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        try {
            mc.getEntityRenderDispatcher().render(frame, 0, 0, 0, 0f, 1f, matrices, immediate,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE);
        } catch (Throwable ignored) {
        }
        immediate.draw();
        matrices.pop();
    }
}
