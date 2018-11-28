package org.innovateuk.ifs.application.finance.view;

import org.innovateuk.ifs.competition.resource.CompetitionResource;

public interface FinanceHandlerProvider {

    FinanceFormHandler getFinanceFormHandler(CompetitionResource competition, long organisationType);
    FinanceModelManager getFinanceModelManager(CompetitionResource competition, long organisationType);

}
