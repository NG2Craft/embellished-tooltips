package dev.quentintyr.embellishedtooltips.mixin.client;

import dev.quentintyr.embellishedtooltips.client.render.TooltipRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin for DrawContext to intercept tooltip rendering calls.
 * This serves as a fallback for tooltips that aren't caught by
 * HandledScreenMixin.
 */
@Mixin(DrawContext.class)
public abstract class GuiGraphicsMixin {

    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V", at = @At("HEAD"), cancellable = true)
    private void onDrawTooltip(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y,
            TooltipPositioner positioner, CallbackInfo ci) {

        // Since we don't have access to the ItemStack in this method, we'll use EMPTY
        // as fallback
        // Only attempt to render if this is likely an item tooltip (HandledScreenMixin
        // handles real items)
        // Prevent icons on non-item tooltips
        ItemStack fallbackStack = ItemStack.EMPTY;
        if (components != null && !components.isEmpty()) {
            // Only call our renderer if the first component is an item tooltip (heuristic)
            TooltipComponent first = components.get(0);
            String className = first.getClass().getSimpleName().toLowerCase();
            if (className.contains("item") || className.contains("stack")) {
                try {
                    if (TooltipRenderer.render((DrawContext) (Object) this, fallbackStack, textRenderer,
                            components, x, y, positioner)) {
                        ci.cancel();
                    }
                } catch (Exception e) {
                    // If there's any error, fall back to vanilla rendering
                    System.err.println("Error in custom tooltip rendering: " + e.getMessage());
                }
            }
        }
    }
}
