package dev.quentintyr.embellishedtooltips.client.style;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.item.ItemStack;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.nbt.NbtCompound;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class StyleFilter implements Predicate<ItemStack> {
    public int priority = 0;
    private final List<Predicate<ItemStack>> predicates = new ArrayList<>();

    /**
     * Creates a new empty style filter.
     */
    public StyleFilter() {
        // Default constructor creates a filter that matches nothing
    }

    /**
     * Creates a style filter from a JSON object.
     *
     * @param json The JSON object to parse.
     * @return A new style filter.
     */
    public static StyleFilter fromJson(JsonObject json) {
        StyleFilter filter = new StyleFilter();

        // Parse priority
        if (json.has("priority")) {
            filter.priority = json.get("priority").getAsInt();
        }

        // Parse mod filter
        if (json.has("mod")) {
            String modId = json.get("mod").getAsString();
            filter.predicates.add(stack -> getNamespace(stack).equals(modId.toLowerCase()));
        }

        // Parse mods filter (array)
        if (json.has("mods")) {
            JsonArray modsArray = json.get("mods").getAsJsonArray();
            List<String> mods = new ArrayList<>();
            for (JsonElement element : modsArray) {
                mods.add(element.getAsString().toLowerCase());
            }
            filter.predicates.add(stack -> mods.contains(getNamespace(stack)));
        }

        // Parse rarity filter
        if (json.has("rarity")) {
            String rarity = json.get("rarity").getAsString();
            filter.predicates.add(stack -> stack.getRarity().name().toLowerCase().equals(rarity.toLowerCase()));
        }

        // Parse rarities filter (array)
        if (json.has("rarities")) {
            JsonArray raritiesArray = json.get("rarities").getAsJsonArray();
            List<String> rarities = new ArrayList<>();
            for (JsonElement element : raritiesArray) {
                rarities.add(element.getAsString().toLowerCase());
            }
            filter.predicates.add(stack -> rarities.contains(stack.getRarity().name().toLowerCase()));
        }

        // Parse item filter
        if (json.has("item")) {
            String itemId = json.get("item").getAsString();
            Identifier id = new Identifier(itemId);
            filter.predicates.add(stack -> Registries.ITEM.getId(stack.getItem()).equals(id));
        }

        // Parse items filter (array)
        if (json.has("items")) {
            JsonArray itemsArray = json.get("items").getAsJsonArray();
            List<Identifier> items = new ArrayList<>();
            for (JsonElement element : itemsArray) {
                items.add(new Identifier(element.getAsString()));
            }
            filter.predicates.add(stack -> items.contains(Registries.ITEM.getId(stack.getItem())));
        }

        // Parse tag filter
        if (json.has("tag")) {
            JsonObject tagObject = json.getAsJsonObject("tag");
            filter.predicates.add(stack -> !hasTagMismatch(stack, tagObject));
        }

        // Parse enchantments filter
        if (json.has("enchantments")) {
            JsonObject enchantments = json.getAsJsonObject("enchantments");

            if (enchantments.has("any_match")) {
                JsonArray anyMatch = enchantments.getAsJsonArray("any_match");
                List<String> enchantmentList = new ArrayList<>();
                for (JsonElement element : anyMatch) {
                    enchantmentList.add(element.getAsString().toLowerCase());
                }
                filter.predicates.add(stack -> !hasEnchantmentMismatch(stack, enchantmentList,
                        results -> results.stream().anyMatch(Boolean::booleanValue)));
            }

            if (enchantments.has("all_match")) {
                JsonArray allMatch = enchantments.getAsJsonArray("all_match");
                List<String> enchantmentList = new ArrayList<>();
                for (JsonElement element : allMatch) {
                    enchantmentList.add(element.getAsString().toLowerCase());
                }
                filter.predicates.add(stack -> !hasEnchantmentMismatch(stack, enchantmentList,
                        results -> results.stream().allMatch(Boolean::booleanValue)));
            }
        }

        // Parse keywords filter
        if (json.has("keywords")) {
            JsonObject keywords = json.getAsJsonObject("keywords");

            if (keywords.has("any_match")) {
                JsonArray anyMatch = keywords.getAsJsonArray("any_match");
                List<String> keywordList = new ArrayList<>();
                for (JsonElement element : anyMatch) {
                    keywordList.add(element.getAsString().toLowerCase());
                }
                filter.predicates.add(stack -> !hasKeywordMismatch(stack, keywordList,
                        results -> results.stream().anyMatch(Boolean::booleanValue)));
            }

            if (keywords.has("all_match")) {
                JsonArray allMatch = keywords.getAsJsonArray("all_match");
                List<String> keywordList = new ArrayList<>();
                for (JsonElement element : allMatch) {
                    keywordList.add(element.getAsString().toLowerCase());
                }
                filter.predicates.add(stack -> !hasKeywordMismatch(stack, keywordList,
                        results -> results.stream().allMatch(Boolean::booleanValue)));
            }

            if (keywords.has("none_match")) {
                JsonArray noneMatch = keywords.getAsJsonArray("none_match");
                List<String> keywordList = new ArrayList<>();
                for (JsonElement element : noneMatch) {
                    keywordList.add(element.getAsString().toLowerCase());
                }
                filter.predicates.add(stack -> !hasKeywordMismatch(stack, keywordList,
                        results -> results.stream().noneMatch(Boolean::booleanValue)));
            }
        }

        return filter;
    }

    @Override
    public boolean test(ItemStack stack) {
        // Empty predicates list means match everything
        if (predicates.isEmpty()) {
            return true;
        }

        // All predicates must match
        for (Predicate<ItemStack> predicate : predicates) {
            if (!predicate.test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "StyleFilter<%s>".formatted(priority);
    }

    private static String getNamespace(ItemStack stack) {
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return id != null ? id.getNamespace() : "null";
    }

    private static boolean hasTagMismatch(ItemStack stack, JsonObject tagObject) {
        if (!stack.hasNbt()) {
            return true;
        }

        try {
            NbtCompound nbt = stack.getNbt();
            for (String key : tagObject.keySet()) {
                if (!nbt.contains(key)) {
                    return true;
                }

                JsonElement jsonValue = tagObject.get(key);
                if (!jsonValue.isJsonPrimitive()) {
                    return true;
                }

                JsonPrimitive primitive = jsonValue.getAsJsonPrimitive();

                if (primitive.isString()) {
                    if (!primitive.getAsString().equals(nbt.getString(key))) {
                        return true;
                    }
                } else if (primitive.isBoolean()) {
                    if (primitive.getAsBoolean() != nbt.getBoolean(key)) {
                        return true;
                    }
                } else if (primitive.isNumber()) {
                    if (primitive.getAsInt() != nbt.getInt(key) &&
                            primitive.getAsDouble() != nbt.getDouble(key) &&
                            primitive.getAsFloat() != nbt.getFloat(key) &&
                            primitive.getAsByte() != nbt.getByte(key)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean hasEnchantmentMismatch(ItemStack stack, List<String> enchantments, Mode mode) {
        List<Boolean> results = new ArrayList<>();
        var enchantmentMap = EnchantmentHelper.get(stack);

        for (String enchantmentId : enchantments) {
            results.add(enchantmentMap.keySet().stream().anyMatch(enchantment -> {
                Identifier enchantmentKey = Registries.ENCHANTMENT.getId(enchantment);
                return enchantmentKey != null && enchantmentKey.toString().equals(enchantmentId);
            }));
        }

        return !mode.test(results);
    }

    private static boolean hasKeywordMismatch(ItemStack stack, List<String> keywords, Mode mode) {
        List<Boolean> results = new ArrayList<>();

        for (String keyword : keywords) {
            switch (keyword) {
                case "enchanted":
                    results.add(stack.hasEnchantments());
                    break;
                case "enchanted_book":
                    results.add(stack.getItem() instanceof EnchantedBookItem);
                    break;
                case "cursed":
                    var enchantmentMap = EnchantmentHelper.get(stack);
                    results.add(enchantmentMap.keySet().stream().anyMatch(Enchantment::isCursed));
                    break;
                case "foil":
                    results.add(stack.hasGlint());
                    break;
                default:
                    results.add(false);
                    break;
            }
        }

        return !mode.test(results);
    }

    @FunctionalInterface
    protected interface Mode {
        boolean test(List<Boolean> results);
    }
}
