package org.innovateuk.ifs.application.forms.sections.yourprojectlocation.controller;

import org.innovateuk.ifs.application.forms.sections.yourprojectlocation.form.YourProjectLocationForm;
import org.innovateuk.ifs.application.forms.sections.yourprojectlocation.form.YourProjectLocationFormPopulator;
import org.innovateuk.ifs.application.forms.sections.yourprojectlocation.viewmodel.YourProjectLocationViewModel;
import org.innovateuk.ifs.application.forms.sections.yourprojectlocation.viewmodel.YourProjectLocationViewModelPopulator;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.service.ApplicationFinanceRestService;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserRestService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.function.Supplier;

import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.APPLICATION_BASE_URL;

@Controller
@RequestMapping(APPLICATION_BASE_URL + "{applicationId}/form/project-location/{sectionId}")
@PreAuthorize("hasAuthority('applicant')")
@SecuredBySpring(value = "PROJECT_LOCATION_APPLICANT",
        description = "Applicants can all fill out the Project Location section of the application.")
public class YourProjectLocationController {

    private YourProjectLocationViewModelPopulator viewModelPopulator;
    private YourProjectLocationFormPopulator formPopulator;
    private ApplicationFinanceRestService applicationFinanceRestService;
    private SectionService sectionService;
    private UserRestService userRestService;

    YourProjectLocationController(
            YourProjectLocationViewModelPopulator viewModelPopulator,
            YourProjectLocationFormPopulator formPopulator,
            ApplicationFinanceRestService applicationFinanceRestService,
            SectionService sectionService,
            UserRestService userRestService) {

        this.viewModelPopulator = viewModelPopulator;
        this.formPopulator = formPopulator;
        this.applicationFinanceRestService = applicationFinanceRestService;
        this.sectionService = sectionService;
        this.userRestService = userRestService;
    }

    // TODO DW - parallelize?
    @GetMapping
    public String view(
            @PathVariable("applicationId") long applicationId,
            @PathVariable("sectionId") long sectionId,
            UserResource loggedInUser,
            Model model) {

        long userId = loggedInUser.getId();

        ProcessRoleResource processRole = userRestService.findProcessRole(userId, applicationId).getSuccess();

        YourProjectLocationViewModel viewModel = viewModelPopulator.populate(userId, applicationId, sectionId);
        YourProjectLocationForm form = formPopulator.populate(applicationId, processRole.getOrganisationId());

        model.addAttribute("model", viewModel);
        model.addAttribute("form", form);

        return "application/sections/your-project-location/your-project-location";
    }

    @PostMapping
    public String update(
            @PathVariable("applicationId") long applicationId,
            @PathVariable("sectionId") long sectionId,
            UserResource loggedInUser,
            @ModelAttribute YourProjectLocationForm form) {

        ProcessRoleResource processRole = userRestService.findProcessRole(loggedInUser.getId(), applicationId).getSuccess();

        updatePostcode(applicationId, form, processRole);

        return redirectToViewPage(applicationId, sectionId);
    }

    private void updatePostcode(long applicationId,
                                YourProjectLocationForm form,
                                ProcessRoleResource processRole) {
        
        ApplicationFinanceResource finance =
                applicationFinanceRestService.getFinanceDetails(applicationId, processRole.getOrganisationId()).getSuccess();

        finance.setWorkPostcode(form.getPostcode());

        applicationFinanceRestService.update(finance.getId(), finance).getSuccess();
    }

    @PostMapping(params = "mark-as-complete")
    public String markAsComplete(
            @PathVariable("applicationId") long applicationId,
            @PathVariable("sectionId") long sectionId,
            UserResource loggedInUser,
            @Valid @ModelAttribute YourProjectLocationForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler) {

        Supplier<String> failureHandler = () -> "application/sections/your-project-location/your-project-location";
        Supplier<String> successHandler = () -> redirectToViewPage(applicationId, sectionId);

        return validationHandler.failNowOrSucceedWith(
                failureHandler,
                () -> {
                    ProcessRoleResource processRole = userRestService.findProcessRole(loggedInUser.getId(), applicationId).getSuccess();

                    updatePostcode(applicationId, form, processRole);

                    List<ValidationMessages> validationMessages = sectionService.markAsComplete(sectionId, applicationId, processRole.getId());

                    validationMessages.forEach(validationHandler::addAnyErrors);

                    return validationHandler.failNowOrSucceedWith(failureHandler, successHandler);
                });
    }

    @PostMapping(params = "mark-as-incomplete")
    public String markAsIncomplete(
            @PathVariable("applicationId") long applicationId,
            @PathVariable("sectionId") long sectionId,
            UserResource loggedInUser) {

        ProcessRoleResource processRole = userRestService.findProcessRole(loggedInUser.getId(), applicationId).getSuccess();
        sectionService.markAsInComplete(sectionId, applicationId, processRole.getId());
        return redirectToViewPage(applicationId, sectionId);
    }

    private String redirectToViewPage(long applicationId, long sectionId) {
        return "redirect:" + APPLICATION_BASE_URL + String.format("%d/form/project-location/%d", applicationId, sectionId);
    }
}
