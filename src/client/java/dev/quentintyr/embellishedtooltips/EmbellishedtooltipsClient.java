package dev.quentintyr.embellishedtooltips;

import net.fabricmc.api.ClientModInitializer;
import dev.quentintyr.embellishedtooltips.client.ResourceLoader;
import dev.quentintyr.embellishedtooltips.client.StyleManager;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.text.Text;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.item.TooltipData;
import dev.quentintyr.embellishedtooltips.client.tooltip.ProbeTooltipData;
import dev.quentintyr.embellishedtooltips.client.tooltip.ProbeTooltipComponent;

public class EmbellishedTooltipsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EmbellishedTooltips.LOGGER.info("Initializing Embellished Tooltips client");

        // Initialize style manager
        StyleManager.getInstance();

        // Register resource loader for data pack resources
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(ResourceLoader.INSTANCE);

        // Map custom TooltipData -> TooltipComponent
        TooltipComponentCallback.EVENT.register((TooltipData data) -> {
            if (data instanceof ProbeTooltipData probe) {
                return new ProbeTooltipComponent(probe);
            }
            return null;
        });

        // Minimal probe to ensure Fabric tooltip event wiring works on this version
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
                // Append a tiny marker in dev to confirm interception; keep unobtrusive
                try {
                    lines.add(Text.literal("§8[ET]"));
                } catch (Throwable ignored) {
                }
            });
        }

        EmbellishedTooltips.LOGGER.info("Embellished Tooltips client initialized");
    }
}