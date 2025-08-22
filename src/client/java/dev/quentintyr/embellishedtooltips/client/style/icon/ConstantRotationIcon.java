package dev.quentintyr.embellishedtooltips.client.style.icon;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import org.joml.Vector3f;

public class ConstantRotationIcon implements TooltipIcon {
    public void render(TooltipContext context, int x, int y) {
        float rotation = context.time() * 180.0F; // Rotate 180 degrees per second
        Vector3f rotationVec = new Vector3f(0.0F, rotation, 0.0F);
        Vector3f scale = new Vector3f(1.0F, 1.0F, 1.0F);

        context.push(() -> {
            context.translate(x, y, 0.0F);
            context.renderItem(rotationVec, scale);
        });
    }
}
