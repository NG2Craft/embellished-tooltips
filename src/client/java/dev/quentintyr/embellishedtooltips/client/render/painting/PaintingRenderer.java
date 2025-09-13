package dev.quentintyr.embellishedtooltips.client.render.painting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import java.awt.Point;

import java.util.Optional;

/**
 * Renders a preview of a painting item using its variant sprite.
 * Logic inspired by Forge/Quilt example but adapted to Fabric/Yarn 1.20.1.
 */
public final class PaintingRenderer {

    private PaintingRenderer() {
    }

    // Holds the selected default-variant across frames within a single hover
    private static PaintingVariant sCurrentDefaultVariant;
    private static float sLastHoverSeconds = -1f;

    /**
     * Attempt to render the painting preview if the stack represents a painting
     * with a variant.
     */
    public static void renderPaintingPreview(DrawContext ctx, ItemStack stack, Vec2f center, Point panelSize,
            float hoverSeconds) {
        if (stack.isEmpty() || stack.getItem() != Items.PAINTING)
            return;

        // Variant from NBT if present; otherwise pick a stable pseudo-random one per
        // hover
        PaintingVariant variant = getPaintingVariant(stack)
                .orElseGet(() -> pickStableVariantForHover(stack, hoverSeconds));

        // Fetch the sprite for the painting variant
        Sprite sprite = MinecraftClient.getInstance().getPaintingManager().getPaintingSprite(variant);

        // Compute scaled size preserving aspect ratio within the panel's inner area
        int maxW = Math.max(1, panelSize.x - 8);
        int maxH = Math.max(1, panelSize.y - 8);
        int vw = variant.getWidth(); // blocks
        int vh = variant.getHeight(); // blocks
        // Each block corresponds to 16x16 px on the sprite
        int texW = vw * 16;
        int texH = vh * 16;
        float scale = Math.min((float) maxW / texW, (float) maxH / texH);
        int drawW = Math.max(1, Math.round(texW * scale));
        int drawH = Math.max(1, Math.round(texH * scale));

        int x = Math.round(center.x - drawW / 2f);
        int y = Math.round(center.y - drawH / 2f); // perfect vertical centering

        // Bind the painting atlas and draw the sprite
        ctx.drawSprite(x, y, 450, drawW, drawH, sprite);
    }

    /**
     * Compute a suitable panel size for the given painting stack:
     * - Max dimension ~64px; preserves aspect ratio; adds an 8px padding budget.
     */
    public static Point computePanelSize(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.PAINTING)
            return new Point(64, 64);
        Optional<PaintingVariant> opt = getPaintingVariant(stack);
        if (opt.isEmpty())
            return new Point(64, 64);
        PaintingVariant variant = opt.get();
        int vw = variant.getWidth() * 16;
        int vh = variant.getHeight() * 16;
        int max = 64 - 8; // inner size target; outer panel will add back the padding budget
        float scale = Math.min((float) max / Math.max(1, vw), (float) max / Math.max(1, vh));
        int drawW = Math.max(1, Math.round(vw * scale));
        int drawH = Math.max(1, Math.round(vh * scale));
        return new Point(drawW + 8, drawH + 8);
    }

    /**
     * Hover-aware variant: If the stack has no variant, base sizing on the current
     * hover's chosen random variant.
     */
    public static Point computePanelSize(ItemStack stack, float hoverSeconds) {
        if (stack.isEmpty() || stack.getItem() != Items.PAINTING)
            return new Point(64, 64);
        Optional<PaintingVariant> opt = getPaintingVariant(stack);
        PaintingVariant variant = opt.orElseGet(() -> pickStableVariantForHover(stack, hoverSeconds));
        int vw = variant.getWidth() * 16;
        int vh = variant.getHeight() * 16;
        int max = 64 - 8;
        float scale = Math.min((float) max / Math.max(1, vw), (float) max / Math.max(1, vh));
        int drawW = Math.max(1, Math.round(vw * scale));
        int drawH = Math.max(1, Math.round(vh * scale));
        return new Point(drawW + 8, drawH + 8);
    }

    /**
     * Extract the painting variant from the ItemStack's NBT, matching vanilla
     * structure:
     * - the Painting item stores an EntityTag with a 'variant' id
     * (ResourceLocation)
     */
    private static Optional<PaintingVariant> getPaintingVariant(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag == null)
            return Optional.empty();
        NbtCompound entity = tag.getCompound("EntityTag");
        if (entity == null || !entity.contains("variant"))
            return Optional.empty();

        try {
            Identifier id = new Identifier(entity.getString("variant"));
            return Registries.PAINTING_VARIANT.getOrEmpty(id);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    /**
     * For a default Painting item with no variant, pick a random variant but keep
     * it
     * stable during the current hover and reasonably varied across stacks.
     */
    private static PaintingVariant pickStableVariantForHover(ItemStack stack, float hoverSeconds) {
        // New hover detected when hoverSeconds resets (less than last seen)
        boolean newHover = hoverSeconds < sLastHoverSeconds - 0.0001f || sCurrentDefaultVariant == null;
        if (newHover) {
            var all = Registries.PAINTING_VARIANT.stream().toList();
            if (!all.isEmpty()) {
                int idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(all.size());
                sCurrentDefaultVariant = all.get(idx);
            }
        }
        sLastHoverSeconds = hoverSeconds;
        // Fallback if registry empty (shouldn't happen)
        if (sCurrentDefaultVariant == null) {
            Identifier any = Registries.PAINTING_VARIANT.getIds().stream().findFirst()
                    .orElse(new Identifier("minecraft", "kebab"));
            sCurrentDefaultVariant = Registries.PAINTING_VARIANT.get(any);
        }
        return sCurrentDefaultVariant;
    }
}
