package dev.quentintyr.embellishedtooltips;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmbellishedTooltips implements ModInitializer {
	// Must match the "id" in fabric.mod.json and asset namespaces
	public static final String MODID = "embellished_tooltips";
	public static final Logger LOGGER = LogManager.getLogger("EmbellishedTooltips");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Embellished Tooltips (common)");
		// Common setup (if needed). Most functionality is client-side.
	}
}
