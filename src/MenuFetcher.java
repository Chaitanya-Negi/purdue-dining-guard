import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonSyntaxException;

public class MenuFetcher {

    public static void main(String[] args) {

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

                System.out.println("Location: " + menu.Location);
                System.out.println("Date: " + menu.Date);
                System.out.println("Published: " + menu.IsPublished);

                if (menu.Meals != null && !menu.Meals.isEmpty()) {

                    System.out.println("Number of meals: " + menu.Meals.size());

                    MenuParser.Meal firstMeal = menu.Meals.get(0);

                    System.out.println("First meal: " + firstMeal.Name);
                    System.out.println("Meal type: " + firstMeal.Type);
                    System.out.println("Meal status: " + firstMeal.Status);

                    if (firstMeal.Stations != null && !firstMeal.Stations.isEmpty()) {

                        System.out.println("Number of stations: " + firstMeal.Stations.size());

                        MenuParser.Station firstStation = firstMeal.Stations.get(0);

                        System.out.println("First station: " + firstStation.Name);

                        if (firstStation.Items != null && !firstStation.Items.isEmpty()) {

                            MenuParser.Item firstItem = firstStation.Items.get(0);

                            System.out.println("First food item: " + firstItem.Name);
                            System.out.println("Vegetarian: " + firstItem.IsVegetarian);
                            System.out.println("Nutrition ready: " + firstItem.NutritionReady);

                            if (firstItem.Allergens != null && !firstItem.Allergens.isEmpty()) {

                                System.out.println("Allergens:");

                                for (MenuParser.Allergen allergen : firstItem.Allergens) {
                                    System.out.println(allergen.Name + ": " + allergen.Value);
                                }

                            } else {
                                System.out.println("No allergen data available.");
                            }

                        } else {
                            System.out.println("No food items available.");
                        }

                    } else {
                        System.out.println("No station data available.");
                    }

                } else {
                    System.out.println("No meal data available.");
                }

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