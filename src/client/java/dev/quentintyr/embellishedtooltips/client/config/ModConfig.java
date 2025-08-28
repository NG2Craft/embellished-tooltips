package dev.quentintyr.embellishedtooltips.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main configuration class for Embellished Tooltips mod.
 * Handles loading, saving, and managing all mod settings.
 */
public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("EmbellishedTooltips/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "embellished-tooltips.json";

    private static ModConfig instance;
    private static Path configPath;

    // Configuration sections
    public AnimationConfig animations = new AnimationConfig();
    public RenderingConfig rendering = new RenderingConfig();
    public CompatibilityConfig compatibility = new CompatibilityConfig();

    private ModConfig() {
        // Private constructor for singleton
    }

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = new ModConfig();
            configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
            instance.load();
        }
        return instance;
    }

    /**
     * Load configuration from file, or create default if not exists
     */
    public void load() {
        try {
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                ModConfig loaded = GSON.fromJson(json, ModConfig.class);

                if (loaded != null) {
                    // Copy loaded values
                    this.animations = loaded.animations != null ? loaded.animations : new AnimationConfig();
                    this.rendering = loaded.rendering != null ? loaded.rendering : new RenderingConfig();
                    this.compatibility = loaded.compatibility != null ? loaded.compatibility
                            : new CompatibilityConfig();

                    LOGGER.info("Configuration loaded from {}", configPath);
                } else {
                    LOGGER.warn("Configuration file is empty or invalid, using defaults");
                    useDefaults();
                }
            } else {
                LOGGER.info("Configuration file not found, creating default configuration");
                useDefaults();
                save(); // Create the file with defaults
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load configuration file", e);
            useDefaults();
        } catch (JsonSyntaxException e) {
            LOGGER.error("Configuration file has invalid JSON syntax", e);
            useDefaults();
        }
    }

    /**
     * Save current configuration to file
     */
    public void save() {
        try {
            String json = GSON.toJson(this);
            Files.writeString(configPath, json);
            LOGGER.info("Configuration saved to {}", configPath);
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration file", e);
        }
    }

    /**
     * Reset all settings to their default values
     */
    public void useDefaults() {
        this.animations = new AnimationConfig();
        this.rendering = new RenderingConfig();
        this.compatibility = new CompatibilityConfig();
        LOGGER.info("Using default configuration values");
    }

    /**
     * Configuration section for animation settings
     */
    public static class AnimationConfig {
        public boolean enableAnimations = true;
        public float animationSpeed = 1.0f;
        public boolean enableIconRotation = true;
        public boolean enableIconScaling = true;
        public int reHoverTimeoutMs = 100;
        public boolean enableParticleEffects = true; // Particle effects for animated tooltips
    }

    /**
     * Configuration section for rendering settings
     */
    public static class RenderingConfig {
        public boolean enableCustomTooltips = true;
        public boolean enableSidePanels = true;
        public boolean enableArmorPreview = true;
        public boolean enableEnchantmentLines = true;
        public boolean showStatIcons = true;
        public boolean showRarityText = true;
        public float tooltipScale = 1.0f;
        public int maxTooltipWidth = 300;
    }

    /**
     * Configuration section for mod compatibility settings
     */
    public static class CompatibilityConfig {
        public boolean enableInInventory = true;
        public boolean enableInContainers = true;
        public boolean enableInCreative = true;
        public boolean respectOtherTooltipMods = true;
    }
}
