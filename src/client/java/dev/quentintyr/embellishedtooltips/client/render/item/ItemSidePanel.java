package dev.quentintyr.embellishedtooltips.client.render.item;

import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.render.TooltipStylePipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import org.joml.Quaternionf;

import java.awt.Point;

public final class ItemSidePanel {
    public static ArmorStandEntity renderStandRef;

    public static Vec2f renderArmorPanel(TooltipContext ec, Vec2f tooltipPos, Point tooltipSize,
            TooltipStylePipeline pipelineOwner, int mouseX, int mouseY, int screenW, int screenH,
            boolean isTooltipOnLeft) {
        final Point panelSize = new Point(32, 64); // Panel for armor display

        float panelX, panelY;

        // Position panel based on tooltip side
        if (isTooltipOnLeft) {
            // Tooltip is on left, so panel goes to the left of cursor
            panelX = mouseX - 12 - panelSize.x; // Panel to the left of cursor
        } else {
            // Tooltip is on right, so panel goes to the right of cursor
            panelX = mouseX + 12; // Panel to the right of cursor
        }
        // Ensure panel stays within screen bounds
        panelX = Math.max(4, Math.min(panelX, screenW - panelSize.x - 4));

    // Align the top of the side panel with the top of the main tooltip
    panelY = tooltipPos.y;
    panelY = Math.max(4, Math.min(panelY, screenH - panelSize.y - 4));

        Vec2f panelPos = new Vec2f(panelX, panelY);
        ec.drawManaged(() -> {
            if (TooltipStylePipeline.renderStyleRef != null)
                TooltipStylePipeline.renderStyleRef.renderBack(ec, panelPos, panelSize, false);
            StyleManager.getInstance().getDefaultStyle().renderBack(ec, panelPos, panelSize, false);
        });
        return new Vec2f(panelPos.x + panelSize.x / 2.0f, panelPos.y + panelSize.y / 2.0f);
    }

    public static Vec2f renderToolPanel(TooltipContext ec, Vec2f tooltipPos, Point tooltipSize,
            TooltipStylePipeline pipelineOwner, int mouseX, int mouseY, int screenW, int screenH,
            boolean isTooltipOnLeft) {
        final Point panelSize = new Point(32, 64); // Panel for tool display

        float panelX, panelY;

        // Position panel based on tooltip side
        if (isTooltipOnLeft) {
            // Tooltip is on left, so panel goes to the left of cursor
            panelX = mouseX - 12 - panelSize.x; // Panel to the left of cursor
        } else {
            // Tooltip is on right, so panel goes to the right of cursor
            panelX = mouseX + 12; // Panel to the right of cursor
        }
        // Ensure panel stays within screen bounds
        panelX = Math.max(4, Math.min(panelX, screenW - panelSize.x - 4));

    // Align the top of the side panel with the top of the main tooltip
    panelY = tooltipPos.y;
    panelY = Math.max(4, Math.min(panelY, screenH - panelSize.y - 4));

        Vec2f panelPos = new Vec2f(panelX, panelY);
        ec.drawManaged(() -> {
            if (TooltipStylePipeline.renderStyleRef != null)
                TooltipStylePipeline.renderStyleRef.renderBack(ec, panelPos, panelSize, false);
            StyleManager.getInstance().getDefaultStyle().renderBack(ec, panelPos, panelSize, false);
        });
        return new Vec2f(panelPos.x + panelSize.x / 2.0f, panelPos.y + panelSize.y / 2.0f);
    }

    public static void renderStand(DrawContext ctx, int x, int y) {
        if (renderStandRef == null)
            return;
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(x, y, 500.0F);
        ms.scale(-30.0F, -30.0F, 30.0F);
        ms.multiply(new Quaternionf().rotationX((float) Math.toRadians(25.0f)));
        float spin = (float) (((System.currentTimeMillis() / 1000.0) % 360.0) * Math.toRadians(20.0));
        ms.multiply(new Quaternionf().rotationY(spin));

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders()
                .getEntityVertexConsumers();
        DiffuseLighting.enableGuiDepthLighting();
        MinecraftClient.getInstance().getEntityRenderDispatcher().render(renderStandRef, 0, 0, 0, 0, 1.0f, ms,
                immediate, 15728880);
        immediate.draw();
        DiffuseLighting.disableGuiDepthLighting();
        ms.pop();
    }

    public static void renderSpinningItem(DrawContext ctx, ItemStack stack, Vec2f center) {
        ItemRenderer3D.renderSpinningItem(ctx, stack, center);
    }
}
