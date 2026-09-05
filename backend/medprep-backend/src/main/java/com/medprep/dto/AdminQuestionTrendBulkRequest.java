package com.medprep.dto;

import java.util.List;

public class AdminQuestionTrendBulkRequest {

    private List<AdminQuestionTrendRequest> trends;

    public AdminQuestionTrendBulkRequest() {
    }

    public List<AdminQuestionTrendRequest> getTrends() {
        return trends;
    }

    public void setTrends(
            List<AdminQuestionTrendRequest> trends) {

        this.trends = trends;
    }
}