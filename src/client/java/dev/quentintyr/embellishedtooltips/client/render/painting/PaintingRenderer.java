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

import java.util.Optional;

/**
 * Renders a preview of a painting item using its variant sprite.
 * Logic inspired by Forge/Quilt example but adapted to Fabric/Yarn 1.20.1.
 */
public final class PaintingRenderer {

    private PaintingRenderer() {
    }

    /**
     * Attempt to render the painting preview if the stack represents a painting
     * with a variant.
     */
    public static void renderPaintingPreview(DrawContext ctx, ItemStack stack, Vec2f center) {
        if (stack.isEmpty() || stack.getItem() != Items.PAINTING)
            return;

        Optional<PaintingVariant> opt = getPaintingVariant(stack);
        if (opt.isEmpty())
            return;

        PaintingVariant variant = opt.get();

        // Fetch the sprite for the painting variant
        Sprite sprite = MinecraftClient.getInstance().getPaintingManager().getPaintingSprite(variant);

        // Compute scaled size preserving aspect ratio within a 56x56 content box
        int max = 56; // leave a small margin inside the 64x64 panel
        int vw = variant.getWidth(); // blocks
        int vh = variant.getHeight(); // blocks
        // Each block corresponds to 16x16 px on the sprite
        int texW = vw * 16;
        int texH = vh * 16;
        float scale = Math.min((float) max / texW, (float) max / texH);
        int drawW = Math.max(1, Math.round(texW * scale));
        int drawH = Math.max(1, Math.round(texH * scale));

        int x = Math.round(center.x - drawW / 2f);
        int y = Math.round(center.y - drawH / 2f) - 1; // nudge up a touch

        // Bind the painting atlas and draw the sprite
        ctx.drawSprite(x, y, 450, drawW, drawH, sprite);
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
}
