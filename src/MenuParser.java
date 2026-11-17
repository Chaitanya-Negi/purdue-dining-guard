import com.google.gson.Gson;
import java.util.List;

public class MenuParser {

    static class Menu {
        String Location;
        String Date;
        boolean IsPublished;
        List<Meal> Meals;
    }

    static class Meal {
        String Name;
        String Type;
        String Status;
        List<Station> Stations;
    }

    static class Station {
        String Name;
        List<Item> Items;
    }

    static class Item {
        String Name;
        boolean IsVegetarian;
        boolean NutritionReady;
        List<Allergen> Allergens;
    }

    static class Allergen {
        String Name;
        boolean Value;
    }

    public static Menu parseMenu(String json) {

        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        Gson gson = new Gson();
        return gson.fromJson(json, Menu.class);
    }
}