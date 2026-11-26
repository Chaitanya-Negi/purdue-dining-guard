import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

public class ProfileManager {

    private static final Path PROFILE_PATH =
            Path.of("data", "profile.txt");

    public static void saveProfile(
            List<String> avoid,
            boolean requireVegetarian) throws IOException {

        Files.createDirectories(PROFILE_PATH.getParent());

        String profile =
                "avoid=" + String.join(",", avoid)
                + System.lineSeparator()
                + "vegetarian=" + requireVegetarian
                + System.lineSeparator();

        Files.writeString(
                PROFILE_PATH,
                profile,
                StandardCharsets.UTF_8
        );
    }

    private static void warnInvalid() {
        System.out.println(
                "Warning: Saved profile is invalid. Running with no restrictions."
        );
    }

    public static boolean loadProfile(MenuFetcher.Options options) {

        if (!Files.exists(PROFILE_PATH)) {
            System.out.println("No saved profile found.");
            return false;
        }

        try {

            List<String> lines = Files.readAllLines(
                    PROFILE_PATH,
                    StandardCharsets.UTF_8
            );

            List<String> loadedAvoid = new ArrayList<>();
            boolean loadedVegetarian = false;
            boolean avoidFound = false;
            boolean vegetarianFound = false;

            for (String line : lines) {

                if (line.startsWith("avoid=")) {

                    if (avoidFound) {
                        warnInvalid();
                        return false;
                    }

                    avoidFound = true;

                    String restrictions = line.substring(6);

                    if (!restrictions.isBlank()) {

                        for (String restriction : restrictions.split(",")) {

                            restriction = restriction.trim().toLowerCase();

                            if (!restriction.isEmpty()
                                    && !loadedAvoid.contains(restriction)) {
                                loadedAvoid.add(restriction);
                            }
                        }
                    }

                } else if (line.startsWith("vegetarian=")) {

                    if (vegetarianFound) {
                        warnInvalid();
                        return false;
                    }

                    vegetarianFound = true;

                    String value = line.substring(11).trim();

                    if (!value.equalsIgnoreCase("true")
                            && !value.equalsIgnoreCase("false")) {
                        warnInvalid();
                        return false;
                    }

                    loadedVegetarian = Boolean.parseBoolean(value);

                } else if (!line.isBlank()) {

                    warnInvalid();
                    return false;
                }
            }

            if (!avoidFound || !vegetarianFound) {
                System.out.println(
                        "Warning: Saved profile is incomplete. Running with no restrictions."
                );
                return false;
            }

            options.avoid.clear();
            options.avoid.addAll(loadedAvoid);
            options.requireVegetarian = loadedVegetarian;

            System.out.println("Saved profile loaded.");

            return true;

        } catch (IOException e) {

            System.out.println(
                    "Warning: Unable to load saved profile. Running with no restrictions."
            );
            return false;
        }
    }
}