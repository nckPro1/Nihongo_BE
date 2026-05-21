package org.example.nihongobackend.dto.request.learning;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SubmitQuizResultsRequest {

    @NotNull(message = "results không được null")
    @Valid
    private List<QuizResultItemRequest> results = new ArrayList<>();

    public List<QuizResultItemRequest> getResults() {
        return results;
    }

    public void setResults(List<QuizResultItemRequest> results) {
        this.results = results != null ? results : new ArrayList<>();
    }
}
