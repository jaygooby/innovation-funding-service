package org.innovateuk.ifs.management.competition.setup.application.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.service.QuestionSetupRestService;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.publiccontent.resource.FundingType;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.competition.service.CompetitionSetupRestService;
import org.innovateuk.ifs.management.competition.setup.CompetitionSetupController;
import org.innovateuk.ifs.management.competition.setup.application.form.AbstractQuestionForm.TypeOfQuestion;
import org.innovateuk.ifs.management.competition.setup.application.form.DetailsForm;
import org.innovateuk.ifs.management.competition.setup.application.form.QuestionForm;
import org.innovateuk.ifs.management.competition.setup.application.validator.CompetitionSetupApplicationQuestionValidator;
import org.innovateuk.ifs.management.competition.setup.core.form.CompetitionSetupForm;
import org.innovateuk.ifs.management.competition.setup.core.populator.CompetitionSetupPopulator;
import org.innovateuk.ifs.management.competition.setup.core.service.CompetitionSetupQuestionService;
import org.innovateuk.ifs.management.competition.setup.core.service.CompetitionSetupService;
import org.innovateuk.ifs.management.competition.setup.core.viewmodel.GeneralSetupViewModel;
import org.innovateuk.ifs.management.competition.setup.core.viewmodel.QuestionSetupViewModel;
import org.innovateuk.ifs.question.resource.QuestionSetupType;
import org.innovateuk.ifs.question.service.QuestionSetupCompetitionRestService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.innovateuk.ifs.commons.rest.RestResult.restFailure;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.builder.CompetitionSetupQuestionResourceBuilder.newCompetitionSetupQuestionResource;
import static org.innovateuk.ifs.competition.resource.ApplicationFinanceType.STANDARD;
import static org.innovateuk.ifs.competition.resource.CompetitionSetupSection.APPLICATION_FORM;
import static org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection.*;
import static org.innovateuk.ifs.question.resource.QuestionSetupType.ASSESSED_QUESTION;
import static org.innovateuk.ifs.question.resource.QuestionSetupType.SCOPE;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Class for testing public functions of {@link CompetitionSetupApplicationController}
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CompetitionSetupApplicationControllerTest extends BaseControllerMockMVCTest<CompetitionSetupApplicationController> {
    private static final Long COMPETITION_ID = 12L;
    private static final Long QUESTION_ID = 1L;
    private static final String URL_PREFIX = String.format("/competition/setup/%d/section/application", COMPETITION_ID);
    private static final CompetitionResource UNEDITABLE_COMPETITION = newCompetitionResource()
            .withCompetitionStatus(CompetitionStatus.OPEN)
            .withSetupComplete(true)
            .withStartDate(ZonedDateTime.now().minusDays(1))
            .withFundersPanelDate(ZonedDateTime.now().plusDays(1)).build();

    @Mock
    private CompetitionSetupService competitionSetupService;

    @Mock
    private CompetitionSetupRestService competitionSetupRestService;

    @Mock
    private CompetitionSetupQuestionService competitionSetupQuestionService;

    @Mock
    private CompetitionSetupPopulator competitionSetupPopulator;

    @Mock
    private QuestionSetupRestService questionSetupRestService;

    @Mock
    private QuestionSetupCompetitionRestService questionSetupCompetitionRestService;

    @Mock
    private CompetitionRestService competitionRestService;

    @Spy
    @InjectMocks
    private CompetitionSetupApplicationQuestionValidator competitionSetupApplicationQuestionValidator;

    @Override
    protected CompetitionSetupApplicationController supplyControllerUnderTest() {
        return new CompetitionSetupApplicationController();
    }

    @Before
    public void setUp() {

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        ReflectionTestUtils.setField(competitionSetupApplicationQuestionValidator, "validator", validator);

        when(competitionSetupService.hasInitialDetailsBeenPreviouslySubmitted(COMPETITION_ID)).thenReturn(true);
    }

    @Test
    public void getEditCompetitionFinance() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));

        mockMvc.perform(get(URL_PREFIX + "/question/finance/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/finances"));

        verify(competitionSetupRestService, never()).update(competition);
        verify(competitionSetupRestService).markSectionIncomplete(competition.getId(), CompetitionSetupSection.APPLICATION_FORM);
    }

    @Test
    public void getEditCompetitionFinanceRedirect() throws Exception {
        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(UNEDITABLE_COMPETITION));

        mockMvc.perform(get(URL_PREFIX + "/question/finance/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(competitionSetupRestService, never()).update(UNEDITABLE_COMPETITION);
    }

    @Test
    public void postEditCompetitionFinance() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(FINANCES)))
                .thenReturn(ServiceResult.serviceSuccess());

        mockMvc.perform(post(URL_PREFIX + "/question/finance/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("applicationFinanceType", String.valueOf(STANDARD))
                .param("includeGrowthTable", String.valueOf(false))
                .param("fundingRules", String.valueOf("Funding rules for this competition")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_PREFIX + "/landing-page"));

        verify(competitionSetupService).saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(FINANCES));
    }

    @Test
    public void postEditKtpCompetitionFinance() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withFundingType(FundingType.KTP)
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(FINANCES)))
                .thenReturn(ServiceResult.serviceSuccess());

        mockMvc.perform(post(URL_PREFIX + "/question/finance/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("applicationFinanceType", String.valueOf(STANDARD))
                .param("fundingRules", String.valueOf("Funding rules for this competition")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_PREFIX + "/landing-page"));

        verify(competitionSetupService).saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(FINANCES));
    }

    @Test
    public void postEditCompetitionFinanceWithErrors() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));

        mockMvc.perform(post(URL_PREFIX + "/question/finance/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("financesRequired", String.valueOf(true))
                .param("growthTableRequired", String.valueOf(true)))
                .andExpect(status().isOk())
                .andExpect(model().errorCount(5))
                .andExpect(model().attributeExists("competitionSetupForm"))
                .andExpect(model().attributeHasFieldErrorCode("competitionSetupForm", "applicationFinanceType",
                        "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("competitionSetupForm", "includeGrowthTable",
                        "FieldRequiredIf"))
                .andExpect(model().attributeHasFieldErrorCode("competitionSetupForm", "includeYourOrganisationSection",
                        "FieldRequiredIf"))
                .andExpect(model().attributeHasFieldErrorCode("competitionSetupForm", "includeJesForm",
                        "FieldRequiredIf"))
                .andExpect(model().attributeHasFieldErrorCode("competitionSetupForm", "fundingRules",
                        "FieldRequiredIf"));

        verify(competitionSetupService, never()).saveCompetitionSetupSubsection(isA(CompetitionSetupForm.class),
                eq(competition), eq(APPLICATION_FORM), eq(FINANCES));
    }

    @Test
    public void postEditKtpCompetitionFinanceWithErrors() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withFundingType(FundingType.KTP)
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));

        mockMvc.perform(post(URL_PREFIX + "/question/finance/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("financesRequired", String.valueOf(true))
                .param("growthTableRequired", String.valueOf(false)))
                .andExpect(status().isOk())
                .andExpect(model().errorCount(4))
                .andExpect(model().attributeExists("competitionSetupForm"))
                .andExpect(model().attributeHasFieldErrorCode("competitionSetupForm", "applicationFinanceType",
                        "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("competitionSetupForm", "includeYourOrganisationSection",
                        "FieldRequiredIf"))
                .andExpect(model().attributeHasFieldErrorCode("competitionSetupForm", "includeJesForm",
                        "FieldRequiredIf"))
                .andExpect(model().attributeHasFieldErrorCode("competitionSetupForm", "fundingRules",
                        "FieldRequiredIf"));

        verify(competitionSetupService, never()).saveCompetitionSetupSubsection(isA(CompetitionSetupForm.class),
                eq(competition), eq(APPLICATION_FORM), eq(FINANCES));
    }

    @Test
    public void viewCompetitionFinance() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupPopulator.populateGeneralModelAttributes(eq(competition), any(), eq(CompetitionSetupSection.APPLICATION_FORM)))
                .thenReturn(getBasicGeneralViewModel(CompetitionSetupSection.APPLICATION_FORM, competition, Boolean.FALSE));

        ModelMap model = mockMvc.perform(get(URL_PREFIX + "/question/finance"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("competition/finances"))
                .andReturn().getModelAndView().getModelMap();

        assertEquals(QuestionSetupViewModel.class, model.get("model").getClass());
        QuestionSetupViewModel viewModel = (QuestionSetupViewModel) model.get("model");

        assertEquals(Boolean.FALSE, viewModel.isEditable());
    }

    @Test
    public void applicationProcessLandingPage() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));

        mockMvc.perform(get(URL_PREFIX + "/landing-page"))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/setup"));
        verify(competitionSetupService, atLeastOnce()).populateCompetitionSectionModelAttributes(any(), any(), any());
        verify(competitionSetupRestService, never()).update(competition);
    }

    @Test
    public void setApplicationProcessAsComplete() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupQuestionService.validateApplicationQuestions(eq(competition), any(), any())).thenReturn(serviceSuccess());

        mockMvc.perform(post(URL_PREFIX + "/landing-page"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/competition/setup/" + COMPETITION_ID + "/section/application/landing-page"));
        verify(competitionSetupQuestionService).validateApplicationQuestions(eq(competition), any(), any());
    }

    @Test
    public void submitSectionApplicationAssessedQuestionWithErrors() throws Exception {
        Long questionId = 4L;
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));

        CompetitionSetupQuestionResource question = new CompetitionSetupQuestionResource();
        question.setQuestionId(questionId);
        question.setType(ASSESSED_QUESTION);

        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(QUESTIONS))).thenReturn(serviceFailure(Collections.emptyList()));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));

        mockMvc.perform(post(URL_PREFIX + "/question/" + questionId + "/edit")
                .param("question.type", ASSESSED_QUESTION.name())
                .param("question.questionId", questionId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/setup/question"));
    }

    @Test
    public void submitSectionApplicationScopeQuestionWithErrors() throws Exception {
        Long questionId = 4L;
        CompetitionSetupQuestionResource question = new CompetitionSetupQuestionResource();
        question.setQuestionId(questionId);
        question.setType(SCOPE);
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(QUESTIONS))).thenReturn(serviceFailure(Collections.emptyList()));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));

        mockMvc.perform(post(URL_PREFIX + "/question/" + questionId + "/edit")
                .param("question.questionId", questionId.toString())
                .param("question.type", SCOPE.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/setup/question"));

        verify(questionSetupCompetitionRestService, never()).save(question);
    }

    @Test
    public void submitSectionApplicationAssessedQuestionWithGuidanceRowErrors() throws Exception {
        Long questionId = 4L;
        CompetitionSetupQuestionResource question = new CompetitionSetupQuestionResource();
        question.setQuestionId(questionId);
        question.setType(ASSESSED_QUESTION);
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(QUESTIONS))).thenReturn(serviceFailure(Collections.emptyList()));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));
        when(competitionSetupPopulator.populateGeneralModelAttributes(any(CompetitionResource.class), any(UserResource.class), any(CompetitionSetupSection.class)))
                .thenReturn(getBasicGeneralViewModel(CompetitionSetupSection.APPLICATION_FORM, competition, Boolean.TRUE));

        Map<String, Object> model = mockMvc.perform(post(URL_PREFIX + "/question/" + questionId + "/edit")
                .param("question.type", ASSESSED_QUESTION.name())
                .param("question.questionId", questionId.toString())
                .param("question.title", "My Title")
                .param("question.guidanceTitle", "My Title")
                .param("question.guidance", "My guidance")
                .param("question.maxWords", "400")
                .param("numberOfUploads", "0")
                .param("question.appendix", "false")
                .param("question.scored", "true")
                .param("question.scoreTotal", "100")
                .param("question.writtenFeedback", "true")
                .param("question.assessmentGuidance", "My assessment guidance")
                .param("question.assessmentGuidanceTitle", "My assessment guidance title")
                .param("question.assessmentMaxWords", "200")
                .param("question.type", "")
                .param("guidanceRows[0].scoreFrom", "")
                .param("guidanceRows[0].scoreTo", "")
                .param("guidanceRows[0].justification", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/setup/question"))
                .andReturn().getModelAndView().getModel();

        assertEquals(QuestionSetupViewModel.class, model.get("model").getClass());
        QuestionSetupViewModel viewModel = (QuestionSetupViewModel) model.get("model");

        assertEquals(Boolean.TRUE, viewModel.getGeneral().isEditable());

        verify(questionSetupCompetitionRestService, never()).save(question);
    }

    @Test
    public void submitSectionApplicationScopeQuestionWithGuidanceRowErrors() throws Exception {
        Long questionId = 4L;
        CompetitionSetupQuestionResource question = new CompetitionSetupQuestionResource();
        question.setQuestionId(questionId);
        question.setType(SCOPE);
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(QUESTIONS))).thenReturn(serviceFailure(Collections.emptyList()));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));

        mockMvc.perform(post(URL_PREFIX + "/question/" + questionId + "/edit")
                .param("question.type", SCOPE.name())
                .param("question.questionId", questionId.toString())
                .param("question.title", "My Title")
                .param("question.guidanceTitle", "My Title")
                .param("question.guidance", "My guidance")
                .param("question.maxWords", "400")
                .param("numberOfUploads", "1")
                .param("question.appendix", "true")
                .param("question.scored", "true")
                .param("question.scoreTotal", "100")
                .param("question.writtenFeedback", "true")
                .param("question.assessmentGuidance", "My assessment guidance")
                .param("question.assessmentGuidanceTitle", "My assessment guidance title")
                .param("question.assessmentMaxWords", "200")
                .param("question.type", "Scope")
                .param("guidanceRows[0].subject", "")
                .param("guidanceRows[0].justification", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/setup/question"));

        verify(questionSetupCompetitionRestService, never()).save(question);
    }

    @Test
    public void submitSectionApplicationAssessedQuestionWithoutErrors() throws Exception {
        Long questionId = 4L;
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();
        CompetitionSetupQuestionResource question = new CompetitionSetupQuestionResource();
        question.setQuestionId(questionId);
        question.setType(ASSESSED_QUESTION);
        question.setTemplateFilename("templateFile");

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(QUESTIONS))).thenReturn(serviceFailure(Collections.emptyList()));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));

        mockMvc.perform(post(URL_PREFIX + "/question/" + questionId.toString() + "/edit")
                .param("question.type", ASSESSED_QUESTION.name())
                .param("question.questionId", questionId.toString())
                .param("question.title", "My Title")
                .param("question.shortTitle", "My Short Title")
                .param("question.guidanceTitle", "My Title")
                .param("question.guidance", "My guidance")
                .param("question.textArea", "true")
                .param("question.maxWords", "400")
                .param("numberOfUploads", "1")
                .param("question.appendix", "true")
                .param("question.allowedAppendixResponseFileTypes", "PDF")
                .param("question.appendixGuidance", "Only PDFs allowed")
                .param("question.templateDocument", "true")
                .param("question.allowedTemplateResponseFileTypes", "DOCUMENT")
                .param("question.templateTitle", "Document")
                .param("question.scored", "true")
                .param("question.scoreTotal", "100")
                .param("question.writtenFeedback", "true")
                .param("question.assessmentGuidance", "My assessment guidance")
                .param("question.assessmentGuidanceTitle", "My assessment guidance title")
                .param("question.assessmentMaxWords", "200")
                .param("guidanceRows[0].scoreFrom", "1")
                .param("guidanceRows[0].scoreTo", "10")
                .param("guidanceRows[0].justification", "My justification"))
                .andExpect(model().hasNoErrors())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_PREFIX + "/landing-page"));

        verify(competitionSetupService).saveCompetitionSetupSubsection(isA(QuestionForm.class),
                eq(competition),
                eq(CompetitionSetupSection.APPLICATION_FORM), eq(CompetitionSetupSubsection.QUESTIONS));
    }

    @Test
    public void submitSectionApplicationAssessedQuestionWithAppendixError() throws Exception {
        Long questionId = 4L;
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();
        CompetitionSetupQuestionResource question = new CompetitionSetupQuestionResource();
        question.setQuestionId(questionId);
        question.setType(ASSESSED_QUESTION);
        question.setTemplateFilename("templateFile");

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(QUESTIONS))).thenReturn(serviceFailure(Collections.emptyList()));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "/question/" + questionId.toString() + "/edit")
                .param("question.type", ASSESSED_QUESTION.name())
                .param("question.questionId", questionId.toString())
                .param("question.title", "My Title")
                .param("question.shortTitle", "My Short Title")
                .param("question.guidanceTitle", "My Title")
                .param("question.guidance", "My guidance")
                .param("question.maxWords", "400")
                .param("numberOfUploads", "1")
                .param("question.appendix", "true")
                .param("question.allowedAppendixResponseFileTypes", "")
                .param("question.appendixGuidance", "")
                .param("question.templateDocument", "true")
                .param("question.allowedTemplateResponseFileTypes", "DOCUMENT")
                .param("question.templateTitle", "Document")
                .param("question.scored", "true")
                .param("question.scoreTotal", "100")
                .param("question.writtenFeedback", "true")
                .param("question.assessmentGuidance", "My assessment guidance")
                .param("question.assessmentGuidanceTitle", "My assessment guidance title")
                .param("question.assessmentMaxWords", "200")
                .param("guidanceRows[0].scoreFrom", "1")
                .param("guidanceRows[0].scoreTo", "10")
                .param("guidanceRows[0].justification", "My justification"))
                .andExpect(model().hasErrors())
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult." + CompetitionSetupController.COMPETITION_SETUP_FORM_KEY);
        assertEquals("This field cannot be left blank.", bindingResult.getFieldError("question.allowedAppendixResponseFileTypes").getDefaultMessage());
        assertEquals("This field cannot be left blank.", bindingResult.getFieldError("question.appendixGuidance").getDefaultMessage());

        verify(competitionSetupService, never()).saveCompetitionSetupSubsection(isA(QuestionForm.class), eq(competition), eq(CompetitionSetupSection.APPLICATION_FORM), eq(CompetitionSetupSubsection.QUESTIONS));
    }

    @Test
    public void submitSectionApplicationQuestionWithAppendixWithoutTypeResultsAndGuidanceInErrors() throws Exception {
        Long questionId = 4L;
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();
        CompetitionSetupQuestionResource question = new CompetitionSetupQuestionResource();
        question.setQuestionId(questionId);
        question.setType(ASSESSED_QUESTION);

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(QUESTIONS))).thenReturn(serviceFailure(Collections.emptyList()));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "/question/" + questionId.toString() + "/edit")
                .param("question.type", ASSESSED_QUESTION.name())
                .param("question.questionId", questionId.toString())
                .param("question.title", "My Title")
                .param("question.shortTitle", "My Short Title")
                .param("question.guidanceTitle", "My Title")
                .param("question.guidance", "My guidance")
                .param("question.maxWords", "400")
                .param("numberOfUploads", "1")
                .param("question.appendix", "true")
                .param("question.templateDocument", "true")
                .param("question.scored", "true")
                .param("question.scoreTotal", "100")
                .param("question.writtenFeedback", "true")
                .param("question.assessmentGuidance", "My assessment guidance")
                .param("question.assessmentGuidanceTitle", "My assessment guidance title")
                .param("question.assessmentMaxWords", "200")
                .param("guidanceRows[0].scoreFrom", "1")
                .param("guidanceRows[0].scoreTo", "10")
                .param("guidanceRows[0].justification", "My justification"))
                .andExpect(model().hasErrors())
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult." + CompetitionSetupController.COMPETITION_SETUP_FORM_KEY);
        assertEquals("This field cannot be left blank.", bindingResult.getFieldError("question.allowedAppendixResponseFileTypes").getDefaultMessage());
        assertEquals("This field cannot be left blank.", bindingResult.getFieldError("question.appendixGuidance").getDefaultMessage());
        assertEquals("FieldRequiredIf", bindingResult.getFieldError("question.allowedTemplateResponseFileTypes").getCode());
        assertEquals("FieldRequiredIf", bindingResult.getFieldError("question.templateTitle").getCode());

        verify(competitionSetupService, never()).saveCompetitionSetupSubsection(isA(QuestionForm.class), eq(competition), eq(CompetitionSetupSection.APPLICATION_FORM), eq(CompetitionSetupSubsection.QUESTIONS));
    }

    @Test
    public void submitSectionApplicationAssessedQuestionWithCheckedOptionsShouldResultInError() throws Exception {
        Long questionId = 4L;
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();
        CompetitionSetupQuestionResource question = newCompetitionSetupQuestionResource()
                .withQuestionId(questionId)
                .withType(ASSESSED_QUESTION).build();
        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(QUESTIONS))).thenReturn(serviceFailure(Collections.emptyList()));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));
        when(competitionSetupPopulator.populateGeneralModelAttributes(any(CompetitionResource.class), any(UserResource.class), any(CompetitionSetupSection.class)))
                .thenReturn(getBasicGeneralViewModel(CompetitionSetupSection.APPLICATION_FORM, competition, Boolean.TRUE));

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "/question/" + questionId + "/edit")
                .param("question.questionId", questionId.toString())
                .param("question.writtenFeedback", "true")
                .param("question.assessmentGuidanceTitle", "")
                .param("question.scored", "true")
                .param("question.scoreTotal", "")
                .param("question.type", "ASSESSED_QUESTION")
                .param("guidanceRows[0].scoreFrom", "")
                .param("guidanceRows[0].scoreTo", "")
                .param("guidanceRows[0].justification", ""))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult." + CompetitionSetupController.COMPETITION_SETUP_FORM_KEY);
        assertEquals(QuestionSetupViewModel.class, result.getModelAndView().getModel().get("model").getClass());
        QuestionSetupViewModel viewModel = (QuestionSetupViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(Boolean.TRUE, viewModel.getGeneral().isEditable());

        assertEquals("FieldRequiredIf", bindingResult.getFieldError("question.scoreTotal").getCode());
        assertEquals("FieldRequiredIf", bindingResult.getFieldError("question.assessmentGuidanceTitle").getCode());
        assertEquals("NotBlank", bindingResult.getFieldError("guidanceRows[0].justification").getCode());
        assertEquals("NotNull", bindingResult.getFieldError("guidanceRows[0].scoreTo").getCode());
        assertEquals("NotNull", bindingResult.getFieldError("guidanceRows[0].scoreFrom").getCode());
    }

    @Test
    public void submitSectionApplicationAssessedQuestionWithUncheckedOptionsShouldNotResultInError() throws Exception {
        Long questionId = 4L;
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();
        CompetitionSetupQuestionResource question = newCompetitionSetupQuestionResource()
                .withQuestionId(questionId)
                .withType(ASSESSED_QUESTION).build();
        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "/question/" + questionId + "/edit")
                .param("question.questionId", questionId.toString())
                .param("question.writtenGuidance", "false")
                .param("question.guidanceTitle", "")
                .param("question.scored", "false")
                .param("question.scoreTotal", "")
                .param("question.type", "ASSESSED_QUESTION")
                .param("guidanceRows[0].scoreFrom", "")
                .param("guidanceRows[0].scoreTo", "")
                .param("guidanceRows[0].justification", ""))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult." + CompetitionSetupController.COMPETITION_SETUP_FORM_KEY);

        assertNull(bindingResult.getFieldError("question.scoreTotal"));
        assertNull(bindingResult.getFieldError("question.assessmentGuidanceTitle"));
        assertNull(bindingResult.getFieldError("guidanceRows[0].justification"));
        assertNull(bindingResult.getFieldError("guidanceRows[0].scoreTo"));
        assertNull(bindingResult.getFieldError("guidanceRows[0].scoreFrom"));
    }

    @Test
    public void submitSectionApplicationScopeQuestionWithCheckedOptionsShouldResultInError() throws Exception {
        Long questionId = 4L;
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();
        CompetitionSetupQuestionResource question = newCompetitionSetupQuestionResource()
                .withQuestionId(questionId)
                .withType(ASSESSED_QUESTION).build();


        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));
        when(competitionSetupPopulator.populateGeneralModelAttributes(any(CompetitionResource.class), any(UserResource.class), any(CompetitionSetupSection.class)))
                .thenReturn(getBasicGeneralViewModel(CompetitionSetupSection.APPLICATION_FORM, competition, Boolean.TRUE));

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "/question/" + questionId + "/edit")
                .param("question.questionId", questionId.toString())
                .param("question.writtenFeedback", "true")
                .param("question.assessmentGuidanceTitle", "")
                .param("question.scored", "true")
                .param("question.scoreTotal", "")
                .param("question.guidanceRows[0].subject", "")
                .param("question.guidanceRows[0].justification", ""))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult." + CompetitionSetupController.COMPETITION_SETUP_FORM_KEY);
        Map<String, Object> model = result.getModelAndView().getModel();

        assertEquals(QuestionSetupViewModel.class, model.get("model").getClass());
        QuestionSetupViewModel viewModel = (QuestionSetupViewModel) model.get("model");

        assertEquals(Boolean.TRUE, viewModel.isEditable());

        assertEquals("FieldRequiredIf", bindingResult.getFieldError("question.scoreTotal").getCode());
        assertEquals("FieldRequiredIf", bindingResult.getFieldError("question.assessmentGuidanceTitle").getCode());
        assertEquals("NotBlank", bindingResult.getFieldError("question.guidanceRows[0].justification").getCode());
        assertEquals("NotBlank", bindingResult.getFieldError("question.guidanceRows[0].subject").getCode());
    }

    @Test
    public void submitSectionApplicationScopeQuestionWithUncheckedOptionsShouldNotResultInError() throws Exception {
        Long questionId = 4L;
        CompetitionSetupQuestionResource question = newCompetitionSetupQuestionResource()
                .withQuestionId(questionId)
                .withType(ASSESSED_QUESTION).build();
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .withId(COMPETITION_ID)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));
        when(competitionSetupPopulator.populateGeneralModelAttributes(eq(competition), any(), eq(CompetitionSetupSection.APPLICATION_FORM)))
                .thenReturn(getBasicGeneralViewModel(CompetitionSetupSection.APPLICATION_FORM, competition, Boolean.TRUE));

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "/question/" + questionId + "/edit")
                .param("question.questionId", questionId.toString())
                .param("question.writtenGuidance", "false")
                .param("question.guidanceTitle", "")
                .param("question.scored", "false")
                .param("question.scoreTotal", "")
                .param("question.guidanceRows[0].subject", "")
                .param("question.guidanceRows[0].justification", ""))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult." + CompetitionSetupController.COMPETITION_SETUP_FORM_KEY);

        Map<String, Object> model = result.getModelAndView().getModel();
        assertEquals(QuestionSetupViewModel.class, model.get("model").getClass());

        QuestionSetupViewModel viewModel = (QuestionSetupViewModel) model.get("model");
        assertEquals(Boolean.TRUE, viewModel.isEditable());

        assertNull(bindingResult.getFieldError("question.scoreTotal"));
        assertNull(bindingResult.getFieldError("question.assessmentGuidanceTitle"));
        assertNull(bindingResult.getFieldError("question.guidanceRows[0].justification"));
        assertNull(bindingResult.getFieldError("quesiton.guidanceRows[0].subject"));
    }

    @Test
    public void validateConditionalTextArea() throws Exception {
        Long questionId = 4L;
        CompetitionSetupQuestionResource question = newCompetitionSetupQuestionResource()
                .withQuestionId(questionId)
                .withType(ASSESSED_QUESTION).build();
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .withId(COMPETITION_ID)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));
        when(competitionSetupPopulator.populateGeneralModelAttributes(eq(competition), any(), eq(CompetitionSetupSection.APPLICATION_FORM)))
                .thenReturn(getBasicGeneralViewModel(CompetitionSetupSection.APPLICATION_FORM, competition, Boolean.TRUE));

        mockMvc.perform(post(URL_PREFIX + "/question/" + questionId + "/edit")
                .param("question.questionId", questionId.toString())
                .param("question.type", "ASSESSED_QUESTION")
                .param("typeOfQuestion", TypeOfQuestion.FREE_TEXT.name()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(model().attributeHasFieldErrorCode(CompetitionSetupController.COMPETITION_SETUP_FORM_KEY, "question.maxWords", "NotNull"));
    }

    @Test
    public void validateMultipleChoice() throws Exception {
        Long questionId = 4L;
        CompetitionSetupQuestionResource question = newCompetitionSetupQuestionResource()
                .withQuestionId(questionId)
                .withType(ASSESSED_QUESTION).build();
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .withId(COMPETITION_ID)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(questionSetupCompetitionRestService.getByQuestionId(questionId)).thenReturn(restSuccess(question));
        when(competitionSetupPopulator.populateGeneralModelAttributes(eq(competition), any(), eq(CompetitionSetupSection.APPLICATION_FORM)))
                .thenReturn(getBasicGeneralViewModel(CompetitionSetupSection.APPLICATION_FORM, competition, Boolean.TRUE));

        mockMvc.perform(post(URL_PREFIX + "/question/" + questionId + "/edit")
                .param("question.questionId", questionId.toString())
                .param("question.type", "ASSESSED_QUESTION")
                .param("typeOfQuestion", TypeOfQuestion.MULTIPLE_CHOICE.name())
                .param("question.choices[0].text", "abc")
                .param("question.choices[1].text", "abc")
                .param("question.choices[2].text", ""))
                .andExpect(status().is2xxSuccessful())
                .andExpect(model().attributeHasFieldErrorCode(CompetitionSetupController.COMPETITION_SETUP_FORM_KEY, "question.choices[0].text", "validation.competition.setup.multiple.choice.duplicate"))
                .andExpect(model().attributeHasFieldErrorCode(CompetitionSetupController.COMPETITION_SETUP_FORM_KEY, "question.choices[1].text", "validation.competition.setup.multiple.choice.duplicate"))
                .andExpect(model().attributeHasFieldErrorCode(CompetitionSetupController.COMPETITION_SETUP_FORM_KEY, "question.choices[2].text", "NotBlank"));
    }
    @Test
    public void submitSectionApplicationScopeQuestionWithoutErrors() throws Exception {
        Long questionId = 4L;
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .withId(2L)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(PROJECT_DETAILS))).thenReturn(serviceSuccess());

        mockMvc.perform(post(URL_PREFIX + "/question/" + questionId + "/edit")
                .param("question.type", SCOPE.name())
                .param("question.questionId", questionId.toString())
                .param("question.title", "My Title")
                .param("question.shortTitle", "Title")
                .param("question.guidanceTitle", "My Title")
                .param("question.guidance", "My guidance")
                .param("question.maxWords", "400")
                .param("numberOfUploads", "1")
                .param("question.appendix", "true")
                .param("question.allowedAppendixResponseFileTypes", "SPREADSHEET")
                .param("question.appendixGuidance", "Spreadsheet only")
                .param("question.scored", "true")
                .param("question.scoreTotal", "100")
                .param("question.writtenFeedback", "true")
                .param("question.assessmentGuidance", "My assessment guidance")
                .param("question.assessmentGuidanceTitle", "My assessment guidance title")
                .param("question.assessmentMaxWords", "200")
                .param("question.guidanceRows[0].subject", "YES")
                .param("question.guidanceRows[0].justification", "My justification")
                .param("question.guidanceRows[1].subject", "NO")
                .param("question.guidanceRows[1].justification", "My justification"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_PREFIX + "/landing-page"));

        verify(competitionSetupService).saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(PROJECT_DETAILS));
    }

    @Test
    public void getEditCompetitionApplicationDetails() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .withId(COMPETITION_ID)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupPopulator.populateGeneralModelAttributes(eq(competition), any(), eq(CompetitionSetupSection.APPLICATION_FORM)))
                .thenReturn(getBasicGeneralViewModel(CompetitionSetupSection.APPLICATION_FORM, competition, Boolean.TRUE));

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/detail/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/application-details"))
                .andReturn();

        ModelMap model = result.getModelAndView().getModelMap();
        assertTrue(model.containsAttribute("model"));
        assertEquals(model.get("model").getClass(), QuestionSetupViewModel.class);
        QuestionSetupViewModel viewModel = (QuestionSetupViewModel) model.get("model");

        assertEquals(Boolean.TRUE, viewModel.getGeneral().isEditable());
        assertEquals(CompetitionSetupSection.APPLICATION_FORM, viewModel.getGeneral().getCurrentSection());

        verify(competitionSetupRestService, never()).update(competition);
        verify(competitionSetupRestService).markSectionIncomplete(competition.getId(), CompetitionSetupSection.APPLICATION_FORM);
    }

    private GeneralSetupViewModel getBasicGeneralViewModel(CompetitionSetupSection section, CompetitionResource competition, Boolean editable) {
        return new GeneralSetupViewModel(editable, competition, section, CompetitionSetupSection.values(), Boolean.TRUE, Boolean.FALSE);
    }

    @Test
    public void getEditCompetitionApplicationDetailsRedirect() throws Exception {
        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(UNEDITABLE_COMPETITION));

        mockMvc.perform(get(URL_PREFIX + "/detail/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(competitionSetupRestService, never()).update(UNEDITABLE_COMPETITION);
    }

    @Test
    public void viewCompetitionApplicationDetails() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .withId(COMPETITION_ID)
                .build();
        DetailsForm form = new DetailsForm();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(competitionSetupService.getSubsectionFormData(
                competition,
                APPLICATION_FORM,
                CompetitionSetupSubsection.APPLICATION_DETAILS,
                Optional.empty())
        ).thenReturn(form);

        mockMvc.perform(get(URL_PREFIX + "/detail"))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/application-details"))
                .andExpect(model().attribute("competitionSetupForm", form));

        verify(competitionSetupRestService, never()).update(competition);
    }

    @Test
    public void postCompetitionApplicationDetails() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .withId(COMPETITION_ID)
                .build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        final Boolean useResubmissionQuestion = true;
        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(APPLICATION_DETAILS))).thenReturn(ServiceResult.serviceSuccess());

        mockMvc.perform(post(URL_PREFIX + "/detail/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("useResubmissionQuestion", String.valueOf(useResubmissionQuestion)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_PREFIX + "/landing-page"));

        verify(competitionSetupService).saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(APPLICATION_DETAILS));
    }

    @Test
    public void postCompetitionApplicationDetailsWithError() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .withId(COMPETITION_ID)
                .build();

        when(competitionSetupService.saveCompetitionSetupSubsection(any(CompetitionSetupForm.class), eq(competition), eq(APPLICATION_FORM), eq(APPLICATION_DETAILS))).thenReturn(ServiceResult.serviceSuccess());
        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));

        mockMvc.perform(post(URL_PREFIX + "/detail/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("useResubmissionQuestion", String.valueOf("Invalid")))
                .andExpect(view().name("competition/application-details"));
    }

    @Test
    public void getEditCompetitionQuestion() throws Exception {

        CompetitionResource competition = newCompetitionResource().withId(QUESTION_ID).withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();
        CompetitionSetupQuestionResource question = newCompetitionSetupQuestionResource()
                .withType(QuestionSetupType.ASSESSED_QUESTION).build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(questionSetupCompetitionRestService.getByQuestionId(QUESTION_ID)).thenReturn(restSuccess(question));

        mockMvc.perform(get(URL_PREFIX + "/question/" + QUESTION_ID + "/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/setup/question"));

        verify(questionSetupCompetitionRestService, atLeastOnce()).getByQuestionId(QUESTION_ID);
        verify(competitionSetupRestService, never()).update(competition);
        verify(competitionSetupRestService).markSectionIncomplete(competition.getId(), CompetitionSetupSection.APPLICATION_FORM);
        verify(questionSetupRestService, times(1)).markQuestionSetupIncomplete(competition.getId(), CompetitionSetupSection.APPLICATION_FORM, QUESTION_ID);
    }

    @Test
    public void getEditCompetitionQuestionRedirect() throws Exception {
        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(UNEDITABLE_COMPETITION));

        mockMvc.perform(get(URL_PREFIX + "/question/" + QUESTION_ID + "/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(questionSetupCompetitionRestService, never()).getByQuestionId(QUESTION_ID);
        verify(competitionSetupRestService, never()).update(UNEDITABLE_COMPETITION);
    }

    @Test
    public void createQuestion() throws Exception {
        CompetitionSetupQuestionResource competitionSetupQuestionResource = newCompetitionSetupQuestionResource().withQuestionId(10L).build();

        when(questionSetupCompetitionRestService.addDefaultToCompetition(COMPETITION_ID)).thenReturn(restSuccess((competitionSetupQuestionResource)));

        mockMvc.perform(post(URL_PREFIX + "/landing-page")
                .param("createQuestion", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" + URL_PREFIX + "/question/10/edit"));

        verify(questionSetupCompetitionRestService).addDefaultToCompetition(COMPETITION_ID);
    }

    @Test
    public void createQuestion_serviceErrorResultsInInternalServerErrorResponse() throws Exception {
        Error error = Error.globalError("Something is wrong.");

        when(questionSetupCompetitionRestService.addDefaultToCompetition(COMPETITION_ID)).thenReturn(restFailure(error));

        mockMvc.perform(post(URL_PREFIX + "/landing-page")
                .param("createQuestion", "true"))
                .andExpect(status().is5xxServerError());

        verify(questionSetupCompetitionRestService).addDefaultToCompetition(COMPETITION_ID);
    }

    @Test
    public void deleteQuestion() throws Exception {
        Long questionId = 1L;

        CompetitionResource competition = newCompetitionResource().build();

        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(questionSetupCompetitionRestService.deleteById(questionId)).thenReturn(restSuccess());

        mockMvc.perform(post(URL_PREFIX + "/landing-page")
                .param("deleteQuestion", questionId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("redirect:" + URL_PREFIX + "/landing-page"));

        verify(questionSetupCompetitionRestService).deleteById(questionId);
    }

    @Test
    public void uploadTemplateDocumentFile() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();
        CompetitionSetupQuestionResource question = newCompetitionSetupQuestionResource()
                .withQuestionId(QUESTION_ID)
                .withType(ASSESSED_QUESTION).build();
        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(questionSetupCompetitionRestService.getByQuestionId(QUESTION_ID)).thenReturn(restSuccess(question));
        MockMultipartFile uploadedFile = new MockMultipartFile("templateDocumentFile", "filename.txt", "text/plain", "My content!".getBytes());
        when(questionSetupCompetitionRestService.uploadTemplateDocument(QUESTION_ID, "text/plain", 11, "filename.txt", "My content!".getBytes())).thenReturn(restSuccess());

        mockMvc.perform(multipart(URL_PREFIX + "/question/" + QUESTION_ID + "/edit")
                .file(uploadedFile)
                .param("uploadTemplateDocumentFile","true")
                .param("question.type", "ASSESSED_QUESTION"))
                .andExpect(status().is2xxSuccessful());

        verify(questionSetupCompetitionRestService).uploadTemplateDocument(QUESTION_ID, "text/plain", 11, "filename.txt", "My content!".getBytes());
    }

    @Test
    public void removeTemplateDocumentFile() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();
        CompetitionSetupQuestionResource question = newCompetitionSetupQuestionResource()
                .withQuestionId(QUESTION_ID)
                .withType(ASSESSED_QUESTION).build();
        when(competitionRestService.getCompetitionById(COMPETITION_ID)).thenReturn(restSuccess(competition));
        when(questionSetupCompetitionRestService.getByQuestionId(QUESTION_ID)).thenReturn(restSuccess(question));
        when(questionSetupCompetitionRestService.deleteTemplateDocument(QUESTION_ID)).thenReturn(restSuccess());

        mockMvc.perform(post(URL_PREFIX + "/question/" + QUESTION_ID + "/edit")
                .param("removeTemplateDocumentFile","true")
                .param("question.type", "ASSESSED_QUESTION"))
                .andExpect(status().is2xxSuccessful());

        verify(questionSetupCompetitionRestService).deleteTemplateDocument(QUESTION_ID);

    }
}