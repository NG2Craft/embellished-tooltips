package dev.quentintyr.embellishedtooltips.mixin.client;

import dev.quentintyr.embellishedtooltips.client.render.TooltipRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow
    protected abstract Slot getSlotAt(double x, double y);

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void onDrawMouseoverTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        Slot hoveredSlot = getSlotAt(x, y);

        if (hoveredSlot != null && hoveredSlot.hasStack()) {
            ItemStack stack = hoveredSlot.getStack();
            if (!stack.isEmpty() && TooltipRenderer.render(context, stack, x, y)) {
                ci.cancel();
            }
        }
    }
}
