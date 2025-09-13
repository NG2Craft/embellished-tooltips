package dev.quentintyr.embellishedtooltips.client.style.frame;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import java.awt.Point;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;

public class TextureFrame implements TooltipFrame {
    protected final Identifier TEXTURE;

    public TextureFrame(Identifier texture) {
        this.TEXTURE = texture;
    }

    public void render(TooltipContext context, Vec2f pos, Point size) {
        context.blit(this.TEXTURE, (int) pos.x + size.x / 2 - 30, (int) pos.y - 10, 10, 0, 60, 16, 80, 32);
        context.blit(this.TEXTURE, (int) pos.x + size.x / 2 - 30, (int) pos.y + size.y - 6, 10, 16, 60, 16, 80, 32);
        context.blit(this.TEXTURE, (int) pos.x - 5, (int) pos.y - 5, 0, 0, 10, 10, 80, 32);
        context.blit(this.TEXTURE, (int) pos.x + size.x - 5, (int) pos.y - 5, 70, 0, 10, 10, 80, 32);
        context.blit(this.TEXTURE, (int) pos.x - 5, (int) pos.y + size.y - 5, 0, 22, 10, 10, 80, 32);
        context.blit(this.TEXTURE, (int) pos.x + size.x - 5, (int) pos.y + size.y - 5, 70, 22, 10, 10, 80, 32);
    }
}
