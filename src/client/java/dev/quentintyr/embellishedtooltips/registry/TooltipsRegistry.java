package dev.quentintyr.embellishedtooltips.registry;

import com.google.gson.JsonObject;
import dev.quentintyr.embellishedtooltips.EmbellishedTooltips;
import dev.quentintyr.embellishedtooltips.client.style.effect.TooltipEffect;
import dev.quentintyr.embellishedtooltips.client.style.frame.TooltipFrame;
import dev.quentintyr.embellishedtooltips.client.style.frame.TextureFrame;
import dev.quentintyr.embellishedtooltips.client.style.icon.TooltipIcon;
import dev.quentintyr.embellishedtooltips.client.style.icon.StaticIcon;
import dev.quentintyr.embellishedtooltips.client.style.icon.DescentSimpleIcon;
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

    public static final TooltipElement<TextureFrame> FRAME_TEXTURE = registerFrame(key("texture"), (params) -> {
        return new TextureFrame(new Identifier(params.get("texture").getAsString()));
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
            return Optional.of((TooltipPanel) ((TooltipElement) PANELS.get(factory)).build(params));
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
            return Optional.of((TooltipFrame) ((TooltipElement) FRAMES.get(factory)).build(params));
        } catch (Exception var4) {
            EmbellishedTooltips.LOGGER.error(BUILDER, "Failed to build Frame <{}> from factory <{}>", key, factory);
            var4.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<TooltipIcon> buildIcon(Identifier key, JsonObject params) {
        Identifier factory = FactoryHelper.key(params, "factory");

        try {
            return Optional.of((TooltipIcon) ((TooltipElement) ICONS.get(factory)).build(params));
        } catch (Exception var4) {
            EmbellishedTooltips.LOGGER.error(BUILDER, "Failed to build Icon <{}> from factory <{}>", key, factory);
            var4.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<TooltipEffect> buildEffect(Identifier key, JsonObject params) {
        Identifier factory = FactoryHelper.key(params, "factory");

        try {
            return Optional.of((TooltipEffect) ((TooltipElement) EFFECTS.get(factory)).build(params));
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
