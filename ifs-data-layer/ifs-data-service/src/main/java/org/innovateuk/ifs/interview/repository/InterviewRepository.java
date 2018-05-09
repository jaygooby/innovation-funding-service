package org.innovateuk.ifs.interview.repository;

import org.innovateuk.ifs.interview.domain.Interview;
import org.innovateuk.ifs.interview.resource.InterviewApplicationResource;
import org.innovateuk.ifs.workflow.repository.ProcessRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface InterviewRepository extends ProcessRepository<Interview>, PagingAndSortingRepository<Interview, Long> {
    String INTERVIEW_COUNT_QUERY = "SELECT count(interviewAssignment.id) " +
            " FROM InterviewAssignment interviewAssignment " +
            " WHERE " +
            " interviewAssignment.target.competition.id = :competitionId AND " +
            " interviewAssignment.activityState IN (org.innovateuk.ifs.interview.resource.InterviewAssignmentState.AWAITING_FEEDBACK_RESPONSE, org.innovateuk.ifs.interview.resource.InterviewAssignmentState.SUBMITTED_FEEDBACK_RESPONSE) ";


    // TODO Probably should be in InterviewAssignmentRepository
    String INTERVIEW_PAGE_RESOURCE_QUERY =
            " SELECT NEW org.innovateuk.ifs.interview.resource.InterviewApplicationResource(" +
            "   interviewAssignment.target.id, " +
            "   interviewAssignment.target.name, " +
            "   organisation.name, " +
            "   count(allInterviews) " +
            " ) " +
            " FROM InterviewAssignment interviewAssignment" +
            " JOIN ProcessRole processRole ON processRole.applicationId = interviewAssignment.target.id " +
            " JOIN Organisation organisation ON organisation.id = processRole.organisationId " +
            " LEFT JOIN Interview allInterviews ON allInterviews.target.id = processRole.applicationId " +
            " WHERE type(allInterviews) = Interview AND " +
            "   processRole.role = org.innovateuk.ifs.user.resource.Role.LEADAPPLICANT AND " +
            "   interviewAssignment.target.competition.id = :competitionId AND " +
            "   interviewAssignment.activityState IN (" +
            "     org.innovateuk.ifs.interview.resource.InterviewAssignmentState.AWAITING_FEEDBACK_RESPONSE, " +
            "     org.innovateuk.ifs.interview.resource.InterviewAssignmentState.SUBMITTED_FEEDBACK_RESPONSE " +
            " ) ";

    String ASSIGNED_INTERVIEW_SUB_QUERY =  " (SELECT innerInterview.id FROM Interview innerInterview WHERE " +
            " innerInterview.target.id = interviewAssignment.target.id AND " +
            " innerInterview.participant.user.id = :assessorId) ";

    List<Interview> findByParticipantUserIdAndTargetCompetitionIdOrderByActivityStateAscIdAsc(long userId, long competitionId);


    @Query(INTERVIEW_PAGE_RESOURCE_QUERY +
            " AND NOT EXISTS " +
            ASSIGNED_INTERVIEW_SUB_QUERY
    )
    Page<InterviewApplicationResource> findApplicationsNotAssignedToAssessor(long competitionId, long assessorId, Pageable pageable);

    @Query(INTERVIEW_PAGE_RESOURCE_QUERY +
            " AND EXISTS " +
            ASSIGNED_INTERVIEW_SUB_QUERY
    )
    Page<InterviewApplicationResource> findApplicationsAssignedToAssessor(long competitionId, long assessorId, Pageable pageable);

    @Query(INTERVIEW_COUNT_QUERY +
            " AND NOT EXISTS " +
            ASSIGNED_INTERVIEW_SUB_QUERY)
    long countUnallocatedApplications(long competitionId, long assessorId);

    @Query(INTERVIEW_COUNT_QUERY +
            " AND EXISTS " +
            ASSIGNED_INTERVIEW_SUB_QUERY)
    long countAllocatedApplications(long competitionId, long assessorId);
}