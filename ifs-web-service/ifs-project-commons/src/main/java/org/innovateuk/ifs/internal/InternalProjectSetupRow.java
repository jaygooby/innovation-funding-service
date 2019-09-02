package org.innovateuk.ifs.internal;

import org.innovateuk.ifs.project.internal.ProjectSetupStage;
import org.innovateuk.ifs.project.resource.ProjectState;

import java.util.Map;

public class InternalProjectSetupRow {

    private String projectName;
    private Long applicationNumber;
    private ProjectState projectState;
    private int numberOfPartners;
    private long competitionId;
    private String projectLeadOrganisationName;
    private Long projectId;

    private Map<ProjectSetupStage, InternalProjectSetupCell> states;

    public InternalProjectSetupRow(String projectName, Long applicationNumber, ProjectState projectState, int numberOfPartners, long competitionId, String projectLeadOrganisationName, Long projectId, Map<ProjectSetupStage, InternalProjectSetupCell> states) {
        this.projectName = projectName;
        this.applicationNumber = applicationNumber;
        this.projectState = projectState;
        this.numberOfPartners = numberOfPartners;
        this.competitionId = competitionId;
        this.projectLeadOrganisationName = projectLeadOrganisationName;
        this.projectId = projectId;
        this.states = states;
    }

    public String getProjectName() {
        return projectName;
    }

    public Long getApplicationNumber() {
        return applicationNumber;
    }

    public ProjectState getProjectState() {
        return projectState;
    }

    public int getNumberOfPartners() {
        return numberOfPartners;
    }

    public long getCompetitionId() {
        return competitionId;
    }

    public String getProjectLeadOrganisationName() {
        return projectLeadOrganisationName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Map<ProjectSetupStage, InternalProjectSetupCell> getStates() {
        return states;
    }
}
