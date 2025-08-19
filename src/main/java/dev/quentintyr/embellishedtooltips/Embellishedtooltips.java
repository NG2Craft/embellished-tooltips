package dev.quentintyr.embellishedtooltips;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmbellishedTooltips implements ModInitializer {
    public static final String MODID = "embellished_tooltips";
    public static final Logger LOGGER = LogManager.getLogger("EmbellishedTooltips");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Embellished Tooltips");

        // Register common functionality here (if any)
        // Most of our mod is client-side, so the main initialization happens in
        // EmbellishedTooltipsClient
    }
}
