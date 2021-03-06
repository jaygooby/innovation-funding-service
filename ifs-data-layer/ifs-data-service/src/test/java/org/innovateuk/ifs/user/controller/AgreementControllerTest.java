package org.innovateuk.ifs.user.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.user.transactional.AgreementService;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.Matchers.is;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.user.builder.AgreementResourceBuilder.newAgreementResource;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AgreementControllerTest extends BaseControllerMockMVCTest<AgreementController> {

    @Mock
    private AgreementService agreementServiceMock;

    @Override
    protected AgreementController supplyControllerUnderTest() {
        return new AgreementController();
    }

    @Test
    public void findCurrent() throws Exception {
        String agreementText = "agreement text";

        when(agreementServiceMock.getCurrent()).thenReturn(serviceSuccess(newAgreementResource().withText(agreementText).build()));

        mockMvc.perform(get("/agreement/find-current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(agreementText)));
    }
}
