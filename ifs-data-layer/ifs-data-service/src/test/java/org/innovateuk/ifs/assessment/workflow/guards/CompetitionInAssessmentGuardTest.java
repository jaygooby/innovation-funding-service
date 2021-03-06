package org.innovateuk.ifs.assessment.workflow.guards;

import org.innovateuk.ifs.BaseUnitTestMocksTest;
import org.innovateuk.ifs.assessment.domain.Assessment;
import org.innovateuk.ifs.assessment.resource.AssessmentEvent;
import org.innovateuk.ifs.assessment.resource.AssessmentState;
import org.innovateuk.ifs.competition.domain.Competition;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.statemachine.StateContext;

import static java.time.ZonedDateTime.now;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.assessment.builder.AssessmentBuilder.newAssessment;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.CLOSED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompetitionInAssessmentGuardTest extends BaseUnitTestMocksTest {

    @InjectMocks
    private CompetitionInAssessmentGuard competitionInAssessmentGuard = new CompetitionInAssessmentGuard();

    @Test
    public void evaluate_assessmentDatesNotSet() throws Exception {
        Assessment assessment = buildAssessment(
                newCompetition()
                        .withCompetitionStatus(CLOSED)
                        .build()
        );

        assertFalse(competitionInAssessmentGuard.evaluate(setupContext(assessment)));
    }

    @Test
    public void evaluate_assessmentStartsInFuture() throws Exception {
        Assessment assessment = buildAssessment(
                newCompetition()
                        .withCompetitionStatus(CLOSED)
                        .withAssessorsNotifiedDate(now().plusDays(10L))
                        .build()
        );

        assertFalse(competitionInAssessmentGuard.evaluate(setupContext(assessment)));
    }

    @Test
    public void evaluate_assessmentStartsAndClosesInFuture() throws Exception {
        Assessment assessment = buildAssessment(
                newCompetition()
                        .withCompetitionStatus(CLOSED)
                        .withAssessorsNotifiedDate(now().plusDays(10L))
                        .withAssessmentClosedDate(now().plusDays(20L))
                        .build()
        );

        assertFalse(competitionInAssessmentGuard.evaluate(setupContext(assessment)));
    }

    @Test
    public void evaluate_assessmentStartedAndClosed() throws Exception {
        Assessment assessment = buildAssessment(
                newCompetition()
                        .withCompetitionStatus(CLOSED)
                        .withAssessorsNotifiedDate(now().minusDays(10L))
                        .withAssessmentClosedDate(now().minusDays(1L))
                        .build()
        );

        assertFalse(competitionInAssessmentGuard.evaluate(setupContext(assessment)));
    }

    @Test
    public void evaluate_assessmentStartedButWillCloseInFuture() throws Exception {
        Assessment assessment = buildAssessment(
                newCompetition()
                        .withCompetitionStatus(CLOSED)
                        .withAssessorsNotifiedDate(now().minusDays(10L))
                        .withAssessmentClosedDate(now().plusDays(10L))
                        .build()
        );

        assertTrue(competitionInAssessmentGuard.evaluate(setupContext(assessment)));
    }

    @Test
    public void evaluate_assessmentStartedButCloseDateNotSet() throws Exception {
        Assessment assessment = buildAssessment(
                newCompetition()
                        .withCompetitionStatus(CLOSED)
                        .withAssessorsNotifiedDate(now().minusDays(10L))
                        .build()
        );

        assertTrue(competitionInAssessmentGuard.evaluate(setupContext(assessment)));
    }

    @Test
    public void evaluate_assessmentStartDateNotSetButClosesInFuture() throws Exception {
        Assessment assessment = buildAssessment(
                newCompetition()
                        .withCompetitionStatus(CLOSED)
                        .withAssessmentClosedDate(now().plusDays(10L))
                        .build()
        );

        assertFalse(competitionInAssessmentGuard.evaluate(setupContext(assessment)));
    }

    private Assessment buildAssessment(Competition competition) {
        return newAssessment()
                .withApplication(
                        newApplication()
                                .withCompetition(competition)
                                .build()
                )
                .build();
    }

    private StateContext<AssessmentState, AssessmentEvent> setupContext(Assessment assessment) {
        StateContext<AssessmentState, AssessmentEvent> context = mock(StateContext.class);
        when(context.getMessageHeader("target")).thenReturn(assessment);
        return context;
    }
}
