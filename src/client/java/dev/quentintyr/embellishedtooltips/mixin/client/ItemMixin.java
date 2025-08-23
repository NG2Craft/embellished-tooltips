package dev.quentintyr.embellishedtooltips.mixin.client;

import dev.quentintyr.embellishedtooltips.client.tooltip.ProbeTooltipData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Environment(EnvType.CLIENT)
@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void embellishedTooltips$injectProbeData(ItemStack stack,
            CallbackInfoReturnable<Optional<TooltipData>> cir) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
            return;
        if (stack != null && stack.isOf(Items.DIAMOND_SWORD)) {
            cir.setReturnValue(Optional.of(new ProbeTooltipData("Probe component")));
        }
    }
}
