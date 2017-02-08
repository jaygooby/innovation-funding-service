package org.innovateuk.ifs.publiccontent.modelpopulator.section;


import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentSectionType;
import org.innovateuk.ifs.publiccontent.modelpopulator.AbstractPublicContentGroupViewModelPopulator;
import org.innovateuk.ifs.publiccontent.modelpopulator.PublicContentViewModelPopulator;
import org.innovateuk.ifs.publiccontent.viewmodel.section.EligibilityViewModel;
import org.springframework.stereotype.Service;


@Service
public class EligibilityViewModelPopulator extends AbstractPublicContentGroupViewModelPopulator<EligibilityViewModel> implements PublicContentViewModelPopulator<EligibilityViewModel> {

    @Override
    protected EligibilityViewModel createInitial() {
        return new EligibilityViewModel();
    }

    @Override
    protected PublicContentSectionType getType() {
        return PublicContentSectionType.ELIGIBILITY;
    }
}
