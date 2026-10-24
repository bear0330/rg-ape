import apebind.generated.rg_ape.RgAPEBinding;

import java.nio.file.Files;
import java.nio.file.Path;

public class RgSmoke {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("rg-ape-");
        Files.writeString(directory.resolve("note.txt"), "alpha ape\nbeta\n");

        String text = RgAPEBinding.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("ape")
                .path(directory)
                .lineNumber(true)
                .build());
        require(text.contains("note.txt:1:alpha ape"), text);
    }

    private static void require(boolean condition, String actual) {
        if (!condition) {
            throw new AssertionError(actual);
        }
    }
}
