package dev.quentintyr.embellishedtooltips.client.style.panel;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;

/**
 * A panel that renders a colored rectangle as the tooltip background.
 */
@Environment(EnvType.CLIENT)
public class ColorRectPanel implements TooltipPanel {
    private final int backTopColor;
    private final int backBottomColor;
    private final int borderTopColor;
    private final int borderBottomColor;
    private final int slotColor;

    /**
     * Creates a new color rectangle panel.
     *
     * @param backTopColor     The top color of the background.
     * @param backBottomColor  The bottom color of the background.
     * @param borderTopColor   The top color of the border.
     * @param borderBottomColor The bottom color of the border.
     * @param slotColor        The color of the slot indicator.
     */
    public ColorRectPanel(int backTopColor, int backBottomColor, int borderTopColor, int borderBottomColor, int slotColor) {
        this.backTopColor = backTopColor;
        this.backBottomColor = backBottomColor;
        this.borderTopColor = borderTopColor;
        this.borderBottomColor = borderBottomColor;
        this.slotColor = slotColor;
    }

    @Override
    public void render(DrawContext drawContext, TooltipContext context) {
        // Draw tooltip background with custom colors
        int x = context.getX();
        int y = context.getY();
        int width = context.getWidth();
        int height = context.getHeight();
        
        // Fill the background
        drawContext.fill(x + 1, y, x + width - 1, y + height, this.backTopColor);
        drawContext.fill(x, y + 1, x + 1, y + height - 1, this.backTopColor);
        drawContext.fill(x + width - 1, y + 1, x + width, y + height - 1, this.backTopColor);
        
        // Draw the borders
        drawContext.fill(x + 1, y + height - 1, x + width - 1, y + height, this.borderBottomColor);
        drawContext.fill(x, y + height - 1, x + 1, y + height, this.borderBottomColor);
        drawContext.fill(x + width - 1, y + height - 1, x + width, y + height, this.borderBottomColor);
        
        // Draw the top border
        drawContext.fill(x + 1, y, x + width - 1, y + 1, this.borderTopColor);
        drawContext.fill(x, y, x + 1, y + 1, this.borderTopColor);
        drawContext.fill(x + width - 1, y, x + width, y + 1, this.borderTopColor);
        
        // Draw slot indicator for item tooltips
        if (context.getStack() != null) {
            drawContext.fill(
                x + 2, 
                y + 1, 
                x + 2 + 20, 
                y + 1 + 1, 
                this.slotColor
            );
        }
    }
}
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
