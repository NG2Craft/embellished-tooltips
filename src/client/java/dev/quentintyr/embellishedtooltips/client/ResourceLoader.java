// package dev.quentintyr.embellishedtooltips.client;

// import com.google.common.collect.Lists;
// import com.google.gson.JsonObject;
// import com.google.gson.JsonParser;
// import dev.quentintyr.embellishedtooltips.EmbellishedTooltips;
// import dev.quentintyr.embellishedtooltips.client.renderer.TooltipRenderer;
// import dev.quentintyr.embellishedtooltips.client.style.StyleFilter;
// import dev.quentintyr.embellishedtooltips.client.style.TooltipStylePreset;
// import dev.quentintyr.embellishedtooltips.client.style.effect.TooltipEffect;
// import dev.quentintyr.embellishedtooltips.client.style.frame.TooltipFrame;
// import dev.quentintyr.embellishedtooltips.client.style.icon.TooltipIcon;
// import dev.quentintyr.embellishedtooltips.client.style.panel.TooltipPanel;
// import dev.quentintyr.embellishedtooltips.registry.TooltipsRegistry;
// import java.io.BufferedReader;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.nio.charset.StandardCharsets;
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.List;
// import java.util.Objects;
// import java.util.Optional;
// import java.util.function.BiFunction;
// import net.minecraft.resources.ResourceLocation;
// import net.minecraft.server.packs.PackResources;
// import net.minecraft.server.packs.PackType;
// import net.minecraft.server.packs.resources.ResourceManager;
// import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
// import net.minecraft.world.item.ItemStack;
// import org.apache.logging.log4j.Marker;
// import org.apache.logging.log4j.MarkerManager;
// import oshi.util.tuples.Pair;

// public final class ResourceLoader implements ResourceManagerReloadListener {
// public static Marker LOADER = MarkerManager.getMarker("LOADER");
// public static final ResourceLoader INSTANCE = new ResourceLoader();
// private static final HashMap<ResourceLocation, TooltipPanel> PANELS = new
// HashMap();
// private static final HashMap<ResourceLocation, TooltipFrame> FRAMES = new
// HashMap();
// private static final HashMap<ResourceLocation, TooltipIcon> ICONS = new
// HashMap();
// private static final HashMap<ResourceLocation, TooltipEffect> EFFECTS = new
// HashMap();
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
