package org.innovateuk.ifs.application.forms.yourprojectcosts.populator;

import org.innovateuk.ifs.application.forms.yourprojectcosts.form.*;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.finance.resource.BaseFinanceResource;
import org.innovateuk.ifs.finance.resource.category.DefaultCostCategory;
import org.innovateuk.ifs.finance.resource.category.LabourCostCategory;
import org.innovateuk.ifs.finance.resource.category.OverheadCostCategory;
import org.innovateuk.ifs.finance.resource.cost.*;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.function.Function;

import static org.innovateuk.ifs.application.forms.yourprojectcosts.form.AbstractCostRowForm.EMPTY_ROW_ID;
import static org.innovateuk.ifs.util.CollectionFunctions.toLinkedMap;

public abstract class AbstractYourProjectCostsFormPopulator {

    @Autowired
    private OrganisationRestService organisationRestService;

    @Autowired
    private QuestionRestService questionRestService;

    @Autowired
    private ApplicationService applicationService;

    public void populateForm(YourProjectCostsForm form, long applicationId, UserResource user) {
        OrganisationResource organisation = organisationRestService.getByUserAndApplicationId(user.getId(), applicationId).getSuccess();
        populateForm(form, applicationId, organisation.getId());
    }

    public void populateForm(YourProjectCostsForm form, long targetId, Long organisationId) {
        BaseFinanceResource finance = getFinanceResource(targetId, organisationId);

        form.setLabour(labour(finance));
        form.setOverhead(overhead(finance));
        form.setCapitalUsageRows(capitalUsageRows(finance));
        form.setMaterialRows(materialRows(finance));
        form.setOtherRows(otherRows(finance));
        form.setSubcontractingRows(subcontractingRows(finance));
        form.setTravelRows(travelRows(finance));

    }

    private LabourForm labour(BaseFinanceResource finance) {
        LabourCostCategory costCategory = (LabourCostCategory) finance.getFinanceOrganisationDetails().get(FinanceRowType.LABOUR);
        costCategory.calculateTotal();
        LabourForm labourForm = new LabourForm();
        labourForm.setWorkingDaysPerYear(costCategory.getWorkingDaysPerYear());
        labourForm.setRows(labourCosts(costCategory));
        return labourForm;
    }

    private OverheadForm overhead(BaseFinanceResource finance) {
        OverheadCostCategory costCategory = (OverheadCostCategory) finance.getFinanceOrganisationDetails().get(FinanceRowType.OVERHEADS);
        Overhead overhead = costCategory.getCosts().stream().findFirst().map(Overhead.class::cast).orElseGet(Overhead::new);
        return new OverheadForm(overhead);
    }


    private Map<String, LabourRowForm> labourCosts(LabourCostCategory costCategory) {
        Map<String, LabourRowForm> rows = costCategory.getCosts().stream()
                .map(LabourCost.class::cast)
                .map(LabourRowForm::new)
                .collect(toLinkedMap((row) -> String.valueOf(row.getCostId()), Function.identity()));

        if (shouldAddEmptyRow()) {
            rows.put(EMPTY_ROW_ID, new LabourRowForm());
        }
        return rows;
    }

    private Map<String, MaterialRowForm> materialRows(BaseFinanceResource finance) {
        DefaultCostCategory costCategory = (DefaultCostCategory) finance.getFinanceOrganisationDetails().get(FinanceRowType.MATERIALS);
        Map<String, MaterialRowForm> rows = costCategory.getCosts().stream()
                .map(Materials.class::cast)
                .map(MaterialRowForm::new)
                .collect(toLinkedMap((row) -> String.valueOf(row.getCostId()), Function.identity()));
        if (shouldAddEmptyRow()) {
            rows.put(EMPTY_ROW_ID, new MaterialRowForm());
        }
        return rows;
    }

    private Map<String, CapitalUsageRowForm> capitalUsageRows(BaseFinanceResource finance) {
        DefaultCostCategory costCategory = (DefaultCostCategory) finance.getFinanceOrganisationDetails().get(FinanceRowType.CAPITAL_USAGE);
        Map<String, CapitalUsageRowForm> rows = costCategory.getCosts().stream()
                .map(CapitalUsage.class::cast)
                .map(CapitalUsageRowForm::new)
                .collect(toLinkedMap((row) -> String.valueOf(row.getCostId()), Function.identity()));
        if (shouldAddEmptyRow()) {
            rows.put(EMPTY_ROW_ID, new CapitalUsageRowForm());
        }
        return rows;
    }

    private Map<String, OtherCostRowForm> otherRows(BaseFinanceResource finance) {
        DefaultCostCategory costCategory = (DefaultCostCategory) finance.getFinanceOrganisationDetails().get(FinanceRowType.OTHER_COSTS);
        Map<String, OtherCostRowForm> rows = costCategory.getCosts().stream()
                .map(OtherCost.class::cast)
                .map(OtherCostRowForm::new)
                .collect(toLinkedMap((row) -> String.valueOf(row.getCostId()), Function.identity()));
        if (shouldAddEmptyRow()) {
            rows.put(EMPTY_ROW_ID, new OtherCostRowForm());
        }
        return rows;
    }

    private Map<String, SubcontractingRowForm> subcontractingRows(BaseFinanceResource finance) {
        DefaultCostCategory costCategory = (DefaultCostCategory) finance.getFinanceOrganisationDetails().get(FinanceRowType.SUBCONTRACTING_COSTS);
        Map<String, SubcontractingRowForm> rows = costCategory.getCosts().stream()
                .map(SubContractingCost.class::cast)
                .map(SubcontractingRowForm::new)
                .collect(toLinkedMap((row) -> String.valueOf(row.getCostId()), Function.identity()));
        if (shouldAddEmptyRow()) {
            rows.put(EMPTY_ROW_ID, new SubcontractingRowForm());
        }
        return rows;
    }

    private Map<String, TravelRowForm> travelRows(BaseFinanceResource finance) {
        DefaultCostCategory costCategory = (DefaultCostCategory) finance.getFinanceOrganisationDetails().get(FinanceRowType.TRAVEL);
        Map<String, TravelRowForm> rows = costCategory.getCosts().stream()
                .map(TravelCost.class::cast)
                .map(TravelRowForm::new)
                .collect(toLinkedMap((row) -> String.valueOf(row.getCostId()), Function.identity()));
        if (shouldAddEmptyRow()) {
            rows.put(EMPTY_ROW_ID, new TravelRowForm());
        }
        return rows;
    }

    protected abstract BaseFinanceResource getFinanceResource(long targetId, long organisationId);

    protected abstract boolean shouldAddEmptyRow();

}
