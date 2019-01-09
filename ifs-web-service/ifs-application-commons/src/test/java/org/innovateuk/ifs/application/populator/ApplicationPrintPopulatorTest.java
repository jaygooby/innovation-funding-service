package org.innovateuk.ifs.application.populator;

import org.innovateuk.ifs.application.builder.ApplicationResourceBuilder;
import org.innovateuk.ifs.application.builder.FormInputResponseResourceBuilder;
import org.innovateuk.ifs.application.finance.view.ApplicationFinanceOverviewModelManager;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.FormInputResponseResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.form.builder.QuestionResourceBuilder;
import org.innovateuk.ifs.form.service.FormInputResponseRestService;
import org.innovateuk.ifs.form.service.FormInputResponseService;
import org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.question.resource.QuestionSetupType;
import org.innovateuk.ifs.user.builder.ProcessRoleResourceBuilder;
import org.innovateuk.ifs.user.builder.UserResourceBuilder;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserRestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPrintPopulatorTest {

    @InjectMocks
    private ApplicationPrintPopulator target;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private CompetitionRestService competitionRestService;

    @Mock
    private QuestionRestService questionRestService;

    @Mock
    private FormInputResponseRestService formInputResponseRestService;

    @Mock
    private FormInputResponseService formInputResponseService;

    @Mock
    private UserRestService userRestService;

    @Mock
    private ApplicationModelPopulator applicationModelPopulator;

    @Mock
    private ApplicationSectionAndQuestionModelPopulator applicationSectionAndQuestionModelPopulator;

    @Mock
    private OrganisationDetailsModelPopulator organisationDetailsModelPopulator;

    @Mock
    private ApplicationFinanceOverviewModelManager applicationFinanceOverviewModelManager;

    @Mock
    private SectionService sectionService;

    @Test
    public void testPrint() {
        Long applicationId = 1L;
        Model model = mock(Model.class);
        UserResource user = UserResourceBuilder.newUserResource().build();
        CompetitionResource competition = CompetitionResourceBuilder.newCompetitionResource().build();
        ApplicationResource application = ApplicationResourceBuilder.newApplicationResource()
                .withCompetition(competition.getId()).build();
        List<FormInputResponseResource> responses = FormInputResponseResourceBuilder.newFormInputResponseResource().build(2);
        List<ProcessRoleResource> userApplicationRoles = ProcessRoleResourceBuilder.newProcessRoleResource().build(1);
        Optional<OrganisationResource> userOrganisation = Optional.of(OrganisationResourceBuilder.newOrganisationResource().build());
        Map<Long, FormInputResponseResource> mappedResponses = mock(Map.class);
        Optional<Boolean> markAsCompleteEnabled = Optional.empty();

        when(applicationService.getById(applicationId)).thenReturn(application);
        when(competitionRestService.getCompetitionById(application.getCompetition())).thenReturn(restSuccess(competition));
        when(formInputResponseRestService.getResponsesByApplicationId(applicationId)).thenReturn(restSuccess(responses));
        when(questionRestService.getQuestionByCompetitionIdAndQuestionSetupType(competition.getId(),
                QuestionSetupType.RESEARCH_CATEGORY)).thenReturn(restSuccess(QuestionResourceBuilder
                .newQuestionResource().build()));
        when(userRestService.findProcessRole(application.getId())).thenReturn(restSuccess(userApplicationRoles));
        when(applicationModelPopulator.getUserOrganisation(user.getId(), userApplicationRoles)).thenReturn(userOrganisation);
        when(formInputResponseService.mapFormInputResponsesToFormInput(responses)).thenReturn(mappedResponses);
        when(sectionService.getCompletedSectionsByOrganisation(application.getId())).thenReturn(emptyMap());

        target.print(applicationId, model, user);

        //Verify model attributes set
        verify(model).addAttribute("responses", mappedResponses);
        verify(model).addAttribute("currentApplication", application);
        verify(model).addAttribute("currentCompetition", competition);
        verify(model).addAttribute("researchCategoryRequired", true);
        verify(model).addAttribute("userOrganisation", userOrganisation.orElse(null));

        //verify populators called
        verify(organisationDetailsModelPopulator).populateModel(model, application.getId(), userApplicationRoles);
        verify(applicationSectionAndQuestionModelPopulator).addQuestionsDetails(model, application, null);
        verify(applicationModelPopulator).addUserDetails(model, user, userApplicationRoles);
        verify(applicationSectionAndQuestionModelPopulator).addMappedSectionsDetails(model, application, competition, Optional.empty(), userOrganisation, user.getId(), emptyMap(), markAsCompleteEnabled);
        verify(applicationFinanceOverviewModelManager).addFinanceDetails(model, competition.getId(), applicationId);
    }
}
