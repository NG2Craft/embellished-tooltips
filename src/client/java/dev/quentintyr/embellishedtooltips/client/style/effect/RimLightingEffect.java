// package dev.quentintyr.embellishedtooltips.client.style.effect;

// import com.mojang.blaze3d.vertex.VertexConsumer;
// import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;
// import dev.quentintyr.embellishedtooltips.client.style.particle.LineParticle;
// import
// dev.quentintyr.embellishedtooltips.client.style.particle.TooltipParticle;
// import java.awt.Point;
// import java.util.ArrayList;
// import java.util.List;
// import net.minecraft.client.renderer.RenderType;
// import net.minecraft.world.phys.Vec2;
// import org.apache.commons.lang3.RandomUtils;
// import org.joml.Matrix4f;

// public class RimLightingEffect implements TooltipEffect {
// protected final int START;
// protected final int END;
// protected final int PARTICLE_CENTER;
// protected final int PARTICLE_EDGE;
// protected final List<TooltipParticle> particles = new ArrayList();
// protected float lastParticle = 0.0F;

// public RimLightingEffect(int start, int end, int particleCenter, int
// particleEdge) {
// this.START = start;
// this.END = end;
// this.PARTICLE_CENTER = particleCenter;
// this.PARTICLE_EDGE = particleEdge;
// }

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
