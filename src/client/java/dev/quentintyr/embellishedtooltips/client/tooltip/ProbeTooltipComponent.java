package dev.quentintyr.embellishedtooltips.client.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public record ProbeTooltipComponent(ProbeTooltipData data) implements TooltipComponent {
    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return textRenderer.getWidth(Text.literal(data.label()));
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext drawContext) {
        drawContext.drawText(textRenderer, Text.literal(data.label()), x, y, 0xFF00FFFF, true);
    }
}
