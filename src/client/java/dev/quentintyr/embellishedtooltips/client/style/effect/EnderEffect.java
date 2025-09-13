package dev.quentintyr.embellishedtooltips.client.style.effect;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.style.Effects;
// TODO: Implement particle system
// import dev.quentintyr.embellishedtooltips.client.style.particle.EnderParticle;
// import dev.quentintyr.embellishedtooltips.client.style.particle.TooltipParticle;
import java.awt.Point;
import net.minecraft.util.math.Vec2f;

public class EnderEffect implements TooltipEffect {
    protected final int CENTER_COLOR;
    protected final int EDGE_COLOR;
    // TODO: Implement particle system
    // protected final List<TooltipParticle> particles = new ArrayList<>();
    protected float lastParticle = -0.2F;

    public EnderEffect(int centerColor, int edgeColor) {
        this.CENTER_COLOR = centerColor;
        this.EDGE_COLOR = edgeColor;
    }

    @Override
    public Effects getLayer() {
        return Effects.FRONT;
    }

    @Override
    public void render(TooltipContext context, Vec2f pos, Point size, boolean slot) {
        float time = context.time();

        // Simplified ender effect - create a swirling dark effect around the tooltip
        int x = (int) pos.x;
        int y = (int) pos.y;

        context.push(() -> {
            context.translate(0.0F, 0.0F, 600.0F); // Render on top

            // Create a subtle animated border effect
            float swirl = time * 2.0F;
            float alpha = 0.4F + 0.3F * (float) Math.sin(time * 4.0F);

            // Draw some simple "ender" particles as rectangles around the tooltip
            for (int i = 0; i < 8; i++) {
                float angle = (float) (i * Math.PI / 4.0) + swirl;
                float distance = 15.0F + 5.0F * (float) Math.sin(time * 3.0F + i);

                float px = x + size.x / 2.0F + (float) Math.cos(angle) * distance;
                float py = y + size.y / 2.0F + (float) Math.sin(angle) * distance;

                int color = mixColor(CENTER_COLOR, EDGE_COLOR, alpha);
                context.fill((int) px - 1, (int) py - 1, 2, 2, color);
            }
        });

        // TODO: Implement particle effects once particle system is ready
        /*
         * if (time - this.lastParticle >= 0.25F) {
         * this.lastParticle = time;
         * Vec2f center = new Vec2f(pos.x + 13.0F, pos.y + 13.0F);
         * this.particles.add(new EnderParticle(this.CENTER_COLOR, this.EDGE_COLOR,
         * 3.0F, center, 13.0F));
         * }
         * context.renderParticles(this.particles);
         */
    }

    /**
     * Mixes two colors with the given blend factor.
     */
    private int mixColor(int color1, int color2, float blend) {
        blend = Math.max(0.0F, Math.min(1.0F, blend));

        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * blend);
        int r = (int) (r1 + (r2 - r1) * blend);
        int g = (int) (g1 + (g2 - g1) * blend);
        int b = (int) (b1 + (b2 - b1) * blend);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void reset() {
        this.lastParticle = -0.2F;
        // TODO: Clear particles when implemented
        // this.particles.clear();
    }
}
// }

// context.renderParticles(this.particles);
// }

// public void reset() {
// this.lastParticle = -0.2F;
// this.particles.clear();
// }

// public Effects.Order order() {
// return Effects.Order.LAYER_5_FRONT;
// }

// public boolean canStackWith(TooltipEffect other) {
// return !(other instanceof EnderEffect);
// }
// }
