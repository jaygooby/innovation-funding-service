package org.innovateuk.ifs.invite.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.documentation.ApplicationInviteResourceDocs;
import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.invite.resource.InviteOrganisationResource;
import org.innovateuk.ifs.invite.transactional.InviteOrganisationService;
import org.junit.Test;
import org.mockito.Mock;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.InviteOrganisationDocs.inviteOrganisationFields;
import static org.innovateuk.ifs.documentation.InviteOrganisationDocs.inviteOrganisationResourceBuilder;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InviteOrganisationControllerDocumentation extends BaseControllerMockMVCTest<InviteOrganisationController> {

    @Mock
    private InviteOrganisationService inviteOrganisationServiceMock;

    @Override
    protected InviteOrganisationController supplyControllerUnderTest() {
        return new InviteOrganisationController();
    }

    @Test
    public void getById() throws Exception {
        InviteOrganisationResource inviteOrganisationResource = inviteOrganisationResourceBuilder.build();

        when(inviteOrganisationServiceMock.getById(inviteOrganisationResource.getId()))
                .thenReturn(serviceSuccess(inviteOrganisationResource));

        mockMvc.perform(get("/inviteorganisation/{id}", inviteOrganisationResource.getId())
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andDo(document("invite-organisation/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("Id of the invite organisation that is being requested")
                        ),
                        responseFields(inviteOrganisationFields)
                                .andWithPrefix("inviteResources[].", ApplicationInviteResourceDocs.applicationInviteResourceFields)
                ));
    }

    @Test
    public void getByOrganisationIdWithInvitesForApplication() throws Exception {
        InviteOrganisationResource inviteOrganisationResource = inviteOrganisationResourceBuilder.build();
        long organisationId = 1L;
        long applicationId = 1L;

        when(inviteOrganisationServiceMock.getByOrganisationIdWithInvitesForApplication(organisationId, applicationId))
                .thenReturn(serviceSuccess(inviteOrganisationResource));

        mockMvc.perform(get("/inviteorganisation/organisation/{organisationId}/application/{applicationId}",
                organisationId, applicationId)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andDo(document("invite-organisation/{method-name}",
                        pathParameters(
                                parameterWithName("organisationId").description("Id of the organisation that invite organisations are being requested for"),
                                parameterWithName("applicationId").description("Id of the application that invites are being requested for")
                        ),
                        responseFields(inviteOrganisationFields)
                                .andWithPrefix("inviteResources[].", ApplicationInviteResourceDocs.applicationInviteResourceFields)
                ));
    }
}