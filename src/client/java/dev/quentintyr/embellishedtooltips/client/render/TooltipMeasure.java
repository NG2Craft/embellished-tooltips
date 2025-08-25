package dev.quentintyr.embellishedtooltips.client.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

interface RenderSource {
    TooltipSize measure(TextRenderer font, TooltipLayout L, boolean isArmor, int rarityWidth);

    void renderText(DrawContext ctx, TextRenderer font, int x, int y, TooltipLayout L);
}

final class ComponentsSource implements RenderSource {
    private final List<TooltipComponent> components;

    ComponentsSource(List<TooltipComponent> components) {
        this.components = components;
    }

    @Override
    public TooltipSize measure(TextRenderer font, TooltipLayout L, boolean isArmor, int rarityWidth) {
        int w = 0, h = 0;
        for (int i = 0; i < components.size(); i++) {
            TooltipComponent c = components.get(i);
            int cw = c.getWidth(font);
            if (i == 0)
                cw += L.leftGutter;
            w = Math.max(w, cw);
            h += c.getHeight();
            if (i < components.size() - 1)
                h += L.lineGap;
        }
        if (isArmor) {
            int cappedStatsWidth = TooltipSummaryRow.ArmorRow.width(font);
            w = Math.max(w, L.leftGutter + cappedStatsWidth);
        } else {
            w = Math.max(w, L.leftGutter + rarityWidth);
        }
        return new TooltipSize(w, h);
    }

    @Override
    public void renderText(DrawContext ctx, TextRenderer font, int x, int y, TooltipLayout L) {
        MatrixStack matrices = ctx.getMatrices();
        VertexConsumerProvider.Immediate vcp = ctx.getVertexConsumers();
        int cy = y;
        for (int i = 0; i < components.size(); i++) {
            TooltipComponent c = components.get(i);
            int xOff = (i == 0) ? (L.leftGutter + L.firstLineXOffset) : 0;
            int yOff = (i == 0) ? L.firstLineYOffset : 0;
            matrices.push();
            c.drawText(font, x + xOff, cy + yOff, matrices.peek().getPositionMatrix(), vcp);
            c.drawItems(font, x + xOff, cy + yOff, ctx);
            matrices.pop();
            cy += c.getHeight() + L.lineGap + (i == 0 ? L.titleExtra : 0);
        }
        try {
            vcp.drawCurrentLayer();
        } catch (Exception ignored) {
        }
    }
}
