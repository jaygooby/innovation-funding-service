package org.innovateuk.ifs.application.forms.controller;

import org.innovateuk.ifs.BaseUnitTest;
import org.innovateuk.ifs.application.forms.validator.ApplicationDetailsEditableValidator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.application.resource.QuestionStatusResource;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.competition.resource.CompetitionSetupQuestionType;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.builder.QuestionStatusResourceBuilder.newQuestionStatusResource;
import static org.innovateuk.ifs.competition.resource.CompetitionSetupQuestionType.APPLICATION_DETAILS;
import static org.innovateuk.ifs.form.builder.QuestionResourceBuilder.newQuestionResource;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ApplicationDetailsEditableValidatorTest extends BaseUnitTest {

    @InjectMocks
    private ApplicationDetailsEditableValidator applicationDetailsEditableValidator;

    @Mock
    private QuestionService questionService;

    @Test
    public void questionAndApplicationHaveAllowedState_shouldReturnTrueWhenApplicationIsOpenAndDetailsNotMarkedAsComplete() throws Exception {
        Long questionId = 1L;
        Long applicationId = 2L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withApplicationState(ApplicationState.OPEN).build();

        QuestionResource questionResource = newQuestionResource()
                .withShortName(APPLICATION_DETAILS.getShortName())
                .withQuestionSetupType(CompetitionSetupQuestionType.APPLICATION_DETAILS)
                .build();
        List<QuestionStatusResource> questionStatusResources = newQuestionStatusResource().withMarkedAsComplete(false).build(2);

        when(questionService.getById(questionId)).thenReturn(questionResource);
        when(questionService.findQuestionStatusesByQuestionAndApplicationId(questionId, applicationId)).thenReturn(questionStatusResources);

        boolean result = applicationDetailsEditableValidator.questionAndApplicationHaveAllowedState(questionId,applicationResource);

        assertTrue(result);
    }

    @Test
    public void questionAndApplicationHaveAllowedState_shouldReturnFalseWhenApplicationIsNotOpenAndDetailsNotMarkedAsComplete() throws Exception {
        Long questionId = 1L;
        Long applicationId = 2L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withApplicationState(ApplicationState.SUBMITTED).build();

        QuestionResource questionResource = newQuestionResource()
                .withShortName(APPLICATION_DETAILS.getShortName())
                .withQuestionSetupType(CompetitionSetupQuestionType.APPLICATION_DETAILS)
                .build();
        List<QuestionStatusResource> questionStatusResources = newQuestionStatusResource().withMarkedAsComplete(false).build(2);

        when(questionService.getById(questionId)).thenReturn(questionResource);
        when(questionService.findQuestionStatusesByQuestionAndApplicationId(questionId, applicationId)).thenReturn(questionStatusResources);

        boolean result = applicationDetailsEditableValidator.questionAndApplicationHaveAllowedState(questionId,applicationResource);

        assertFalse(result);
    }

    @Test
    public void questionAndApplicationHaveAllowedState_shouldReturnFalseWhenApplicationIsOpenAndDetailsMarkedAsComplete() throws Exception {
        Long questionId = 1L;
        Long applicationId = 2L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withApplicationState(ApplicationState.OPEN).build();

        QuestionResource questionResource = newQuestionResource()
                .withShortName(APPLICATION_DETAILS.getShortName())
                .withQuestionSetupType(CompetitionSetupQuestionType.APPLICATION_DETAILS)
                .build();
        List<QuestionStatusResource> questionStatusResources = newQuestionStatusResource().withMarkedAsComplete(true).build(2);

        when(questionService.getById(questionId)).thenReturn(questionResource);
        when(questionService.findQuestionStatusesByQuestionAndApplicationId(questionId, applicationId)).thenReturn(questionStatusResources);

        boolean result = applicationDetailsEditableValidator.questionAndApplicationHaveAllowedState(questionId,applicationResource);

        assertFalse(result);
    }

    @Test
    public void questionAndApplicationHaveAllowedState_shouldReturnFalseWhenApplicationIsNotOpenAndDetailsMarkedAsComplete() throws Exception {
        Long questionId = 1L;
        Long applicationId = 2L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withApplicationState(ApplicationState.SUBMITTED).build();

        QuestionResource questionResource = newQuestionResource()
                .withShortName("non-application details")
                .withQuestionSetupType(CompetitionSetupQuestionType.PROJECT_SUMMARY)
                .build();
        List<QuestionStatusResource> questionStatusResources = newQuestionStatusResource().withMarkedAsComplete(true).build(2);

        when(questionService.getById(questionId)).thenReturn(questionResource);
        when(questionService.findQuestionStatusesByQuestionAndApplicationId(questionId, applicationId)).thenReturn(questionStatusResources);

        boolean result = applicationDetailsEditableValidator.questionAndApplicationHaveAllowedState(questionId,applicationResource);

        assertFalse(result);
    }

    @Test
    public void questionAndApplicationHaveAllowedState_shouldReturnFalseWhenNotApplicationDetails() throws Exception {
        Long questionId = 1L;
        Long applicationId = 2L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withApplicationState(ApplicationState.OPEN).build();

        QuestionResource questionResource = newQuestionResource()
                .withShortName("non-application details")
                .withQuestionSetupType(CompetitionSetupQuestionType.PROJECT_SUMMARY)
                .build();
        List<QuestionStatusResource> questionStatusResources = newQuestionStatusResource().withMarkedAsComplete(false).build(2);

        when(questionService.getById(questionId)).thenReturn(questionResource);
        when(questionService.findQuestionStatusesByQuestionAndApplicationId(questionId, applicationId)).thenReturn(questionStatusResources);

        boolean result = applicationDetailsEditableValidator.questionAndApplicationHaveAllowedState(questionId,applicationResource);

        assertFalse(result);
    }
}