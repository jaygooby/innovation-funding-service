package org.innovateuk.ifs.project.pendingpartner.controller;


import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.service.ProjectRestService;
import org.innovateuk.ifs.project.status.controller.SetupStatusController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static java.lang.Boolean.TRUE;

@Controller
@RequestMapping("/project/{projectId}/organisation/{organisationId}/your-organisation")
@SecuredBySpring(value = "Controller", description = "TODO", securedType = SetupStatusController.class)
@PreAuthorize("hasAnyAuthority('applicant')")
public class ProjectYourOrganisationController {

    @Autowired
    private CompetitionRestService competitionRestService;

    @Autowired
    private ProjectRestService projectRestService;

    @GetMapping
    public String viewPage(@PathVariable long projectId,
                           @PathVariable long organisationId) {
        ProjectResource project = projectRestService.getProjectById(projectId).getSuccess();

        boolean includeGrowthTable = isIncludingGrowthTable(project.getCompetition());


        return String.format("redirect:/project/%d/organisation/%d/your-organisation/%s",
                        projectId,
                        organisationId,
                        includeGrowthTable ? "with-growth-table" : "without-growth-table");
    }

    private boolean isIncludingGrowthTable(long competitionId) {
        return competitionRestService.getCompetitionById(competitionId).
                andOnSuccessReturn(competition -> TRUE.equals(competition.getIncludeProjectGrowthTable())).getSuccess();
    }
}
