import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
}