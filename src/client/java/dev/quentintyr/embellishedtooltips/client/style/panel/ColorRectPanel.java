// package dev.quentintyr.embellishedtooltips.client.style.panel;

// import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;
// import java.awt.Point;
// import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
// import net.minecraft.world.phys.Vec2;

// public class ColorRectPanel implements TooltipPanel {
// private final int BACK_TOP;
// private final int BACK_BOTTOM;
// private final int BORDER_TOP;
// private final int BORDER_BOTTOM;
// private final int SLOT;

// public ColorRectPanel(int backTop, int backBottom, int borderTop, int
// borderBottom, int slot) {
// this.BACK_TOP = backTop;
// this.BACK_BOTTOM = backBottom;
// this.BORDER_TOP = borderTop;
// this.BORDER_BOTTOM = borderBottom;
// this.SLOT = slot;
// }

// public void render(TooltipContext context, Vec2 pos, Point size, boolean
// slot) {
// int x = (int) pos.f_82470_;
// int y = (int) pos.f_82471_;
// TooltipRenderUtil.renderTooltipBackground(context.context(), x, y, size.x,
// size.y, 400, this.BACK_TOP,
// this.BACK_BOTTOM, this.BORDER_TOP, this.BORDER_BOTTOM);
// if (slot) {
// context.push(() -> {
// context.translate(0.0F, 0.0F, 400.0F);
// context.fillGradient(x + 2, y + 1, 20, 1, this.SLOT, this.SLOT);
// context.fillGradient(x + 1, y + 2, 22, 20, this.SLOT, this.SLOT);
// context.fillGradient(x + 2, y + 22, 20, 1, this.SLOT, this.SLOT);
// });
// }
// }
// }
