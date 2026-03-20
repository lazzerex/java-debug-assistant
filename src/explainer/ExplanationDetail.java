package explainer;

import java.util.List;

public class ExplanationDetail {
    private final String explanation;
    private final List<String> suggestions;

    public ExplanationDetail(String explanation, List<String> suggestions) {
        this.explanation = explanation;
        this.suggestions = suggestions;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
