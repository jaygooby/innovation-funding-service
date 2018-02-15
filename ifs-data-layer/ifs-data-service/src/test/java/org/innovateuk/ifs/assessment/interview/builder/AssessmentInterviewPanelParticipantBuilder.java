package org.innovateuk.ifs.assessment.interview.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.Builder;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.invite.domain.ParticipantStatus;
import org.innovateuk.ifs.invite.domain.competition.CompetitionParticipantRole;
import org.innovateuk.ifs.invite.domain.competition.InterviewInvite;
import org.innovateuk.ifs.invite.domain.competition.InterviewParticipant;
import org.innovateuk.ifs.invite.domain.competition.RejectionReason;
import org.innovateuk.ifs.user.domain.User;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;

/**
 * Builder for {@link InterviewParticipant}s.
 */
public class AssessmentInterviewPanelParticipantBuilder extends BaseBuilder<InterviewParticipant, AssessmentInterviewPanelParticipantBuilder> {

    public static AssessmentInterviewPanelParticipantBuilder newAssessmentInterviewPanelParticipant() {
        return new AssessmentInterviewPanelParticipantBuilder(emptyList()).with(uniqueIds());
    }

    private AssessmentInterviewPanelParticipantBuilder(List<BiConsumer<Integer, InterviewParticipant>> multiActions) {
        super(multiActions);
    }

    @Override
    protected AssessmentInterviewPanelParticipantBuilder createNewBuilderWithActions(List<BiConsumer<Integer, InterviewParticipant>> actions) {
        return new AssessmentInterviewPanelParticipantBuilder(actions);
    }

    @Override
    protected InterviewParticipant createInitial() {
        try {
            Constructor c = InterviewParticipant.class.getDeclaredConstructor();
            c.setAccessible(true);
            InterviewParticipant instance = (InterviewParticipant) c.newInstance();
            setField("status", ParticipantStatus.PENDING, instance);
            return instance;
        } catch (NoSuchMethodException | IllegalAccessException |InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException("Missing default constructor for InterviewParticipant", e);
        }
    }

    public AssessmentInterviewPanelParticipantBuilder withId(Long... ids) {
        return withArray((id, i) -> setField("id", id, i), ids);
    }

    public AssessmentInterviewPanelParticipantBuilder withStatus(ParticipantStatus... states) {
        return withArray((status, s) -> setField("status", status, s), states);
    }

    public AssessmentInterviewPanelParticipantBuilder withRole(CompetitionParticipantRole... roles) {
        return withArray((role, r) -> setField("role", role, r), roles);
    }

    public AssessmentInterviewPanelParticipantBuilder withCompetition(Competition... competitions) {
        return withArray((competition, p) -> setField("competition", competition, p), competitions);
    }

    public AssessmentInterviewPanelParticipantBuilder withCompetition(Builder<Competition, ?> competitions) {
        return withCompetition(competitions.build());
    }

    public AssessmentInterviewPanelParticipantBuilder withInvite(InterviewInvite... invites) {
        return withArray((invite, i) -> setField("invite", invite, i), invites);
    }

    public AssessmentInterviewPanelParticipantBuilder withInvite(Builder<InterviewInvite, ?> invite) {
        return withInvite(invite.build());
    }

    public AssessmentInterviewPanelParticipantBuilder withUser(User... users) {
        return withArray((user, u) -> setField("user", user, u), users);
    }

    public AssessmentInterviewPanelParticipantBuilder withUser(Builder<User, ?> user) {
        return withUser(user.build());
    }

    public AssessmentInterviewPanelParticipantBuilder withRejectionReason(RejectionReason... rejectionReasons) {
        return withArray((rejectionReason, r) -> setField("rejectionReason", rejectionReason, r), rejectionReasons);
    }

    public AssessmentInterviewPanelParticipantBuilder withRejectionReason(Builder<RejectionReason, ?> rejectionReason) {
        return withRejectionReason(rejectionReason.build());
    }

    public AssessmentInterviewPanelParticipantBuilder withRejectionComment(String... rejectionReasonComments) {
        return withArray((rejectionComment, r) -> setField("rejectionReasonComment", rejectionComment, r), rejectionReasonComments);
    }
}
