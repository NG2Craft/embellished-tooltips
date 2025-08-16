// package dev.quentintyr.embellishedtooltips.client.style.particle;

// import com.mojang.math.Axis;
// import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;
// import net.minecraft.util.Mth;
// import net.minecraft.world.phys.Vec2;

// public class SparkleParticle extends TooltipParticle {
// protected final int CENTER_COLOR;
// protected final int EDGE_COLOR;
// protected Vec2 end;

// public SparkleParticle(int centerColor, int edgeColor, float lifetime, Vec2
// start, Vec2 end) {
// super(lifetime);
// this.CENTER_COLOR = centerColor;
// this.EDGE_COLOR = edgeColor;
// this.position = start;
// this.end = end;
// }

// public void renderParticle(TooltipContext context, float lifetime) {
// float mod = 1.0F - (float) Math.pow((double) (1.0F - lifetime /
// this.MAX_LIFETIME), 3.0D);
// float scale = mod < 0.5F ? mod * 2.0F : (mod < 0.8F ? 1.0F : 1.0F - (mod -
// 0.8F) / 0.2F);
// context.push(() -> {
// context.translate(Mth.m_14179_(mod, this.position.f_82470_,
// this.end.f_82470_),
// Mth.m_14179_(mod, this.position.f_82471_, this.end.f_82471_), 0.0F);
// context.scale(scale, scale, scale);
// context.mul(Axis.f_252403_.m_252961_((float) Math.pow((double) lifetime,
// 4.0D)));
// context.push(() -> {
// context.translate(-0.5F, 0.5F, 0.0F);
// context.fill(0, 0, 1, 1, this.CENTER_COLOR);
// context.fill(-1, 0, 1, 1, this.EDGE_COLOR);
// context.fill(1, 0, 1, 1, this.EDGE_COLOR);
// context.fill(0, -1, 1, 1, this.EDGE_COLOR);
// context.fill(0, 1, 1, 1, this.EDGE_COLOR);
// });
// });
// }
// }
