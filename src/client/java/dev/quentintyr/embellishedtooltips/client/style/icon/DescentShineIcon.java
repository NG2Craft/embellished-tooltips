// package dev.quentintyr.embellishedtooltips.client.style.icon;

// import com.mojang.blaze3d.vertex.VertexConsumer;
// import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;
// import
// dev.quentintyr.embellishedtooltips.client.style.particle.SparkleParticle;
// import
// dev.quentintyr.embellishedtooltips.client.style.particle.TooltipParticle;
// import java.util.ArrayList;
// import java.util.List;
// import net.minecraft.client.renderer.RenderType;
// import net.minecraft.world.phys.Vec2;
// import org.joml.Matrix4f;
// import org.joml.Vector2f;
// import org.joml.Vector3f;

// public class DescentShineIcon implements TooltipIcon {
// protected final int CENTER_COLOR;
// protected final int START_COLOR;
// protected final int END_COLOR;
// protected final int PARTICLE_CENTER_COLOR;
// protected final int PARTICLE_EDGE_COLOR;
// protected final List<TooltipParticle> particles = new ArrayList();
// protected float lastParticle = 0.0F;

// public DescentShineIcon(int centerColor, int startColor, int endColor, int
// particleCenterColor,
// int particleEdgeColor) {
// this.CENTER_COLOR = centerColor;
// this.START_COLOR = startColor;
// this.END_COLOR = endColor;
// this.PARTICLE_CENTER_COLOR = particleCenterColor;
// this.PARTICLE_EDGE_COLOR = particleEdgeColor;
// }

// public void render(TooltipContext context, int x, int y) {
// float scale = (double) context.time() < 0.25D
// ? (1.0F - (float) Math.pow((double) (1.0F - context.time() * 4.0F), 3.0D)) *
// 1.5F
// : ((double) context.time() < 0.5D
// ? 1.5F - (1.0F - (float) Math.pow((double) (1.0F - (context.time() - 0.25F) *
// 4.0F), 3.0D)) * 0.25F
// : 1.25F);
// float scale2 = (double) (context.time() * 0.5F) < 0.25D
// ? (1.0F - (float) Math.pow((double) (1.0F - context.time() * 0.5F * 4.0F),
// 3.0D)) * 1.5F
// : ((double) (context.time() * 0.5F) < 0.5D ? 1.5F
// - (1.0F - (float) Math.pow((double) (1.0F - (context.time() * 0.5F - 0.25F) *
// 4.0F), 3.0D)) * 0.25F
// : 1.25F);
// context.pose().m_85841_(scale, scale, scale);
// float centerX = (float) (x + 8);
// float centerY = (float) (y + 8);
// float dist1 = 8.0F + (float) Math.pow((double) scale2, 6.0D);
// float dist2 = (float) Math.pow((double) scale2, 6.0D) * 1.3F;
// float mod = context.time() < 1.0F ? (1.0F - context.time()) * 0.03F : 0.0F;

// float rotation;
// double d1;
// double d2;
// Vector2f first;
// Vector2f second;
// Matrix4f matrix4f;
// VertexConsumer vertexconsumer;
// for (rotation = 0.0F; rotation < 2.0F; rotation += 0.2F) {
// d1 = 3.141592653589793D * (double) (rotation + 0.1F);
// d2 = 3.141592653589793D * (double) (rotation - 0.1F);
// first = new Vector2f((float) (Math.cos(d1) * (double) dist2), (float)
// (Math.sin(d1) * (double) dist2));
// second = new Vector2f((float) (Math.cos(d2) * (double) dist2), (float)
// (Math.sin(d2) * (double) dist2));
// matrix4f = context.pose().m_85850_().m_252922_();
// vertexconsumer = context.bufferSource().m_6299_(RenderType.m_286086_());
// vertexconsumer.m_252986_(matrix4f, centerX, centerY,
// 0.0F).m_193479_(this.START_COLOR).m_5752_();
// vertexconsumer.m_252986_(matrix4f, centerX, centerY,
// 0.0F).m_193479_(this.START_COLOR).m_5752_();
// vertexconsumer.m_252986_(matrix4f, first.x, first.y,
// 0.0F).m_193479_(this.END_COLOR).m_5752_();
// vertexconsumer.m_252986_(matrix4f, second.x, second.y,
// 0.0F).m_193479_(this.END_COLOR).m_5752_();
// }

// for (rotation = 0.0F; rotation < 2.0F; rotation += 0.2F) {
// d1 = (double) context.time() + 3.141592653589793D * (double) (rotation +
// 0.05F + mod);
// d2 = (double) context.time() + 3.141592653589793D * (double) (rotation -
// 0.05F - mod);
// first = new Vector2f((float) (Math.cos(d1) * (double) dist1), (float)
// (Math.sin(d1) * (double) dist1));
// second = new Vector2f((float) (Math.cos(d2) * (double) dist1), (float)
// (Math.sin(d2) * (double) dist1));
// matrix4f = context.pose().m_85850_().m_252922_();
// vertexconsumer = context.bufferSource().m_6299_(RenderType.m_286086_());
// vertexconsumer.m_252986_(matrix4f, centerX, centerY,
// 0.0F).m_193479_(this.CENTER_COLOR).m_5752_();
// vertexconsumer.m_252986_(matrix4f, centerX, centerY,
// 0.0F).m_193479_(this.CENTER_COLOR).m_5752_();
// vertexconsumer.m_252986_(matrix4f, first.x, first.y,
// 0.0F).m_193479_(this.END_COLOR).m_5752_();
// vertexconsumer.m_252986_(matrix4f, second.x, second.y,
// 0.0F).m_193479_(this.END_COLOR).m_5752_();
// }

// if (context.time() - this.lastParticle >= 0.1F) {
// this.lastParticle = context.time();
// rotation = (float) (Math.random() * 6.283185307179586D);
// this.particles.add(new SparkleParticle(this.PARTICLE_CENTER_COLOR,
// this.PARTICLE_EDGE_COLOR, 1.5F,
// new Vec2(0.0F, 0.0F),
// new Vec2((float) Math.cos((double) rotation) * 10.0F, (float)
// Math.sin((double) rotation) * 10.0F)));
// }

// context.renderParticles(this.particles);
// context.renderItem(new Vector3f(0.0F,
// 360.0F * (1.0F - (float) Math.pow((double) (1.0F - Math.min(1.0F,
// context.time() * 2.0F)), 3.0D)), 0.0F),
// new Vector3f(1.0F));
// }

// public void reset() {
// this.lastParticle = 0.0F;
// this.particles.clear();
// }
// }
