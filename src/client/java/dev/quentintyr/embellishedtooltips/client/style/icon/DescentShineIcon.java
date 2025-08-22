package dev.quentintyr.embellishedtooltips.client.style.icon;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
// TODO: Implement particle system
// import dev.quentintyr.embellishedtooltips.client.style.particle.SparkleParticle;
// import dev.quentintyr.embellishedtooltips.client.style.particle.TooltipParticle;
import org.joml.Vector3f;

public class DescentShineIcon implements TooltipIcon {
    protected final int CENTER_COLOR;
    protected final int START_COLOR;
    protected final int END_COLOR;
    protected final int PARTICLE_CENTER_COLOR;
    protected final int PARTICLE_EDGE_COLOR;
    // TODO: Implement particle system
    // protected final List<TooltipParticle> particles = new ArrayList<>();
    protected float lastParticle = 0.0F;

    public DescentShineIcon(int centerColor, int startColor, int endColor, int particleCenterColor,
            int particleEdgeColor) {
        this.CENTER_COLOR = centerColor;
        this.START_COLOR = startColor;
        this.END_COLOR = endColor;
        this.PARTICLE_CENTER_COLOR = particleCenterColor;
        this.PARTICLE_EDGE_COLOR = particleEdgeColor;
    }

    public void render(TooltipContext context, int x, int y) {
        float time = context.time();

        // Primary scale animation
        float scale = (double) time < 0.25D
                ? (1.0F - (float) Math.pow((double) (1.0F - time * 4.0F), 3.0D)) * 1.5F
                : ((double) time < 0.5D
                        ? 1.5F - (1.0F - (float) Math.pow((double) (1.0F - (time - 0.25F) * 4.0F), 3.0D)) * 0.25F
                        : 1.25F);

        // TODO: Implement shine effect rendering once we have proper vertex consumers
        // For now, render the item with rotation and scale

        context.push(() -> {
            context.scale(scale, scale, scale);

            // Item rotation effect
            float rotation = 360.0F * (1.0F - (float) Math.pow((double) (1.0F - Math.min(1.0F, time * 2.0F)), 3.0D));
            Vector3f rotationVec = new Vector3f(0.0F, rotation, 0.0F);
            Vector3f scaleVec = new Vector3f(1.0F, 1.0F, 1.0F);

            context.translate(x, y, 0.0F);
            context.renderItem(rotationVec, scaleVec);
        });

        // TODO: Implement particle effects once particle system is ready
        /*
         * if (time - this.lastParticle >= 0.1F) {
         * this.lastParticle = time;
         * float particleRotation = (float) (Math.random() * 6.283185307179586D);
         * this.particles.add(new SparkleParticle(this.PARTICLE_CENTER_COLOR,
         * this.PARTICLE_EDGE_COLOR, 1.5F,
         * new Vec2f(0.0F, 0.0F),
         * new Vec2f((float) Math.cos((double) particleRotation) * 10.0F,
         * (float) Math.sin((double) particleRotation) * 10.0F)));
         * }
         * 
         * context.renderParticles(this.particles);
         */
    }

    @Override
    public void reset() {
        this.lastParticle = 0.0F;
        // TODO: Clear particles when implemented
        // this.particles.clear();
    }
}
