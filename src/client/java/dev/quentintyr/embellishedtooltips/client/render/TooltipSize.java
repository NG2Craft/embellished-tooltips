package dev.quentintyr.embellishedtooltips.client.render;

final class TooltipSize {
    final int contentWidth, contentHeight;

    TooltipSize(int w, int h) {
        this.contentWidth = w;
        this.contentHeight = h;
    }

    int widthWithPadding(TooltipLayout L) {
        return Math.max(contentWidth, L.leftGutter) + L.paddingX * 2;
    }

    int heightWithPadding(TooltipLayout L) {
        return L.paddingTop + contentHeight + L.paddingBottom + L.titleExtra;
    }
}
