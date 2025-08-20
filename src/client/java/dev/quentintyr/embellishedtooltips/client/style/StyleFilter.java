package dev.quentintyr.embellishedtooltips.client.style;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.enchantment.Enchantment;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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

        // Parse rarity filter
        if (json.has("rarity")) {
            String rarity = json.get("rarity").getAsString();
            filter.predicates.add((stack) -> {
                return stack.getRarity().name().toLowerCase().equals(rarity.toLowerCase());
            });
        }

        // Parse item filter
        if (json.has("item")) {
            String itemId = json.get("item").getAsString();
            Identifier id = new Identifier(itemId);
            filter.predicates.add((stack) -> {
                return Registries.ITEM.getId(stack.getItem()).equals(id);
            });
        }

        // Parse priority
        if (json.has("priority")) {
            filter.priority = json.get("priority").getAsInt();
        }

        return filter;
    }

    @Override
    public boolean test(ItemStack stack) {
        // Empty predicates list means match nothing
        if (predicates.isEmpty()) {
            return false;
        }

        // All predicates must match
        for (Predicate<ItemStack> predicate : predicates) {
            if (!predicate.test(stack)) {
                return false;
            }
        }
        return true;
    }
}

// while (var2.hasNext()) {
// element = (JsonElement) var2.next();
// predicate.MODS.add(element.getAsString().toLowerCase());
// }
// }

// if (root.has("rarity")) {
// predicate.RARITIES.add(root.get("rarity").getAsString().toLowerCase());
// }

// if (root.has("rarities")) {
// var2 = root.get("rarities").getAsJsonArray().iterator();

// while (var2.hasNext()) {
// element = (JsonElement) var2.next();
// predicate.RARITIES.add(element.getAsString().toLowerCase());
// }
// }

// JsonObject keywords;
// Iterator var6;
// if (root.has("tag")) {
// keywords = root.getAsJsonObject("tag");
// var6 = keywords.keySet().iterator();

// while (var6.hasNext()) {
// String key = (String) var6.next();
// predicate.TAG.add(key, keywords.get(key));
// }
// }

// JsonElement element;
// if (root.has("enchantments")) {
// keywords = root.getAsJsonObject("enchantments");
// if (keywords.has("any_match")) {
// var6 = keywords.get("any_match").getAsJsonArray().iterator();

// while (var6.hasNext()) {
// element = (JsonElement) var6.next();
// predicate.ENCHANTMENTS_ANY_MATCH.add(element.getAsString().toLowerCase());
// }
// }

// if (keywords.has("all_match")) {
// var6 = keywords.get("all_match").getAsJsonArray().iterator();

// while (var6.hasNext()) {
// element = (JsonElement) var6.next();
// predicate.ENCHANTMENTS_ALL_MATCH.add(element.getAsString().toLowerCase());
// }
// }
// }

// if (root.has("keywords")) {
// keywords = root.getAsJsonObject("keywords");
// if (keywords.has("any_match")) {
// var6 = keywords.get("any_match").getAsJsonArray().iterator();

// while (var6.hasNext()) {
// element = (JsonElement) var6.next();
// predicate.KEYWORDS_ANY_MATCH.add(element.getAsString().toLowerCase());
// }
// }

// if (keywords.has("all_match")) {
// var6 = keywords.get("all_match").getAsJsonArray().iterator();

// while (var6.hasNext()) {
// element = (JsonElement) var6.next();
// predicate.KEYWORDS_ALL_MATCH.add(element.getAsString().toLowerCase());
// }
// }

// if (keywords.has("none_match")) {
// var6 = keywords.get("none_match").getAsJsonArray().iterator();

// while (var6.hasNext()) {
// element = (JsonElement) var6.next();
// predicate.KEYWORDS_NONE_MATCH.add(element.getAsString().toLowerCase());
// }
// }
// }

// return predicate;
// }

// public boolean test(ItemStack stack) {
// if (!this.MODS.isEmpty() && !this.MODS.contains(this.getNamespace(stack))) {
// return false;
// } else if (!this.RARITIES.isEmpty() &&
// !this.RARITIES.contains(stack.m_41791_().name().toLowerCase())) {
// return false;
// } else if (this.TAG.size() > 0 && this.hasTagMismatch(stack)) {
// return false;
// } else if (!this.ENCHANTMENTS_ANY_MATCH.isEmpty()
// && this.hasEnchantmentMismatch(stack, this.ENCHANTMENTS_ANY_MATCH, (results)
// -> {
// return results.stream().anyMatch((result) -> {
// return result;
// });
// })) {
// return false;
// } else if (!this.ENCHANTMENTS_ALL_MATCH.isEmpty()
// && this.hasEnchantmentMismatch(stack, this.ENCHANTMENTS_ALL_MATCH, (results)
// -> {
// return results.stream().allMatch((result) -> {
// return result;
// });
// })) {
// return false;
// } else if (!this.KEYWORDS_ANY_MATCH.isEmpty()
// && this.hasKeywordMismatch(stack, this.KEYWORDS_ANY_MATCH, (results) -> {
// return results.stream().anyMatch((result) -> {
// return result;
// });
// })) {
// return false;
// } else if (!this.KEYWORDS_ALL_MATCH.isEmpty()
// && this.hasKeywordMismatch(stack, this.KEYWORDS_ALL_MATCH, (results) -> {
// return results.stream().allMatch((result) -> {
// return result;
// });
// })) {
// return false;
// } else if (!this.KEYWORDS_NONE_MATCH.isEmpty()
// && this.hasKeywordMismatch(stack, this.KEYWORDS_NONE_MATCH, (results) -> {
// return results.stream().noneMatch((result) -> {
// return result;
// });
// })) {
// return false;
// } else {
// return this.ITEMS.isEmpty() || this.ITEMS.contains(stack.m_41720_());
// }
// }

// public String toString() {
// return "StyleFilter<%s>".formatted(new Object[] { this.priority });
// }

// private boolean hasEnchantmentMismatch(ItemStack stack, List<String>
// enchantments, StyleFilter.Mode mode) {
// List<Boolean> results = new ArrayList();
// Set<Enchantment> set = stack.getAllEnchantments().keySet();
// Iterator var6 = enchantments.iterator();

// while (var6.hasNext()) {
// String key = (String) var6.next();
// results.add(set.stream().anyMatch((enchantment) -> {
// ResourceLocation enchantmentKey =
// ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
// return enchantmentKey != null && enchantmentKey.toString().equals(key);
// }));
// }

// return !mode.test(results);
// }

// private boolean hasKeywordMismatch(ItemStack stack, List<String> keywords,
// StyleFilter.Mode mode) {
// List<Boolean> results = new ArrayList();
// Iterator var5 = keywords.iterator();

// while (var5.hasNext()) {
// String key = (String) var5.next();
// if (key.equals("enchanted")) {
// results.add(stack.m_41793_());
// }

// if (key.equals("enchanted_book")) {
// results.add(stack.m_41720_() instanceof EnchantedBookItem);
// }

// if (key.equals("cursed")) {
// results.add(stack.getAllEnchantments().entrySet().stream().anyMatch((entry)
// -> {
// return ((Enchantment) entry.getKey()).m_6589_();
// }));
// }

// if (key.equals("foil")) {
// results.add(stack.m_41790_());
// }
// }

// return !mode.test(results);
// }

// private boolean hasTagMismatch(ItemStack stack) {
// if (!stack.m_41782_()) {
// return true;
// } else {
// try {
// Iterator var2 = this.TAG.keySet().iterator();

// String key;
// JsonPrimitive value;
// do {
// if (!var2.hasNext()) {
// return false;
// }

// key = (String) var2.next();
// if (!stack.m_41784_().m_128441_(key)) {
// return true;
// }

// if (!this.TAG.get(key).isJsonPrimitive()) {
// return true;
// }

// value = this.TAG.get(key).getAsJsonPrimitive();
// } while (value.isString() &&
// value.getAsString().equals(stack.m_41784_().m_128461_(key))
// || value.isBoolean() && value.getAsBoolean() ==
// stack.m_41784_().m_128471_(key)
// || value.isNumber() && (value.getAsInt() == stack.m_41784_().m_128451_(key)
// || value.getAsDouble() == stack.m_41784_().m_128459_(key)
// || value.getAsFloat() == stack.m_41784_().m_128457_(key)
// || value.getAsByte() == stack.m_41784_().m_128445_(key)));

// return true;
// } catch (Exception var5) {
// return true;
// }
// }
// }

// private String getNamespace(ItemStack stack) {
// ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.m_41720_());
// return key != null ? key.m_135827_() : "null";
// }

// @FunctionalInterface
// protected interface Mode {
// boolean test(List<Boolean> var1);
// }
// }
