package dev.quentintyr.embellishedtooltips.client.style.panel;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec2f;
import java.awt.Point;

/**
 * A panel that renders a colored rectangle as the tooltip background.
 * This matches the original ColorRectPanel from Obscure Tooltips.
 */
@Environment(EnvType.CLIENT)
public class ColorRectPanel implements TooltipPanel {
    private final int BACK_TOP;
    private final int BACK_BOTTOM;
    private final int BORDER_TOP;
    private final int BORDER_BOTTOM;
    private final int SLOT;

    /**
     * Creates a new color rectangle panel.
     *
     * @param backTop      The top color of the background.
     * @param backBottom   The bottom color of the background.
     * @param borderTop    The top color of the border.
     * @param borderBottom The bottom color of the border.
     * @param slot         The color of the slot indicator.
     */
    public ColorRectPanel(int backTop, int backBottom, int borderTop, int borderBottom, int slot) {
        this.BACK_TOP = backTop;
        this.BACK_BOTTOM = backBottom;
        this.BORDER_TOP = borderTop;
        this.BORDER_BOTTOM = borderBottom;
        this.SLOT = slot;
    }

    @Override
    public void render(TooltipContext context, Vec2f pos, Point size, boolean slot) {
        int x = (int) pos.x;
        int y = (int) pos.y;

        // Render tooltip background similar to
        // TooltipRenderUtil.renderTooltipBackground
        renderTooltipBackground(context, x, y, size.x, size.y, 400, BACK_TOP, BACK_BOTTOM, BORDER_TOP, BORDER_BOTTOM);

        // Render slot highlight if requested
        if (slot) {
            context.push(() -> {
                context.translate(0.0F, 0.0F, 400.0F);
                context.fillGradient(x + 2, y + 1, 20, 1, SLOT, SLOT);
                context.fillGradient(x + 1, y + 2, 22, 20, SLOT, SLOT);
                context.fillGradient(x + 2, y + 22, 20, 1, SLOT, SLOT);
            });
        }
    }

    /**
     * Renders a tooltip background similar to Forge's TooltipRenderUtil.
     */
    private void renderTooltipBackground(TooltipContext context, int x, int y, int width, int height, int z,
            int backgroundColorStart, int backgroundColorEnd,
            int borderColorStart, int borderColorEnd) {
        context.push(() -> {
            context.translate(0.0F, 0.0F, z);

            // Draw background gradient
            context.fillGradient(x + 1, y, width - 2, 1, backgroundColorStart, backgroundColorStart);
            context.fillGradient(x + 1, y + height - 1, width - 2, 1, backgroundColorEnd, backgroundColorEnd);
            context.fillGradient(x + 1, y + 1, width - 2, height - 2, backgroundColorStart, backgroundColorEnd);
            context.fillGradient(x, y + 1, 1, height - 2, backgroundColorStart, backgroundColorEnd);
            context.fillGradient(x + width - 1, y + 1, 1, height - 2, backgroundColorStart, backgroundColorEnd);

            // Draw border
            context.fillGradient(x + 1, y + 1, width - 2, 1, borderColorStart, borderColorStart);
            context.fillGradient(x + 1, y + height - 2, width - 2, 1, borderColorEnd, borderColorEnd);
            context.fillGradient(x + 1, y + 1, 1, height - 2, borderColorStart, borderColorEnd);
            context.fillGradient(x + width - 2, y + 1, 1, height - 2, borderColorStart, borderColorEnd);
        });
    }
}
