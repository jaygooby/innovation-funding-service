package org.innovateuk.ifs.management.viewmodel;


import org.innovateuk.ifs.application.resource.ApplicationSummaryPageResource;

public class ManageFundingApplicationViewModel {

    private ApplicationSummaryPageResource results;
    private String sortField;
    private String filter;
    private long competitionId;
    private String competitionName;


    public ManageFundingApplicationViewModel(ApplicationSummaryPageResource results, String sortField, String filter, long competitionId, String competitionName) {
        this.results = results;
        this.sortField = sortField;
        this.filter = filter;
        this.competitionId = competitionId;
        this.competitionName = competitionName;
    }

    public ApplicationSummaryPageResource getResults() {
        return results;
    }

    public String getSortField() {
        return sortField;
    }

    public long getCompetitionId() {
        return competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public String getFilter() {
        return filter;
    }
}
