package org.innovateuk.ifs.project.status.viewmodel;

import org.innovateuk.ifs.competition.resource.CompetitionOpenQueryResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;

import java.util.List;

public class CompetitionOpenQueriesViewModel {
    private long competitionId;
    private String competitionName;
    private List<CompetitionOpenQueryResource> openQueries;
    private long openQueryCount;
    private boolean showTabs;

    public CompetitionOpenQueriesViewModel(CompetitionResource competition, List<CompetitionOpenQueryResource> openQueries, long openQueryCount, boolean showTabs) {
        this.competitionId = competition.getId();
        this.competitionName = competition.getName();
        this.openQueries = openQueries;
        this.openQueryCount = openQueryCount;
        this.showTabs = showTabs;
    }

    public long getCompetitionId() { return competitionId; }

    public String getCompetitionName() { return competitionName; }

    public List<CompetitionOpenQueryResource> getOpenQueries() { return openQueries; }

    public long getOpenQueryCount() { return openQueryCount; }

    public boolean isShowTabs() { return showTabs; }
}
