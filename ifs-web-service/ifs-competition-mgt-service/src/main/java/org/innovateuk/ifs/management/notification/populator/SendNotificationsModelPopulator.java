package org.innovateuk.ifs.management.notification.populator;

import org.innovateuk.ifs.application.resource.ApplicationSummaryPageResource;
import org.innovateuk.ifs.application.resource.ApplicationSummaryResource;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.service.ApplicationNotificationTemplateRestService;
import org.innovateuk.ifs.application.service.ApplicationSummaryRestService;
import org.innovateuk.ifs.assessment.resource.ApplicationAssessmentAggregateResource;
import org.innovateuk.ifs.assessment.service.AssessorFormInputResponseRestService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.management.funding.form.NotificationEmailsForm;
import org.innovateuk.ifs.management.notification.viewmodel.SendNotificationsViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

@Component
public class SendNotificationsModelPopulator {

    @Autowired
    private ApplicationSummaryRestService applicationSummaryRestService;

    @Autowired
    private CompetitionRestService competitionRestService;

    @Autowired
    private ApplicationNotificationTemplateRestService applicationNotificationTemplateRestService;

    @Autowired
    private AssessorFormInputResponseRestService assessorFormInputResponseRestService;


    public SendNotificationsViewModel populate(long competitionId, List<Long> applicationIds, NotificationEmailsForm form) {

        ApplicationSummaryPageResource pagedApplications = applicationSummaryRestService
                .getAllApplications(competitionId, null, 0, Integer.MAX_VALUE, empty())
                .getSuccess();

        List<ApplicationSummaryResource> filteredApplications = pagedApplications.getContent().stream()
                .filter(application -> applicationIds.contains(application.getId()) )
                .collect(toList());

        Map<Long, BigDecimal> averageAssessorScoresMap = getApplicationAssessmentAggregateResources(filteredApplications);

        CompetitionResource competitionResource = competitionRestService.getCompetitionById(competitionId).getSuccess();

        long successfulCount = getApplicationCountByFundingDecision(filteredApplications, FundingDecision.FUNDED);
        long unsuccessfulCount = getApplicationCountByFundingDecision(filteredApplications, FundingDecision.UNFUNDED);
        long onHoldCount = getApplicationCountByFundingDecision(filteredApplications, FundingDecision.ON_HOLD);

        if (form.getMessage() == null) {
            tryToPrePopulateMessage(competitionId, successfulCount, unsuccessfulCount, onHoldCount, form);
        }

        return new SendNotificationsViewModel(filteredApplications,
                                              successfulCount,
                                              unsuccessfulCount,
                                              onHoldCount,
                                              competitionId,
                                              competitionResource.getName(),
                                              competitionResource.isH2020(),
                                              averageAssessorScoresMap);
    }

    private Map<Long, BigDecimal> getApplicationAssessmentAggregateResources(List<ApplicationSummaryResource> applicationSummaryResources) {
        Map<Long, BigDecimal> applicationAssessmentAggregateResources = new HashMap<>();

        applicationSummaryResources.forEach(application -> {
            BigDecimal applicationAssessments = assessorFormInputResponseRestService.getApplicationAssessmentAggregate(application.getId()).getSuccess().getAveragePercentage();
            applicationAssessmentAggregateResources.put(application.getId(), applicationAssessments);
        });
        return  applicationAssessmentAggregateResources;
    }

    private long getApplicationCountByFundingDecision(List<ApplicationSummaryResource> filteredApplications, FundingDecision fundingDecision) {
        return filteredApplications.stream()
                .filter(application -> application.getFundingDecision() == fundingDecision)
                .count();
    }

    private void tryToPrePopulateMessage(long competitionId, long successfulCount, long unsuccessfulCount, long onHoldCount, NotificationEmailsForm form) {
        if (onlySuccessfulEmails(successfulCount, unsuccessfulCount, onHoldCount)) {
            form.setMessage(applicationNotificationTemplateRestService.getSuccessfulNotificationTemplate(competitionId).getSuccess().getMessageBody());
        } else if (onlyUnsuccessfulEmails(successfulCount, unsuccessfulCount, onHoldCount)) {
            form.setMessage(applicationNotificationTemplateRestService.getUnsuccessfulNotificationTemplate(competitionId).getSuccess().getMessageBody());
        }
    }

    private boolean onlyUnsuccessfulEmails(long successfulCount, long unsuccessfulCount, long onHoldCount) {
        return unsuccessfulCount > 0 && successfulCount == 0 && onHoldCount == 0;
    }

    private boolean onlySuccessfulEmails(long successfulCount, long unsuccessfulCount, long onHoldCount) {
        return successfulCount > 0 && unsuccessfulCount == 0 && onHoldCount == 0;
    }


}
