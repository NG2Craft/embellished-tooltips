package dev.quentintyr.embellishedtooltips.client.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record ProbeTooltipData(String label) implements net.minecraft.client.item.TooltipData {
}
