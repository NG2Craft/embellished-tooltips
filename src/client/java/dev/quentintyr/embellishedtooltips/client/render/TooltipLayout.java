package dev.quentintyr.embellishedtooltips.client.render;

final class TooltipLayout {
    final int leftGutter, paddingX, paddingTop, paddingBottom, lineGap, titleExtra;
    final int firstLineYOffset, firstLineXOffset;

    private TooltipLayout(int leftGutter, int paddingX, int paddingTop, int paddingBottom,
            int lineGap, int titleExtra, int firstLineYOffset, int firstLineXOffset) {
        this.leftGutter = leftGutter;
        this.paddingX = paddingX;
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
        this.lineGap = lineGap;
        this.titleExtra = titleExtra;
        this.firstLineYOffset = firstLineYOffset;
        this.firstLineXOffset = firstLineXOffset;
    }

    static TooltipLayout defaults() {
        return new TooltipLayout(21, 6, 4, 5, 2, 13, 3, 3);
    }
}
