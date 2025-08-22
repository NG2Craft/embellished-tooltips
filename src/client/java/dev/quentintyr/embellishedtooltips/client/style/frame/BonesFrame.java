package dev.quentintyr.embellishedtooltips.client.style.frame;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import java.awt.Point;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import org.joml.Quaternionf;

public class BonesFrame implements TooltipFrame {
    private static final Identifier DECOR = new Identifier("embellished_tooltips",
            "textures/tooltips/animated/bones.png");

    public void render(TooltipContext context, Vec2f pos, Point size) {
        int x = (int) pos.x;
        int y = (int) pos.y;
        int width = size.x;
        int height = size.y;

        float time = context.time();
        float timeOffset = 2.0F + (float) Math.cos((double) time) * 6.0F;
        float rotation1 = (float) Math.cos((double) (time * 1.3F + timeOffset)) * 0.2F;
        float rotation2 = (float) Math.cos((double) (time * 1.3F + 0.5F + timeOffset)) * 0.2F;
        float rotation3 = (float) Math.cos((double) (time * 1.3F + 1.0F + timeOffset)) * 0.2F;

        context.push(() -> {
            context.translate(0.0F, 0.0F, 410.0F);

            // Corner bones (static)
            context.blit(DECOR, x - 5, y - 5, 0, 0, 6, 6, 80, 32);
            context.blit(DECOR, x + width - 1, y - 5, 74, 0, 6, 6, 80, 32);
            context.blit(DECOR, x - 5, y + height - 1, 0, 26, 6, 6, 80, 32);
            context.blit(DECOR, x + width - 1, y + height - 1, 74, 26, 6, 6, 80, 32);

            // Top center decoration
            context.blit(DECOR, x + width / 2 - 10, y - 10, 29, 1, 21, 10, 80, 32);

            // Animated bones on top (simplified - just the main ones)
            context.pushAndMul(new Quaternionf().rotationZ(rotation3), () -> {
                context.translate((float) x + (float) width / 2.0F - 16.0F, (float) y - 1.0F, 0.0F);
            }, () -> {
                context.blit(DECOR, -3, -3, 11, 6, 3, 3, 80, 32);
            });

            context.pushAndMul(new Quaternionf().rotationZ(rotation2), () -> {
                context.translate((float) x + (float) width / 2.0F - 11.0F, (float) y - 1.0F, 0.0F);
            }, () -> {
                context.blit(DECOR, -6, -5, 14, 4, 6, 5, 80, 32);
            });

            context.pushAndMul(new Quaternionf().rotationZ(rotation1), () -> {
                context.translate((float) x + (float) width / 2.0F - 4.0F, (float) y - 1.0F, 0.0F);
            }, () -> {
                context.blit(DECOR, -9, -7, 20, 2, 9, 7, 80, 32);
            });

            // Right side bones (mirrored)
            context.pushAndMul(new Quaternionf().rotationZ(-rotation3), () -> {
                context.translate((float) x + (float) width / 2.0F + 17.0F, (float) y - 1.0F, 0.0F);
            }, () -> {
                context.blit(DECOR, 0, -3, 65, 6, 3, 3, 80, 32);
            });

            context.pushAndMul(new Quaternionf().rotationZ(-rotation2), () -> {
                context.translate((float) x + (float) width / 2.0F + 12.0F, (float) y - 1.0F, 0.0F);
            }, () -> {
                context.blit(DECOR, 0, -5, 59, 4, 6, 5, 80, 32);
            });

            context.pushAndMul(new Quaternionf().rotationZ(-rotation1), () -> {
                context.translate((float) x + (float) width / 2.0F + 5.0F, (float) y - 1.0F, 0.0F);
            }, () -> {
                context.blit(DECOR, 0, -7, 50, 2, 9, 7, 80, 32);
            });

            // Bottom center decoration
            context.blit(DECOR, x + width / 2 - 1, y + height + 2, 38, 24, 3, 4, 80, 32);

            // Bottom bones (simplified - just main ones)
            context.pushAndMul(new Quaternionf().rotationZ(-rotation1), () -> {
                context.translate((float) x + (float) width / 2.0F - 1.0F, (float) (y + height) + 1.0F, 0.0F);
            }, () -> {
                context.blit(DECOR, -8, 0, 30, 23, 8, 7, 80, 32);
            });

            context.pushAndMul(new Quaternionf().rotationZ(rotation1), () -> {
                context.translate((float) x + (float) width / 2.0F + 2.0F, (float) (y + height) + 1.0F, 0.0F);
            }, () -> {
                context.blit(DECOR, 0, 0, 41, 23, 8, 7, 80, 32);
            });
        });
    }
}
