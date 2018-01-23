package org.innovateuk.ifs.project.grantofferletter.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.commons.error.CommonFailureKeys;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.project.grantofferletter.form.GrantOfferLetterForm;
import org.innovateuk.ifs.project.grantofferletter.populator.GrantOfferLetterModelPopulator;
import org.innovateuk.ifs.project.grantofferletter.viewmodel.GrantOfferLetterModel;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.file.builder.FileEntryResourceBuilder.newFileEntryResource;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class GrantOfferLetterControllerTest extends BaseControllerMockMVCTest<GrantOfferLetterController> {

    @Spy
    @InjectMocks
    @SuppressWarnings("unused")
    private GrantOfferLetterModelPopulator grantOfferLetterViewModelPopulator;

    @Test
    public void testViewGrantOfferLetterPageWithSignedOffer() throws Exception {
        long projectId = 123L;
        long userId = 1L;

        ProjectResource project = newProjectResource().withId(projectId).build();

        setupSignedSentGrantOfferLetterExpectations(projectId, userId, project);

        MvcResult result = mockMvc.perform(get("/project/{projectId}/offer", project.getId())).
                andExpect(status().isOk()).
                andExpect(view().name("project/grant-offer-letter")).
                andReturn();

        GrantOfferLetterModel model = (GrantOfferLetterModel) result.getModelAndView().getModel().get("model");

        // test the view model
        assertEquals(project.getId(), model.getProjectId());
        assertEquals(project.getName(), model.getProjectName());
        assertTrue(model.isOfferSigned());
        assertNull(model.getSubmitDate());
        assertTrue(model.isShowSubmitButton());
        assertNull(model.getSubmitDate());
        assertFalse(model.isSubmitted());
        assertFalse(model.isGrantOfferLetterApproved());
        assertFalse(model.isGrantOfferLetterRejected());
    }

    @Test
    public void testViewGrantOfferLetterPageWithApprovedOffer() throws Exception {
        long projectId = 123L;
        long userId = 1L;

        ProjectResource project = newProjectResource().withId(projectId).build();

        setupSignedSentGrantOfferLetterExpectations(projectId, userId, project);

        when(grantOfferLetterService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(true));

        MvcResult result = mockMvc.perform(get("/project/{projectId}/offer", project.getId())).
                andExpect(status().isOk()).
                andExpect(view().name("project/grant-offer-letter")).
                andReturn();

        GrantOfferLetterModel model = (GrantOfferLetterModel) result.getModelAndView().getModel().get("model");

        // test the view model
        assertEquals(project.getId(), model.getProjectId());
        assertEquals(project.getName(), model.getProjectName());
        assertTrue(model.isOfferSigned());
        assertNull(model.getSubmitDate());
        assertTrue(model.isShowSubmitButton());
        assertNull(model.getSubmitDate());
        assertFalse(model.isSubmitted());
        assertTrue(model.isGrantOfferLetterApproved());
        assertFalse(model.isGrantOfferLetterRejected());
    }

    @Test
    public void testViewGrantOfferLetterPageWithRejectedOffer() throws Exception {
        long projectId = 123L;
        long userId = 1L;

        ProjectResource project = newProjectResource().withId(projectId).build();

        setupSignedSentGrantOfferLetterExpectations(projectId, userId, project);

        when(grantOfferLetterService.isSignedGrantOfferLetterRejected(projectId)).thenReturn(serviceSuccess(true));

        MvcResult result = mockMvc.perform(get("/project/{projectId}/offer", project.getId())).
                andExpect(status().isOk()).
                andExpect(view().name("project/grant-offer-letter")).
                andReturn();

        GrantOfferLetterModel model = (GrantOfferLetterModel) result.getModelAndView().getModel().get("model");

        // test the view model
        assertEquals(project.getId(), model.getProjectId());
        assertEquals(project.getName(), model.getProjectName());
        assertTrue(model.isOfferSigned());
        assertNull(model.getSubmitDate());
        assertTrue(model.isShowSubmitButton());
        assertNull(model.getSubmitDate());
        assertFalse(model.isSubmitted());
        assertFalse(model.isGrantOfferLetterApproved());
        assertTrue(model.isGrantOfferLetterRejected());
    }

    private void setupSignedSentGrantOfferLetterExpectations(long projectId, long userId, ProjectResource project) {
        FileEntryResource grantOfferLetter = newFileEntryResource().build();
        FileEntryResource signedGrantOfferLetter = newFileEntryResource().build();
        FileEntryResource additionalContractFile = newFileEntryResource().build();

        when(projectService.getById(projectId)).thenReturn(project);
        when(projectService.isProjectManager(userId, projectId)).thenReturn(true);
        when(projectService.isUserLeadPartner(projectId, userId)).thenReturn(true);
        when(grantOfferLetterService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.of(signedGrantOfferLetter));
        when(grantOfferLetterService.getGrantOfferFileDetails(projectId)).thenReturn(Optional.of(grantOfferLetter));
        when(grantOfferLetterService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.of(additionalContractFile));
        when(grantOfferLetterService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(true));
        when(grantOfferLetterService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(false));
        when(grantOfferLetterService.isSignedGrantOfferLetterRejected(projectId)).thenReturn(serviceSuccess(false));
    }

    @Test
    public void testDownloadUnsignedGrantOfferLetter() throws Exception {

        FileEntryResource fileDetails = newFileEntryResource().withName("A name").build();
        ByteArrayResource fileContents = new ByteArrayResource("My content!".getBytes());

        when(grantOfferLetterService.getGrantOfferFile(123L)).
                thenReturn(Optional.of(fileContents));

        when(grantOfferLetterService.getGrantOfferFileDetails(123L)).
                thenReturn(Optional.of(fileDetails));

        MvcResult result = mockMvc.perform(get("/project/{projectId}/offer/grant-offer-letter", 123L)).
                andExpect(status().isOk()).
                andReturn();

        assertEquals("My content!", result.getResponse().getContentAsString());
        assertEquals("inline; filename=\"" + fileDetails.getName() + "\"",
                result.getResponse().getHeader("Content-Disposition"));
    }

    @Test
    public void testDownloadSignedGrantOfferLetterByLead() throws Exception {

        FileEntryResource fileDetails = newFileEntryResource().withName("A name").build();
        ByteArrayResource fileContents = new ByteArrayResource("My content!".getBytes());

        when(grantOfferLetterService.getSignedGrantOfferLetterFile(123L)).
                thenReturn(Optional.of(fileContents));

        when(grantOfferLetterService.getSignedGrantOfferLetterFileDetails(123L)).
                thenReturn(Optional.of(fileDetails));

        when(projectService.isUserLeadPartner(123L, 1L)).thenReturn(true);

        MvcResult result = mockMvc.perform(get("/project/{projectId}/offer/signed-grant-offer-letter", 123L)).
                andExpect(status().isOk()).
                andReturn();

        assertEquals("My content!", result.getResponse().getContentAsString());
        assertEquals("inline; filename=\"" + fileDetails.getName() + "\"",
                result.getResponse().getHeader("Content-Disposition"));
    }

    @Test
    public void testDownloadSignedGrantOfferLetterByNonLead() throws Exception {

        mockMvc.perform(get("/project/{projectId}/offer/signed-grant-offer-letter", 123L)).
                andExpect(status().isInternalServerError());
    }

    @Test
    public void testDownloadAdditionalContract() throws Exception {

        FileEntryResource fileDetails = newFileEntryResource().withName("A name").build();
        ByteArrayResource fileContents = new ByteArrayResource("My content!".getBytes());

        when(grantOfferLetterService.getAdditionalContractFile(123L)).
                thenReturn(Optional.of(fileContents));

        when(grantOfferLetterService.getAdditionalContractFileDetails(123L)).
                thenReturn(Optional.of(fileDetails));

        MvcResult result = mockMvc.perform(get("/project/{projectId}/offer/additional-contract", 123L)).
                andExpect(status().isOk()).
                andReturn();

        assertEquals("My content!", result.getResponse().getContentAsString());
        assertEquals("inline; filename=\"" + fileDetails.getName() + "\"",
                result.getResponse().getHeader("Content-Disposition"));
    }

    @Test
    public void testUploadSignedGrantOfferLetter() throws Exception {

        FileEntryResource createdFileDetails = newFileEntryResource().withName("A name").build();

        MockMultipartFile uploadedFile = new MockMultipartFile("signedGrantOfferLetter", "filename.txt", "text/plain", "My content!".getBytes());

        ProjectResource project = newProjectResource().withId(123L).build();

        List<ProjectUserResource> pmUser = newProjectUserResource().
                withRoleName(UserRoleType.PROJECT_MANAGER).
                withUser(loggedInUser.getId()).
                build(1);

        when(projectService.getById(123L)).thenReturn(project);
        when(projectService.getProjectUsersForProject(123L)).thenReturn(pmUser);
        when(grantOfferLetterService.getGrantOfferFileDetails(123L)).thenReturn(Optional.of(createdFileDetails));
        when(grantOfferLetterService.addSignedGrantOfferLetter(123L, "text/plain", 11, "filename.txt", "My content!".getBytes())).
                thenReturn(serviceSuccess(createdFileDetails));

        mockMvc.perform(
                fileUpload("/project/123/offer").
                        file(uploadedFile).
                        param("uploadSignedGrantOfferLetterClicked", "")).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/project/123/offer"));
    }

    @Test
    public void testUploadSignedGrantOfferLetterGolNotSent() throws Exception {

        FileEntryResource createdFileDetails = newFileEntryResource().withName("A name").build();

        MockMultipartFile uploadedFile = new MockMultipartFile("signedGrantOfferLetter", "filename.txt", "text/plain", "My content!".getBytes());

        ProjectResource project = newProjectResource().withId(123L).build();

        ProjectUserResource pmUser = newProjectUserResource().withRoleName(UserRoleType.PROJECT_MANAGER).withUser(loggedInUser.getId()).build();
        List<ProjectUserResource> puRes = new ArrayList<ProjectUserResource>(Arrays.asList(pmUser));

        when(projectService.getById(123L)).thenReturn(project);
        when(projectService.getProjectUsersForProject(123L)).thenReturn(puRes);
        when(grantOfferLetterService.getSignedGrantOfferLetterFileDetails(123L)).thenReturn(Optional.empty());
        when(grantOfferLetterService.getGrantOfferFileDetails(123L)).thenReturn(Optional.of(createdFileDetails));
        when(grantOfferLetterService.getAdditionalContractFileDetails(123L)).thenReturn(Optional.empty());
        when(grantOfferLetterService.addSignedGrantOfferLetter(123L, "text/plain", 11, "filename.txt", "My content!".getBytes())).
                thenReturn(serviceFailure(CommonFailureKeys.GRANT_OFFER_LETTER_MUST_BE_SENT_BEFORE_UPLOADING_SIGNED_COPY));
        when(grantOfferLetterService.isGrantOfferLetterAlreadySent(123L)).thenReturn(serviceSuccess(true));
        when(grantOfferLetterService.isSignedGrantOfferLetterApproved(project.getId())).thenReturn(serviceSuccess(false));
        when(grantOfferLetterService.isSignedGrantOfferLetterRejected(project.getId())).thenReturn(serviceSuccess(false));

        MvcResult mvcResult = mockMvc.perform(
                fileUpload("/project/123/offer").
                        file(uploadedFile).
                        param("uploadSignedGrantOfferLetterClicked", "")).
                andExpect(status().isOk()).
                andExpect(view().name("project/grant-offer-letter")).andReturn();
        GrantOfferLetterForm form = (GrantOfferLetterForm) mvcResult.getModelAndView().getModel().get("form");

        assertEquals(1, form.getObjectErrors().size());
        assertEquals(form.getObjectErrors(), form.getBindingResult().getFieldErrors("signedGrantOfferLetter"));
        assertTrue(form.getObjectErrors().get(0) instanceof FieldError);
    }

    @Test
    public void testUploadSignedGrantOfferLetterGolRejected() throws Exception {

        FileEntryResource createdFileDetails = newFileEntryResource().withName("A name").build();

        MockMultipartFile uploadedFile = new MockMultipartFile("signedGrantOfferLetter", "filename.txt", "text/plain", "My content!".getBytes());

        ProjectResource project = newProjectResource().withId(123L).build();

        List<ProjectUserResource> pmUser = newProjectUserResource().
                withRoleName(UserRoleType.PROJECT_MANAGER).
                withUser(loggedInUser.getId()).
                build(1);

        when(projectService.getById(123L)).thenReturn(project);
        when(projectService.getProjectUsersForProject(123L)).thenReturn(pmUser);
        when(grantOfferLetterService.getSignedGrantOfferLetterFileDetails(123L)).thenReturn(Optional.empty());
        when(grantOfferLetterService.getGrantOfferFileDetails(123L)).thenReturn(Optional.of(createdFileDetails));
        when(grantOfferLetterService.getAdditionalContractFileDetails(123L)).thenReturn(Optional.empty());
        when(grantOfferLetterService.addSignedGrantOfferLetter(123L, "text/plain", 11, "filename.txt", "My content!".getBytes())).
                thenReturn(serviceFailure(CommonFailureKeys.GRANT_OFFER_LETTER_MUST_BE_SENT_BEFORE_UPLOADING_SIGNED_COPY));
        when(grantOfferLetterService.isGrantOfferLetterAlreadySent(123L)).thenReturn(serviceSuccess(true));
        when(grantOfferLetterService.isSignedGrantOfferLetterApproved(project.getId())).thenReturn(serviceSuccess(false));
        when(grantOfferLetterService.isSignedGrantOfferLetterRejected(project.getId())).thenReturn(serviceSuccess(true));

        MvcResult mvcResult = mockMvc.perform(
                fileUpload("/project/123/offer").
                        file(uploadedFile).
                        param("uploadSignedGrantOfferLetterClicked", "")).
                andExpect(status().isOk()).
                andExpect(view().name("project/grant-offer-letter")).andReturn();
        GrantOfferLetterForm form = (GrantOfferLetterForm) mvcResult.getModelAndView().getModel().get("form");

        assertEquals(1, form.getObjectErrors().size());
        assertEquals(form.getObjectErrors(), form.getBindingResult().getFieldErrors("signedGrantOfferLetter"));
        assertTrue(form.getObjectErrors().get(0) instanceof FieldError);
    }

    @Test
    public void testConfirmationView() throws Exception {
        mockMvc.perform(get("/project/123/offer/confirmation")).
                andExpect(status().isOk()).
                andExpect(view().name("project/grant-offer-letter-confirmation"));
    }

    @Test
    public void testSubmitOfferLetter() throws Exception {
        long projectId = 123L;

        when(grantOfferLetterService.submitGrantOfferLetter(projectId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/" + projectId + "/offer").
                param("confirmSubmit", "")).
                andExpect(status().is3xxRedirection());

        verify(grantOfferLetterService).submitGrantOfferLetter(projectId);
    }

    @Test
    public void testRemoveSignedGrantOfferLetter() throws Exception {

        when(grantOfferLetterService.removeSignedGrantOfferLetter(123L)).
                thenReturn(serviceSuccess());

        mockMvc.perform(
                post("/project/123/offer").
                        param("removeSignedGrantOfferLetterClicked", "")).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/project/123/offer"));
    }

    @Override
    protected GrantOfferLetterController supplyControllerUnderTest() {
        return new GrantOfferLetterController();
    }
}
