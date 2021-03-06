package org.innovateuk.ifs.application.forms.sections.yourprojectcosts.populator;

import org.innovateuk.ifs.application.forms.sections.yourprojectcosts.form.YourProjectCostsForm;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.finance.resource.BaseFinanceResource;
import org.innovateuk.ifs.finance.resource.cost.OverheadRateType;
import org.innovateuk.ifs.finance.service.OverheadFileRestService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.innovateuk.ifs.file.builder.FileEntryResourceBuilder.newFileEntryResource;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AbstractYourProjectCostsFormPopulatorTest {

    @Mock
    private OverheadFileRestService overheadFileRestService;

    @InjectMocks
    private AbstractYourProjectCostsFormPopulator target = new AbstractYourProjectCostsFormPopulator() {
        @Override
        protected BaseFinanceResource getFinanceResource(long targetId, long organisationId) {
            return newApplicationFinanceResource().withIndustrialCosts().build();
        }

        @Override
        protected boolean shouldAddEmptyRow() {
            return true;
        }

        @Override
        protected Optional<FileEntryResource> overheadFile(long costId) {
            return overheadFileRestService.getOverheadFileDetails(costId).getOptionalSuccessObject();
        }
    };

    @Test
    public void populate() {
        when(overheadFileRestService.getOverheadFileDetails(any())).thenReturn(RestResult.restSuccess(newFileEntryResource().withName("filename").build()));

        YourProjectCostsForm form = target.populateForm( 1L, 2L);

        Assert.assertEquals((Integer) 250, form.getLabour().getWorkingDaysPerYear());
        Assert.assertEquals(3, form.getLabour().getRows().size());

        Assert.assertEquals((Long) 1L, form.getOverhead().getCostId());
        Assert.assertEquals(OverheadRateType.TOTAL, form.getOverhead().getRateType());
        Assert.assertEquals("filename", form.getOverhead().getFilename());
        Assert.assertEquals((Integer) 1000, form.getOverhead().getTotalSpreadsheet());

        Assert.assertEquals(3, form.getMaterialRows().size());
        Assert.assertEquals(3, form.getCapitalUsageRows().size());
        Assert.assertEquals(3, form.getSubcontractingRows().size());
        Assert.assertEquals(3, form.getTravelRows().size());
        Assert.assertEquals(3, form.getOtherRows().size());
    }

}
