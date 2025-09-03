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
    private static Vec2f lastMaterialsPos;
    private static Point lastMaterialsSize;
    private static int lastMouseX;
    private static int lastMouseY;
    private static String lastHoverKey = "";
    private static List<RegistryEntry<ArmorTrimMaterial>> materialEntries = new ArrayList<>();
    private static int selectedMaterialIndex = -1;
    private static long lastActiveMs = 0L;
    private static final long ACTIVE_TIMEOUT_MS = 750L; // consider hover active if rendered recently

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
        lastPanelPos = panelPos;
        lastPanelSize = panelSize;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
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
        renderMaterialsTooltip(ctx);
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
        }
    }

    private static Optional<RegistryEntry<ArmorTrimMaterial>> getSelectedMaterial() {
        if (materialEntries == null || materialEntries.isEmpty())
            return Optional.empty();
        if (selectedMaterialIndex < 0 || selectedMaterialIndex >= materialEntries.size())
            return Optional.empty();
        return Optional.of(materialEntries.get(selectedMaterialIndex));
    }

    private static void renderMaterialsTooltip(DrawContext ctx) {
        try {
            if (lastPanelPos == null || lastPanelSize == null)
                return;
            if (materialEntries == null || materialEntries.isEmpty())
                return;

            // Layout
            final int chipW = 18, chipH = 18;
            final int pad = 6;
            final int gapX = 2, gapY = 2;
            final int maxCols = 7;

            int count = materialEntries.size();
            int cols = Math.min(maxCols, Math.max(1, count));
            int rows = (int) Math.ceil(count / (double) cols);

            int innerW = cols * chipW + (cols - 1) * gapX;
            int innerH = rows * chipH + (rows - 1) * gapY;
            int boxW = innerW + pad * 2;
            int boxH = innerH + pad * 2;

            // Place below the stand panel, horizontally centered
            int screenW = ctx.getScaledWindowWidth();
            int screenH = ctx.getScaledWindowHeight();
            int boxX = (int) (lastPanelPos.x + (lastPanelSize.x - boxW) / 2);
            int boxY = (int) (lastPanelPos.y + lastPanelSize.y + 4);

            // Clamp to visible area
            if (boxX < 4)
                boxX = 4;
            if (boxX + boxW > screenW - 4)
                boxX = Math.max(4, screenW - 4 - boxW);
            if (boxY + boxH > screenH - 4)
                boxY = Math.max(4, screenH - 4 - boxH);

            lastMaterialsPos = new Vec2f(boxX, boxY);
            lastMaterialsSize = new Point(boxW, boxH);

            // Background and border
            ctx.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xAA1C1C1C);
            ctx.drawBorder(boxX, boxY, boxW, boxH, 0xFF3C3C3C);

            // Chips
            int i = 0;
            for (var entry : materialEntries) {
                int r = i / cols;
                int c = i % cols;
                int cx = boxX + pad + c * (chipW + gapX);
                int cy = boxY + pad + r * (chipH + gapY);

                var mat = entry.value();
                var itemEntry = mat.ingredient();
                var item = itemEntry.value();
                ItemStack stack = new ItemStack(item);

                boolean selected = getSelectedMaterial().map(e -> e.value() == entry.value()).orElse(false);
                int bg = selected ? 0xCC2A2A2A : 0x88222222;
                int border = selected ? 0xFF00FFA8 : 0xFF444444;
                ctx.fill(cx - 1, cy - 1, cx + chipW + 1, cy + chipH + 1, bg);
                ctx.drawBorder(cx - 1, cy - 1, chipW + 2, chipH + 2, border);
                ctx.drawItem(stack, cx + 1, cy + 1);

                // Hover label above the box
                if (lastMouseX >= cx && lastMouseX <= cx + chipW && lastMouseY >= cy && lastMouseY <= cy + chipH) {
                    String label = mat.description().getString();
                    var tr = MinecraftClient.getInstance().textRenderer;
                    int w = tr.getWidth(label) + 6;
                    int h = tr.fontHeight + 4;
                    int tx = Math.max(4, Math.min(cx - (w - chipW) / 2, screenW - 4 - w));
                    int ty = Math.max(4, boxY - h - 3);
                    ctx.fill(tx, ty, tx + w, ty + h, 0xCC111111);
                    ctx.drawBorder(tx, ty, w, h, 0xFF3C3C3C);
                    ctx.drawText(tr, label, tx + 3, ty + 2, 0xFFDADADA, false);
                }
                i++;
            }
        } catch (Exception ignored) {
        }
    }

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
            lastMouseX = (int) mouseX;
            lastMouseY = (int) mouseY;
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
}
