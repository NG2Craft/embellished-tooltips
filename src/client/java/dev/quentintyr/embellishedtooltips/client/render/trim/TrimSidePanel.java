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
import java.util.Optional;

/**
 * Side panel and preview for Armor Trims. Renders a full netherite set with a
 * selected trim.
 */
public final class TrimSidePanel {

    private static ArmorStandEntity cachedStand;
    private static RegistryEntry<ArmorTrimPattern> cachedPattern;
    private static RegistryEntry<ArmorTrimMaterial> cachedMaterial;

    private TrimSidePanel() {
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
        ec.drawManaged(() -> {
            if (TooltipStylePipeline.renderStyleRef != null)
                TooltipStylePipeline.renderStyleRef.renderBack(ec, panelPos, panelSize, false);
            StyleManager.getInstance().getDefaultStyle().renderBack(ec, panelPos, panelSize, false);
        });
        return new Vec2f(panelPos.x + panelSize.x / 2.0f, panelPos.y + panelSize.y / 2.0f);
    }

    public static void renderTrimPreview(DrawContext ctx, ItemStack hovered, Vec2f center) {
        prepareStandFor(hovered);
        if (cachedStand == null)
            return;
        ItemSidePanel.renderStandRef = cachedStand;
        ItemSidePanel.renderStand(ctx, (int) center.x, (int) (center.y + 26));
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
        RegistryEntry<ArmorTrimMaterial> material = resolveMaterial(hovered).orElseGet(TrimSidePanel::defaultMaterial);

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
}
