package dev.quentintyr.embellishedtooltips;

import net.fabricmc.api.ClientModInitializer;
import dev.quentintyr.embellishedtooltips.client.ResourceLoader;
import dev.quentintyr.embellishedtooltips.client.StyleManager;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class EmbellishedTooltipsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EmbellishedTooltips.LOGGER.info("Initializing Embellished Tooltips client");

        // Initialize style manager
        StyleManager.getInstance();

        // Register resource loader for data pack resources
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(ResourceLoader.INSTANCE);

        EmbellishedTooltips.LOGGER.info("Embellished Tooltips client initialized");
    }
}