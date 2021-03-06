package org.innovateuk.ifs.application.forms.questions.applicationdetails.form;

import org.hibernate.validator.constraints.Range;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.CompanyAge;
import org.innovateuk.ifs.application.resource.CompanyPrimaryFocus;
import org.innovateuk.ifs.application.resource.CompetitionReferralSource;
import org.innovateuk.ifs.commons.validation.constraints.FieldComparison;
import org.innovateuk.ifs.commons.validation.constraints.FieldRequiredIf;
import org.innovateuk.ifs.commons.validation.predicate.BiPredicateProvider;
import org.innovateuk.ifs.util.DateUtil;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.function.BiPredicate;

/**
 * Form for application details.
 */
@FieldRequiredIf(required = "startDate", argument = "ktpCompetition", predicate = false, message = "{validation.project.start.date.is.valid.date}")
@FieldComparison(
        firstField = "startDate",
        secondField = "ktpCompetition",
        message = "{validation.project.start.date.not.in.future}",
        predicate = ApplicationDetailsForm.FutureLocalDatePredicateProvider.class
)
public class ApplicationDetailsForm {

    @NotBlank(message = "{validation.project.name.must.not.be.empty}")
    private String name;

    private LocalDate startDate;

    @NotNull
    @Range(min = 1, message = "{validation.project.duration.range.invalid}")
    private Long durationInMonths;

    @NotNull(message = "{validation.application.must.indicate.resubmission.or.not}")
    private Boolean resubmission;

    private String previousApplicationNumber;

    private String previousApplicationTitle;

    private CompetitionReferralSource competitionReferralSource;

    private CompanyAge companyAge;

    private CompanyPrimaryFocus companyPrimaryFocus;

    private Object innovationAreaErrorHolder;

    private Boolean ktpCompetition;

    public void populateForm(ApplicationResource application) {
        this.name = application.getName();
        this.durationInMonths = application.getDurationInMonths();
        this.resubmission = application.getResubmission();
        this.previousApplicationNumber = application.getPreviousApplicationNumber();
        this.previousApplicationTitle = application.getPreviousApplicationTitle();
        this.startDate = application.getStartDate();
        this.competitionReferralSource = application.getCompetitionReferralSource();
        this.companyAge = application.getCompanyAge();
        this.companyPrimaryFocus = application.getCompanyPrimaryFocus();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDurationInMonths() {
        return durationInMonths;
    }

    public void setDurationInMonths(Long durationInMonths) {
        this.durationInMonths = durationInMonths;
    }

    public Boolean getResubmission() {
        return resubmission;
    }

    public void setResubmission(Boolean resubmission) {
        this.resubmission = resubmission;
    }

    public String getPreviousApplicationNumber() {
        return previousApplicationNumber;
    }

    public String getPreviousApplicationTitle() {
        return previousApplicationTitle;
    }

    public void setPreviousApplicationNumber(String previousApplicationNumber) {
        this.previousApplicationNumber = previousApplicationNumber;
    }

    public void setPreviousApplicationTitle(String previousApplicationTitle) {
        this.previousApplicationTitle = previousApplicationTitle;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public CompetitionReferralSource getCompetitionReferralSource() {
        return competitionReferralSource;
    }

    public void setCompetitionReferralSource(CompetitionReferralSource competitionReferralSource) {
        this.competitionReferralSource = competitionReferralSource;
    }

    public CompanyAge getCompanyAge() {
        return companyAge;
    }

    public void setCompanyAge(CompanyAge companyAge) {
        this.companyAge = companyAge;
    }

    public CompanyPrimaryFocus getCompanyPrimaryFocus() {
        return companyPrimaryFocus;
    }

    public void setCompanyPrimaryFocus(CompanyPrimaryFocus companyPrimaryFocus) {
        this.companyPrimaryFocus = companyPrimaryFocus;
    }

    public Object getInnovationAreaErrorHolder() {
        return innovationAreaErrorHolder;
    }

    public void setInnovationAreaErrorHolder(Object innovationAreaErrorHolder) {
        this.innovationAreaErrorHolder = innovationAreaErrorHolder;
    }

    public boolean isKtpCompetition() {
        return ktpCompetition;
    }

    public void setKtpCompetition(boolean ktpCompetition) {
        this.ktpCompetition = ktpCompetition;
    }

    public static class FutureLocalDatePredicateProvider implements BiPredicateProvider<LocalDate, Boolean> {

        public BiPredicate<LocalDate, Boolean> predicate() {
            return (startDate, ktpCompetition) -> isDateInFuture(startDate, ktpCompetition);
        }

        private boolean isDateInFuture(LocalDate startDate, Boolean ktpCompetition) {
            if(!ktpCompetition) {
                return DateUtil.isFutureDate(startDate);
            }

            return true;
        }
    }
}
