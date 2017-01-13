package org.innovateuk.ifs.application.finance.view;

import org.apache.commons.lang3.tuple.Pair;
import org.innovateuk.ifs.application.finance.service.FinanceService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.service.FileEntryRestService;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.BaseFinanceResource;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Configurable
public class OrganisationApplicationFinanceOverview implements OrganisationFinanceOverview {

    private Long applicationId;
    private List<ApplicationFinanceResource> applicationFinances = new ArrayList<>();

    @Autowired
    private FinanceService financeService;

    @Autowired
    private FileEntryRestService fileEntryService;

    public OrganisationApplicationFinanceOverview() {
    	// no-arg constructor
    }

    public OrganisationApplicationFinanceOverview(FinanceService financeService, FileEntryRestService fileEntryRestService, Long applicationId) {
        this.applicationId = applicationId;
        this.financeService = financeService;
        this.fileEntryService = fileEntryRestService;
        initializeOrganisationFinances();
    }

    private void initializeOrganisationFinances() {
        applicationFinances = financeService.getApplicationFinanceTotals(applicationId);
    }

    public Map<Long, BaseFinanceResource> getFinancesByOrganisation(){
        return applicationFinances
                .stream()
                .collect(Collectors.toMap(ApplicationFinanceResource::getOrganisation, f -> f));
    }

    public Map<Long, Pair<BaseFinanceResource, FileEntryResource>> getAcademicOrganisationFileEntries(){
        ArrayList<BaseFinanceResource> applicationFinance = new ArrayList<>(this.getFinancesByOrganisation().values());
        Map<Long, Pair<BaseFinanceResource, FileEntryResource>> files = applicationFinance.stream()
                .filter(o -> ((ApplicationFinanceResource)o).getFinanceFileEntry() != null)
                .collect(HashMap::new, (m,v)->m.put(v.getOrganisation(), Pair.of(v, getFileEntry(v))), HashMap::putAll);
        return files;
    }

    public FileEntryResource getFileEntry(BaseFinanceResource orgFinance){
        if(((ApplicationFinanceResource)orgFinance).getFinanceFileEntry() != null && ((ApplicationFinanceResource)orgFinance).getFinanceFileEntry() > 0L){
            RestResult<FileEntryResource> result = fileEntryService.findOne(((ApplicationFinanceResource)orgFinance).getFinanceFileEntry());
            if(result.isSuccess()){
                return result.getSuccessObject();
            }
        }
        return null;
    }


    public Map<FinanceRowType, BigDecimal> getTotalPerType() {
        Map<FinanceRowType, BigDecimal> totalPerType = new EnumMap<>(FinanceRowType.class);
        for(FinanceRowType costType : FinanceRowType.values()) {
            BigDecimal typeTotal = applicationFinances.stream()
                    .filter(o -> o.getFinanceOrganisationDetails(costType) != null)
                    .map(o -> o.getFinanceOrganisationDetails(costType).getTotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalPerType.put(costType, typeTotal);
        }

        return totalPerType;
    }

    public BigDecimal getTotal() {
        return applicationFinances.stream()
                .map(of -> of.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalFundingSought() {
        BigDecimal totalFundingSought = applicationFinances.stream()
                .filter(of -> of != null && of.getGrantClaimPercentage() != null)
                .map(of -> of.getTotalFundingSought())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalFundingSought;
    }

    public BigDecimal getTotalContribution() {
        return applicationFinances.stream()
                .filter(of -> of != null)
                .map(of -> of.getTotalContribution())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalOtherFunding() {
        return applicationFinances.stream()
                .filter(of -> of != null)
                .map(of -> of.getTotalOtherFunding())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
