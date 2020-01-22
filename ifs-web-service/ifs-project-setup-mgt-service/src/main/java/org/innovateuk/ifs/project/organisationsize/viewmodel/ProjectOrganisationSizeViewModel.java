package org.innovateuk.ifs.project.organisationsize.viewmodel;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.innovateuk.ifs.application.forms.sections.yourorganisation.form.FormOption;
import org.innovateuk.ifs.finance.resource.OrganisationSize;
import org.innovateuk.ifs.project.resource.ProjectResource;

import java.math.BigDecimal;
import java.util.List;

import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

public class ProjectOrganisationSizeViewModel {

    private final ProjectResource project;
    private final String organisationName;
    private final long organisationId;
    private final OrganisationSize organisationSize;
    private final BigDecimal turnover;
    private final Long employees;
    private boolean showStateAidAgreement;
    private boolean fundingSectionComplete;
    private boolean h2020;
    private boolean readOnly;


    public ProjectOrganisationSizeViewModel(ProjectResource project, String organisationName, long organisationId, OrganisationSize organisationSize, BigDecimal turnover, Long employees) {
        this.project = project;
        this.organisationName = organisationName;
        this.organisationId = organisationId;
        this.organisationSize = organisationSize;
        this.turnover = turnover;
        this.employees = employees;
    }

    public List<FormOption> getOrganisationSizeOptions() {
        return simpleMap(OrganisationSize.values(), size -> new FormOption(size.getDescription(), size.name()));
    }

    public ProjectResource getProject() {
        return project;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public long getOrganisationId() {
        return organisationId;
    }

    public OrganisationSize getOrganisationSize() {
        return organisationSize;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public Long getEmployees() {
        return employees;
    }

    public boolean isShowStateAidAgreement() {
        return showStateAidAgreement;
    }

    public boolean isShowOrganisationSizeAlert() {
        return fundingSectionComplete;
    }

    public boolean isH2020() {
        return h2020;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProjectOrganisationSizeViewModel that = (ProjectOrganisationSizeViewModel) o;

        return new EqualsBuilder()
                .append(project, that.project)
                .append(organisationName, that.organisationName)
                .append(organisationId, that.organisationId)
                .append(organisationSize, that.organisationSize)
                .append(turnover, that.turnover)
                .append(employees, that.employees)
                .append(showStateAidAgreement, that.showStateAidAgreement)
                .append(fundingSectionComplete, that.fundingSectionComplete)
                .append(h2020, that.h2020)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(project)
                .append(organisationName)
                .append(organisationId)
                .append(organisationSize)
                .append(turnover)
                .append(employees)
                .append(showStateAidAgreement)
                .append(fundingSectionComplete)
                .append(h2020)
                .toHashCode();
    }
}
