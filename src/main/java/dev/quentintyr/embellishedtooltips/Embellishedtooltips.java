package dev.quentintyr.embellishedtooltips;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmbellishedTooltips implements ModInitializer {
    // Must match the "id" in fabric.mod.json and asset namespaces
    public static final String MODID = "embellished-tooltips";
    public static final Logger LOGGER = LogManager.getLogger("EmbellishedTooltips");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Embellished Tooltips");

        // Register common functionality here (if any)
        // Most of our mod is client-side, so the main initialization happens in
        // EmbellishedTooltipsClient
    }
}
