package dev.quentintyr.embellishedtooltips.client.render;

import java.awt.Point;

final class TooltipPlacement {
    static Point place(int mouseX, int mouseY, int width, int height,
            int screenW, int screenH, boolean hasSide, int panelW, int gap) {
        int x, y;
        if (hasSide) {
            final int panelRightX = mouseX + 12;
            int posXRight = panelRightX + panelW + gap;
            x = (posXRight + width <= screenW - 4) ? posXRight : Math.max(4, mouseX - 16 - width - gap - panelW);
        } else {
            x = mouseX + 12;
            if (x + width > screenW)
                x = mouseX - 16 - width;
            x = Math.max(4, Math.min(x, screenW - width - 4));
        }
        y = mouseY - 12;
        if (y + height + 6 > screenH)
            y = screenH - height - 6;
        y = Math.max(4, Math.min(y, screenH - height - 4));
        return new Point(x, y);
    }
}
