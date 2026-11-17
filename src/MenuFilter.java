import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class MenuFilter {

    public enum FilterResult {
        SAFE,
        UNSAFE,
        UNKNOWN
    }

    private static final Set<String> SUPPORTED_ALLERGENS = Set.of(
            "coconut",
            "eggs",
            "fish",
            "gluten",
            "milk",
            "peanuts",
            "sesame",
            "shellfish",
            "soy",
            "tree nuts",
            "wheat"
    );

    public static FilterResult checkItem(
            MenuParser.Item item,
            List<String> avoid) {

        if (item == null || avoid == null) {
            return FilterResult.UNKNOWN;
        }

        List<String> cleanedAvoid = new ArrayList<>();

        for (String restriction : avoid) {
            if (restriction != null && !restriction.isBlank()) {
                cleanedAvoid.add(restriction);
            }
        }

        avoid = cleanedAvoid;

        if (avoid.isEmpty()) {
            return FilterResult.SAFE;
        }

        if (item.Allergens == null || item.Allergens.isEmpty()) {
            return FilterResult.UNKNOWN;
        }

        boolean hasUnknownRestriction = false;

        for (String restriction : avoid) {
            if (!SUPPORTED_ALLERGENS.contains(normalize(restriction))) {
                hasUnknownRestriction = true;
            }
        }

        for (MenuParser.Allergen allergen : item.Allergens) {

            if (allergen == null || allergen.Name == null) {
                continue;
            }

            String name = normalize(allergen.Name);

            // These describe the diet and not the allergens in the food
            if (name.equals("vegetarian") || name.equals("vegan")) {
                continue;
            }

            if (allergen.Value) {

                for (String restriction : avoid) {

                    if (name.equals(normalize(restriction))) {
                        // A confirmed match comes first even if other information is missing
                        return FilterResult.UNSAFE;
                    }
                }
            }
        }

        // We cant verify restrictions that are not supported yet
        if (hasUnknownRestriction || !item.NutritionReady) {
            return FilterResult.UNKNOWN;
        }

        return FilterResult.SAFE;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase();
    }
}
