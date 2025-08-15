package dev.quentintyr.embellishedtooltips.client.renderer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
// import com.obscuria.tooltips.ObscureTooltipsConfig;
import dev.quentintyr.embellishedtooltips.client.StyleManager;
import dev.quentintyr.embellishedtooltips.client.style.Effects;
import dev.quentintyr.embellishedtooltips.client.style.TooltipStyle;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber({ Dist.CLIENT })
public final class TooltipRenderer {
   @Nullable
   private static TooltipStyle renderStyle = null;
   @Nullable
   private static ArmorStand renderStand;
   private static ItemStack renderStack;
   private static long tooltipStartMillis;
   private static float tooltipSeconds;

   public static boolean render(TooltipContext renderer, ItemStack stack, Font font,
         List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner) {
      updateStyle(stack);
      if (renderStyle != null && !components.isEmpty()) {
         renderer.define(renderStack, tooltipSeconds);
         Component summaryField = getRarityName(stack);
         Point size = calculateSize(font, components, summaryField);
         Vector2ic rawPos = positioner.m_262814_(renderer.width(), renderer.height(), x, y, size.x, size.y);
         Vec2 pos = new Vec2((float) rawPos.x(), (float) rawPos.y());
         renderer.pose().m_85836_();
         renderer.drawManaged(() -> {
            renderStyle.renderEffects(Effects.Order.LAYER_1_BACK, renderer, pos, size);
         });
         renderer.drawManaged(() -> {
            renderStyle.renderBack(renderer, pos, size, true);
         });
         renderer.pose().m_252880_(0.0F, 0.0F, 400.0F);
         GuiGraphics var10000 = renderer.context();
         int var10003 = (int) pos.f_82470_ + 26;
         var10000.m_280430_(Minecraft.m_91087_().f_91062_, summaryField, var10003, (int) pos.f_82471_ + 13, -11513776);
         renderer.drawManaged(() -> {
            renderStyle.renderEffects(Effects.Order.LAYER_2_BACK$TEXT, renderer, pos, size);
         });
         renderText(renderer, font, components, pos);
         renderImages(renderer, font, components, pos);
         renderer.drawManaged(() -> {
            renderStyle.renderFront(renderer, pos, size);
         });
         renderer.drawManaged(() -> {
            renderStyle.renderEffects(Effects.Order.LAYER_5_FRONT, renderer, pos, size);
         });
         renderer.pose().m_85849_();
         Vec2 center;
         if (stack.m_41720_() instanceof ArmorItem && (Boolean) ObscureTooltipsConfig.Client.displayArmorModels.get()) {
            center = renderSecondPanel(renderer, pos);
            equip(stack);
            renderStand(renderer, center.m_165910_(new Vec2(0.0F, 26.0F)));
         } else if (stack.m_41720_() instanceof TieredItem
               && (Boolean) ObscureTooltipsConfig.Client.displayToolModels.get()) {
            center = renderSecondPanel(renderer, pos);
            renderer.pose().m_85836_();
            renderer.pose().m_252880_(center.f_82470_, center.f_82471_, 500.0F);
            renderer.pose().m_85841_(2.75F, 2.75F, 2.75F);
            renderer.pose().m_252781_(Axis.f_252529_.m_252977_(-30.0F));
            renderer.pose().m_252781_(
                  Axis.f_252436_.m_252977_((float) ((double) System.currentTimeMillis() / 1000.0D % 360.0D) * -20.0F));
            renderer.pose().m_252781_(Axis.f_252403_.m_252977_(-45.0F));
            renderer.pose().m_85836_();
            renderer.pose().m_252880_(-8.0F, -8.0F, -150.0F);
            renderer.context().m_280480_(stack, 0, 0);
            renderer.pose().m_85849_();
            renderer.pose().m_85849_();
         }

         renderer.flush();
         return true;
      } else {
         return false;
      }
   }

   private static Vec2 renderSecondPanel(TooltipContext renderer, Vec2 pos) {
      renderer.drawManaged(() -> {
         renderStyle.renderBack(renderer, pos.m_165910_(new Vec2(-55.0F, 0.0F)), new Point(30, 60), false);
      });
      return pos.m_165910_(new Vec2(-40.0F, 30.0F));
   }

   @Contract("_ -> new")
   private static Component getRarityName(ItemStack stack) {
      return Component.m_237115_("rarity." + stack.m_41791_().name().toLowerCase() + ".name");
   }

   private static Point calculateSize(Font font, List<ClientTooltipComponent> components, Component summaryField) {
      int width = 26 + ((ClientTooltipComponent) components.get(0)).m_142069_(font);
      int height = 14;

      ClientTooltipComponent component;
      for (Iterator var5 = components.iterator(); var5.hasNext(); height += component.m_142103_()) {
         component = (ClientTooltipComponent) var5.next();
         int componentWidth = component.m_142069_(font);
         if (componentWidth > width) {
            width = componentWidth;
         }
      }

      int SummaryWidth = 26 + font.m_92895_(summaryField.getString());
      if (SummaryWidth > width) {
         width = SummaryWidth;
      }

      return new Point(width, height);
   }

   private static void renderText(TooltipContext renderer, Font font, List<ClientTooltipComponent> components,
         Vec2 pos) {
      int offset = (int) pos.f_82471_ + 3;

      for (int i = 0; i < components.size(); ++i) {
         ClientTooltipComponent component = (ClientTooltipComponent) components.get(i);
         component.m_142440_(font, (int) pos.f_82470_ + (i == 0 ? 26 : 0), offset,
               renderer.pose().m_85850_().m_252922_(), renderer.bufferSource());
         offset += component.m_142103_() + (i == 0 ? 13 : 0);
      }

   }

   private static void renderImages(TooltipContext renderer, Font font, List<ClientTooltipComponent> components,
         Vec2 pos) {
      int offset = (int) pos.f_82471_ + 4;

      for (int i = 0; i < components.size(); ++i) {
         ClientTooltipComponent component = (ClientTooltipComponent) components.get(i);
         component.m_183452_(font, (int) pos.f_82470_, offset, renderer.context());
         offset += component.m_142103_() + (i == 0 ? 13 : 0);
      }

   }

   private static void renderStand(TooltipContext renderer, Vec2 pos) {
      if (renderStand != null && Minecraft.m_91087_().f_91074_ != null) {
         renderer.push(() -> {
            renderer.translate(pos.f_82470_, pos.f_82471_, 500.0F);
            renderer.scale(-30.0F, -30.0F, 30.0F);
            renderer.mul(Axis.f_252529_.m_252977_(25.0F));
            renderer.mul(
                  Axis.f_252436_.m_252977_((float) ((double) System.currentTimeMillis() / 1000.0D % 360.0D) * 20.0F));
            Lighting.m_166384_();
            EntityRenderDispatcher entityrenderdispatcher = Minecraft.m_91087_().m_91290_();
            entityrenderdispatcher.m_114468_(false);
            RenderSystem.runAsFancy(() -> {
               entityrenderdispatcher.m_114384_(renderStand, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, renderer.pose(),
                     renderer.bufferSource(), 15728880);
            });
            renderer.flush();
            entityrenderdispatcher.m_114468_(true);
         });
         Lighting.m_84931_();
      }
   }

   private static void equip(ItemStack stack) {
      if (renderStand != null) {
         Item var2 = stack.m_41720_();
         if (var2 instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem) var2;
            renderStand.m_8061_(EquipmentSlot.HEAD, ItemStack.f_41583_);
            renderStand.m_8061_(EquipmentSlot.CHEST, ItemStack.f_41583_);
            renderStand.m_8061_(EquipmentSlot.LEGS, ItemStack.f_41583_);
            renderStand.m_8061_(EquipmentSlot.FEET, ItemStack.f_41583_);
            renderStand.m_8061_(armorItem.m_40402_(), stack);
         }

      }
   }

   private static void updateStyle(ItemStack stack) {
      if (renderStand == null && Minecraft.m_91087_().f_91073_ != null) {
         renderStand = new ArmorStand(EntityType.f_20529_, Minecraft.m_91087_().f_91073_);
      }

      if (stack.m_41619_()) {
         reset();
      } else {
         tooltipSeconds = (float) (System.currentTimeMillis() - tooltipStartMillis) / 1000.0F;
         if (stack == renderStack) {
            return;
         }

         reset();
         renderStack = stack;
         renderStyle = (TooltipStyle) StyleManager.getStyleFor(stack).orElse((Object) null);
         if (renderStyle != null) {
            renderStyle.reset();
         }
      }

      renderStack = stack;
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
      renderStack = ItemStack.f_41583_;
      if (renderStyle != null) {
         renderStyle.reset();
      }

      renderStyle = null;
   }

   static {
      renderStack = ItemStack.f_41583_;
      tooltipStartMillis = 0L;
      tooltipSeconds = 0.0F;
   }
}
