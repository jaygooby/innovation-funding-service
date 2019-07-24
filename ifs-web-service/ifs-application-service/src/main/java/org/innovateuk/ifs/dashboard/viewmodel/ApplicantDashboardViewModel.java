package org.innovateuk.ifs.dashboard.viewmodel;

import java.util.List;

/**
 * Applicant dashboard view model
 */
public class ApplicantDashboardViewModel {

    private final List<InSetupDashboardRowViewModel> inSetup;
    private final List<EuGrantTransferDashboardRowViewModel> euGrantTransfers;
    private final List<InProgressDashboardRowViewModel> inProgress;
    private final List<PreviousDashboardRowViewModel> previous;
    private final String originQuery;

    public ApplicantDashboardViewModel(List<InSetupDashboardRowViewModel> inSetup,
                                       List<EuGrantTransferDashboardRowViewModel> euGrantTransfers,
                                       List<InProgressDashboardRowViewModel> inProgress,
                                       List<PreviousDashboardRowViewModel> previous,
                                       String originQuery) {
        this.inSetup = inSetup;
        this.inProgress = inProgress;
        this.euGrantTransfers = euGrantTransfers;
        this.previous = previous;
        this.originQuery = originQuery;
    }

    public List<InSetupDashboardRowViewModel> getProjects() {
        return inSetup;
    }

    public List<InProgressDashboardRowViewModel> getInProgress() {
        return inProgress;
    }

    public List<EuGrantTransferDashboardRowViewModel> getEuGrantTransfers() {
        return euGrantTransfers;
    }

    public List<PreviousDashboardRowViewModel> getPrevious() {
        return previous;
    }

    public String getOriginQuery() {
        return originQuery;
    }

    /* View logic */
    public String getApplicationInProgressText() {
        return inProgress.size() == 1 ?
                "Application in progress" : "Applications in progress";
    }
}
