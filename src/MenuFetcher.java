
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.io.IOException;
import java.time.Duration;

public class MenuFetcher {

    public static void main(String[] args) {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hfs.purdue.edu/menus/v2/locations/Earhart/2026-11-23"))
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

            System.out.println("\nResponse Preview:");

            System.out.println(
                    body.substring(0, Math.min(body.length(), 500))
            );

        } catch (HttpTimeoutException e) {
            System.out.println("Error: The request timed out. The server may be taking too long to respond.");

        } catch (IOException e) {
            System.out.println("Error: Unable to connect to the Purdue Dining API.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Error: The API request was interrupted.");
        }
    }
}
