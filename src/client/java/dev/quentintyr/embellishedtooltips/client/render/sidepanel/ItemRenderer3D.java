package dev.quentintyr.embellishedtooltips.client.render.sidepanel;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import org.joml.Quaternionf;

/**
 * Handles 3D rendering of items in side panels (spinning tools, weapons, etc.)
 */
public class ItemRenderer3D {
    
    /**
     * Renders a spinning item in the center of the side panel
     */
    public static void renderSpinningItem(DrawContext ctx, ItemStack stack, Vec2f center) {
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        
        // Center the item at the panel center
        ms.translate(center.x, center.y, 500.0F);
        
        // Apply 3D transformations for a nice presentation
        ms.scale(2.75f, 2.75f, 2.75f);
        ms.multiply(new Quaternionf().rotationX((float) Math.toRadians(-30.0f)));
        
        // Spinning animation based on time
        float spin = (float) (((System.currentTimeMillis() / 1000.0) % 360.0) * Math.toRadians(-20.0));
        ms.multiply(new Quaternionf().rotationY(spin));
        ms.multiply(new Quaternionf().rotationZ((float) Math.toRadians(-45.0f)));
        
        // Draw the item at the transformed position
        ms.push();
        ms.translate(-8.0F, -8.0F, -150.0F);
        ctx.drawItem(stack, 0, 0);
        ms.pop();
        
        ms.pop();
    }
    
    /**
     * Renders a static item (useful for items that shouldn't spin)
     */
    public static void renderStaticItem(DrawContext ctx, ItemStack stack, Vec2f center) {
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        
        ms.translate(center.x, center.y, 500.0F);
        ms.scale(2.0f, 2.0f, 2.0f);
        
        ms.push();
        ms.translate(-8.0F, -8.0F, -100.0F);
        ctx.drawItem(stack, 0, 0);
        ms.pop();
        
        ms.pop();
    }
}
