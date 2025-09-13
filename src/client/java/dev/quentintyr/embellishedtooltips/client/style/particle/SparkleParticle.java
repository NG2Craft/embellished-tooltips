package dev.quentintyr.embellishedtooltips.client.style.particle;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.joml.Quaternionf;

public class SparkleParticle extends TooltipParticle {
    protected final int CENTER_COLOR;
    protected final int EDGE_COLOR;
    protected Vec2f end;

    public SparkleParticle(int centerColor, int edgeColor, float lifetime, Vec2f start, Vec2f end) {
        super(lifetime);
        this.CENTER_COLOR = centerColor;
        this.EDGE_COLOR = edgeColor;
        this.position = start;
        this.end = end;
    }

    @Override
    public void renderParticle(TooltipContext context, float time) {
        float progress = time / this.MAX_LIFETIME;
        if (progress > 1.0F)
            return;

        // Cubic ease out for smooth motion
        float mod = 1.0F - (float) Math.pow((double) (1.0F - progress), 3.0D);

        // Scale animation: start small, grow to full, then shrink at end
        float scale = mod < 0.5F ? mod * 2.0F : (mod < 0.8F ? 1.0F : 1.0F - (mod - 0.8F) / 0.2F);

        context.push(() -> {
            // Interpolate position from start to end
            float x = MathHelper.lerp(mod, this.position.x, this.end.x);
            float y = MathHelper.lerp(mod, this.position.y, this.end.y);

            context.translate(x, y, 0.0F);
            context.scale(scale, scale, scale);

            // Add some rotation based on time
            float rotation = (float) Math.pow((double) time, 4.0D);
            context.mul(new Quaternionf().rotationZ(rotation));

            context.push(() -> {
                // Render sparkle as a small cross shape
                context.translate(-0.5F, -0.5F, 0.0F);

                // Center pixel
                context.fill(0, 0, 1, 1, this.CENTER_COLOR);

                // Cross arms
                context.fill(-1, 0, 1, 1, this.EDGE_COLOR);
                context.fill(1, 0, 1, 1, this.EDGE_COLOR);
                context.fill(0, -1, 1, 1, this.EDGE_COLOR);
                context.fill(0, 1, 1, 1, this.EDGE_COLOR);
            });
        });
    }
}
