package org.innovateuk.ifs.project.projectteam.populator;

import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.projectteam.viewmodel.ProjectOrganisationUserRowViewModel;
import org.innovateuk.ifs.project.projectteam.viewmodel.ProjectOrganisationViewModel;
import org.innovateuk.ifs.project.projectteam.viewmodel.ProjectTeamViewModel;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.project.status.resource.ProjectTeamStatusResource;
import org.innovateuk.ifs.project.status.security.SetupSectionAccessibilityHelper;
import org.innovateuk.ifs.status.StatusService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.user.resource.Role.PROJECT_MANAGER;
import static org.innovateuk.ifs.util.CollectionFunctions.*;

@Component
public class ProjectTeamViewModelPopulator {

    private final ProjectService projectService;

    private final CompetitionRestService competitionRestService;

    private final StatusService statusService;

    @Autowired
    public ProjectTeamViewModelPopulator(
            ProjectService projectService,
            CompetitionRestService competitionRestService,
            StatusService statusService
    ) {
        this.projectService = projectService;
        this.competitionRestService = competitionRestService;
        this.statusService = statusService;
    }

    public ProjectTeamViewModel populate(long projectId, UserResource loggedInUser) {

        ProjectResource projectResource = projectService.getById(projectId);
        CompetitionResource competitionResource = competitionRestService.getCompetitionById(projectResource.getCompetition()).getSuccess();

        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectResource.getId());
        List<OrganisationResource> projectOrganisations = projectService.getPartnerOrganisationsForProject(projectId);
        OrganisationResource leadOrganisation = projectService.getLeadOrganisation(projectId);
        ProjectUserResource loggedInProjectUser = simpleFindFirst(projectUsers,
                                                                  pu -> pu.getUser().equals(loggedInUser.getId())).get();
        OrganisationResource loggedInUserOrg = simpleFindFirst(projectOrganisations,
                                                               org -> org.getId().equals(loggedInProjectUser.getOrganisation())).get();

        boolean isLead = loggedInUserOrg.equals(leadOrganisation);

        ProjectOrganisationViewModel leadOrgModel = mapToProjectOrganisationViewModel(projectUsers, leadOrganisation, true);
        ProjectOrganisationViewModel loggedInUserOrgModel = mapToProjectOrganisationViewModel(projectUsers, loggedInUserOrg, isLead);
        List<ProjectOrganisationViewModel> partnerOrgModels = simpleMap(projectOrganisations,
                                                                        org -> mapToProjectOrganisationViewModel(projectUsers,
                                                                                                                 org,
                                                                                                                 org.equals(leadOrganisation)));
        
        ProjectTeamStatusResource teamStatus = statusService.getProjectTeamStatus(projectId, Optional.empty());
        SetupSectionAccessibilityHelper statusAccessor = new SetupSectionAccessibilityHelper(teamStatus);
        boolean spendProfileGenerated = statusAccessor.isSpendProfileGenerated();
        boolean monitoringOfficerAssigned = statusAccessor.isMonitoringOfficerAssigned();

        return new ProjectTeamViewModel(
                competitionResource.getName(),
                projectResource.getName(),
                projectResource.getId(),
                partnerOrgModels,
                loggedInUserOrgModel,
                leadOrgModel,
                getProjectManager(projectResource.getId()).orElse(null),
                isLead,
                loggedInUser.getId(),
                monitoringOfficerAssigned,
                spendProfileGenerated,
                statusAccessor.isGrantOfferLetterGenerated(),
                false);
    }

    private Optional<ProjectUserResource> getProjectManager(Long projectId) {
        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectId);
        return simpleFindFirst(projectUsers, pu -> PROJECT_MANAGER.getId() == pu.getRole());
    }

    private ProjectOrganisationViewModel mapToProjectOrganisationViewModel(List<ProjectUserResource> totalUsers, OrganisationResource organisation, boolean isLead) {
        List<ProjectUserResource> usersForOrganisation = simpleFilter(totalUsers,
                                                                      user -> user.getOrganisation().equals(organisation.getId()));
        return new ProjectOrganisationViewModel(mapUsersToViewModelRows(usersForOrganisation), organisation.getName(), organisation.getId(), isLead);
    }

    private List<ProjectOrganisationUserRowViewModel> mapUsersToViewModelRows(List<ProjectUserResource> usersForOrganisation) {

        return usersForOrganisation.stream()
                .map(pu -> {
                    boolean duplicateExists = usersForOrganisation.stream()
                            .filter(u -> u.getUser().equals(pu.getUser()))
                            .count() > 1;

                    return new ProjectOrganisationUserRowViewModel(pu.getEmail(),
                                                    pu.getUserName(),
                                                    pu.getUser(),
                                                    duplicateExists || pu.isProjectManager(),
                                                    duplicateExists || pu.isFinanceContact());
                })
                .distinct()
                .collect(Collectors.toList());
    }
}
