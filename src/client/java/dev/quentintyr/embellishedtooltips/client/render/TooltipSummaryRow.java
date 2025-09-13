package dev.quentintyr.embellishedtooltips.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;

final class TooltipSummaryRow {
    static void render(DrawContext ctx, TextRenderer font, ItemStack stack, int rowX, int rowY) {
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(0, 0, 450.0F);
        if (stack.getItem() instanceof ArmorItem armorItem) {
            int armor = armorItem.getProtection();
            float tough = armorItem.getToughness();
            int maxDur = stack.getMaxDamage();
            int curDur = Math.max(0, maxDur - stack.getDamage());
            ArmorRow.draw(ctx, font, rowX, rowY, armor, tough, maxDur, curDur);
        } else {
            Text rarity = TooltipRenderer.getRarityName(stack);
            int color = TooltipRenderer.getRarityColor(stack);
            ctx.drawText(font, rarity, rowX, rowY, color, false);
        }
        ms.pop();
    }

    static final class ArmorRow {
        private static final String DURABILITY_CAP = "0000/0000";
        static final Identifier TEX_ARMOR = new Identifier("embellished_tooltips", "textures/stats/armor.png");
        static final Identifier TEX_ARMOR_TOUGH = new Identifier("embellished_tooltips",
                "textures/stats/armor_toughness.png");
        static final Identifier TEX_DURABILITY = new Identifier("embellished_tooltips",
                "textures/stats/durability.png");

        static int width(TextRenderer font) {
            int icon = 10, gapNum = 1, spacing = 3;
            return (icon + gapNum + font.getWidth("00")) + spacing
                    + (icon + gapNum + font.getWidth("00")) + spacing
                    + (icon + gapNum + font.getWidth(DURABILITY_CAP));
        }

        static void draw(DrawContext ctx, TextRenderer font, int x, int y, int armor, float tough, int maxDur,
                int curDur) {
            final int icon = 10, spacing = 3, gapNum = 1;
            final int color = 0xFF6A6A6A;
            int cursor = x;

            ctx.drawTexture(TEX_ARMOR, cursor, y - 2, 0, 0, icon, icon, icon, icon);
            cursor += icon + gapNum;
            String armorStr = Integer.toString(armor);
            ctx.drawText(font, Text.literal(armorStr), cursor, y, color, false);
            cursor += font.getWidth(armorStr) + spacing;

            ctx.drawTexture(TEX_ARMOR_TOUGH, cursor, y - 2, 0, 0, icon, icon, icon, icon);
            cursor += icon + gapNum;
            String toughStr = (tough % 1.0f == 0.0f) ? Integer.toString((int) tough) : String.format("%.1f", tough);
            ctx.drawText(font, Text.literal(toughStr), cursor, y, color, false);
            cursor += font.getWidth(toughStr) + spacing;

            ctx.drawTexture(TEX_DURABILITY, cursor, y - 2, 0, 0, icon, icon, icon, icon);
            cursor += icon + gapNum;
            String duraStr = maxDur + "/" + curDur;
            ctx.drawText(font, Text.literal(duraStr), cursor, y, color, false);
        }
    }
}
