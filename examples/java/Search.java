import apebind.generated.rg_ape.RgAPEBinding;

import java.nio.file.Path;

public class Search {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: Search PATTERN PATH");
            System.exit(2);
        }

        String text = RgAPEBinding.search(
            RgAPEBinding.SearchParameters.builder()
                .pattern(args[0])
                .path(Path.of(args[1]))
                .lineNumber(true)
                .build()
        );
        System.out.println(text);
    }
}
