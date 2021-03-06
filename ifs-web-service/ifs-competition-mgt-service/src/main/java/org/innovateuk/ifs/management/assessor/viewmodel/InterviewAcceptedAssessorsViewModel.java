package org.innovateuk.ifs.management.assessor.viewmodel;

import org.innovateuk.ifs.management.navigation.Pagination;

import java.util.List;

/**
 *  Holder of model attributes for the assessors shown in the 'Allocate applications to assessors' page
 */
public class InterviewAcceptedAssessorsViewModel {
    private long competitionId;
    private String competitionName;
    private List<InterviewAcceptedAssessorsRowViewModel> assessors;
    private Pagination pagination;

    public InterviewAcceptedAssessorsViewModel(long competitionId,
                                               String competitionName,
                                               List<InterviewAcceptedAssessorsRowViewModel> assessors,
                                               Pagination pagination
    ) {
        this.competitionId = competitionId;
        this.competitionName = competitionName;
        this.assessors = assessors;
        this.pagination = pagination;
    }

    public long getCompetitionId() {
        return competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public List<InterviewAcceptedAssessorsRowViewModel> getAssessors() {
        return assessors;
    }

    public void setAssessors(List<InterviewAcceptedAssessorsRowViewModel> assessors) {
        this.assessors = assessors;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
