package org.innovateuk.ifs.finance.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.competition.resource.CompetitionTypeResource;
import org.innovateuk.ifs.finance.domain.GrantClaimMaximum;
import org.innovateuk.ifs.finance.resource.GrantClaimMaximumResource;
import org.innovateuk.ifs.finance.transactional.GrantClaimMaximumService;
import org.innovateuk.ifs.util.CollectionFunctions;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import java.util.Set;

import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.finance.builder.GrantClaimMaximumResourceBuilder.newGrantClaimMaximumResource;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GrantClaimMaximumControllerTest extends BaseControllerMockMVCTest<GrantClaimMaximumController> {

    @Mock
    private GrantClaimMaximumService grantClaimMaximumService;


    @Override
    protected GrantClaimMaximumController supplyControllerUnderTest() {
        return new GrantClaimMaximumController(grantClaimMaximumService);
    }

    @Test
    public void getGrantClaimMaximumById() throws Exception {
        GrantClaimMaximumResource gcm = newGrantClaimMaximumResource().build();

        when(grantClaimMaximumService.getGrantClaimMaximumById(gcm.getId())).thenReturn(serviceSuccess(gcm));

        mockMvc.perform(get("/grant-claim-maximum/{id}", gcm.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(gcm)));

        verify(grantClaimMaximumService, only()).getGrantClaimMaximumById(gcm.getId());
    }

    @Test
    public void getGrantClaimMaximumByIdNotFound() throws Exception {
        when(grantClaimMaximumService.getGrantClaimMaximumById(1L)).thenReturn(serviceFailure(notFoundError(GrantClaimMaximum.class, 1L)));
        mockMvc.perform(get("/grant-claim-maximum/{id}", 1L))
                .andExpect(status().isNotFound());

        verify(grantClaimMaximumService, only()).getGrantClaimMaximumById(1L);
    }

    @Test
    public void getGrantClaimMaximumsForCompetitionType() throws Exception {
        Long competitionType = 1L;
        Set<Long> expectedGcms = CollectionFunctions.asLinkedSet(2L, 3L);
        when(grantClaimMaximumService.getGrantClaimMaximumsForCompetitionType(competitionType)).thenReturn(serviceSuccess(expectedGcms));

        mockMvc.perform(get("/grant-claim-maximum/get-for-competition-type/{competitionTypeId}", competitionType))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expectedGcms)));

        verify(grantClaimMaximumService, only()).getGrantClaimMaximumsForCompetitionType(competitionType);
    }

    @Test
    public void getGrantClaimMaximumsForCompetitionTypeNotFound() throws Exception {
        when(grantClaimMaximumService.getGrantClaimMaximumsForCompetitionType(1L)).thenReturn(serviceFailure(notFoundError(CompetitionTypeResource.class, 1L)));
        mockMvc.perform(get("/grant-claim-maximum/get-for-competition-type/{competitionTypeId}", 1L))
                .andExpect(status().isNotFound());

        verify(grantClaimMaximumService, only()).getGrantClaimMaximumsForCompetitionType(1L);
    }

    @Test
    public void save() throws Exception {
        GrantClaimMaximumResource gcmResource = newGrantClaimMaximumResource().build();

        when(grantClaimMaximumService.save(any(GrantClaimMaximumResource.class))).thenReturn(serviceSuccess(gcmResource));

        mockMvc.perform(post("/grant-claim-maximum/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson((gcmResource))))
                .andExpect(status().isCreated());

        verify(grantClaimMaximumService, only()).save(any(GrantClaimMaximumResource.class));
    }

    @Test
    public void isMaximumFundingLevelOverridden() throws Exception {
        long competitionId = 1L;
        boolean expectedResult = true;

        when(grantClaimMaximumService.isMaximumFundingLevelOverridden(competitionId)).thenReturn(serviceSuccess
                (expectedResult));

        mockMvc.perform(get("/grant-claim-maximum/maximum-funding-level-overridden/{competitionId}", competitionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(String.valueOf(expectedResult)))
                .andExpect(status().isOk());

        verify(grantClaimMaximumService).isMaximumFundingLevelOverridden(competitionId);
    }
}
