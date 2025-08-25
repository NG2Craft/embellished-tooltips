package dev.quentintyr.embellishedtooltips.client.render;

import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.style.TooltipStyle;
import net.minecraft.util.math.Vec2f;

import java.awt.Point;

final class TooltipStylePipeline {
    static TooltipStyle renderStyleRef;

    static void renderBackLayers(dev.quentintyr.embellishedtooltips.client.render.TooltipContext ec, Vec2f pos,
            Point size) {
        ec.drawManaged(() -> StyleManager.getInstance().getDefaultStyle().renderBack(ec, pos, size, true));
        TooltipStyle s = renderStyleRef;
        if (s != null) {
            ec.drawManaged(() -> s.renderEffects(dev.quentintyr.embellishedtooltips.client.style.Effects.BACKGROUND, ec,
                    pos, size));
            ec.drawManaged(() -> s.renderBack(ec, pos, size, true));
        }
    }

    static void renderBetweenTextEffects(dev.quentintyr.embellishedtooltips.client.render.TooltipContext ec, Vec2f pos,
            Point size) {
        TooltipStyle s = renderStyleRef;
        if (s != null) {
            ec.drawManaged(() -> s.renderEffects(
                    dev.quentintyr.embellishedtooltips.client.style.Effects.TEXT_BACKGROUND, ec, pos, size));
        }
    }

    static void renderFrontLayers(dev.quentintyr.embellishedtooltips.client.render.TooltipContext ec, Vec2f pos,
            Point size) {
        TooltipStyle s = renderStyleRef;
        if (s != null) {
            ec.drawManaged(() -> s.renderEffects(dev.quentintyr.embellishedtooltips.client.style.Effects.TEXT_FRAME, ec,
                    pos, size));
            ec.drawManaged(() -> s.renderFront(ec, pos, size));
            ec.drawManaged(() -> s.renderEffects(dev.quentintyr.embellishedtooltips.client.style.Effects.FRAME, ec, pos,
                    size));
            ec.drawManaged(() -> s.renderEffects(dev.quentintyr.embellishedtooltips.client.style.Effects.FRONT, ec, pos,
                    size));
        }
    }
}
