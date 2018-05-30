package org.innovateuk.ifs.management.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.resource.ApplicationIneligibleSendResource;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationRestService;
import org.innovateuk.ifs.management.notification.controller.CompetitionManagementSendIneligibleController;
import org.innovateuk.ifs.management.notification.populator.InformIneligibleModelPopulator;
import org.innovateuk.ifs.management.notification.viewmodel.InformIneligibleViewModel;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.innovateuk.ifs.user.viewmodel.UserApplicationRole.COLLABORATOR;
import static org.innovateuk.ifs.user.viewmodel.UserApplicationRole.LEAD_APPLICANT;
import static org.innovateuk.ifs.application.builder.ApplicationIneligibleSendResourceBuilder.newApplicationIneligibleSendResource;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.resource.ApplicationState.INELIGIBLE;
import static org.innovateuk.ifs.application.resource.ApplicationState.OPEN;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class CompetitionManagementSendIneligibleControllerTest extends BaseControllerMockMVCTest<CompetitionManagementSendIneligibleController> {

    @Spy
    @InjectMocks
    private InformIneligibleModelPopulator informIneligibleModelPopulator;

    @Mock
    private ApplicationRestService applicationRestService;

    @Mock
    private ProcessRoleService processRoleService;

    @Override
    protected CompetitionManagementSendIneligibleController supplyControllerUnderTest() {
        return new CompetitionManagementSendIneligibleController();
    }

    @Test
    public void getSendIneligible() throws Exception {
        long applicationId = 1L;
        long competitionId = 2L;
        String competitionName = "competition";
        String applicationName = "application";
        String leadApplicant = "lead applicant";


        ApplicationResource applicationResource = newApplicationResource()
                .withId(applicationId)
                .withApplicationState(INELIGIBLE)
                .withCompetitionName(competitionName)
                .withName(applicationName)
                .withCompetition(competitionId)
                .build();
        List<ProcessRoleResource> processRoles = newProcessRoleResource()
                .withRoleName(COLLABORATOR.getRoleName(), LEAD_APPLICANT.getRoleName(), COLLABORATOR.getRoleName())
                .withUserName("other", leadApplicant, "an other")
                .build(3);

        InformIneligibleViewModel expectedViewModel =
                new InformIneligibleViewModel(competitionId, applicationId, competitionName, applicationName, leadApplicant);

        when(applicationRestService.getApplicationById(applicationId)).thenReturn(restSuccess(applicationResource));
        when(processRoleService.findProcessRolesByApplicationId(applicationId)).thenReturn(processRoles);

        mockMvc.perform(get("/competition/application/{applicationId}/ineligible", applicationId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", expectedViewModel));

        verify(applicationRestService, only()).getApplicationById(applicationId);
        verify(processRoleService, only()).findProcessRolesByApplicationId(applicationId);
    }

    @Test
    public void getSendIneligibleWrongState() throws Exception {
        long applicationId = 1L;
        long competitionId = 2L;

        ApplicationResource applicationResource = newApplicationResource()
                .withApplicationState(OPEN)
                .withId(applicationId)
                .withCompetition(competitionId)
                .build();

        when(applicationRestService.getApplicationById(applicationId)).thenReturn(restSuccess(applicationResource));

        mockMvc.perform(get("/competition/application/{applicationId}/ineligible", applicationId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/competition/" + competitionId + "/applications/ineligible"));

        verify(applicationRestService, only()).getApplicationById(applicationId);
    }

    @Test
    public void sendEmail() throws Exception {
        long applicationId = 1L;
        long competitionId = 2L;
        String subject = "subject";
        String message = "message";

        ApplicationResource applicationResource = newApplicationResource()
                .withId(applicationId)
                .withCompetition(competitionId)
                .build();

        ApplicationIneligibleSendResource expectedSendResource = newApplicationIneligibleSendResource()
                .withSubject(subject)
                .withMessage(message)
                .build();

        when(applicationRestService.getApplicationById(applicationId)).thenReturn(restSuccess(applicationResource));
        when(applicationRestService.informIneligible(applicationId, expectedSendResource)).thenReturn(restSuccess());

        mockMvc.perform(post("/competition/application/{applicationId}/ineligible/send", applicationId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("subject", subject)
                .param("message", message))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/competition/" + competitionId + "/applications/ineligible"));

        InOrder inOrder = inOrder(applicationRestService);
        inOrder.verify(applicationRestService).getApplicationById(applicationId);
        inOrder.verify(applicationRestService).informIneligible(applicationId, expectedSendResource);
        inOrder.verifyNoMoreInteractions();
    }
}
