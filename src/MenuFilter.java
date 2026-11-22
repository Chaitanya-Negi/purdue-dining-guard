
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

    public static List<String> getUnsupportedRestrictions(List<String> avoid) {

        List<String> unsupported = new ArrayList<>();

        if (avoid == null) {
            return unsupported;
        }

        for (String restriction : avoid) {

            if (restriction == null || restriction.isBlank()) {
                continue;
            }

            String name = normalize(restriction);

            if (!SUPPORTED_ALLERGENS.contains(name)
                    && !unsupported.contains(name)) {
                unsupported.add(name);
            }
        }

        return unsupported;
    }

    public static FilterResult checkItem(
            MenuParser.Item item,
            List<String> avoid,
            boolean requireVegetarian) {

        if (item == null || avoid == null) {
            return FilterResult.UNKNOWN;
        }

        List<String> cleanedAvoid = new ArrayList<>();

        for (String restriction : avoid) {
            if (restriction != null && !restriction.isBlank()) {
                cleanedAvoid.add(normalize(restriction));
            }
        }

        avoid = cleanedAvoid;

        boolean hasUnknownRestriction =
                !getUnsupportedRestrictions(avoid).isEmpty();

        // check allergen info
        if (item.Allergens == null || item.Allergens.isEmpty()) {

            if (!avoid.isEmpty()) {
                return FilterResult.UNKNOWN;
            }

        } else {

            for (MenuParser.Allergen allergen : item.Allergens) {

                if (allergen == null || allergen.Name == null) {
                    continue;
                }

                String name = normalize(allergen.Name);

                if (name.equals("vegetarian") || name.equals("vegan")) {
                    continue;
                }

                if (allergen.Value && avoid.contains(name)) {
                    // confirmed allergen match always ----> UNSAFE
                    return FilterResult.UNSAFE;
                }
            }
        }

        // check vegetarian requirement
        if (requireVegetarian) {

            if (!item.NutritionReady || item.IsVegetarian == null) {
                return FilterResult.UNKNOWN;
            }

            if (!item.IsVegetarian) {
                return FilterResult.UNSAFE;
            }
        }

        // unknown for anything that is not supported yet
        if (hasUnknownRestriction || !item.NutritionReady) {
            return FilterResult.UNKNOWN;
        }

        return FilterResult.SAFE;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase();
    }
}
