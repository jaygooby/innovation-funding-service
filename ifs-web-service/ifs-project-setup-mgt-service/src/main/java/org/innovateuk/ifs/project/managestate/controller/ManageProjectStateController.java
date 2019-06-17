package org.innovateuk.ifs.project.managestate.controller;

import org.innovateuk.ifs.commons.exception.IFSRuntimeException;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.project.managestate.form.ManageProjectStateForm;
import org.innovateuk.ifs.project.managestate.viewmodel.ManageProjectStateViewModel;
import org.innovateuk.ifs.project.resource.ProjectState;
import org.innovateuk.ifs.project.service.ProjectRestService;
import org.innovateuk.ifs.project.service.ProjectStateRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.function.Supplier;

import static java.lang.Boolean.TRUE;

@Controller
@RequestMapping("/competition/{competitionId}/project/{projectId}/manage-status")
@PreAuthorize("hasAuthority('ifs_administrator')")
@SecuredBySpring(value = "MANAGE_PROJECT_STATE", description = "Only IFS Admin can manage project state")
public class ManageProjectStateController {

    @Autowired
    private ProjectRestService projectRestService;

    @Autowired
    private ProjectStateRestService projectStateRestService;

    @GetMapping
    public String manageProjectState(@ModelAttribute(value = "form", binding = false) ManageProjectStateForm form,
                                      BindingResult result,
                                      @PathVariable long projectId,
                                      Model model) {
        model.addAttribute("model", new ManageProjectStateViewModel(projectRestService.getProjectById(projectId).getSuccess()));
        return "project/manage-project-state";
    }

    @PostMapping
    public String setProjectState(@Valid @ModelAttribute(value = "form") ManageProjectStateForm form,
                                   BindingResult result,
                                   ValidationHandler validationHandler,
                                   @PathVariable long projectId,
                                   @PathVariable long competitionId,
                                   Model model) {
        validate(form, result);
        Supplier<String> failureView = () -> manageProjectState(form, result, projectId, model);
        Supplier<String> successView = () -> String.format("redirect:/competition/%d/project/%d/manage-status", competitionId, projectId);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {
            validationHandler.addAnyErrors(updateProjectState(form.getState(), projectId));
            return validationHandler.failNowOrSucceedWith(failureView, successView);
        });
    }

    private RestResult<Void> updateProjectState(ProjectState state, long projectId) {
        switch (state) {
            case WITHDRAWN:
                return projectStateRestService.withdrawProject(projectId);
            case HANDLED_OFFLINE:
                return projectStateRestService.handleProjectOffline(projectId);
            case COMPLETED_OFFLINE:
                return projectStateRestService.completeProjectOffline(projectId);
            default:
                throw new IFSRuntimeException("Unknown project state");
        }
    }

    private void validate(@Valid ManageProjectStateForm form, BindingResult result) {
        if (result.hasErrors()) {
            return;
        }

        if (form.isHandledOffline() && !TRUE.equals(form.getConfirmationOffline())) {
            result.rejectValue("confirmationOffline", "validation.field.must.not.be.blank");
            return;
        }

        if (form.isWithdrawn() && !TRUE.equals(form.getConfirmationWithdrawn())) {
            result.rejectValue("confirmationWithdrawn", "validation.field.must.not.be.blank");
            return;
        }

        if (form.isCompletedOffline() && !TRUE.equals(form.getConfirmationCompleteOffline())) {
            result.rejectValue("confirmationCompleteOffline", "validation.field.must.not.be.blank");
            return;
        }
    }
}
