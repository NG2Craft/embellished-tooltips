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

@Mixin(DrawContext.class)
public abstract class GuiGraphicsMixin {
    
    // Hook into the general tooltip drawing method
    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V", 
            at = @At("HEAD"), cancellable = true)
    private void onDrawTooltip(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, CallbackInfo ci) {
        // Try to get the current tooltip stack from somewhere
        // For now, we'll use a fallback approach
        ItemStack stack = ItemStack.EMPTY;
        
        // Attempt to render with our custom system
        if (TooltipRenderer.render((DrawContext)(Object)this, stack, textRenderer, components, x, y, positioner)) {
            ci.cancel();
        }
    }
}
