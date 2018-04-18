package org.innovateuk.ifs.project.projectdetails.controller;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.projectdetails.viewmodel.ProjectDetailsViewModel;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.util.PrioritySorting;
import org.innovateuk.ifs.util.RedirectUtils;
import org.innovateuk.ifs.util.SecurityRuleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.user.resource.Role.PARTNER;
import static org.innovateuk.ifs.user.resource.Role.PROJECT_MANAGER;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFilter;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;

/**
 * This controller will handle all requests that are related to project details.
 */
@Controller
@RequestMapping("/competition/{competitionId}/project")
@SecuredBySpring(value = "Controller", description = "TODO", securedType = ProjectDetailsController.class)
@PreAuthorize("hasAnyAuthority('project_finance', 'comp_admin', 'support', 'innovation_lead')")
public class ProjectDetailsController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private OrganisationService organisationService;

    @GetMapping("/{projectId}/details")
    public String viewProjectDetails(@PathVariable("competitionId") final Long competitionId,
                                     @PathVariable("projectId") final Long projectId, Model model,
                                     UserResource loggedInUser) {

        ProjectResource projectResource = projectService.getById(projectId);
        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectResource.getId());
        OrganisationResource leadOrganisationResource = projectService.getLeadOrganisation(projectId);

        CompetitionResource competitionResource = competitionService.getById(competitionId);
        String competitionName = competitionResource.getName();

        boolean isIfsAdministrator = SecurityRuleUtil.isIFSAdmin(loggedInUser);

        List<OrganisationResource> partnerOrganisations = sortedOrganisations(getPartnerOrganisations(projectUsers), leadOrganisationResource);

        model.addAttribute("model", new ProjectDetailsViewModel(projectResource,
                competitionId,
                isIfsAdministrator,
                competitionName,
                leadOrganisationResource.getName(),
                getProjectManager(projectUsers).orElse(null),
                getFinanceContactForPartnerOrganisation(projectUsers, partnerOrganisations)));

        return "project/detail";
    }

    @PostMapping("/{projectId}/withdraw")
    public String withdrawProject(@PathVariable("projectId") final long projectId, HttpServletRequest request) {
         projectService.withdrawProject(projectId);

        return RedirectUtils.redirectToCompetitionManagementService(request, "/competition/{competitionId}/applications/unsuccessful");
    }

    private List<OrganisationResource> getPartnerOrganisations(final List<ProjectUserResource> projectRoles) {
        return  projectRoles.stream()
                .filter(uar -> uar.getRole() == PARTNER.getId())
                .map(uar -> organisationService.getOrganisationById(uar.getOrganisation()))
                .collect(Collectors.toList());
    }

    private List<OrganisationResource> sortedOrganisations(List<OrganisationResource> organisations,
                                                           OrganisationResource lead)
    {
        return new PrioritySorting<>(organisations, lead, OrganisationResource::getName).unwrap();
    }

    private Optional<ProjectUserResource> getProjectManager(List<ProjectUserResource> projectUsers) {
        return simpleFindFirst(projectUsers, pu -> PROJECT_MANAGER.getId() == pu.getRole());
    }

    private Map<OrganisationResource, ProjectUserResource> getFinanceContactForPartnerOrganisation(List<ProjectUserResource> projectUsers, List<OrganisationResource> partnerOrganisations) {
        List<ProjectUserResource> financeRoles = simpleFilter(projectUsers, ProjectUserResource::isFinanceContact);

        Map<OrganisationResource, ProjectUserResource> organisationFinanceContactMap = new LinkedHashMap<>();

        partnerOrganisations.stream().forEach(organisation ->
                organisationFinanceContactMap.put(organisation,
                        simpleFindFirst(financeRoles, financeUserResource -> financeUserResource.getOrganisation().equals(organisation.getId())).orElse(null))
        );

        return organisationFinanceContactMap;
    }
}
