// package dev.quentintyr.embellishedtooltips.mixin.client;

// import dev.quentintyr.embellishedtooltips.client.renderer.TooltipRenderer;
// import net.minecraft.client.gui.GuiGraphics;
// import net.minecraft.client.gui.screen.Screen;
// import net.minecraft.world.item.ItemStack;
// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.injection.At;
// import org.spongepowered.asm.mixin.injection.Inject;
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// @Mixin(Screen.class)
// public class ScreenMixin {

// @Inject(method =
// "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/ItemStack;II)V",
// at = @At("HEAD"), cancellable = true)
// private void onRenderTooltip(GuiGraphics graphics, ItemStack stack, int x,
// int y, CallbackInfo ci) {
// // We pass the call to our TooltipRenderer, but with a simplified set of
// // parameters.
// // The original mod will need to be refactored to work with this.
// // if (!stack.isEmpty() && TooltipRenderer.render(graphics, stack, x, y)) {
// // ci.cancel();
// // }
// }
// }
