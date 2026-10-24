package rg_ape;

import apebind.generated.rg_ape.APEProcessException;
import apebind.generated.rg_ape.RgAPEBinding;

import java.util.List;
import java.util.function.Supplier;


public final class SearchClient {
    private String invoke(Supplier<String> operation, boolean raiseOnNoMatch) {
        try {
            return operation.get();
        } catch (APEProcessException error) {
            if (raiseOnNoMatch || error.result().returnCode() != 1) {
                throw error;
            }

            return "";
        }
    }

    public String search(RgAPEBinding.SearchParameters parameters) {
        return search(parameters, true);
    }

    public String search(RgAPEBinding.SearchParameters parameters, boolean raiseOnNoMatch) {
        return invoke(() -> RgAPEBinding.search(parameters), raiseOnNoMatch);
    }

    public String search(List<?> arguments) {
        return search(arguments, true);
    }

    public String search(List<?> arguments, boolean raiseOnNoMatch) {
        return invoke(() -> RgAPEBinding.raw(arguments).stdout().strip(), raiseOnNoMatch);
    }
}
