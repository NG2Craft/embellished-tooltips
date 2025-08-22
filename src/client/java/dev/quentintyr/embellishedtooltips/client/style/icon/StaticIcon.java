package dev.quentintyr.embellishedtooltips.client.style.icon;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;

public class StaticIcon implements TooltipIcon {
    public void render(TooltipContext context, int x, int y) {
        context.context().drawItem(context.stack(), x, y);
    }
}
