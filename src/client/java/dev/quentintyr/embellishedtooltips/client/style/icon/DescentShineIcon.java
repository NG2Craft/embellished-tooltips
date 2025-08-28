package dev.quentintyr.embellishedtooltips.client.style.icon;

import dev.quentintyr.embellishedtooltips.client.config.ModConfig;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.style.particle.SparkleParticle;
import dev.quentintyr.embellishedtooltips.client.style.particle.TooltipParticle;
import org.joml.Vector3f;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.List;

public class DescentShineIcon implements TooltipIcon {
    protected final int CENTER_COLOR;
    protected final int START_COLOR;
    protected final int END_COLOR;
    protected final int PARTICLE_CENTER_COLOR;
    protected final int PARTICLE_EDGE_COLOR;
    protected final List<TooltipParticle> particles = new ArrayList<>();
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
        ModConfig config = ModConfig.getInstance();

        // If animations are disabled, render static
        if (!config.animations.enableAnimations) {
            context.context().drawItem(context.stack(), x, y);
            return;
        }

        float time = context.time() * config.animations.animationSpeed;

        // Primary scale animation
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

        // TODO: Implement shine effect rendering once we have proper vertex consumers
        // For now, render the item with rotation and scale

        context.push(() -> {
            // Pivot at slot center and apply rotation/scale so it lands centered
            float t = Math.min(1.0F, time * 2.0F); // 0..1 over first 0.5s
            final float rotation;
            if (config.animations.enableIconRotation) {
                if (time >= 0.5F) {
                    // Stay flat after half a second
                    rotation = 0.0F;
                } else {
                    // Rotate from 180 degrees to 0 degrees over first 0.5s with cubic easing
                    float easedT = (1.0F - (float) Math.pow((double) (1.0F - t), 3.0D)); // cubic ease out
                    rotation = 180.0F * (1.0F - easedT); // 180° -> 0°
                }
            } else {
                rotation = 0.0F; // Default flat
            }
            Vector3f rotationVec = new Vector3f(0.0F, (float) Math.toRadians(rotation), 0.0F);
            Vector3f scaleVec = new Vector3f(scale, scale, scale);

            context.translate(x + 8.0F, y + 8.0F, 0.0F);
            context.renderItem(rotationVec, scaleVec);
        });

        // Implement particle effects with config check
        if (config.animations.enableParticleEffects) {
            System.out.println("DEBUG: Particles enabled, time=" + time + ", lastParticle=" + this.lastParticle + ", diff=" + (time - this.lastParticle));
            
            if (time - this.lastParticle >= 0.1F) {
                this.lastParticle = time;
                float particleRotation = (float) (Math.random() * 6.283185307179586D);
                this.particles.add(new SparkleParticle(this.PARTICLE_CENTER_COLOR,
                        this.PARTICLE_EDGE_COLOR, 1.5F,
                        new Vec2f(x + 8.0F, y + 8.0F),
                        new Vec2f(x + 8.0F + (float) Math.cos((double) particleRotation) * 10.0F,
                                y + 8.0F + (float) Math.sin((double) particleRotation) * 10.0F)));
                System.out.println("DEBUG: Spawned particle, total particles: " + this.particles.size());
            }

            System.out.println("DEBUG: Rendering " + this.particles.size() + " particles");
            context.renderParticles(this.particles);
        }
    }

    @Override
    public void reset() {
        this.lastParticle = 0.0F;
        // TODO: Clear particles when implemented
        // this.particles.clear();
    }
}
