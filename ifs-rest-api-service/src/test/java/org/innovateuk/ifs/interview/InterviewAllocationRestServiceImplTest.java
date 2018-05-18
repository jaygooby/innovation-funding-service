package org.innovateuk.ifs.interview;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.commons.service.ParameterizedTypeReferences;
import org.innovateuk.ifs.interview.resource.InterviewAcceptedAssessorsPageResource;
import org.innovateuk.ifs.interview.resource.InterviewApplicationPageResource;
import org.innovateuk.ifs.interview.service.InterviewAllocationRestServiceImpl;
import org.junit.Test;

import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.innovateuk.ifs.interview.builder.InterviewAcceptedAssessorsPageResourceBuilder.newInterviewAcceptedAssessorsPageResource;
import static org.innovateuk.ifs.interview.builder.InterviewApplicationPageResourceBuilder.newInterviewApplicationPageResource;
import static org.junit.Assert.assertEquals;

public class InterviewAllocationRestServiceImplTest extends BaseRestServiceUnitTest<InterviewAllocationRestServiceImpl> {

    private static final String restUrl = "/interview-panel";

    @Override
    protected InterviewAllocationRestServiceImpl registerRestServiceUnderTest() {
        InterviewAllocationRestServiceImpl InterviewAllocateRestService = new InterviewAllocationRestServiceImpl();
        return InterviewAllocateRestService;
    }

    @Test
    public void getAllocateApplicationsOverview() throws Exception {
        long competitionId = 1L;
        int page = 1;

        InterviewAcceptedAssessorsPageResource expected = newInterviewAcceptedAssessorsPageResource().build();

        String expectedUrl = format("%s/%s/%s?page=1", restUrl, "allocate-assessors", competitionId);

        setupGetWithRestResultExpectations(expectedUrl, InterviewAcceptedAssessorsPageResource.class, expected);

        InterviewAcceptedAssessorsPageResource actual = service.getInterviewAcceptedAssessors(competitionId, page)
                .getSuccess();

        assertEquals(expected, actual);
    }

    @Test
    public void getAllocatedApplications() {
        long competitionId = 1L;
        long userId = 1L;
        int page = 1;

        InterviewApplicationPageResource expected = newInterviewApplicationPageResource().build();

        String expectedUrl = format("%s/%s/%s/%s?page=1", restUrl, competitionId, "allocated-applications", userId);

        setupGetWithRestResultExpectations(expectedUrl, InterviewApplicationPageResource.class, expected);

        InterviewApplicationPageResource actual = service.getAllocatedApplications(competitionId, userId, page)
                .getSuccess();

        assertEquals(expected, actual);
    }

    @Test
    public void getUnallocatedApplications() {
        long competitionId = 1L;
        long userId = 1L;
        int page = 1;

        InterviewApplicationPageResource expected = newInterviewApplicationPageResource().build();

        String expectedUrl = format("%s/%s/%s/%s?page=1", restUrl, competitionId, "unallocated-applications", userId);

        setupGetWithRestResultExpectations(expectedUrl, InterviewApplicationPageResource.class, expected);

        InterviewApplicationPageResource actual = service.getUnallocatedApplications(competitionId, userId, page)
                .getSuccess();

        assertEquals(expected, actual);
    }

    @Test
    public void getUnallocatedApplicationIds() {
        long competitionId = 1L;
        long userId = 1L;

        List<Long> expected = asList(1L);

        String expectedUrl = format("%s/%s/%s/%s", restUrl, competitionId, "unallocated-application-ids", userId);

        setupGetWithRestResultExpectations(expectedUrl, ParameterizedTypeReferences.longsListType(), expected);

        List<Long> actual = service.getUnallocatedApplicationIds(competitionId, userId)
                .getSuccess();

        assertEquals(expected, actual);
    }
}