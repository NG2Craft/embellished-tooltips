package dev.quentintyr.embellishedtooltips.registry;

import com.google.gson.JsonObject;
import dev.quentintyr.embellishedtooltips.EmbellishedTooltips;
import dev.quentintyr.embellishedtooltips.client.style.effect.TooltipEffect;
import dev.quentintyr.embellishedtooltips.client.style.effect.RimLightingEffect;
import dev.quentintyr.embellishedtooltips.client.style.effect.EnderEffect;
import dev.quentintyr.embellishedtooltips.client.style.frame.TooltipFrame;
import dev.quentintyr.embellishedtooltips.client.style.frame.TextureFrame;
import dev.quentintyr.embellishedtooltips.client.style.frame.BonesFrame;
import dev.quentintyr.embellishedtooltips.client.style.icon.TooltipIcon;
import dev.quentintyr.embellishedtooltips.client.style.icon.StaticIcon;
import dev.quentintyr.embellishedtooltips.client.style.icon.DescentSimpleIcon;
import dev.quentintyr.embellishedtooltips.client.style.icon.DescentComplexIcon;
import dev.quentintyr.embellishedtooltips.client.style.icon.DescentShineIcon;
import dev.quentintyr.embellishedtooltips.client.style.icon.ConstantRotationIcon;
import dev.quentintyr.embellishedtooltips.client.style.panel.ColorRectPanel;
import dev.quentintyr.embellishedtooltips.client.style.panel.TooltipPanel;
import java.util.HashMap;
import java.util.Optional;
import net.minecraft.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Contract;

@Environment(EnvType.CLIENT)
public final class TooltipsRegistry {
    public static Marker BUILDER = MarkerManager.getMarker("BUILDER");
    private static final HashMap<Identifier, TooltipElement<? extends TooltipPanel>> PANELS = new HashMap<>();
    private static final HashMap<Identifier, TooltipElement<? extends TooltipFrame>> FRAMES = new HashMap<>();
    private static final HashMap<Identifier, TooltipElement<? extends TooltipIcon>> ICONS = new HashMap<>();
    private static final HashMap<Identifier, TooltipElement<? extends TooltipEffect>> EFFECTS = new HashMap<>();

    public static final TooltipElement<ColorRectPanel> PANEL_COLOR_RECT = registerPanel(key("color_rect"), (params) -> {
        return new ColorRectPanel(FactoryHelper.color(params, "back_top_color"),
                FactoryHelper.color(params, "back_bottom_color"), FactoryHelper.color(params, "border_top_color"),
                FactoryHelper.color(params, "border_bottom_color"),
                FactoryHelper.color(params, "slot_color"));
    });

    public static final TooltipElement<StaticIcon> ICON_STATIC = registerIcon(key("static"), (params) -> {
        return new StaticIcon();
    });

    public static final TooltipElement<DescentSimpleIcon> ICON_DESCENT_SIMPLE = registerIcon(key("descent_simple"),
            (params) -> {
                return new DescentSimpleIcon();
            });

    public static final TooltipElement<DescentComplexIcon> ICON_DESCENT_COMPLEX = registerIcon(key("descent_complex"),
            (params) -> {
                return new DescentComplexIcon();
            });

    public static final TooltipElement<DescentShineIcon> ICON_DESCENT_SHINE = registerIcon(key("descent_shine"),
            (params) -> {
                return new DescentShineIcon(FactoryHelper.color(params, "center_color"),
                        FactoryHelper.color(params, "start_color"), FactoryHelper.color(params, "end_color"),
                        FactoryHelper.color(params, "particle_center_color"),
                        FactoryHelper.color(params, "particle_edge_color"));
            });

    public static final TooltipElement<ConstantRotationIcon> ICON_CONSTANT_ROTATION = registerIcon(
            key("constant_rotation"), (params) -> {
                return new ConstantRotationIcon();
            });

    public static final TooltipElement<TextureFrame> FRAME_TEXTURE = registerFrame(key("texture"), (params) -> {
        return new TextureFrame(new Identifier(params.get("texture").getAsString()));
    });

    public static final TooltipElement<BonesFrame> FRAME_BONES = registerFrame(key("bones"), (params) -> {
        return new BonesFrame();
    });

    public static final TooltipElement<RimLightingEffect> EFFECT_RIM_LIGHTING = registerEffect(key("rim_lighting"),
            (params) -> {
                return new RimLightingEffect(FactoryHelper.color(params, "start_color"),
                        FactoryHelper.color(params, "end_color"), FactoryHelper.color(params, "particle_center_color"),
                        FactoryHelper.color(params, "particle_edge_color"));
            });

    public static final TooltipElement<EnderEffect> EFFECT_ENDER = registerEffect(key("ender"), (params) -> {
        return new EnderEffect(FactoryHelper.color(params, "center_color"),
                FactoryHelper.color(params, "edge_color"));
    });

    // Note: These will be uncommented once the implementations are fixed
    /*
     * public static final TooltipElement<BonesFrame> FRAME_BONES =
     * registerFrame(key("bones"), (params) -> {
     * return new BonesFrame();
     * });
     * public static final TooltipElement<TextureFrame> FRAME_TEXTURE =
     * registerFrame(key("texture"), (params) -> {
     * return new TextureFrame(new Identifier(params.get("texture").getAsString()));
     * });
     * public static final TooltipElement<StaticIcon> ICON_STATIC =
     * registerIcon(key("static"), (params) -> {
     * return new StaticIcon();
     * });
     * public static final TooltipElement<DescentSimpleIcon> ICON_DESCENT_SIMPLE =
     * registerIcon(key("descent_simple"),
     * (params) -> {
     * return new DescentSimpleIcon();
     * });
     * public static final TooltipElement<DescentComplexIcon> ICON_DESCENT_COMPLEX =
     * registerIcon(key("descent_complex"),
     * (params) -> {
     * return new DescentComplexIcon();
     * });
     * public static final TooltipElement<DescentShineIcon> ICON_DESCENT_SHINE =
     * registerIcon(key("descent_shine"),
     * (params) -> {
     * return new DescentShineIcon(FactoryHelper.color(params, "center_color"),
     * FactoryHelper.color(params, "start_color"), FactoryHelper.color(params,
     * "end_color"),
     * FactoryHelper.color(params, "particle_center_color"),
     * FactoryHelper.color(params, "particle_edge_color"));
     * });
     * public static final TooltipElement<ConstantRotationIcon>
     * ICON_CONSTANT_ROTATION = registerIcon(
     * key("constant_rotation"), (params) -> {
     * return new ConstantRotationIcon();
     * });
     * public static final TooltipElement<RimLightingEffect> EFFECT_ENCHANTMENT =
     * registerEffect(key("rim_lighting"),
     * (params) -> {
     * return new RimLightingEffect(FactoryHelper.color(params, "start_color"),
     * FactoryHelper.color(params, "end_color"), FactoryHelper.color(params,
     * "particle_center_color"),
     * FactoryHelper.color(params, "particle_edge_color"));
     * });
     * public static final TooltipElement<EnderEffect> EFFECT_ENDER =
     * registerEffect(key("ender"), (params) -> {
     * return new EnderEffect(FactoryHelper.color(params, "center_color"),
     * FactoryHelper.color(params, "edge_color"));
     * });
     */

    // Register aliases for backward compatibility with obscure_tooltips namespace
    static {
        // Panel aliases
        PANELS.put(new Identifier("obscure_tooltips", "color_rect"), PANEL_COLOR_RECT);
        PANELS.put(new Identifier("obscure_tooltips", "golden"), PANEL_COLOR_RECT);
        PANELS.put(new Identifier("obscure_tooltips", "silver"), PANEL_COLOR_RECT);

        // Frame aliases
        FRAMES.put(new Identifier("obscure_tooltips", "texture"), FRAME_TEXTURE);
        FRAMES.put(new Identifier("obscure_tooltips", "bones"), FRAME_BONES);
        FRAMES.put(new Identifier("obscure_tooltips", "golden"), FRAME_BONES);
        FRAMES.put(new Identifier("obscure_tooltips", "silver"), FRAME_BONES);

        // Icon aliases
        ICONS.put(new Identifier("obscure_tooltips", "static"), ICON_STATIC);
        ICONS.put(new Identifier("obscure_tooltips", "descent_simple"), ICON_DESCENT_SIMPLE);
        ICONS.put(new Identifier("obscure_tooltips", "descent_complex"), ICON_DESCENT_COMPLEX);
        ICONS.put(new Identifier("obscure_tooltips", "descent_shine"), ICON_DESCENT_SHINE);
        ICONS.put(new Identifier("obscure_tooltips", "constant_rotation"), ICON_CONSTANT_ROTATION);
        ICONS.put(new Identifier("obscure_tooltips", "rare"), ICON_DESCENT_SIMPLE);
        ICONS.put(new Identifier("obscure_tooltips", "epic"), ICON_DESCENT_SHINE);

        // Effect aliases
        EFFECTS.put(new Identifier("obscure_tooltips", "rim_lighting"), EFFECT_RIM_LIGHTING);
        EFFECTS.put(new Identifier("obscure_tooltips", "ender"), EFFECT_ENDER);
        EFFECTS.put(new Identifier("obscure_tooltips", "enchantment"), EFFECT_RIM_LIGHTING);
        EFFECTS.put(new Identifier("obscure_tooltips", "curse"), EFFECT_RIM_LIGHTING);
    }

    public static <T extends TooltipPanel> TooltipElement<T> registerPanel(Identifier key, TooltipElement<T> factory) {
        PANELS.put(key, factory);
        return factory;
    }

    public static <T extends TooltipFrame> TooltipElement<T> registerFrame(Identifier key, TooltipElement<T> factory) {
        FRAMES.put(key, factory);
        return factory;
    }

    public static <T extends TooltipIcon> TooltipElement<T> registerIcon(Identifier key, TooltipElement<T> factory) {
        ICONS.put(key, factory);
        return factory;
    }

    public static <T extends TooltipEffect> TooltipElement<T> registerEffect(Identifier key,
            TooltipElement<T> factory) {
        EFFECTS.put(key, factory);
        return factory;
    }

    public static Optional<TooltipPanel> buildPanel(Identifier key, JsonObject params) {
        Identifier factory = FactoryHelper.key(params, "factory");

        try {
            TooltipElement<? extends TooltipPanel> element = PANELS.get(factory);
            return Optional.of(element.build(params));
        } catch (Exception var4) {
            EmbellishedTooltips.LOGGER.error(BUILDER, "Failed to build custom Panel <{}> from factory <{}>", key,
                    factory);
            var4.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<TooltipFrame> buildFrame(Identifier key, JsonObject params) {
        Identifier factory = FactoryHelper.key(params, "factory");

        try {
            TooltipElement<? extends TooltipFrame> element = FRAMES.get(factory);
            return Optional.of(element.build(params));
        } catch (Exception var4) {
            EmbellishedTooltips.LOGGER.error(BUILDER, "Failed to build Frame <{}> from factory <{}>", key, factory);
            var4.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<TooltipIcon> buildIcon(Identifier key, JsonObject params) {
        Identifier factory = FactoryHelper.key(params, "factory");

        try {
            TooltipElement<? extends TooltipIcon> element = ICONS.get(factory);
            return Optional.of(element.build(params));
        } catch (Exception var4) {
            EmbellishedTooltips.LOGGER.error(BUILDER, "Failed to build Icon <{}> from factory <{}>", key, factory);
            var4.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<TooltipEffect> buildEffect(Identifier key, JsonObject params) {
        Identifier factory = FactoryHelper.key(params, "factory");

        try {
            TooltipElement<? extends TooltipEffect> element = EFFECTS.get(factory);
            return Optional.of(element.build(params));
        } catch (Exception var4) {
            EmbellishedTooltips.LOGGER.error(BUILDER, "Failed to build Effect <{}> from factory <{}>", key, factory);
            var4.printStackTrace();
            return Optional.empty();
        }
    }

    @Contract("_ -> new")
    private static Identifier key(String key) {
        return new Identifier(EmbellishedTooltips.MODID, key);
    }
}
