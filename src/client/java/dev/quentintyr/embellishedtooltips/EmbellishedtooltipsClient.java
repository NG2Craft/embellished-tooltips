package dev.quentintyr.embellishedtooltips;

import net.fabricmc.api.ClientModInitializer;
import dev.quentintyr.embellishedtooltips.client.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;

public class EmbellishedTooltipsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

	}

	public static void setup() {
		ResourceManager var1 = Minecraft.m_91087_().m_91098_();
		if (var1 instanceof ReloadableResourceManager) {
			ReloadableResourceManager manager = (ReloadableResourceManager) var1;
			manager.m_7217_(ResourceLoader.INSTANCE);
		}

	}
}