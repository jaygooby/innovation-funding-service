package org.innovateuk.ifs.grant.service;

import org.innovateuk.ifs.application.domain.FormInputResponse;
import org.innovateuk.ifs.application.repository.FormInputResponseRepository;
import org.innovateuk.ifs.commons.mapper.GlobalMapperConfig;
import org.innovateuk.ifs.competition.domain.CompetitionParticipantRole;
import org.innovateuk.ifs.competition.domain.InnovationLead;
import org.innovateuk.ifs.competition.repository.InnovationLeadRepository;
import org.innovateuk.ifs.finance.domain.ApplicationFinance;
import org.innovateuk.ifs.finance.domain.ProjectFinance;
import org.innovateuk.ifs.finance.domain.ProjectFinanceRow;
import org.innovateuk.ifs.finance.repository.ApplicationFinanceRepository;
import org.innovateuk.ifs.finance.repository.ProjectFinanceRepository;
import org.innovateuk.ifs.finance.repository.ProjectFinanceRowRepository;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.project.core.domain.PartnerOrganisation;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.financechecks.domain.Cost;
import org.innovateuk.ifs.project.financechecks.domain.CostCategory;
import org.innovateuk.ifs.project.spendprofile.domain.SpendProfile;
import org.innovateuk.ifs.project.spendprofile.repository.SpendProfileRepository;
import org.innovateuk.ifs.sil.grant.resource.Forecast;
import org.innovateuk.ifs.sil.grant.resource.Grant;
import org.innovateuk.ifs.sil.grant.resource.Participant;
import org.innovateuk.ifs.sil.grant.resource.Period;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.Role;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.PROJECT_MANAGER;
import static org.innovateuk.ifs.project.grantofferletter.model.GrantOfferLetterFinanceTotalsTablePopulator.GRANT_CLAIM_IDENTIFIER;
import static org.innovateuk.ifs.util.CollectionFunctions.*;

@Mapper(config = GlobalMapperConfig.class)
class GrantMapper {

    private static final String NO_PROJECT_SUMMARY = "no project summary";
    private static final String NO_PUBLIC_DESCRIPTION = "no public description";
    private static final String ACADEMIC_ORGANISATION_SIZE_VALUE = "ACADEMIC";

    @Autowired
    private FormInputResponseRepository formInputResponseRepository;

    @Autowired
    private ProjectFinanceRepository projectFinanceRepository;

    @Autowired
    private ProjectFinanceRowRepository projectFinanceRowRepository;

    @Autowired
    private SpendProfileRepository spendProfileRepository;

    @Autowired
    private ApplicationFinanceRepository applicationFinanceRepository;

    @Autowired
    private InnovationLeadRepository innovationLeadRepository;

    Grant mapToGrant(Project project) {

        long applicationId = project.getApplication().getId();
        long competitionId = project.getApplication().getCompetition().getId();

        Grant grant = new Grant();
        grant.setSourceSystem("IFS");
        grant.setId(applicationId);
        grant.setCompetitionCode(competitionId);
        grant.setTitle(project.getName());
        grant.setGrantOfferLetterDate(project.getOfferSubmittedDate());
        grant.setStartDate(project.getTargetStartDate());
        grant.setDuration(project.getDurationInMonths());

        List<FormInputResponse> formInputResponses = formInputResponseRepository
                .findByApplicationId(applicationId);
        grant.setSummary(formInputResponses.stream()
                .filter(response -> "project summary"
                        .equalsIgnoreCase(response.getFormInput().getDescription()))
                .findFirst()
                .map(FormInputResponse::getValue)
                .orElse(NO_PROJECT_SUMMARY));
        grant.setPublicDescription(formInputResponses.stream()
                .filter(response -> "public description"
                        .equalsIgnoreCase(response.getFormInput().getDescription()))
                .findFirst()
                .map(FormInputResponse::getValue)
                .orElse(NO_PUBLIC_DESCRIPTION));

        GrantMapper.Context context = new GrantMapper.Context()
                .withProjectId(project.getId())
                .withApplicationId(applicationId)
                .withStartDate(project.getTargetStartDate());

        List<Participant> financeContactParticipants = simpleMap(project.getPartnerOrganisations(), partnerOrganisation -> {

            ProjectUser financeContact = simpleFindFirstMandatory(project.getProjectUsers(), projectUser ->
                    projectUser.getOrganisation().getId().equals(partnerOrganisation.getOrganisation().getId()) &&
                            projectUser.getRole().isFinanceContact());

            return toProjectTeamParticipant(context, partnerOrganisation, financeContact);
        });

        PartnerOrganisation leadOrganisation =
                simpleFindFirstMandatory(project.getPartnerOrganisations(), PartnerOrganisation::isLeadOrganisation);

        ProjectUser projectManager =
                getOnlyElement(project.getProjectUsersWithRole(PROJECT_MANAGER));

        Participant projectManagerParticipant = toProjectTeamParticipant(context, leadOrganisation, projectManager);

        InnovationLead innovationLead = innovationLeadRepository.getByCompetitionIdAndRole(competitionId, CompetitionParticipantRole.INNOVATION_LEAD).get(0);
        User innovationLeadUser = innovationLead.getUser();

        Participant innovationLeadParticipant = toSimpleContactParticipant(
                innovationLeadUser.getId(),
                Role.INNOVATION_LEAD,
                innovationLeadUser.getEmail());

//        User monitoringOfficer = null;
//        Participant monitoringOfficerParticipant = toSimpleContactParticipant(
//                monitoringOfficer.getId(),
//                Role.MONITORING_OFFICER,
//                monitoringOfficer.getEmail());

        List<Participant> fullParticipantList = combineLists(
                financeContactParticipants,
                projectManagerParticipant,
                innovationLeadParticipant //,
                // monitoringOfficerParticipant
                );

        grant.setParticipants(fullParticipantList);

        return grant;
    }

    private Participant toProjectTeamParticipant(
            Context context,
            PartnerOrganisation partnerOrganisation,
            ProjectUser contactUser) {

        Organisation organisation = partnerOrganisation.getOrganisation();

        Optional<SpendProfile> spendProfile = spendProfileRepository
                .findOneByProjectIdAndOrganisationId(context.getProjectId(), organisation.getId());

        if (!spendProfile.isPresent()) {
            throw new IllegalStateException("Project " + context.getProjectId() + " and organisation "
                    + organisation.getId() + " does not have a spend profile.  All organisations MUST "
                    + "have a spend profile to send grant");
        }

        /*
         * Calculate overhead percentage
         */
        SpendProfileCalculations grantCalculator = new SpendProfileCalculations(spendProfile.get());

        /*
         * Get cap limit
         */
        ApplicationFinance applicationFinance = applicationFinanceRepository.findByApplicationIdAndOrganisationId(
                context.getApplicationId(), organisation.getId()
        );

        /*
         * Calculate award
         */
        ProjectFinance projectFinance = projectFinanceRepository
                .findByProjectIdAndOrganisationId(context.getProjectId(), organisation.getId());

        String organisationSizeOrAcademic = projectFinance.getOrganisationSize() != null ?
                projectFinance.getOrganisationSize().name() : ACADEMIC_ORGANISATION_SIZE_VALUE;

        List<ProjectFinanceRow> projectFinanceRows = projectFinanceRowRepository.findByTargetId(projectFinance.getId());
        ProjectFinanceRow awardRow = simpleFindFirstMandatory(projectFinanceRows, row -> GRANT_CLAIM_IDENTIFIER.equals(row.getName()));
        BigDecimal awardPercentage = BigDecimal.valueOf(awardRow.getQuantity());

        List<Forecast> forecasts = contactUser.isFinanceContact() ? spendProfile.get()
                .getSpendProfileFigures()
                .getCosts().stream()
                .sorted(Comparator.comparing(this::getFullCostCategoryName))
                .collect(groupingBy(this::getFullCostCategoryName))
                .values().stream()
                .map(this::toForecast)
                .collect(Collectors.toList()) :
                null;

        return Participant.createProjectTeamParticipant(
                organisation.getId(),
                organisation.getOrganisationType().getName(),
                partnerOrganisation.isLeadOrganisation() ? "lead" : "collaborator",
                contactUser.getUser().getId(),
                contactUser.getRole().getName(),
                contactUser.getUser().getEmail(),
                organisationSizeOrAcademic,
                BigDecimal.valueOf(applicationFinance.getMaximumFundingLevel()),
                awardPercentage,
                grantCalculator.getOverheadPercentage(),
                forecasts);
    }

    private Participant toSimpleContactParticipant(
            long userId,
            Role role,
            String userEmail) {

        return Participant.createSimpleContactParticipant(
                userId,
                role.getName(),
                userEmail);
    }

    private String getFullCostCategoryName(Cost cost) {
        CostCategory category = cost.getCostCategory();
        return !isBlank(category.getLabel()) ?
                category.getLabel() + " - " + category.getName() :
                category.getName();
    }

    private Forecast toForecast(List<Cost> costs) {
        Forecast forecast = new Forecast();
        forecast.setCostCategory(getFullCostCategoryName(costs.stream()
                .findFirst()
                .orElseThrow(IllegalStateException::new)));
        forecast.setCost(costs.stream()
                .map(Cost::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(0, RoundingMode.HALF_UP).longValue()
        );
        forecast.setPeriods(costs
                .stream()
                .sorted(Comparator.comparing(cost -> cost.getCostTimePeriod().getOffsetAmount()))
                .map(this::toPeriod)
                .collect(Collectors.toList()));
        return forecast;
    }

    private Period toPeriod(Cost cost) {
        Period period = new Period();
        period.setMonth(cost.getCostTimePeriod().getOffsetAmount());
        period.setValue(cost.getValue()
                .setScale(0, RoundingMode.HALF_UP).longValue());
        return period;
    }

    private static class Context {
        private long projectId;
        private long applicationId;
        private LocalDate startDate;

        Context withProjectId(long projectId) {
            this.projectId = projectId;
            return this;
        }

        Context withApplicationId(long applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        Context withStartDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        long getProjectId() {
            return projectId;
        }

        long getApplicationId() {
            return applicationId;
        }

        LocalDate getStartDate() {
            return startDate;
        }
    }
}
