package org.innovateuk.ifs.interview.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.interview.resource.InterviewAssessorAllocateApplicationsPageResource;

/**
 * REST service for allocating application to assessors in interview panels
 */
public interface InterviewAllocateRestService {

    RestResult<InterviewAssessorAllocateApplicationsPageResource> getAllocateApplicationsOverview(long competitionId, int page);

}
