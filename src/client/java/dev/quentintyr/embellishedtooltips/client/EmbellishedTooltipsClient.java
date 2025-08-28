package dev.quentintyr.embellishedtooltips.client;

import dev.quentintyr.embellishedtooltips.client.config.ConfigCommand;
import dev.quentintyr.embellishedtooltips.client.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side mod initializer for Embellished Tooltips.
 * Handles client-specific setup, configuration loading, and command
 * registration.
 */
public class EmbellishedTooltipsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("EmbellishedTooltips");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Embellished Tooltips client");

        // Register resource loader
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(ResourceLoader.INSTANCE);
        LOGGER.info("Resource loader registered");

        // Load configuration
        ModConfig.getInstance().load();
        LOGGER.info("Configuration loaded");

        // Register client commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ConfigCommand.register(dispatcher);
            LOGGER.info("Client commands registered");
        });

        LOGGER.info("Embellished Tooltips client initialized successfully");
    }
}
