package com.obscuria.tooltips.registry;

import com.google.gson.JsonObject;
import com.obscuria.tooltips.ObscureTooltips;
import com.obscuria.tooltips.client.style.effect.EnderEffect;
import com.obscuria.tooltips.client.style.effect.RimLightingEffect;
import com.obscuria.tooltips.client.style.effect.TooltipEffect;
import com.obscuria.tooltips.client.style.frame.BonesFrame;
import com.obscuria.tooltips.client.style.frame.TextureFrame;
import com.obscuria.tooltips.client.style.frame.TooltipFrame;
import com.obscuria.tooltips.client.style.icon.ConstantRotationIcon;
import com.obscuria.tooltips.client.style.icon.DescentComplexIcon;
import com.obscuria.tooltips.client.style.icon.DescentShineIcon;
import com.obscuria.tooltips.client.style.icon.DescentSimpleIcon;
import com.obscuria.tooltips.client.style.icon.StaticIcon;
import com.obscuria.tooltips.client.style.icon.TooltipIcon;
import com.obscuria.tooltips.client.style.panel.ColorRectPanel;
import com.obscuria.tooltips.client.style.panel.TooltipPanel;
import java.util.HashMap;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Contract;

@OnlyIn(Dist.CLIENT)
public final class TooltipsRegistry {
   public static Marker BUILDER = MarkerManager.getMarker("BUILDER");
   private static final HashMap<ResourceLocation, TooltipElement<? extends TooltipPanel>> PANELS = new HashMap();
   private static final HashMap<ResourceLocation, TooltipElement<? extends TooltipFrame>> FRAMES = new HashMap();
   private static final HashMap<ResourceLocation, TooltipElement<? extends TooltipIcon>> ICONS = new HashMap();
   private static final HashMap<ResourceLocation, TooltipElement<? extends TooltipEffect>> EFFECTS = new HashMap();
   public static final TooltipElement<ColorRectPanel> PANEL_COLOR_RECT = registerPanel(key("color_rect"), (params) -> {
      return new ColorRectPanel(FactoryHelper.color(params, "back_top_color"), FactoryHelper.color(params, "back_bottom_color"), FactoryHelper.color(params, "border_top_color"), FactoryHelper.color(params, "border_bottom_color"), FactoryHelper.color(params, "slot_color"));
   });
   public static final TooltipElement<BonesFrame> FRAME_BONES = registerFrame(key("bones"), (params) -> {
      return new BonesFrame();
   });
   public static final TooltipElement<TextureFrame> FRAME_TEXTURE = registerFrame(key("texture"), (params) -> {
      return new TextureFrame(new ResourceLocation(params.get("texture").getAsString()));
   });
   public static final TooltipElement<StaticIcon> ICON_STATIC = registerIcon(key("static"), (params) -> {
      return new StaticIcon();
   });
   public static final TooltipElement<DescentSimpleIcon> ICON_DESCENT_SIMPLE = registerIcon(key("descent_simple"), (params) -> {
      return new DescentSimpleIcon();
   });
   public static final TooltipElement<DescentComplexIcon> ICON_DESCENT_COMPLEX = registerIcon(key("descent_complex"), (params) -> {
      return new DescentComplexIcon();
   });
   public static final TooltipElement<DescentShineIcon> ICON_DESCENT_SHINE = registerIcon(key("descent_shine"), (params) -> {
      return new DescentShineIcon(FactoryHelper.color(params, "center_color"), FactoryHelper.color(params, "start_color"), FactoryHelper.color(params, "end_color"), FactoryHelper.color(params, "particle_center_color"), FactoryHelper.color(params, "particle_edge_color"));
   });
   public static final TooltipElement<ConstantRotationIcon> ICON_CONSTANT_ROTATION = registerIcon(key("constant_rotation"), (params) -> {
      return new ConstantRotationIcon();
   });
   public static final TooltipElement<RimLightingEffect> EFFECT_ENCHANTMENT = registerEffect(key("rim_lighting"), (params) -> {
      return new RimLightingEffect(FactoryHelper.color(params, "start_color"), FactoryHelper.color(params, "end_color"), FactoryHelper.color(params, "particle_center_color"), FactoryHelper.color(params, "particle_edge_color"));
   });
   public static final TooltipElement<EnderEffect> EFFECT_ENDER = registerEffect(key("ender"), (params) -> {
      return new EnderEffect(FactoryHelper.color(params, "center_color"), FactoryHelper.color(params, "edge_color"));
   });

   public static <T extends TooltipPanel> TooltipElement<T> registerPanel(ResourceLocation key, TooltipElement<T> factory) {
      PANELS.put(key, factory);
      return factory;
   }

   public static <T extends TooltipFrame> TooltipElement<T> registerFrame(ResourceLocation key, TooltipElement<T> factory) {
      FRAMES.put(key, factory);
      return factory;
   }

   public static <T extends TooltipIcon> TooltipElement<T> registerIcon(ResourceLocation key, TooltipElement<T> factory) {
      ICONS.put(key, factory);
      return factory;
   }

   public static <T extends TooltipEffect> TooltipElement<T> registerEffect(ResourceLocation key, TooltipElement<T> factory) {
      EFFECTS.put(key, factory);
      return factory;
   }

   public static Optional<TooltipPanel> buildPanel(ResourceLocation key, JsonObject params) {
      ResourceLocation factory = FactoryHelper.key(params, "factory");

      try {
         return Optional.of((TooltipPanel)((TooltipElement)PANELS.get(factory)).build(params));
      } catch (Exception var4) {
         ObscureTooltips.LOGGER.error(BUILDER, "Failed to build custom Panel <{}> from factory <{}>", key, factory);
         var4.printStackTrace();
         return Optional.empty();
      }
   }

   public static Optional<TooltipFrame> buildFrame(ResourceLocation key, JsonObject params) {
      ResourceLocation factory = FactoryHelper.key(params, "factory");

      try {
         return Optional.of((TooltipFrame)((TooltipElement)FRAMES.get(factory)).build(params));
      } catch (Exception var4) {
         ObscureTooltips.LOGGER.error(BUILDER, "Failed to build Frame <{}> from factory <{}>", key, factory);
         var4.printStackTrace();
         return Optional.empty();
      }
   }

   public static Optional<TooltipIcon> buildIcon(ResourceLocation key, JsonObject params) {
      ResourceLocation factory = FactoryHelper.key(params, "factory");

      try {
         return Optional.of((TooltipIcon)((TooltipElement)ICONS.get(factory)).build(params));
      } catch (Exception var4) {
         ObscureTooltips.LOGGER.error(BUILDER, "Failed to build Icon <{}> from factory <{}>", key, factory);
         var4.printStackTrace();
         return Optional.empty();
      }
   }

   public static Optional<TooltipEffect> buildEffect(ResourceLocation key, JsonObject params) {
      ResourceLocation factory = FactoryHelper.key(params, "factory");

      try {
         return Optional.of((TooltipEffect)((TooltipElement)EFFECTS.get(factory)).build(params));
      } catch (Exception var4) {
         ObscureTooltips.LOGGER.error(BUILDER, "Failed to build Effect <{}> from factory <{}>", key, factory);
         var4.printStackTrace();
         return Optional.empty();
      }
   }

   @Contract("_ -> new")
   private static ResourceLocation key(String key) {
      return new ResourceLocation("obscure_tooltips", key);
   }
}
