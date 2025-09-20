package dev.quentintyr.embellishedtooltips.tooltip;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

/**
 * Logical tooltip data container indicating a framed preview should be rendered
 * for the provided ItemStack.
 */
public record ItemFramePreviewTooltipData(ItemStack stack) implements TooltipData {
}
