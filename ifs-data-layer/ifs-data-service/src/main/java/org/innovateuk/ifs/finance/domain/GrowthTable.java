package org.innovateuk.ifs.finance.domain;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class GrowthTable extends CompanyFinances {
    private LocalDate financialYearEnd;
    private BigDecimal annualTurnover;
    private BigDecimal annualProfits;
    private BigDecimal annualExport;
    private BigDecimal researchAndDevelopment;

    public LocalDate getFinancialYearEnd() {
        return financialYearEnd;
    }

    public void setFinancialYearEnd(LocalDate financialYearEnd) {
        this.financialYearEnd = financialYearEnd;
    }

    public BigDecimal getAnnualTurnover() {
        return annualTurnover;
    }

    public void setAnnualTurnover(BigDecimal annualTurnover) {
        this.annualTurnover = annualTurnover;
    }

    public BigDecimal getAnnualProfits() {
        return annualProfits;
    }

    public void setAnnualProfits(BigDecimal annualProfits) {
        this.annualProfits = annualProfits;
    }

    public BigDecimal getAnnualExport() {
        return annualExport;
    }

    public void setAnnualExport(BigDecimal annualExport) {
        this.annualExport = annualExport;
    }

    public BigDecimal getResearchAndDevelopment() {
        return researchAndDevelopment;
    }

    public void setResearchAndDevelopment(BigDecimal researchAndDevelopment) {
        this.researchAndDevelopment = researchAndDevelopment;
    }
}
