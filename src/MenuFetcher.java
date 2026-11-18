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

import com.google.gson.JsonSyntaxException;

public class MenuFetcher {

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

                // track filtering results
                int safeCount = 0;
                int unsafeCount = 0;
                int unknownCount = 0;

                System.out.println("\n========== DINING MENU ==========");

                // checks every meal
                for (MenuParser.Meal meal : menu.Meals) {

                    if (meal == null) {
                        continue;
                    }

                    System.out.println("\nMEAL: " + meal.Name);
                    System.out.println("Type: " + meal.Type);
                    System.out.println("Status: " + meal.Status);

                    if (meal.Stations == null || meal.Stations.isEmpty()) {
                        System.out.println("No station data available.");
                        continue;
                    }

                    // check every station
                    for (MenuParser.Station station : meal.Stations) {

                        if (station == null) {
                            continue;
                        }

                        System.out.println("\n  STATION: " + station.Name);

                        if (station.Items == null || station.Items.isEmpty()) {
                            System.out.println("  No food items available.");
                            continue;
                        }

                        // check every food item
                        for (MenuParser.Item item : station.Items) {

                            String itemName;

                            if (item == null) {
                                itemName = "Unknown item";
                            } else {
                                itemName = item.Name;
                            }

                            // apply dietary restrictions
                            MenuFilter.FilterResult result =
                                    MenuFilter.checkItem(
                                            item,
                                            avoid,
                                            requireVegetarian
                                    );

                            System.out.println("    " + itemName + " -> " + result);

                            // update result counts
                            switch (result) {
                                case SAFE:
                                    safeCount++;
                                    break;

                                case UNSAFE:
                                    unsafeCount++;
                                    break;

                                case UNKNOWN:
                                    unknownCount++;
                                    break;
                            }
                        }
                    }
                }

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
}
