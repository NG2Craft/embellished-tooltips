package dev.quentintyr.embellishedtooltips.client.style.icon;

import dev.quentintyr.embellishedtooltips.client.config.ModConfig;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;

public class DescentSimpleIcon implements TooltipIcon {
    public void render(TooltipContext context, int x, int y) {
        ModConfig config = ModConfig.getInstance();

        // If animations are disabled, render static
        if (!config.animations.enableAnimations) {
            context.context().drawItem(context.stack(), x, y);
            return;
        }

        float time = context.time() * config.animations.animationSpeed;

        // Scale animation
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

        // Centered scale around slot center
        context.push(() -> {
            context.translate(x + 8.0F, y + 8.0F, 0.0F);
            context.renderItem(new org.joml.Vector3f(0.0F, 0.0F, 0.0F), new org.joml.Vector3f(scale, scale, scale));
        });
    }
}
