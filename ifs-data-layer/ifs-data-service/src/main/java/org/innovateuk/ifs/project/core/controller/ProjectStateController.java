package org.innovateuk.ifs.project.core.controller;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.project.core.transactional.ProjectStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProjectStateController exposes Project state data and operations through a REST API.
 */
@RestController
@RequestMapping("/project")
public class ProjectStateController {

    private ProjectStateService projectStateService;

    @Autowired
    public ProjectStateController(ProjectStateService projectStateService) {
        this.projectStateService = projectStateService;
    }

    @PostMapping("/{projectId}/withdraw")
    public RestResult<Void> withdrawProject(@PathVariable("projectId") final long projectId) {
        return projectStateService.withdrawProject(projectId).toPostWithBodyResponse();
    }

    @PostMapping("/{projectId}/handle-offline")
    public RestResult<Void> handleProjectOffline(@PathVariable("projectId") final long projectId) {
        return projectStateService.handleProjectOffline(projectId).toPostWithBodyResponse();
    }

    @PostMapping("/{projectId}/complete-offline")
    public RestResult<Void> completeProjectOffline(@PathVariable("projectId") final long projectId) {
        return projectStateService.completeProjectOffline(projectId).toPostWithBodyResponse();
    }
}
