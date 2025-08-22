package dev.quentintyr.embellishedtooltips.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Context for rendering tooltips, providing convenience methods and state
 * management.
 * This is a wrapper around DrawContext that matches the original Obscure
 * Tooltips design.
 */
@Environment(EnvType.CLIENT)
public final class TooltipContext {
    private final DrawContext drawContext;
    private ItemStack stack = ItemStack.EMPTY;
    private float seconds = 0.0F;

    public TooltipContext(DrawContext drawContext) {
        this.drawContext = drawContext;
    }

    /**
     * Defines the current tooltip state.
     * 
     * @param stack   The item stack being shown
     * @param seconds Time since tooltip started showing
     */
    public void define(ItemStack stack, float seconds) {
        this.stack = stack;
        this.seconds = seconds;
    }

    /**
     * Gets the current item stack.
     */
    public ItemStack stack() {
        return this.stack;
    }

    /**
     * Gets the current time in seconds.
     */
    public float time() {
        return this.seconds;
    }

    /**
     * Gets the underlying DrawContext.
     */
    public DrawContext context() {
        return this.drawContext;
    }

    /**
     * Gets the pose/matrix stack.
     */
    public MatrixStack pose() {
        return this.drawContext.getMatrices();
    }

    /**
     * Gets the buffer source for rendering.
     */
    public VertexConsumerProvider.Immediate bufferSource() {
        return this.drawContext.getVertexConsumers();
    }

    /**
     * Renders an item with rotation and scale.
     */
    public void renderItem(Vector3f rot, Vector3f scale) {
        push(() -> {
            translate(0.0F, 0.0F, 500.0F);
            scale(scale.x, scale.y, scale.z);
            pose().multiply(new Quaternionf().rotationX(rot.x));
            pose().multiply(new Quaternionf().rotationY(rot.y));
            pose().multiply(new Quaternionf().rotationZ(rot.z));
            push(() -> {
                translate(-8.0F, -8.0F, -150.0F);
                drawContext.drawItem(this.stack, 0, 0);
            });
        });
    }

    /**
     * Fills a rectangle with a solid color.
     */
    public void fill(int x, int y, int width, int height, int color) {
        drawContext.fill(x, y, x + width, y + height, color);
    }

    /**
     * Fills a rectangle with a gradient.
     */
    public void fillGradient(int x, int y, int width, int height, int startColor, int endColor) {
        drawContext.fillGradient(x, y, x + width, y + height, startColor, endColor);
    }

    /**
     * Draws a texture.
     */
    public void blit(Identifier texture, int x, int y, int u, int v, int width, int height, int textureWidth,
            int textureHeight) {
        drawContext.drawTexture(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    /**
     * Executes code within a matrix push/pop.
     */
    public void push(Runnable runnable) {
        pose().push();
        try {
            runnable.run();
        } catch (Exception e) {
            // Silently handle exceptions like the original
        }
        pose().pop();
    }

    /**
     * Executes code with a quaternion transformation.
     */
    public void pushAndMul(Quaternionf quaternion, Runnable before, Runnable after) {
        pose().push();

        try {
            before.run();
        } catch (Exception e) {
            // Silently handle exceptions
        }

        pose().multiply(quaternion);

        try {
            after.run();
        } catch (Exception e) {
            // Silently handle exceptions
        }

        pose().pop();
    }

    /**
     * Linear interpolation between two Vec2f points.
     */
    public Vec2f lerp(Vec2f from, Vec2f to, float progress) {
        return new Vec2f(
                MathHelper.lerp(progress, from.x, to.x),
                MathHelper.lerp(progress, from.y, to.y));
    }

    /**
     * Calculates angle between two points.
     */
    public float angle(Vec2f from, Vec2f to) {
        return (float) Math.atan2(to.y - from.y, to.x - from.x);
    }

    /**
     * Flushes the rendering buffers.
     */
    public void flush() {
        bufferSource().draw();
    }

    /**
     * Gets the screen width.
     */
    public int width() {
        return drawContext.getScaledWindowWidth();
    }

    /**
     * Gets the screen height.
     */
    public int height() {
        return drawContext.getScaledWindowHeight();
    }

    /**
     * Translates the matrix stack.
     */
    public void translate(float x, float y, float z) {
        pose().translate(x, y, z);
    }

    /**
     * Scales the matrix stack.
     */
    public void scale(float x, float y, float z) {
        pose().scale(x, y, z);
    }

    /**
     * Multiplies the matrix stack with a quaternion.
     */
    public void mul(Quaternionf quaternion) {
        pose().multiply(quaternion);
    }
}
