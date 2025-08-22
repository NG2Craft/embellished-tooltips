package dev.quentintyr.embellishedtooltips.client.style.icon;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import org.joml.Vector3f;

public class DescentComplexIcon implements TooltipIcon {
    public void render(TooltipContext context, int x, int y) {
        float time = context.time();

        // Scale animation similar to DescentSimpleIcon but more complex
        float scale = (double) time < 0.25D
                ? (1.0F - (float) Math.pow((double) (1.0F - time * 4.0F), 3.0D)) * 1.5F
                : ((double) time < 0.5D
                        ? 1.5F - (1.0F - (float) Math.pow((double) (1.0F - (time - 0.25F) * 4.0F), 3.0D)) * 0.25F
                        : 1.25F);

        // Rotation animation - starts at 180 degrees and rotates to 360
        float rotation = 180.0F
                + 180.0F * (1.0F - (float) Math.pow((double) (1.0F - Math.min(1.0F, time * 2.0F)), 3.0D));

        Vector3f rotationVec = new Vector3f(0.0F, rotation, 0.0F);
        Vector3f scaleVec = new Vector3f(scale, scale, scale);

        context.push(() -> {
            context.translate(x, y, 0.0F);
            context.renderItem(rotationVec, scaleVec);
        });
    }
}
