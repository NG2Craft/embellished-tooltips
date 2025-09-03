package dev.quentintyr.embellishedtooltips.mixin.client;

import dev.quentintyr.embellishedtooltips.client.render.trim.TrimSidePanel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseScrollMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    // Mouse#onMouseScroll(JDD)V in 1.20.1: window, horizontal, vertical
    @Inject(method = "onMouseScroll(JDD)V", at = @At("TAIL"))
    private void embellishedTooltips$afterScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (this.client == null)
            return;
        if (this.client.currentScreen == null)
            return;
        // Convert raw mouse position to GUI-scaled coordinates (matches DrawContext)
        var win = this.client.getWindow();
        double mouseX = this.client.mouse.getX() * (double) win.getScaledWidth() / (double) win.getWidth();
        double mouseY = this.client.mouse.getY() * (double) win.getScaledHeight() / (double) win.getHeight();
        // Forward only vertical scroll to our handler; it will ignore if not over the
        // panel
        TrimSidePanel.onScroll(mouseX, mouseY, vertical);
    }
}
