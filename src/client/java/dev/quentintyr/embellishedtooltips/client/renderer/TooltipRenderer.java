package dev.quentintyr.embellishedtooltips.client.renderer;

// import com.obscuria.tooltips.ObscureTooltipsConfig;
// import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.style.Effects;
import dev.quentintyr.embellishedtooltips.client.style.TooltipStyle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.text.Text;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.util.math.Vec2f;
import org.joml.Vector3f;
import java.awt.Point;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;

@Environment(EnvType.CLIENT)
public final class TooltipRenderer {
    @Nullable
    private static TooltipStyle renderStyle = null;
    @Nullable
    private static ArmorStandEntity renderStand;
    private static ItemStack renderStack;
    private static long tooltipStartMillis;
    private static float tooltipSeconds;

    public static boolean render(TooltipContext renderer, ItemStack stack, TextRenderer font,
            List<TooltipComponent> components, int x, int y,
            TooltipPositioner positioner) {
        updateStyle(stack);
        if (renderStyle != null && !components.isEmpty()) {
            renderer.define(renderStack, tooltipSeconds);
            Text summaryField = getRarityName(stack);
            Point size = calculateSize(font, components, summaryField);
            Vector2ic rawPos = positioner.getPosition(renderer.width(), renderer.height(),
                    x, y, size.x, size.y);
            Vec2f pos = new Vec2f((float) rawPos.x(), (float) rawPos.y());
            renderer.pose().push();
            renderer.drawManaged(() -> {
                renderStyle.renderEffects(Effects.Order.LAYER_1_BACK, renderer, pos, size);
            });
            renderer.drawManaged(() -> {
                renderStyle.renderBack(renderer, pos, size, true);
            });
            renderer.pose().translate(0.0F, 0.0F, 400.0F);
            DrawContext context = renderer.context();
            int posX = (int) pos.x + 26;
            context.drawText(MinecraftClient.getInstance().textRenderer, summaryField, posX,
                    (int) pos.y + 13, -11513776, false);
            renderer.drawManaged(() -> {
                renderStyle.renderEffects(Effects.Order.LAYER_2_BACK$TEXT, renderer, pos,
                        size);
            });
            renderText(renderer, font, components, pos);
            renderImages(renderer, font, components, pos);
            renderer.drawManaged(() -> {
                renderStyle.renderFront(renderer, pos, size);
            });
            renderer.drawManaged(() -> {
                renderStyle.renderEffects(Effects.Order.LAYER_5_FRONT, renderer, pos, size);
            });
            renderer.pose().pop();

            Vec2f center;
            // Temporarily commenting out config-dependent rendering
            if (stack.getItem() instanceof ArmorItem) { // && ObscureTooltipsConfig.Client.displayArmorModels.get()) {
                center = renderSecondPanel(renderer, pos);
                equip(stack);
                renderStand(renderer, center.add(new Vec2f(0.0F, 26.0F)));
            } else if (stack.getItem() instanceof ToolItem) { // &&
                                                              // ObscureTooltipsConfig.Client.displayToolModels.get()) {
                center = renderSecondPanel(renderer, pos);
                renderer.pose().push();
                renderer.pose().translate(center.x, center.y, 500.0F);
                renderer.pose().scale(2.75F, 2.75F, 2.75F);

                // Create rotation vectors using JOML Vector3f
                Vector3f xAxis = new Vector3f(1, 0, 0);
                Vector3f yAxis = new Vector3f(0, 1, 0);
                Vector3f zAxis = new Vector3f(0, 0, 1);

                // Apply rotations
                renderer.mul(xAxis.rotateX(-30.0F));
                renderer.mul(yAxis.rotateY((float) ((double) System.currentTimeMillis() / 1000.0D % 360.0D) * -20.0F));
                renderer.mul(zAxis.rotateZ(-45.0F));

                renderer.pose().push();
                renderer.pose().translate(-8.0F, -8.0F, -150.0F);
                renderer.context().drawItem(stack, 0, 0);
                renderer.pose().pop();
                renderer.pose().pop();
            }

            renderer.flush();
            return true;
        } else {
            return false;
        }
    }

    private static Vec2f renderSecondPanel(TooltipContext renderer, Vec2f pos) {
        renderer.drawManaged(() -> {
            renderStyle.renderBack(renderer, pos.add(new Vec2f(-55.0F, 0.0F)), new Point(30, 60), false);
        });
        return pos.add(new Vec2f(-40.0F, 30.0F));
    }

    @Contract("_ -> new")
    private static Text getRarityName(ItemStack stack) {
        return Text.translatable("rarity." + stack.getRarity().name().toLowerCase() + ".name");
    }

    private static Point calculateSize(TextRenderer font, List<TooltipComponent> components, Text summaryField) {
        int width = 26 + components.get(0).getWidth(font);
        int height = 14;

        for (TooltipComponent component : components) {
            int componentWidth = component.getWidth(font);
            if (componentWidth > width) {
                width = componentWidth;
            }
            height += component.getHeight();
        }

        int summaryWidth = 26 + font.getWidth(summaryField.getString());
        if (summaryWidth > width) {
            width = summaryWidth;
        }

        return new Point(width, height);
    }

    private static void renderText(TooltipContext renderer, TextRenderer font,
            List<TooltipComponent> components,
            Vec2f pos) {
        int offset = (int) pos.y + 3;

        for (int i = 0; i < components.size(); ++i) {
            TooltipComponent component = components.get(i);
            // This method signature is different in Fabric.
            // Simplified implementation - would need proper BufferSource implementation
            component.drawText(font, (int) pos.x + (i == 0 ? 26 : 0), offset,
                    renderer.pose().peek().getPositionMatrix(), null);
            offset += component.getHeight() + (i == 0 ? 13 : 0);
        }
    }

    // private static void renderImages(TooltipContext renderer, Font font,
    private static void renderImages(TooltipContext renderer, TextRenderer font,
            List<TooltipComponent> components,
            Vec2f pos) {
        int offset = (int) pos.y + 4;

        for (int i = 0; i < components.size(); ++i) {
            TooltipComponent component = components.get(i);
            component.drawItems(font, (int) pos.x, offset, renderer.context());
            offset += component.getHeight() + (i == 0 ? 13 : 0);
        }
    }

    private static void renderStand(TooltipContext renderer, Vec2f pos) {
        if (renderStand != null && MinecraftClient.getInstance().world != null) {
            renderer.push(new Runnable() {
                @Override
                public void run() {
                    renderer.translate(pos.x, pos.y, 500.0F);
                    renderer.scale(-30.0F, -30.0F, 30.0F);

                    Vector3f xAxis = new Vector3f(1, 0, 0);
                    Vector3f yAxis = new Vector3f(0, 1, 0);

                    renderer.mul(xAxis.rotateX(25.0F));
                    renderer.mul(yAxis.rotateY((float) ((double) System.currentTimeMillis() /
                            1000.0D % 360.0D) * 20.0F));

                    DiffuseLighting.enableGuiDepthLighting();
                    // Entity rendering is more complex in Fabric and would require additional work
                    // to properly implement. This is a placeholder.

                    // Simplified rendering that should be expanded
                    renderer.flush();
                }
            });
            DiffuseLighting.enableGuiDepthLighting();
        }
    }

    private static void equip(ItemStack stack) {
        if (renderStand != null) {
            Item item = stack.getItem();
            if (item instanceof ArmorItem) {
                ArmorItem armorItem = (ArmorItem) item;
                renderStand.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
                renderStand.equipStack(EquipmentSlot.CHEST, ItemStack.EMPTY);
                renderStand.equipStack(EquipmentSlot.LEGS, ItemStack.EMPTY);
                renderStand.equipStack(EquipmentSlot.FEET, ItemStack.EMPTY);
                renderStand.equipStack(armorItem.getSlotType(), stack);
            }
        }
    }
    // }

    // private static void updateStyle(ItemStack stack) {
    // if (renderStand == null && Minecraft.m_91087_().f_91073_ != null) {
    private static void updateStyle(ItemStack stack) {
        if (renderStand == null && MinecraftClient.getInstance().world != null) {
            renderStand = new ArmorStandEntity(EntityType.ARMOR_STAND,
                    MinecraftClient.getInstance().world);
        }

        if (stack.isEmpty()) {
            reset();
        } else {
            tooltipSeconds = (float) (System.currentTimeMillis() - tooltipStartMillis) /
                    1000.0F;
            if (stack == renderStack) {
                return;
            }

            reset();
            renderStack = stack;
            // TODO: Implement StyleManager or provide alternative
            renderStyle = null; // (TooltipStyle) StyleManager.getStyleFor(stack).orElse(null);
            if (renderStyle != null) {
                renderStyle.reset();
            }
        }
    }

    private static void reset() {
        if (renderStyle != null) {
            renderStyle.reset();
        }

        renderStyle = null;
        tooltipStartMillis = System.currentTimeMillis();
        tooltipSeconds = 0.0F;
    }

    public static void clear() {
        renderStack = ItemStack.EMPTY; // Changed from f_41583_ to EMPTY
        if (renderStyle != null) {
            renderStyle.reset();
        }

        renderStyle = null;
    }

    static {
        renderStack = ItemStack.EMPTY; // Changed from f_41583_ to EMPTY
        tooltipStartMillis = 0L;
        tooltipSeconds = 0.0F;
    }
    // }
}
