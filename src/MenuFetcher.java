import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MenuFetcher {

    public static void main(String[] args) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hfs.purdue.edu/menus/v2/locations/Earhart/2026-11-23"))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());

        String body = response.body();

        System.out.println("\nResponse Preview:");

        System.out.println(
                body.substring(0, Math.min(body.length(), 500))
        );
    }
}