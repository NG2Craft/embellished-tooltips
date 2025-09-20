package dev.quentintyr.embellishedtooltips.client.render.item;

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
        // Translate to GUI center; push high Z so it draws above tooltip text &
        // backgrounds
        matrices.translate(x, y, 700);

        // Base scale: item frame is 16x16 logical pixels. We want it to fit in `size`
        // (≈46px used by caller)
        float baseScale = size;

        // In GUI space Y grows downward; entity renderer expects Y upward. Invert Y via
        // negative scale component.
        // Using a uniform negative on Y avoids needing the 180° X+Z flips we previously
        // used.
        matrices.scale(baseScale, -baseScale, baseScale);

        // Move pivot to center of frame block before applying our "show some depth"
        // rotations
        matrices.translate(0.5f, 0.5f, 0.5f);

        // Apply a gentle perspective so thickness & item depth are visible (tweakable
        // angles)
        // Y rotation: turn slightly so right edge foreshortens; X rotation: tilt
        // backward a bit.
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(15f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15f));

        // Return pivot back to top-left (entity model assumes origin at its center
        // after translation above)
        matrices.translate(-0.5f, -0.5f, -0.5f);
        // Compensate for perspective shift to visually center inside a 48px panel.
        matrices.translate(0.10f, 0.10f, 0f);

        // Create an item frame entity facing SOUTH (toward the viewer in our contrived
        // space)
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
