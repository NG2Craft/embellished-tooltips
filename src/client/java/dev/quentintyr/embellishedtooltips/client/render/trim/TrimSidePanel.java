package dev.quentintyr.embellishedtooltips.client.render.trim;

import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import dev.quentintyr.embellishedtooltips.client.render.TooltipStylePipeline;
import dev.quentintyr.embellishedtooltips.client.render.item.ItemSidePanel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Side panel and preview for Armor Trims. Renders a full netherite set with a
 * selected trim.
 */
public final class TrimSidePanel {

    private static ArmorStandEntity cachedStand;
    private static RegistryEntry<ArmorTrimPattern> cachedPattern;
    private static RegistryEntry<ArmorTrimMaterial> cachedMaterial;
    // Panel bookkeeping for input/render
    private static Vec2f lastPanelPos;
    private static Point lastPanelSize;
    // Inline materials are now drawn within the main tooltip; these are no longer
    // used
    private static Vec2f lastMaterialsPos;
    private static Point lastMaterialsSize;
    private static String lastHoverKey = "";
    private static List<RegistryEntry<ArmorTrimMaterial>> materialEntries = new ArrayList<>();
    private static int selectedMaterialIndex = -1;
    private static long lastActiveMs = 0L;
    private static final long ACTIVE_TIMEOUT_MS = 750L; // consider hover active if rendered recently

    private TrimSidePanel() {
    }

    // Called when inline materials are rendered to mark hover as active (enables
    // scrolling)
    public static void markInlineMaterialsActive() {
        lastActiveMs = System.currentTimeMillis();
    }

    // Called from tooltip renderer to provide the on-screen area occupied by inline
    // materials
    public static void setInlineMaterialsArea(int x, int y, int w, int h) {
        lastMaterialsPos = new Vec2f(x, y);
        lastMaterialsSize = new Point(w, h);
    }

    public static Vec2f renderTrimPanel(TooltipContext ec, Vec2f tooltipPos, Point tooltipSize,
            TooltipStylePipeline pipelineOwner, int mouseX, int mouseY, int screenW, int screenH,
            boolean isTooltipOnLeft) {
        final Point panelSize = new Point(32, 64);

        float panelX, panelY;
        if (isTooltipOnLeft) {
            panelX = mouseX - 12 - panelSize.x;
        } else {
            panelX = mouseX + 12;
        }
        panelX = Math.max(4, Math.min(panelX, screenW - panelSize.x - 4));
        // Align the top of the side panel with the top of the main tooltip
        panelY = tooltipPos.y;
        panelY = Math.max(4, Math.min(panelY, screenH - panelSize.y - 4));

        Vec2f panelPos = new Vec2f(panelX, panelY);
        lastPanelPos = panelPos;
        lastPanelSize = panelSize;
        // mouse tracking no longer needed for inline materials
        ec.drawManaged(() -> {
            if (TooltipStylePipeline.renderStyleRef != null)
                TooltipStylePipeline.renderStyleRef.renderBack(ec, panelPos, panelSize, false);
            StyleManager.getInstance().getDefaultStyle().renderBack(ec, panelPos, panelSize, false);
        });
        return new Vec2f(panelPos.x + panelSize.x / 2.0f, panelPos.y + panelSize.y / 2.0f);
    }

    public static void renderTrimPreview(DrawContext ctx, ItemStack hovered, Vec2f center) {
        lastActiveMs = System.currentTimeMillis();
        ensureMaterials();
        syncSelectionWithHover(hovered);
        prepareStandFor(hovered);
        if (cachedStand == null)
            return;
        ItemSidePanel.renderStandRef = cachedStand;
        ItemSidePanel.renderStand(ctx, (int) center.x, (int) (center.y + 26));
        // Inline materials are now rendered in the main tooltip, so no extra panel here
    }

    /**
     * Prepares an armor stand with a full netherite set and an ArmorTrim determined
     * by the hovered stack.
     * - If hovering a Smithing Template for an armor trim, use that pattern and
     * default material quartz.
     * - If hovering a known trim material ingredient, use default pattern 'sentry'
     * and that material.
     */
    private static void prepareStandFor(ItemStack hovered) {
        var world = MinecraftClient.getInstance().world;
        if (world == null)
            return;

        RegistryEntry<ArmorTrimPattern> pattern = resolvePattern(hovered).orElseGet(TrimSidePanel::defaultPattern);
        RegistryEntry<ArmorTrimMaterial> material = getSelectedMaterial()
                .orElseGet(() -> resolveMaterial(hovered).orElseGet(TrimSidePanel::defaultMaterial));

        // Avoid re-building if same combination is already cached
        if (cachedStand != null && cachedPattern == pattern && cachedMaterial == material) {
            return;
        }

        cachedPattern = pattern;
        cachedMaterial = material;

        // Build netherite set with trim
        ArmorStandEntity stand = new ArmorStandEntity(world, 0, 0, 0);
        equipTrimmed(stand, new ItemStack(Items.NETHERITE_HELMET), pattern, material);
        equipTrimmed(stand, new ItemStack(Items.NETHERITE_CHESTPLATE), pattern, material);
        equipTrimmed(stand, new ItemStack(Items.NETHERITE_LEGGINGS), pattern, material);
        equipTrimmed(stand, new ItemStack(Items.NETHERITE_BOOTS), pattern, material);
        cachedStand = stand;
    }

    private static void equipTrimmed(ArmorStandEntity stand, ItemStack armor, RegistryEntry<ArmorTrimPattern> pattern,
            RegistryEntry<ArmorTrimMaterial> material) {
        try {
            ArmorTrim trim = new ArmorTrim(material, pattern);
            ArmorTrim.apply(MinecraftClient.getInstance().world.getRegistryManager(), armor, trim);
        } catch (Exception ignored) {
        }
        if (armor.getItem() instanceof ArmorItem armorItem) {
            stand.equipStack(armorItem.getSlotType(), armor);
        }
    }

    private static Optional<RegistryEntry<ArmorTrimPattern>> resolvePattern(ItemStack hovered) {
        Item item = hovered.getItem();
        if (item instanceof SmithingTemplateItem) {
            // Parse pattern id from item ID path: e.g.,
            // minecraft:sentry_armor_trim_smithing_template -> sentry
            Identifier itemId = Registries.ITEM.getId(item);
            String path = itemId.getPath();
            String suffix = "_armor_trim_smithing_template";
            if (path.endsWith(suffix)) {
                String patternKey = path.substring(0, path.length() - suffix.length());
                Identifier pattId = new Identifier(itemId.getNamespace(), patternKey);
                var drm = MinecraftClient.getInstance().world.getRegistryManager();
                var reg = drm.get(RegistryKeys.TRIM_PATTERN);
                if (reg != null) {
                    var ref = reg.getEntry(RegistryKey.of(RegistryKeys.TRIM_PATTERN, pattId));
                    return ref.map(r -> (RegistryEntry<ArmorTrimPattern>) r);
                }
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static Optional<RegistryEntry<ArmorTrimMaterial>> resolveMaterial(ItemStack hovered) {
        // Find a material whose ingredient item matches this stack's item
        var drm = MinecraftClient.getInstance().world.getRegistryManager();
        var reg = drm.get(RegistryKeys.TRIM_MATERIAL);
        if (reg != null) {
            Item hoveredItem = hovered.getItem();
            for (Identifier id : reg.getIds()) {
                ArmorTrimMaterial mat = reg.get(id);
                if (mat == null)
                    continue;
                try {
                    if (mat.ingredient().value() == hoveredItem) {
                        RegistryKey<ArmorTrimMaterial> key = RegistryKey.of(RegistryKeys.TRIM_MATERIAL, id);
                        var ref = reg.getEntry(key);
                        if (ref.isPresent())
                            return Optional.of((RegistryEntry<ArmorTrimMaterial>) ref.get());
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return Optional.empty();
    }

    private static void ensureMaterials() {
        if (materialEntries != null && !materialEntries.isEmpty())
            return;
        var drm = MinecraftClient.getInstance().world.getRegistryManager();
        var reg = drm.get(RegistryKeys.TRIM_MATERIAL);
        materialEntries = new ArrayList<>();
        if (reg != null) {
            for (Identifier id : reg.getIds()) {
                var e = reg.getEntry(RegistryKey.of(RegistryKeys.TRIM_MATERIAL, id));
                e.ifPresent(materialEntries::add);
            }
            // Sort entries to a stable, user-friendly order with diamond first
            java.util.Map<String, Integer> order = new java.util.HashMap<>();
            String[] pref = new String[] {
                    "minecraft:diamond", "minecraft:gold_ingot", "minecraft:amethyst_shard",
                    "minecraft:lapis_lazuli", "minecraft:iron_ingot", "minecraft:netherite_ingot",
                    "minecraft:redstone", "minecraft:emerald", "minecraft:copper_ingot", "minecraft:quartz"
            };
            for (int i = 0; i < pref.length; i++)
                order.put(pref[i], i);
            materialEntries.sort((a, b) -> {
                var drm2 = MinecraftClient.getInstance().world.getRegistryManager();
                var reg2 = drm2.get(RegistryKeys.TRIM_MATERIAL);
                Identifier ida = reg2 != null ? reg2.getId(a.value()) : null;
                Identifier idb = reg2 != null ? reg2.getId(b.value()) : null;
                int ra = ida != null ? order.getOrDefault(ida.toString(), 1000) : 1000;
                int rb = idb != null ? order.getOrDefault(idb.toString(), 1000) : 1000;
                if (ra != rb)
                    return Integer.compare(ra, rb);
                // fallback stable by identifier
                String sa = ida == null ? "" : ida.toString();
                String sb = idb == null ? "" : idb.toString();
                return sa.compareTo(sb);
            });
        }
    }

    private static Optional<RegistryEntry<ArmorTrimMaterial>> getSelectedMaterial() {
        if (materialEntries == null || materialEntries.isEmpty())
            return Optional.empty();
        if (selectedMaterialIndex < 0 || selectedMaterialIndex >= materialEntries.size())
            return Optional.empty();
        return Optional.of(materialEntries.get(selectedMaterialIndex));
    }

    // removed obsolete renderMaterialsTooltip; inline materials are rendered in the
    // main tooltip

    private static void syncSelectionWithHover(ItemStack hovered) {
        try {
            String key;
            Item it = hovered.getItem();
            if (it instanceof SmithingTemplateItem) {
                Identifier itemId = Registries.ITEM.getId(it);
                key = "template:" + itemId.toString();
            } else {
                Identifier itemId = Registries.ITEM.getId(it);
                key = "item:" + (itemId == null ? "unknown" : itemId.toString());
            }
            if (!key.equals(lastHoverKey)) {
                lastHoverKey = key;
                // Try to pick selected index to hovered material if applicable
                var hoveredMat = resolveMaterial(hovered);
                if (hoveredMat.isPresent() && materialEntries != null && !materialEntries.isEmpty()) {
                    int idx = indexOf(materialEntries, hoveredMat.get());
                    selectedMaterialIndex = idx >= 0 ? idx : 0;
                } else {
                    selectedMaterialIndex = 0;
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static int indexOf(List<RegistryEntry<ArmorTrimMaterial>> list, RegistryEntry<ArmorTrimMaterial> target) {
        var drm = MinecraftClient.getInstance().world.getRegistryManager();
        var reg = drm.get(RegistryKeys.TRIM_MATERIAL);
        Identifier targetId = reg != null ? reg.getId(target.value()) : null;
        for (int i = 0; i < list.size(); i++) {
            Identifier id = reg != null ? reg.getId(list.get(i).value()) : null;
            if (id != null && id.equals(targetId))
                return i;
            if (list.get(i).value() == target.value())
                return i;
        }
        return -1;
    }

    public static void onScroll(double mouseX, double mouseY, double vScroll) {
        try {
            // mouse tracking no longer needed for inline materials
            boolean activeHover = (System.currentTimeMillis() - lastActiveMs) <= ACTIVE_TIMEOUT_MS;
            boolean within = false;
            if (!activeHover) {
                int x = (int) mouseX, y = (int) mouseY;
                if (lastPanelPos != null && lastPanelSize != null) {
                    int left = (int) lastPanelPos.x, top = (int) lastPanelPos.y;
                    int right = left + lastPanelSize.x, bottom = top + lastPanelSize.y;
                    within |= (x >= left && x <= right && y >= top && y <= bottom);
                }
                if (lastMaterialsPos != null && lastMaterialsSize != null) {
                    int l2 = (int) lastMaterialsPos.x, t2 = (int) lastMaterialsPos.y;
                    int r2 = l2 + lastMaterialsSize.x, b2 = t2 + lastMaterialsSize.y;
                    within |= (x >= l2 && x <= r2 && y >= t2 && y <= b2);
                }
            }
            if (!activeHover && !within)
                return;
            if (materialEntries == null || materialEntries.isEmpty())
                return;
            int dir = vScroll > 0 ? -1 : (vScroll < 0 ? 1 : 0);
            if (dir == 0)
                return;
            if (selectedMaterialIndex < 0)
                selectedMaterialIndex = 0;
            selectedMaterialIndex = (selectedMaterialIndex + dir + materialEntries.size()) % materialEntries.size();
            // Force stand refresh next frame
            cachedStand = null;
        } catch (Exception ignored) {
        }
    }

    private static RegistryEntry<ArmorTrimPattern> defaultPattern() {
        var drm = MinecraftClient.getInstance().world.getRegistryManager();
        var reg = drm.get(RegistryKeys.TRIM_PATTERN);
        if (reg != null) {
            RegistryKey<ArmorTrimPattern> key = RegistryKey.of(RegistryKeys.TRIM_PATTERN,
                    new Identifier("minecraft", "sentry"));
            var opt = reg.getEntry(key);
            if (opt.isPresent())
                return opt.get();
            for (Identifier id : reg.getIds()) {
                var e = reg.getEntry(RegistryKey.of(RegistryKeys.TRIM_PATTERN, id));
                if (e.isPresent())
                    return e.get();
            }
        }
        throw new IllegalStateException("Trim pattern registry unavailable");
    }

    private static RegistryEntry<ArmorTrimMaterial> defaultMaterial() {
        var drm = MinecraftClient.getInstance().world.getRegistryManager();
        var reg = drm.get(RegistryKeys.TRIM_MATERIAL);
        if (reg != null) {
            RegistryKey<ArmorTrimMaterial> key = RegistryKey.of(RegistryKeys.TRIM_MATERIAL,
                    new Identifier("minecraft", "quartz"));
            var opt = reg.getEntry(key);
            if (opt.isPresent())
                return opt.get();
            for (Identifier id : reg.getIds()) {
                var e = reg.getEntry(RegistryKey.of(RegistryKeys.TRIM_MATERIAL, id));
                if (e.isPresent())
                    return e.get();
            }
        }
        throw new IllegalStateException("Trim material registry unavailable");
    }

    // UI hook: expose currently selected material index for tooltip highlight
    public static int getSelectedMaterialIndexForUI() {
        try {
            ensureMaterials();
            if (materialEntries == null || materialEntries.isEmpty())
                return -1;
            if (selectedMaterialIndex < 0)
                return 0;
            if (selectedMaterialIndex >= materialEntries.size())
                return materialEntries.size() - 1;
            return selectedMaterialIndex;
        } catch (Exception ignored) {
            return -1;
        }
    }

    // Expose material entries for tooltip chip rendering (order must match
    // selection index)
    public static List<RegistryEntry<ArmorTrimMaterial>> getMaterialEntriesForUI() {
        ensureMaterials();
        return materialEntries == null ? java.util.Collections.emptyList() : materialEntries;
    }
}
