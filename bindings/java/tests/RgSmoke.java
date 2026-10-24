import apebind.generated.rg_ape.APEProcessException;
import apebind.generated.rg_ape.RgAPEBinding;

import rg_ape.SearchClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RgSmoke {
    public static void main(String[] args) throws Exception {
        SearchClient client = new SearchClient();
        Path directory = Files.createTempDirectory("rg-ape-");
        Files.writeString(directory.resolve("note.txt"), "alpha ape\nbeta\n");

        String text = client.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("ape")
                .path(directory)
                .lineNumber(true)
                .build());
        require(text.contains("note.txt:1:alpha ape"), text);

        Path duplicates = Files.createTempDirectory("rg-ape-");
        Files.writeString(duplicates.resolve("dup.txt"), "ape ape\nbeta\n");
        String backreference = client.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("(\\w+) \\1")
                .path(duplicates)
                .lineNumber(true)
                .pcre2(true)
                .build());
        require(backreference.contains("dup.txt:1:ape ape"), backreference);

        Path literals = Files.createTempDirectory("rg-ape-");
        Files.writeString(literals.resolve("lit.txt"), "abc\na.c\n");
        String fixed = client.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("a.c")
                .path(literals)
                .lineNumber(true)
                .fixedStrings(true)
                .build());
        require(fixed.contains("lit.txt:2:a.c"), fixed);
        require(!fixed.contains("lit.txt:1:abc"), fixed);

        Path words = Files.createTempDirectory("rg-ape-");
        Files.writeString(words.resolve("words.txt"), "alpha ape\napex\n");
        String word = client.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("ape")
                .path(words)
                .lineNumber(true)
                .wordRegexp(true)
                .build());
        require(word.contains("words.txt:1:alpha ape"), word);
        require(!word.contains("words.txt:2:apex"), word);

        Path lines = Files.createTempDirectory("rg-ape-");
        Files.writeString(lines.resolve("note.txt"), "one\ntwo\nthree\nfour\n");
        String after = client.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("two")
                .path(lines)
                .lineNumber(true)
                .afterContext(1L)
                .build());
        String before = client.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("two")
                .path(lines)
                .lineNumber(true)
                .beforeContext(1L)
                .build());
        String context = client.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("two")
                .path(lines)
                .lineNumber(true)
                .context(1L)
                .build());
        require(after.contains("note.txt:2:two") && after.contains("note.txt-3-three"), after);
        require(!after.contains("note.txt-1-one"), after);
        require(before.contains("note.txt-1-one") && !before.contains("note.txt-3-three"), before);
        require(context.contains("note.txt-1-one") && context.contains("note.txt-3-three"), context);
        require(!context.contains("note.txt-4-four"), context);

        Path typed = Files.createTempDirectory("rg-ape-");
        Files.writeString(typed.resolve("code.py"), "needle\n");
        Files.writeString(typed.resolve("code.txt"), "needle\n");
        String pyOnly = client.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("needle")
                .path(typed)
                .lineNumber(true)
                .fileType("py")
                .build());
        require(pyOnly.contains("code.py:1:needle"), pyOnly);
        require(!pyOnly.contains("code.txt"), pyOnly);

        Path inverted = Files.createTempDirectory("rg-ape-");
        Files.writeString(inverted.resolve("note.txt"), "alpha ape\nbeta\n");
        String other = client.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("ape")
                .path(inverted)
                .lineNumber(true)
                .invertMatch(true)
                .build());
        require(other.contains("note.txt:2:beta"), other);
        require(!other.contains("alpha ape"), other);

        String rawText = client.search(List.of("-n", "-F", "a.c", literals.toString()));
        String rawWithoutException = client.search(
            List.of("-n", "-F", "a.c", literals.toString()),
            false);
        require(rawText.equals(fixed), rawText);
        require(rawWithoutException.equals(fixed), rawWithoutException);

        boolean noMatch = false;
        try {
            client.search(List.of("missing-token", directory.toString()));
        } catch (APEProcessException expected) {
            noMatch = true;
        }
        require(noMatch, "raw no-match should raise");

        String emptyNamed = client.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("missing-token")
                .path(directory)
                .build(),
            false);
        String emptyRaw = client.search(
            List.of("missing-token", directory.toString()),
            false);
        require(emptyNamed.isEmpty(), emptyNamed);
        require(emptyRaw.isEmpty(), emptyRaw);

        boolean failedPath = false;
        try {
            client.search(
                List.of("ape", directory.resolve("not-found.txt").toString()),
                false);
        } catch (APEProcessException error) {
            require(error.result().returnCode() == 2, error.getMessage());
            failedPath = true;
        }
        require(failedPath, "execution errors must still raise");

    }

    private static void require(boolean condition, String actual) {
        if (!condition) {
            throw new AssertionError(actual);
        }
    }
}
