package org.innovateuk.ifs.assessment.transactional;

import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.review.resource.ReviewRejectOutcomeResource;
import org.innovateuk.ifs.review.resource.ReviewResource;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * Service for managing assessment panel status of {@link org.innovateuk.ifs.application.domain.Application}s
 */
public interface AssessmentPanelService {

    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    @SecuredBySpring(
            value = "ASSIGN_APPLICATIONS_TO_PANEL",
            description = "Comp admins and execs can assign applications to an assessment panel")
    ServiceResult<Void> assignApplicationToPanel(long applicationId);

    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    @SecuredBySpring(
            value = "UNASSIGN_APPLICATIONS_FROM_PANEL",
            description = "Comp admins and execs can unassign applications from an assessment panel")
    ServiceResult<Void> unassignApplicationFromPanel(long applicationId);

    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    @SecuredBySpring(
            value = "CREATE_AND_NOTIFY_ASSESSMENT_REVIEWS",
            description = "Comp admins and execs can create and notify assessment reviews on an assessment panel")
    ServiceResult<Void> createAndNotifyReviews(long competitionId);

    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    @SecuredBySpring(
            value = "PENDING_ASSESSMENT_REVIEWS",
            description = "Comp admins and execs can determine if there are pending assessment reviews")
    ServiceResult<Boolean> isPendingReviewNotifications(long competitionId);

    @PostFilter("hasPermission(filterObject, 'READ_PANEL_DASHBOARD')")
    ServiceResult<List<ReviewResource>> getAssessmentReviews(long userId, long competitionId);

    @PreAuthorize("hasPermission(#assessmentReviewId, 'ReviewResource', 'READ')")
    ServiceResult<ReviewResource> getAssessmentReview(long assessmentReviewId);

    @PreAuthorize("hasPermission(#assessmentReviewId, 'ReviewResource', 'UPDATE')")
    ServiceResult<Void> acceptAssessmentReview(long assessmentReviewId);

    @PreAuthorize("hasPermission(#assessmentReviewId, 'ReviewResource', 'UPDATE')")
    ServiceResult<Void> rejectAssessmentReview(long assessmentReviewId, ReviewRejectOutcomeResource reviewRejectOutcomeResource);
}