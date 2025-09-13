// package dev.quentintyr.embellishedtooltips.client.style.effect;

// import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;
// import
// dev.quentintyr.embellishedtooltips.client.style.particle.SparkleParticle;
// import
// dev.quentintyr.embellishedtooltips.client.style.particle.TooltipParticle;
// import java.awt.Point;
// import java.util.ArrayList;
// import java.util.List;
// import net.minecraft.world.phys.Vec2;

// public class TailsEffect implements TooltipEffect {
// private final List<TooltipParticle> particles = new ArrayList();
// private float lastParticle = 0.0F;

// public void render(TooltipContext context, Vec2 pos, Point size) {
// float time = context.time() * 0.5F;
// size = new Point(size.x + 6, size.y + 6);
// Vec2 start = pos.m_165908_(-3.0F);
// Vec2 tail1 = this.calculateTail(start, size, time, 0.0F);
// Vec2 tail2 = this.calculateTail(start, size, time, 0.5F);
// if (time - this.lastParticle >= 0.02F) {
// this.lastParticle = time;
// float rotation = (float) (Math.random() * 6.283185307179586D);
// float radius = 2.0F;
// this.particles.add(new SparkleParticle(-1, -40705, 1.0F, tail1,
// new Vec2(tail1.f_82470_ + (float) Math.cos((double) rotation) * 2.0F,
// tail1.f_82471_ + (float) Math.sin((double) rotation) * 2.0F)));
// this.particles.add(new SparkleParticle(-1, -40705, 1.0F, tail2,
// new Vec2(tail2.f_82470_ + (float) Math.cos((double) rotation) * 2.0F,
// tail2.f_82471_ + (float) Math.sin((double) rotation) * 2.0F)));
// }

// context.renderParticles(this.particles);
// }

// public void reset() {
// this.lastParticle = 0.0F;
// this.particles.clear();
// }

// private Vec2 calculateTail(Vec2 pos, Point size, float seconds, float offset)
// {
// float verticalMod = (float) size.y / 1.0F / (float) size.x;
// float timelapse = (seconds + offset * (2.0F + verticalMod * 2.0F)) % (2.0F +
// verticalMod * 2.0F);
// return timelapse < 1.0F ? new Vec2(pos.f_82470_ + (float) size.x * timelapse,
// pos.f_82471_)
// : (timelapse < 1.0F + verticalMod
// ? new Vec2(pos.f_82470_ + (float) size.x,
// pos.f_82471_ + (float) size.y * ((timelapse - 1.0F) / verticalMod))
// : (timelapse < 2.0F + verticalMod
// ? new Vec2(pos.f_82470_ + (float) size.x - (float) size.x * (timelapse -
// (1.0F + verticalMod)),
// pos.f_82471_ + (float) size.y)
// : new Vec2(pos.f_82470_, pos.f_82471_ + (float) size.y
// - (float) size.y * ((timelapse - (2.0F + verticalMod)) / verticalMod))));
// }

// public boolean canStackWith(TooltipEffect other) {
// return !(other instanceof TailsEffect);
// }
// }
