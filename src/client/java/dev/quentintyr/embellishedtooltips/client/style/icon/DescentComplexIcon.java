package dev.quentintyr.embellishedtooltips.client.style.icon;

import dev.quentintyr.embellishedtooltips.client.config.ModConfig;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import org.joml.Vector3f;

public class DescentComplexIcon implements TooltipIcon {
    @Override
    public void render(TooltipContext context, int x, int y) {
        ModConfig config = ModConfig.getInstance();

        // If animations are disabled, render static
        if (!config.animations.enableAnimations) {
            context.context().drawItem(context.stack(), x, y);
            return;
        }

        float time = context.time() * config.animations.animationSpeed;

        // Same scale animation as DescentSimpleIcon and DescentShineIcon
        final float scale;
        if (config.animations.enableIconScaling) {
            scale = (double) time < 0.25D
                    ? (1.0F - (float) Math.pow((double) (1.0F - time * 4.0F), 3.0D)) * 1.5F
                    : ((double) time < 0.5D
                            ? 1.5F - (1.0F - (float) Math.pow((double) (1.0F - (time - 0.25F) * 4.0F), 3.0D)) * 0.25F
                            : 1.25F);
        } else {
            scale = 1.25F; // Default scale
        }

        // Rotation animation with cubic easing - start upside down and rotate to flat
        final float rotation;
        if (config.animations.enableIconRotation) {
            if (time >= 0.5F) {
                // Stay flat after half a second
                rotation = 0.0F;
            } else {
                // Rotate from 180 degrees to 0 degrees over first 0.5s with cubic easing
                float t = Math.min(1.0F, time * 2.0F); // 0..1 over first 0.5s
                float easedT = (1.0F - (float) Math.pow((double) (1.0F - t), 3.0D)); // cubic ease out
                rotation = 180.0F * (1.0F - easedT); // 180° -> 0°
            }
        } else {
            rotation = 0.0F; // Default flat
        }

        context.push(() -> {
            // Centered transformation like other animated icons
            context.translate(x + 8.0F, y + 8.0F, 0.0F);

            Vector3f rotationVec = new Vector3f(0.0F, (float) Math.toRadians(rotation), 0.0F);
            Vector3f scaleVec = new Vector3f(scale, scale, scale);

            context.renderItem(rotationVec, scaleVec);
        });
    }
}
