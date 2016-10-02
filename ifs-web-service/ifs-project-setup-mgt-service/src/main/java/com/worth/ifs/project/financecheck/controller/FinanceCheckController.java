package com.worth.ifs.project.financecheck.controller;

import com.worth.ifs.application.service.OrganisationService;
import com.worth.ifs.controller.ValidationHandler;
import com.worth.ifs.project.ProjectService;
import com.worth.ifs.project.finance.resource.FinanceCheckResource;
import com.worth.ifs.project.financecheck.FinanceCheckService;
import com.worth.ifs.project.financecheck.form.CostFormField;
import com.worth.ifs.project.financecheck.form.FinanceCheckForm;
import com.worth.ifs.project.financecheck.viewmodel.FinanceCheckViewModel;
import com.worth.ifs.project.resource.ProjectOrganisationCompositeId;
import com.worth.ifs.project.resource.ProjectUserResource;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.resource.OrganisationTypeEnum;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static com.worth.ifs.util.CollectionFunctions.simpleFindFirst;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * This controller is for allowing internal users to view and update application finances entered by applicants
 */
@Controller
@RequestMapping("/project/{projectId}/organisation/{organisationId}/finance-check")
public class FinanceCheckController {
    private static final String FORM_ATTR_NAME = "form";

    @Autowired
    private FinanceCheckService financeCheckService;

    @Autowired
    ProjectService projectService;

    @Autowired
    OrganisationService organisationService;

    @RequestMapping(method = GET)
    public String view(@PathVariable("projectId") final Long projectId, @PathVariable("organisationId") Long organisationId,
                       @ModelAttribute(FORM_ATTR_NAME) @Valid FinanceCheckForm form,
                       @ModelAttribute("loggedInUser") UserResource loggedInUser,
                       Model model){
        FinanceCheckResource financeCheckResource = getFinanceCheckResource(projectId, organisationId);
        populateExitingFinanceCheckDetailsInForm(financeCheckResource, form);
        return doViewFinanceCheckForm(projectId, organisationId, model);
    }

    @RequestMapping(method = POST)
    public String update(@PathVariable("projectId") Long projectId,
                         @PathVariable("organisationId") Long organisationId,
                         @ModelAttribute(FORM_ATTR_NAME) FinanceCheckForm form,
                         @SuppressWarnings("unused") BindingResult bindingResult,
                         ValidationHandler validationHandler,
                         Model model){
        return validationHandler.failNowOrSucceedWith(
                () -> redirectToFinanceCheckForm(projectId, organisationId),
                () -> updateFinanceCheck(projectId, organisationId, getFinanceCheckResource(projectId, organisationId), form));
    }

    private String redirectToFinanceCheckForm(Long projectId, Long organisationId){
        return "redirect:/project/" + projectId + "/organisation/" + organisationId + "/finance-check";
    }

    private void populateExitingFinanceCheckDetailsInForm(FinanceCheckResource financeCheckResource, FinanceCheckForm form){
        form.setCosts(simpleMap(financeCheckResource.getCostGroup().getCosts(), c -> {
            CostFormField cf = new CostFormField();
            cf.setId(c.getId());
            cf.setValue(c.getValue());
            return cf;
        }));
    }

    private String doViewFinanceCheckForm(Long projectId, Long organisationId, Model model){
        OrganisationResource organisationResource = organisationService.getOrganisationById(organisationId);
        boolean isResearch = OrganisationTypeEnum.isResearch(organisationResource.getOrganisationType());
        Optional<ProjectUserResource> financeContact = getFinanceContact(projectId, organisationId);
        FinanceCheckViewModel financeCheckViewModel;
        if(financeContact.isPresent()){
            financeCheckViewModel = new FinanceCheckViewModel(financeContact.get().getUserName(), financeContact.get().getEmail(), isResearch);
        } else {
            financeCheckViewModel = new FinanceCheckViewModel();
            financeCheckViewModel.setResearch(isResearch);
        }
        model.addAttribute("model", financeCheckViewModel);
        return "project/finance-check";
    }

    private Optional<ProjectUserResource> getFinanceContact(Long projectId, Long organisationId){
        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectId);
        return simpleFindFirst(projectUsers, pr -> pr.isFinanceContact() && organisationId.equals(pr.getOrganisation()));
    }

    private FinanceCheckResource getFinanceCheckResource(Long projectId, Long organisationId){
        ProjectOrganisationCompositeId key = new ProjectOrganisationCompositeId(projectId, organisationId);
        return financeCheckService.getByProjectAndOrganisation(key);
    }

    private String updateFinanceCheck(Long projectId, Long organisationId, FinanceCheckResource currentFinanceCheckResource, FinanceCheckForm financeCheckForm){
        // TODO map by name once form is dynamic
        for(int i = 0; i < financeCheckForm.getCosts().size(); i++){
            currentFinanceCheckResource.getCostGroup().getCosts().get(i).setValue(financeCheckForm.getCosts().get(i).getValue());
        }
        financeCheckService.update(currentFinanceCheckResource);

        return redirectToFinanceCheckForm(projectId, organisationId);
    }
}