package org.innovateuk.ifs.assessment.review.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.Builder;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.invite.domain.ParticipantStatus;
import org.innovateuk.ifs.invite.domain.competition.CompetitionParticipantRole;
import org.innovateuk.ifs.invite.domain.competition.RejectionReason;
import org.innovateuk.ifs.invite.domain.competition.ReviewInvite;
import org.innovateuk.ifs.invite.domain.competition.ReviewParticipant;
import org.innovateuk.ifs.user.domain.User;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;

/**
 * Builder for {@link ReviewParticipant}s.
 */
public class AssessmentReviewPanelParticipantBuilder extends BaseBuilder<ReviewParticipant, AssessmentReviewPanelParticipantBuilder> {

    public static AssessmentReviewPanelParticipantBuilder newAssessmentPanelParticipant() {
        return new AssessmentReviewPanelParticipantBuilder(emptyList()).with(uniqueIds());
    }

    private AssessmentReviewPanelParticipantBuilder(List<BiConsumer<Integer, ReviewParticipant>> multiActions) {
        super(multiActions);
    }

    @Override
    protected AssessmentReviewPanelParticipantBuilder createNewBuilderWithActions(List<BiConsumer<Integer, ReviewParticipant>> actions) {
        return new AssessmentReviewPanelParticipantBuilder(actions);
    }

    @Override
    protected ReviewParticipant createInitial() {
        try {
            Constructor c = ReviewParticipant.class.getDeclaredConstructor();
            c.setAccessible(true);
            ReviewParticipant instance = (ReviewParticipant) c.newInstance();
            setField("status", ParticipantStatus.PENDING, instance);
            return instance;
        } catch (NoSuchMethodException | IllegalAccessException |InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException("Missing default constructor for ReviewParticipant", e);
        }
    }

    public AssessmentReviewPanelParticipantBuilder withId(Long... ids) {
        return withArray((id, i) -> setField("id", id, i), ids);
    }

    public AssessmentReviewPanelParticipantBuilder withStatus(ParticipantStatus... states) {
        return withArray((status, s) -> setField("status", status, s), states);
    }

    public AssessmentReviewPanelParticipantBuilder withRole(CompetitionParticipantRole... roles) {
        return withArray((role, r) -> setField("role", role, r), roles);
    }

    public AssessmentReviewPanelParticipantBuilder withCompetition(Competition... competitions) {
        return withArray((competition, p) -> setField("competition", competition, p), competitions);
    }

    public AssessmentReviewPanelParticipantBuilder withCompetition(Builder<Competition, ?> competitions) {
        return withCompetition(competitions.build());
    }

    public AssessmentReviewPanelParticipantBuilder withInvite(ReviewInvite... invites) {
        return withArray((invite, i) -> setField("invite", invite, i), invites);
    }

    public AssessmentReviewPanelParticipantBuilder withInvite(Builder<ReviewInvite, ?> invite) {
        return withInvite(invite.build());
    }

    public AssessmentReviewPanelParticipantBuilder withUser(User... users) {
        return withArray((user, u) -> setField("user", user, u), users);
    }

    public AssessmentReviewPanelParticipantBuilder withUser(Builder<User, ?> user) {
        return withUser(user.build());
    }

    public AssessmentReviewPanelParticipantBuilder withRejectionReason(RejectionReason... rejectionReasons) {
        return withArray((rejectionReason, r) -> setField("rejectionReason", rejectionReason, r), rejectionReasons);
    }

    public AssessmentReviewPanelParticipantBuilder withRejectionReason(Builder<RejectionReason, ?> rejectionReason) {
        return withRejectionReason(rejectionReason.build());
    }

    public AssessmentReviewPanelParticipantBuilder withRejectionComment(String... rejectionReasonComments) {
        return withArray((rejectionComment, r) -> setField("rejectionReasonComment", rejectionComment, r), rejectionReasonComments);
    }
}
