package dev.quentintyr.embellishedtooltips.client.config;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

/**
 * Client-side command for configuring Embellished Tooltips settings.
 */
public class ConfigCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("embellished-tooltips")
                .then(ClientCommandManager.literal("config")
                        // Animation settings
                        .then(ClientCommandManager.literal("animations")
                                .then(ClientCommandManager.literal("enable")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    ModConfig.getInstance().animations.enableAnimations = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Animations " + (value ? "enabled" : "disabled")));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager.literal("speed")
                                        .then(ClientCommandManager
                                                .argument("value", FloatArgumentType.floatArg(0.1f, 3.0f))
                                                .executes(context -> {
                                                    float value = FloatArgumentType.getFloat(context, "value");
                                                    ModConfig.getInstance().animations.animationSpeed = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Animation speed set to " + value + "x"));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager.literal("rotation")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    ModConfig.getInstance().animations.enableIconRotation = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Icon rotation " + (value ? "enabled" : "disabled")));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager.literal("scaling")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    ModConfig.getInstance().animations.enableIconScaling = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Icon scaling " + (value ? "enabled" : "disabled")));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager.literal("timeout")
                                        .then(ClientCommandManager
                                                .argument("value", IntegerArgumentType.integer(50, 1000))
                                                .executes(context -> {
                                                    int value = IntegerArgumentType.getInteger(context, "value");
                                                    ModConfig.getInstance().animations.reHoverTimeoutMs = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Re-hover timeout set to " + value + "ms"));
                                                    return 1;
                                                }))))

                        // Rendering settings
                        .then(ClientCommandManager.literal("rendering")
                                .then(ClientCommandManager.literal("enable")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    ModConfig.getInstance().rendering.enableCustomTooltips = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Custom tooltips " + (value ? "enabled" : "disabled")));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager.literal("enchantments")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    ModConfig.getInstance().rendering.enableEnchantmentLines = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Enchantment lines " + (value ? "enabled" : "disabled")));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager.literal("stats")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    ModConfig.getInstance().rendering.showStatIcons = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Stat icons " + (value ? "enabled" : "disabled")));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager.literal("armor")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    ModConfig.getInstance().rendering.enableArmorPreview = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Armor preview " + (value ? "enabled" : "disabled")));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager.literal("tools")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    ModConfig.getInstance().rendering.enableToolPreviews = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Tool previews " + (value ? "enabled" : "disabled")));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager.literal("scale")
                                        .then(ClientCommandManager
                                                .argument("value", FloatArgumentType.floatArg(0.5f, 2.0f))
                                                .executes(context -> {
                                                    float value = FloatArgumentType.getFloat(context, "value");
                                                    ModConfig.getInstance().rendering.tooltipScale = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Tooltip scale set to " + value + "x"));
                                                    return 1;
                                                }))))

                        // Compatibility settings
                        .then(ClientCommandManager.literal("compatibility")
                                .then(ClientCommandManager.literal("inventory")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    ModConfig.getInstance().compatibility.enableInInventory = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Tooltips in inventory "
                                                                    + (value ? "enabled" : "disabled")));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager.literal("containers")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    ModConfig.getInstance().compatibility.enableInContainers = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Tooltips in containers "
                                                                    + (value ? "enabled" : "disabled")));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager.literal("creative")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    boolean value = BoolArgumentType.getBool(context, "value");
                                                    ModConfig.getInstance().compatibility.enableInCreative = value;
                                                    ModConfig.getInstance().save();
                                                    context.getSource().sendFeedback(Text.literal(
                                                            "Tooltips in creative "
                                                                    + (value ? "enabled" : "disabled")));
                                                    return 1;
                                                }))))

                        // Status command
                        .then(ClientCommandManager.literal("status")
                                .executes(context -> {
                                    ModConfig config = ModConfig.getInstance();
                                    context.getSource()
                                            .sendFeedback(Text.literal("=== Embellished Tooltips Config ==="));
                                    context.getSource().sendFeedback(Text.literal("Animations:"));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Enabled: " + config.animations.enableAnimations));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Speed: " + config.animations.animationSpeed + "x"));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Rotation: " + config.animations.enableIconRotation));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Scaling: " + config.animations.enableIconScaling));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Timeout: " + config.animations.reHoverTimeoutMs + "ms"));
                                    context.getSource().sendFeedback(Text.literal("Rendering:"));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Enabled: " + config.rendering.enableCustomTooltips));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Armor Preview: " + config.rendering.enableArmorPreview));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Tool Previews: " + config.rendering.enableToolPreviews));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Enchantments: " + config.rendering.enableEnchantmentLines));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Stat Icons: " + config.rendering.showStatIcons));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Scale: " + config.rendering.tooltipScale + "x"));
                                    context.getSource().sendFeedback(Text.literal("Compatibility:"));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Inventory: " + config.compatibility.enableInInventory));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Containers: " + config.compatibility.enableInContainers));
                                    context.getSource().sendFeedback(
                                            Text.literal("  Creative: " + config.compatibility.enableInCreative));
                                    return 1;
                                }))

                        // Reset command
                        .then(ClientCommandManager.literal("reset")
                                .executes(context -> {
                                    ModConfig.getInstance().useDefaults();
                                    ModConfig.getInstance().save();
                                    context.getSource().sendFeedback(Text.literal(
                                            "Configuration reset to defaults"));
                                    return 1;
                                }))));
    }
}
