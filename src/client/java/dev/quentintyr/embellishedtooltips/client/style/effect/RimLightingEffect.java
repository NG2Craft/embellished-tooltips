package dev.quentintyr.embellishedtooltips.client.style.effect;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.style.Effects;
// TODO: Implement particle system
// import dev.quentintyr.embellishedtooltips.client.style.particle.LineParticle;
// import dev.quentintyr.embellishedtooltips.client.style.particle.TooltipParticle;
import java.awt.Point;
import net.minecraft.util.math.Vec2f;

public class RimLightingEffect implements TooltipEffect {
    protected final int START;
    protected final int END;
    protected final int PARTICLE_CENTER;
    protected final int PARTICLE_EDGE;
    // TODO: Implement particle system
    // protected final List<TooltipParticle> particles = new ArrayList<>();
    protected float lastParticle = 0.0F;

    public RimLightingEffect(int start, int end, int particleCenter, int particleEdge) {
        this.START = start;
        this.END = end;
        this.PARTICLE_CENTER = particleCenter;
        this.PARTICLE_EDGE = particleEdge;
    }

    @Override
    public Effects getLayer() {
        return Effects.FRAME;
    }

    @Override
    public void render(TooltipContext context, Vec2f pos, Point size, boolean slot) {
        // Simplified rim lighting effect - just render a glowing border
        int x = (int) pos.x;
        int y = (int) pos.y;
        int width = size.x;
        int height = size.y;

        float time = context.time();
        float alpha = 0.3F + 0.2F * (float) Math.sin(time * 3.0F); // Pulsing effect

        // Create a glowing border effect by rendering multiple layers
        context.push(() -> {
            context.translate(0.0F, 0.0F, 450.0F); // Render above the frame

            // Outer glow
            int outerColor = mixColor(START, END, alpha * 0.5F);
            context.fill(x - 2, y - 2, width + 4, 1, outerColor); // Top
            context.fill(x - 2, y + height + 1, width + 4, 1, outerColor); // Bottom
            context.fill(x - 2, y - 1, 1, height + 2, outerColor); // Left
            context.fill(x + width + 1, y - 1, 1, height + 2, outerColor); // Right

            // Inner glow
            int innerColor = mixColor(START, END, alpha);
            context.fill(x - 1, y - 1, width + 2, 1, innerColor); // Top
            context.fill(x - 1, y + height, width + 2, 1, innerColor); // Bottom
            context.fill(x - 1, y, 1, height, innerColor); // Left
            context.fill(x + width, y, 1, height, innerColor); // Right
        });

        // TODO: Implement particle effects once particle system is ready
        /*
         * if (time - this.lastParticle >= 0.1F) {
         * this.lastParticle = time;
         * // Add rim particles
         * this.particles.add(new LineParticle(this.PARTICLE_CENTER, this.PARTICLE_EDGE,
         * new Vec2f(x, y), new Vec2f(x + width, y))); // Top edge
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
        this.lastParticle = 0.0F;
        // TODO: Clear particles when implemented
        // this.particles.clear();
    }
}

// public void render(TooltipContext context, Vec2 pos, Point size) {
// size = new Point(size.x + 8, size.y + 8);
// Vec2 start = pos.m_165908_(-4.0F);
// float width = 10.0F + 5.0F * (float) Math.cos((double) context.time());
// Matrix4f matrix4f = context.pose().m_85850_().m_252922_();
// VertexConsumer buffer =
// context.bufferSource().m_6299_(RenderType.m_286086_());
// buffer.m_252986_(matrix4f, start.f_82470_, start.f_82471_,
// 0.0F).m_193479_(this.START).m_5752_();
// buffer.m_252986_(matrix4f, start.f_82470_, start.f_82471_ + (float) size.y,
// 0.0F).m_193479_(this.START).m_5752_();
// buffer.m_252986_(matrix4f, start.f_82470_ + width, start.f_82471_ + (float)
// size.y - width, 0.0F)
// .m_193479_(this.END).m_5752_();
// buffer.m_252986_(matrix4f, start.f_82470_ + width, start.f_82471_ + width,
// 0.0F).m_193479_(this.END).m_5752_();
// buffer = context.bufferSource().m_6299_(RenderType.m_286086_());
// buffer.m_252986_(matrix4f, start.f_82470_, start.f_82471_,
// 0.0F).m_193479_(this.START).m_5752_();
// buffer.m_252986_(matrix4f, start.f_82470_ + width, start.f_82471_ + width,
// 0.0F).m_193479_(this.END).m_5752_();
// buffer.m_252986_(matrix4f, start.f_82470_ + (float) size.x - width,
// start.f_82471_ + width, 0.0F)
// .m_193479_(this.END).m_5752_();
// buffer.m_252986_(matrix4f, start.f_82470_ + (float) size.x, start.f_82471_,
// 0.0F).m_193479_(this.START).m_5752_();
// buffer = context.bufferSource().m_6299_(RenderType.m_286086_());
// buffer.m_252986_(matrix4f, start.f_82470_ + (float) size.x - width,
// start.f_82471_ + width, 0.0F)
// .m_193479_(this.END).m_5752_();
// buffer.m_252986_(matrix4f, start.f_82470_ + (float) size.x - width,
// start.f_82471_ + (float) size.y - width, 0.0F)
// .m_193479_(this.END).m_5752_();
// buffer.m_252986_(matrix4f, start.f_82470_ + (float) size.x, start.f_82471_ +
// (float) size.y, 0.0F)
// .m_193479_(this.START).m_5752_();
// buffer.m_252986_(matrix4f, start.f_82470_ + (float) size.x, start.f_82471_,
// 0.0F).m_193479_(this.START).m_5752_();
// buffer = context.bufferSource().m_6299_(RenderType.m_286086_());
// buffer.m_252986_(matrix4f, start.f_82470_ + width, start.f_82471_ + (float)
// size.y - width, 0.0F)
// .m_193479_(this.END).m_5752_();
// buffer.m_252986_(matrix4f, start.f_82470_, start.f_82471_ + (float) size.y,
// 0.0F).m_193479_(this.START).m_5752_();
// buffer.m_252986_(matrix4f, start.f_82470_ + (float) size.x, start.f_82471_ +
// (float) size.y, 0.0F)
// .m_193479_(this.START).m_5752_();
// buffer.m_252986_(matrix4f, start.f_82470_ + (float) size.x - width,
// start.f_82471_ + (float) size.y - width, 0.0F)
// .m_193479_(this.END).m_5752_();
// if (context.time() - this.lastParticle >= 0.1F) {
// this.lastParticle = context.time();
// Vec2 center = new Vec2(start.f_82470_ + (float) size.x * 0.5F, start.f_82471_
// + (float) size.y * 0.5F);
// int edge = RandomUtils.nextInt(1, 5);
// float mod = RandomUtils.nextFloat(0.0F, 1.0F);
// Vec2 var10000;
// switch (edge) {
// case 1:
// var10000 = new Vec2(start.f_82470_, start.f_82471_ + (float) size.y * mod);
// break;
// case 2:
// var10000 = new Vec2(start.f_82470_ + (float) size.x, start.f_82471_ + (float)
// size.y * mod);
// break;
// case 3:
// var10000 = new Vec2(start.f_82470_ + (float) size.y * mod, start.f_82471_);
// break;
// default:
// var10000 = new Vec2(start.f_82470_ + (float) size.y * mod, start.f_82471_ +
// (float) size.y);
// }

// Vec2 from = var10000;
// this.particles.add(new LineParticle(this.PARTICLE_CENTER, this.PARTICLE_EDGE,
// 1.0F, from,
// context.lerp(from, center, 0.25F)));
// }

// context.renderParticles(this.particles);
// }

// public void reset() {
// this.lastParticle = 0.0F;
// this.particles.clear();
// }

// public boolean canStackWith(TooltipEffect other) {
// return !(other instanceof RimLightingEffect);
// }
// }
