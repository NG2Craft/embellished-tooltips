package dev.quentintyr.embellishedtooltips.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.quentintyr.embellishedtooltips.EmbellishedTooltips;
import dev.quentintyr.embellishedtooltips.client.style.StyleFilter;
import dev.quentintyr.embellishedtooltips.client.style.TooltipStylePreset;
import dev.quentintyr.embellishedtooltips.client.style.effect.TooltipEffect;
import dev.quentintyr.embellishedtooltips.client.style.frame.TooltipFrame;
import dev.quentintyr.embellishedtooltips.client.style.icon.TooltipIcon;
import dev.quentintyr.embellishedtooltips.client.style.panel.TooltipPanel;
import dev.quentintyr.embellishedtooltips.registry.TooltipsRegistry;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Handles loading of tooltip resources from data packs.
 */
public class ResourceLoader implements SimpleSynchronousResourceReloadListener {
    public static final ResourceLoader INSTANCE = new ResourceLoader();
    private static final Identifier RESOURCE_LOADER_ID = new Identifier(EmbellishedTooltips.MODID, "resource_loader");

    // Registry maps for components
    private static final Map<Identifier, TooltipPanel> PANELS = new HashMap<>();
    private static final Map<Identifier, TooltipFrame> FRAMES = new HashMap<>();
    private static final Map<Identifier, TooltipIcon> ICONS = new HashMap<>();
    private static final Map<Identifier, TooltipEffect> EFFECTS = new HashMap<>();

    // Storage for styles and presets
    private static final List<Identifier> PRESETS_KEYS = new ArrayList<>();
    private static final List<Identifier> STYLES_KEYS = new ArrayList<>();
    private static final Map<Identifier, Pair<StyleFilter, TooltipStylePreset>> PRESETS = new HashMap<>();
    private static final Map<Identifier, Pair<StyleFilter, TooltipStylePreset>> STYLES = new HashMap<>();

    private ResourceLoader() {
    }

    @Override
    public Identifier getFabricId() {
        return RESOURCE_LOADER_ID;
    }

    @Override
    public void reload(ResourceManager resourceManager) {
        // Load the styles from the resource manager
        EmbellishedTooltips.LOGGER.info("Loading tooltip resources");

        // Clear existing data
        clear();

        // Load components and styles from resources
        loadResources(resourceManager);

        // Sort styles by priority
        sort();

        // Initialize the style manager with loaded styles
        StyleManager.getInstance();

        EmbellishedTooltips.LOGGER.info("Loaded {} Elements, {} Presets and {} Styles",
                PANELS.size() + FRAMES.size() + ICONS.size() + EFFECTS.size(),
                PRESETS.size(), STYLES.size());
    }

    /**
     * Get the appropriate tooltip style for an item stack.
     *
     * @param stack The item stack to get a style for.
     * @return An optional containing the style, or empty if no style matches.
     */
    public static Optional<TooltipStylePreset> getStyleFor(ItemStack stack) {
        TooltipStylePreset.Builder builder = new TooltipStylePreset.Builder();

        // First check for specific styles
        for (Identifier key : STYLES_KEYS) {
            Pair<StyleFilter, TooltipStylePreset> pair = STYLES.get(key);
            if (pair.getLeft().test(stack)) {
                pair.getRight().getPanel().ifPresent(builder::withPanel);
                pair.getRight().getFrame().ifPresent(builder::withFrame);
                pair.getRight().getIcon().ifPresent(builder::withIcon);
                builder.withEffects(pair.getRight().getEffects());
                break;
            }
        }

        // Then apply any matching presets
        for (Identifier key : PRESETS_KEYS) {
            Pair<StyleFilter, TooltipStylePreset> pair = PRESETS.get(key);
            if (pair.getLeft().test(stack)) {
                pair.getRight().getPanel().ifPresent(panel -> builder.withPanel(panel, false));
                pair.getRight().getFrame().ifPresent(frame -> builder.withFrame(frame, false));
                pair.getRight().getIcon().ifPresent(icon -> builder.withIcon(icon, false));
                builder.withEffects(pair.getRight().getEffects());
            }
        }

        return builder.isEmpty() ? Optional.empty() : Optional.of(builder.build());
    }

    /**
     * Clear all loaded resources.
     */
    private void clear() {
        PANELS.clear();
        FRAMES.clear();
        ICONS.clear();
        EFFECTS.clear();
        PRESETS_KEYS.clear();
        PRESETS.clear();
        STYLES_KEYS.clear();
        STYLES.clear();
    }

    /**
     * Sort styles and presets by priority.
     */
    private void sort() {
        List<Identifier> presets = PRESETS_KEYS.stream()
                .sorted(Comparator.comparingInt((Identifier key) -> PRESETS.get(key).getLeft().priority).reversed())
                .toList();
        PRESETS_KEYS.clear();
        PRESETS_KEYS.addAll(presets);

        List<Identifier> styles = STYLES_KEYS.stream()
                .sorted(Comparator.comparingInt((Identifier key) -> STYLES.get(key).getLeft().priority).reversed())
                .toList();
        STYLES_KEYS.clear();
        STYLES_KEYS.addAll(styles);
    }

    /**
     * Load all resources from the resource manager.
     *
     * @param manager The resource manager.
     */
    private void loadResources(ResourceManager manager) {
        // Load style components (panels, frames, icons, effects)
        loadElements(manager, "tooltips/panels", PANELS, TooltipsRegistry::buildPanel);
        loadElements(manager, "tooltips/frames", FRAMES, TooltipsRegistry::buildFrame);
        loadElements(manager, "tooltips/icons", ICONS, TooltipsRegistry::buildIcon);
        loadElements(manager, "tooltips/effects", EFFECTS, TooltipsRegistry::buildEffect);

        // Load presets and styles
        loadPresets(manager);
        loadStyles(manager);
    }

    /**
     * Load style elements from JSON files.
     */
    private <T> void loadElements(ResourceManager resourceManager, String path,
            Map<Identifier, T> storage, BiFunction<Identifier, JsonObject, Optional<T>> builder) {
        Map<Identifier, Resource> resources = resourceManager.findResources(path, id -> id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Resource resource = entry.getValue();

            try {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                    // Extract the element name from the path
                    String elementPath = resourceId.getPath();
                    String elementName = elementPath.substring(elementPath.lastIndexOf('/') + 1,
                            elementPath.lastIndexOf('.'));
                    Identifier elementId = new Identifier(resourceId.getNamespace(), elementName);

                    Optional<T> element = builder.apply(elementId, json);
                    if (element.isPresent()) {
                        storage.put(elementId, element.get());
                        EmbellishedTooltips.LOGGER.debug("Loaded {} element: {}", path, elementId);
                    }
                }
            } catch (IOException e) {
                EmbellishedTooltips.LOGGER.error("Failed to load resource: {}", resourceId, e);
            }
        }
    }

    /**
     * Load tooltip style presets.
     */
    private void loadPresets(ResourceManager resourceManager) {
        Map<Identifier, Resource> resources = resourceManager.findResources("tooltips/presets",
                id -> id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Resource resource = entry.getValue();

            try {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                    // Parse the preset
                    Optional<Pair<StyleFilter, TooltipStylePreset>> preset = parsePreset(resourceId, json);
                    if (preset.isPresent()) {
                        PRESETS.put(resourceId, preset.get());
                        PRESETS_KEYS.add(resourceId);
                        EmbellishedTooltips.LOGGER.debug("Loaded preset: {}", resourceId);
                    }
                }
            } catch (IOException e) {
                EmbellishedTooltips.LOGGER.error("Failed to load preset: {}", resourceId, e);
            }
        }
    }

    /**
     * Load tooltip styles.
     */
    private void loadStyles(ResourceManager resourceManager) {
        Map<Identifier, Resource> resources = resourceManager.findResources("tooltips/styles",
                id -> id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Resource resource = entry.getValue();

            try {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                    // Parse the style
                    Optional<Pair<StyleFilter, TooltipStylePreset>> style = parseStyle(resourceId, json);
                    if (style.isPresent()) {
                        STYLES.put(resourceId, style.get());
                        STYLES_KEYS.add(resourceId);
                        EmbellishedTooltips.LOGGER.debug("Loaded style: {}", resourceId);
                    }
                }
            } catch (IOException e) {
                EmbellishedTooltips.LOGGER.error("Failed to load style: {}", resourceId, e);
            }
        }
    }

    private static <T> T getWithAliases(Map<Identifier, T> map, String idString) {
        try {
            Identifier id = new Identifier(idString);
            T value = map.get(id);
            if (value != null)
                return value;
            // Try swapping hyphen/underscore in namespace to tolerate inconsistent assets
            String ns = id.getNamespace();
            String altNs = ns.contains("-") ? ns.replace('-', '_') : ns.replace('_', '-');
            Identifier alt = new Identifier(altNs, id.getPath());
            return map.get(alt);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse a preset from JSON.
     */
    private Optional<Pair<StyleFilter, TooltipStylePreset>> parsePreset(Identifier id, JsonObject json) {
        try {
            // Parse optional filter; default (no predicates) matches everything
            StyleFilter filter = json.has("filter") && json.get("filter").isJsonObject()
                    ? StyleFilter.fromJson(json.getAsJsonObject("filter"))
                    : new StyleFilter();

            // Parse the preset components
            TooltipStylePreset.Builder builder = new TooltipStylePreset.Builder();

            if (json.has("panel")) {
                String panelId = json.get("panel").getAsString();
                TooltipPanel panel = getWithAliases(PANELS, panelId);
                if (panel != null) {
                    builder.withPanel(panel);
                }
            }

            if (json.has("frame")) {
                String frameId = json.get("frame").getAsString();
                TooltipFrame frame = getWithAliases(FRAMES, frameId);
                if (frame != null) {
                    builder.withFrame(frame);
                }
            }

            if (json.has("icon")) {
                String iconId = json.get("icon").getAsString();
                TooltipIcon icon = getWithAliases(ICONS, iconId);
                if (icon != null) {
                    builder.withIcon(icon);
                }
            }

            // Parse effects array
            if (json.has("effects")) {
                JsonArray effectsArray = json.getAsJsonArray("effects");
                List<TooltipEffect> effects = new ArrayList<>();
                for (JsonElement effectElement : effectsArray) {
                    if (effectElement.isJsonPrimitive()) {
                        String effectId = effectElement.getAsString();
                        TooltipEffect effect = EFFECTS.get(new Identifier(effectId));
                        if (effect != null) {
                            effects.add(effect);
                        }
                    }
                }
                if (!effects.isEmpty()) {
                    builder.withEffects(effects);
                }
            }

            return Optional.of(new Pair<>(filter, builder.build()));
        } catch (Exception e) {
            EmbellishedTooltips.LOGGER.error("Failed to parse preset: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * Parse a style from JSON.
     */
    private Optional<Pair<StyleFilter, TooltipStylePreset>> parseStyle(Identifier id, JsonObject json) {
        // For now, styles and presets have the same format
        return parsePreset(id, json);
    }
}
// private static final List<ResourceLocation> PRESETS_KEYS = new ArrayList();
// private static final List<ResourceLocation> STYLES_KEYS = new ArrayList();
// private static final HashMap<ResourceLocation, Pair<StyleFilter,
// TooltipStylePreset>> PRESETS = new HashMap();
// private static final HashMap<ResourceLocation, Pair<StyleFilter,
// TooltipStylePreset>> STYLES = new HashMap();

// private ResourceLoader() {
// }

// public static Optional<TooltipStylePreset> getStyleFor(ItemStack stack) {
// TooltipStylePreset.Builder builder = new TooltipStylePreset.Builder();
// Iterator var2 = STYLES_KEYS.iterator();

// ResourceLocation key;
// Pair preset;
// while (var2.hasNext()) {
// key = (ResourceLocation) var2.next();
// preset = (Pair) STYLES.get(key);
// if (((StyleFilter) preset.getA()).test(stack)) {
// Optional var10000 = ((TooltipStylePreset) preset.getB()).getPanel();
// Objects.requireNonNull(builder);
// var10000.ifPresent(builder::withPanel);
// var10000 = ((TooltipStylePreset) preset.getB()).getFrame();
// Objects.requireNonNull(builder);
// var10000.ifPresent(builder::withFrame);
// var10000 = ((TooltipStylePreset) preset.getB()).getIcon();
// Objects.requireNonNull(builder);
// var10000.ifPresent(builder::withIcon);
// builder.withEffects(((TooltipStylePreset) preset.getB()).getEffects());
// break;
// }
// }

// var2 = PRESETS_KEYS.iterator();

// while (var2.hasNext()) {
// key = (ResourceLocation) var2.next();
// preset = (Pair) PRESETS.get(key);
// if (((StyleFilter) preset.getA()).test(stack)) {
// ((TooltipStylePreset) preset.getB()).getPanel().ifPresent((panel) -> {
// builder.withPanel(panel, false);
// });
// ((TooltipStylePreset) preset.getB()).getFrame().ifPresent((frame) -> {
// builder.withFrame(frame, false);
// });
// ((TooltipStylePreset) preset.getB()).getIcon().ifPresent((icon) -> {
// builder.withIcon(icon, false);
// });
// builder.withEffects(((TooltipStylePreset) preset.getB()).getEffects());
// }
// }

// return builder.isEmpty() ? Optional.empty() : Optional.of(builder.build());
// }

// public void m_6213_(ResourceManager manager) {
// TooltipRenderer.clear();
// this.clear();
// manager.m_7536_().forEach((pack) -> {
// Iterator var2 = pack.m_5698_(PackType.CLIENT_RESOURCES).iterator();

// while (var2.hasNext()) {
// String namespace = (String) var2.next();
// this.loadElements(pack, namespace, "tooltips/panels", PANELS,
// TooltipsRegistry::buildPanel);
// this.loadElements(pack, namespace, "tooltips/frames", FRAMES,
// TooltipsRegistry::buildFrame);
// this.loadElements(pack, namespace, "tooltips/icons", ICONS,
// TooltipsRegistry::buildIcon);
// this.loadElements(pack, namespace, "tooltips/effects", EFFECTS,
// TooltipsRegistry::buildEffect);
// this.loadPresets(pack, namespace);
// this.loadStyles(pack, namespace);
// }

// });
// this.sort();
// ObscureTooltips.LOGGER.debug(LOADER, "Loaded {} Elements, {} Presets and {}
// Styles",
// PANELS.size() + FRAMES.size() + ICONS.size() + EFFECTS.size(),
// PRESETS.size(), STYLES.size());
// }

// private void clear() {
// PANELS.clear();
// FRAMES.clear();
// ICONS.clear();
// EFFECTS.clear();
// PRESETS_KEYS.clear();
// PRESETS.clear();
// STYLES_KEYS.clear();
// STYLES.clear();
// }

// private void sort() {
// List<ResourceLocation> presets =
// PRESETS_KEYS.stream().sorted(Comparator.comparingInt((key) -> {
// return ((StyleFilter) ((Pair) PRESETS.get(key)).getA()).priority;
// })).toList();
// PRESETS_KEYS.clear();
// PRESETS_KEYS.addAll(Lists.reverse(presets));
// List<ResourceLocation> styles =
// STYLES_KEYS.stream().sorted(Comparator.comparingInt((key) -> {
// return ((StyleFilter) ((Pair) STYLES.get(key)).getA()).priority;
// })).toList();
// STYLES_KEYS.clear();
// STYLES_KEYS.addAll(Lists.reverse(styles));
// }

// private <T> void loadElements(PackResources pack, String namespace, String
// path,
// HashMap<ResourceLocation, T> registry, BiFunction<ResourceLocation,
// JsonObject, Optional<T>> builder) {
// pack.m_8031_(PackType.CLIENT_RESOURCES, namespace, path, (location, resource)
// -> {
// if (location.m_135815_().endsWith(".json")) {
// try {
// ResourceLocation key = new ResourceLocation(
// location.toString().replace(path + "/", "").replace(".json", ""));
// ((Optional) builder.apply(key,
// JsonParser
// .parseReader(new BufferedReader(
// new InputStreamReader((InputStream) resource.m_247737_(),
// StandardCharsets.UTF_8)))
// .getAsJsonObject()))
// .ifPresent((element) -> {
// registry.put(key, element);
// });
// } catch (Exception var6) {
// }
// }

// });
// }

// private void loadPresets(PackResources pack, String namespace) {
// pack.m_8031_(PackType.CLIENT_RESOURCES, namespace, "tooltips/presets",
// (location, resource) -> {
// if (location.m_135815_().endsWith(".json")) {
// try {
// ResourceLocation key = new ResourceLocation(
// location.toString().replace("tooltips/presets/", "").replace(".json", ""));
// JsonObject root = JsonParser
// .parseReader(new BufferedReader(
// new InputStreamReader((InputStream) resource.m_247737_(),
// StandardCharsets.UTF_8)))
// .getAsJsonObject();
// TooltipStylePreset style = this.serializeStyle(root);
// StyleFilter predicate = StyleFilter.fromJson(root.getAsJsonObject("filter"));
// if (root.has("priority")) {
// predicate.priority = root.get("priority").getAsInt();
// }

// PRESETS_KEYS.add(key);
// PRESETS.put(key, new Pair(predicate, style));
// } catch (Exception var7) {
// }
// }

// });
// }

// private void loadStyles(PackResources pack, String namespace) {
// pack.m_8031_(PackType.CLIENT_RESOURCES, namespace, "tooltips/styles",
// (location, resource) -> {
// if (location.m_135815_().endsWith(".json")) {
// try {
// ResourceLocation key = new ResourceLocation(
// location.toString().replace("tooltips/styles/", "").replace(".json", ""));
// JsonObject root = JsonParser
// .parseReader(new BufferedReader(
// new InputStreamReader((InputStream) resource.m_247737_(),
// StandardCharsets.UTF_8)))
// .getAsJsonObject();
// TooltipStylePreset style = this.serializeStyle(root);
// StyleFilter predicate = StyleFilter.fromJson(root.getAsJsonObject("filter"));
// if (root.has("priority")) {
// predicate.priority = root.get("priority").getAsInt();
// }

// STYLES_KEYS.add(key);
// STYLES.put(key, new Pair(predicate, style));
// } catch (Exception var7) {
// }
// }

// });
// }

// private TooltipStylePreset serializeStyle(JsonObject root) {
// return (new TooltipStylePreset.Builder()).withPanel(
// root.has("panel") ? (TooltipPanel) PANELS.get(new
// ResourceLocation(root.get("panel").getAsString())) : null)
// .withFrame(
// root.has("frame") ? (TooltipFrame) FRAMES.get(new
// ResourceLocation(root.get("frame").getAsString()))
// : null)
// .withIcon(root.has("icon") ? (TooltipIcon) ICONS.get(new
// ResourceLocation(root.get("icon").getAsString()))
// : null)
// .withEffects(root.has("effects") ?
// root.getAsJsonArray("effects").asList().stream().map((effect) -> {
// return (TooltipEffect) EFFECTS.getOrDefault(new
// ResourceLocation(effect.getAsString()), (a, b, c) -> {
// });
// }).toList() : null).build();
// }
// }
