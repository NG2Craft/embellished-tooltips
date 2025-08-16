// package dev.quentintyr.embellishedtooltips.mixin.client;

// // import dev.quentintyr.embellishedtooltips.client.renderer.TooltipContext;
// // import dev.quentintyr.embellishedtooltips.client.renderer.TooltipRenderer;
// // import java.util.List;
// // import net.minecraft.client.gui.Font;
// // import net.minecraft.client.gui.GuiGraphics;
// // import
// net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
// // import
// net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
// // import net.minecraft.world.item.ItemStack;
// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.Shadow;
// import org.spongepowered.asm.mixin.injection.At;
// import org.spongepowered.asm.mixin.injection.Inject;
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// @Mixin(value = { GuiGraphics.class }, priority = 0)
// public abstract class GuiGraphicsMixin {
// @Shadow(remap = false)
// private ItemStack tooltipStack;

// @Inject(method = { "renderTooltipInternal" }, at = { @At("HEAD") },
// cancellable = true)
// private void renderTooltip(Font font, List<ClientTooltipComponent>
// components, int x, int y,
// ClientTooltipPositioner positioner, CallbackInfo ci) {
// if (TooltipRenderer.render(new TooltipContext((GuiGraphics) this),
// this.tooltipStack, font, components, x, y,
// positioner)) {
// ci.cancel();
// }

// }
// }
