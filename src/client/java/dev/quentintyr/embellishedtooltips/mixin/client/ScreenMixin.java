package dev.quentintyr.embellishedtooltips.mixin.client;

import dev.quentintyr.embellishedtooltips.client.render.TooltipRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public class ScreenMixin {

    // For now, disable the general screen tooltip interception since method
    // signatures are problematic
    // The HandledScreenMixin should handle most tooltip cases

    // @Inject(method = "renderTooltipFromComponents", at = @At("HEAD"), cancellable
    // = true)
    // private void onRenderTooltipFromComponents(DrawContext context,
    // List<TooltipComponent> components, int x, int y, CallbackInfo ci) {
    // // This approach has signature issues, so we'll rely on HandledScreenMixin
    // for now
    // }
}
