// package dev.quentintyr.embellishedtooltips.client.style.icon;

// import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;

// public class DescentSimpleIcon implements TooltipIcon {
// public void render(TooltipContext context, int x, int y) {
// float scale = (double) context.time() < 0.25D
// ? (1.0F - (float) Math.pow((double) (1.0F - context.time() * 4.0F), 3.0D)) *
// 1.5F
// : ((double) context.time() < 0.5D
// ? 1.5F - (1.0F - (float) Math.pow((double) (1.0F - (context.time() - 0.25F) *
// 4.0F), 3.0D)) * 0.25F
// : 1.25F);
// context.scale(scale, scale, scale);
// context.context().m_280480_(context.stack(), x, y);
// }
// }
