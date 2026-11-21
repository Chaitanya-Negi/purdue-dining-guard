import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import com.google.gson.JsonSyntaxException;

public class MenuFetcher {

    static class MealResults {
        String mealName;
        List<StationResults> stations = new ArrayList<>();

        MealResults(String mealName) {
            this.mealName = mealName;
        }
    }

    static class StationResults {
        String stationName;
        List<MenuParser.Item> items = new ArrayList<>();

        StationResults(String stationName) {
            this.stationName = stationName;
        }
    }

    public static void main(String[] args) {

        // dietary preferences
        List<String> avoid = List.of("peanuts");
        boolean requireVegetarian = false;

        // today's date
        String today = LocalDate.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );

        String url = "https://api.hfs.purdue.edu/menus/v2/locations/Earhart/" + today;

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status Code: " + response.statusCode());

            if (response.statusCode() != 200) {
                System.out.println("Error: Unable to retrieve menu data.");
                return;
            }

            String body = response.body();

            try {
                MenuParser.Menu menu = MenuParser.parseMenu(body);

                if (menu == null) {
                    System.out.println("Error: The API returned an empty menu.");
                    return;
                }

                System.out.println("\nLocation: " + menu.Location);
                System.out.println("Date: " + menu.Date);
                System.out.println("Published: " + menu.IsPublished);

                if (menu.Meals == null || menu.Meals.isEmpty()) {
                    System.out.println("No meal data available.");
                    return;
                }

                // store filtering results took way too much time than needed XD
                List<MealResults> safeItems = new ArrayList<>();
                List<MealResults> unknownItems = new ArrayList<>();

                int safeCount = 0;
                int unsafeCount = 0;
                int unknownCount = 0;

                // check every meal
                for (MenuParser.Meal meal : menu.Meals) {

                    if (meal == null || meal.Stations == null) {
                        continue;
                    }

                    // check every station
                    for (MenuParser.Station station : meal.Stations) {

                        if (station == null || station.Items == null) {
                            continue;
                        }

                        // check every food item
                        for (MenuParser.Item item : station.Items) {

                            // apply dietary restrictions
                            MenuFilter.FilterResult result =
                                    MenuFilter.checkItem(
                                            item,
                                            avoid,
                                            requireVegetarian
                                    );

                            switch (result) {
                                case SAFE:
                                    safeCount++;
                                    addItem(
                                            safeItems,
                                            meal.Name,
                                            station.Name,
                                            item
                                    );
                                    break;

                                case UNSAFE:
                                    unsafeCount++;
                                    break;

                                case UNKNOWN:
                                    unknownCount++;
                                    addItem(
                                            unknownItems,
                                            meal.Name,
                                            station.Name,
                                            item
                                    );
                                    break;
                            }
                        }
                    }
                }

                // print safe items
                System.out.println("\n========== SAFE FOOD OPTIONS ==========");
                printResults(safeItems);

                // print unknown items
                System.out.println("\n========== COULD NOT VERIFY ==========");
                printResults(unknownItems);

                // summary
                System.out.println("\n========== FILTER SUMMARY ==========");
                System.out.println("SAFE: " + safeCount);
                System.out.println("UNSAFE: " + unsafeCount);
                System.out.println("UNKNOWN: " + unknownCount);

                System.out.println("Total items: "
                        + (safeCount + unsafeCount + unknownCount));

            } catch (JsonSyntaxException e) {
                System.out.println("Error: The API returned invalid JSON.");
            }

        } catch (HttpTimeoutException e) {
            System.out.println("Error: The request timed out.");

        } catch (IOException e) {
            System.out.println("Error: Unable to connect to the Purdue Dining API.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Error: The API request was interrupted.");
        }
    }

    // add an item to its meal and station
    private static void addItem(
            List<MealResults> results,
            String mealName,
            String stationName,
            MenuParser.Item item) {

        MealResults mealResult = null;

        for (MealResults meal : results) {
            if (Objects.equals(meal.mealName, mealName)) {
                mealResult = meal;
                break;
            }
        }

        if (mealResult == null) {
            mealResult = new MealResults(mealName);
            results.add(mealResult);
        }

        StationResults stationResult = null;

        for (StationResults station : mealResult.stations) {
            if (Objects.equals(station.stationName, stationName)) {
                stationResult = station;
                break;
            }
        }

        if (stationResult == null) {
            stationResult = new StationResults(stationName);
            mealResult.stations.add(stationResult);
        }

        stationResult.items.add(item);
    }

    // print grouped results
    private static void printResults(List<MealResults> results) {

        if (results.isEmpty()) {
            System.out.println("No items to display.");
            return;
        }

        for (MealResults meal : results) {

            String mealName = meal.mealName;

            if (mealName == null || mealName.isBlank()) {
                mealName = "Unknown meal";
            }

            System.out.println();
            System.out.println("##################################");
            System.out.println("  " + mealName.toUpperCase());
            System.out.println("##################################");

            for (StationResults station : meal.stations) {

                String stationName = station.stationName;

                if (stationName == null || stationName.isBlank()) {
                    stationName = "Unknown station";
                }

                System.out.println();
                System.out.println("  > " + stationName);
                System.out.println("  " + "-".repeat(stationName.length() + 2));

                for (MenuParser.Item item : station.items) {

                    String itemName;

                    if (item == null || item.Name == null) {
                        itemName = "Unknown item";
                    } else {
                        itemName = item.Name;
                    }

                    System.out.println("      - " + itemName);
                }
            }
        }
    }
}