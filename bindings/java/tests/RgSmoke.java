import apebind.generated.rg_ape.APEProcessException;
import apebind.generated.rg_ape.RgAPEBinding;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

        Path duplicates = Files.createTempDirectory("rg-ape-");
        Files.writeString(duplicates.resolve("dup.txt"), "ape ape\nbeta\n");
        String backreference = RgAPEBinding.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("(\\w+) \\1")
                .path(duplicates)
                .lineNumber(true)
                .pcre2(true)
                .build());
        require(backreference.contains("dup.txt:1:ape ape"), backreference);

        Path literals = Files.createTempDirectory("rg-ape-");
        Files.writeString(literals.resolve("lit.txt"), "abc\na.c\n");
        String fixed = RgAPEBinding.search(
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
        String word = RgAPEBinding.search(
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
        String after = RgAPEBinding.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("two")
                .path(lines)
                .lineNumber(true)
                .afterContext(1L)
                .build());
        String before = RgAPEBinding.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("two")
                .path(lines)
                .lineNumber(true)
                .beforeContext(1L)
                .build());
        String context = RgAPEBinding.search(
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
        String pyOnly = RgAPEBinding.search(
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
        String other = RgAPEBinding.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern("ape")
                .path(inverted)
                .lineNumber(true)
                .invertMatch(true)
                .build());
        require(other.contains("note.txt:2:beta"), other);
        require(!other.contains("alpha ape"), other);

        String rawText = RgAPEBinding.search(List.of("-n", "-F", "a.c", literals.toString()));
        String rawBuilder = RgAPEBinding.search(
            RgAPEBinding.SearchParameters.builder()
                .raw(List.of("-n", "-F", "a.c", literals.toString()))
                .build());
        require(rawText.equals(fixed), rawText);
        require(rawBuilder.equals(fixed), rawBuilder);

        boolean noMatch = false;
        try {
            RgAPEBinding.search(List.of("missing-token", directory.toString()));
        } catch (APEProcessException expected) {
            noMatch = true;
        }
        require(noMatch, "raw no-match should raise");

        boolean both = false;
        try {
            RgAPEBinding.SearchParameters.builder()
                .pattern("ape")
                .raw(List.of("ape", directory.toString()))
                .build();
        } catch (IllegalArgumentException expected) {
            both = true;
        }
        require(both, "pattern and raw should raise");

        boolean explicitFalse = false;
        try {
            RgAPEBinding.SearchParameters.builder()
                .lineNumber(false)
                .raw(List.of("-n", "ape", directory.toString()))
                .build();
        } catch (IllegalArgumentException expected) {
            explicitFalse = true;
        }
        require(explicitFalse, "an explicit false flag and raw should raise");
    }

    private static void require(boolean condition, String actual) {
        if (!condition) {
            throw new AssertionError(actual);
        }
    }
}
