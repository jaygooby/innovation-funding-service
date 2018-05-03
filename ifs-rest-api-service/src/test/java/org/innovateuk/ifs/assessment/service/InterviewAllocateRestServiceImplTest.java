package org.innovateuk.ifs.assessment.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.interview.resource.InterviewAssessorAllocateApplicationsPageResource;
import org.innovateuk.ifs.interview.service.InterviewAllocateRestServiceImpl;
import org.junit.Test;

import static java.lang.String.format;
import static org.innovateuk.ifs.invite.builder.InterviewAssessorAllocateApplicationsPageResourceBuilder.newInterviewAssessorAllocateApplicationsPageResource;
import static org.junit.Assert.assertEquals;

public class InterviewAllocateRestServiceImplTest extends BaseRestServiceUnitTest<InterviewAllocateRestServiceImpl> {

    private static final String restUrl = "/interview-panel";

    @Override
    protected InterviewAllocateRestServiceImpl registerRestServiceUnderTest() {
        InterviewAllocateRestServiceImpl InterviewAllocateRestService = new InterviewAllocateRestServiceImpl();
        return InterviewAllocateRestService;
    }

    @Test
    public void getAllocateApplicationsOverview() throws Exception {
        long competitionId = 1L;
        int page = 1;

        InterviewAssessorAllocateApplicationsPageResource expected = newInterviewAssessorAllocateApplicationsPageResource().build();

        String expectedUrl = format("%s/%s/%s?page=1", restUrl, "allocate-overview", competitionId);

        setupGetWithRestResultExpectations(expectedUrl, InterviewAssessorAllocateApplicationsPageResource.class, expected);

        InterviewAssessorAllocateApplicationsPageResource actual = service.getAllocateApplicationsOverview(competitionId, page)
                .getSuccess();

        assertEquals(expected, actual);
    }
}