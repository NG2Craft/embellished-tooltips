package dev.quentintyr.embellishedtooltips.client.render;

import java.awt.Point;

final class TooltipPlacement {

    static class PlacementResult {
        public final Point position;
        public final boolean isTooltipOnLeft;

        PlacementResult(Point position, boolean isTooltipOnLeft) {
            this.position = position;
            this.isTooltipOnLeft = isTooltipOnLeft;
        }
    }

    static PlacementResult placeWithSideInfo(int mouseX, int mouseY, int width, int height,
            int screenW, int screenH, boolean hasSide, int panelW, int gap) {
        int x, y;
        boolean isTooltipOnLeft = false;

        if (hasSide) {
            // Panel positioning: when tooltip on right, panel is at mouseX + 12
            // When tooltip on left, panel is at mouseX - 12 - panelW
            int panelLeftX = mouseX + 12;  // Default: panel to right of cursor
            int panelRightX = panelLeftX + panelW;
            int tooltipRightX = panelRightX + gap; // 3 pixel gap between panel and tooltip
            
            if (tooltipRightX + width <= screenW - 4) {
                // Tooltip fits on right side of panel
                x = tooltipRightX;
                isTooltipOnLeft = false;
            } else {
                // No room on right, move both panel and tooltip to left side of cursor
                panelLeftX = mouseX - 12 - panelW;  // Panel to left of cursor
                int tooltipLeftX = panelLeftX - gap - width; // Tooltip to left of panel
                x = Math.max(4, tooltipLeftX);
                isTooltipOnLeft = true;
            }
        } else {
            x = mouseX + 12;
            if (x + width > screenW) {
                x = mouseX - 16 - width;
                isTooltipOnLeft = true;
            }
            x = Math.max(4, Math.min(x, screenW - width - 4));
        }
        y = mouseY - 12; // Standard tooltip offset
        if (y + height + 6 > screenH)
            y = screenH - height - 6;
        y = Math.max(4, Math.min(y, screenH - height - 4));

        return new PlacementResult(new Point(x, y), isTooltipOnLeft);
    }

    static Point place(int mouseX, int mouseY, int width, int height,
            int screenW, int screenH, boolean hasSide, int panelW, int gap) {
        return placeWithSideInfo(mouseX, mouseY, width, height, screenW, screenH, hasSide, panelW, gap).position;
    }
}
