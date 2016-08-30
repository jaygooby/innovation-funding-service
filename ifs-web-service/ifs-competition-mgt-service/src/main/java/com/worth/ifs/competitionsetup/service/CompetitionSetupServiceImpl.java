package com.worth.ifs.competitionsetup.service;

import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.competitionsetup.form.CompetitionSetupForm;
import com.worth.ifs.competitionsetup.service.formpopulator.CompetitionSetupFormPopulator;
import com.worth.ifs.competitionsetup.service.modelpopulator.CompetitionSetupSectionModelPopulator;
import com.worth.ifs.competitionsetup.service.sectionupdaters.CompetitionSetupSectionSaver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CompetitionSetupServiceImpl implements CompetitionSetupService {

	private static final Log LOG = LogFactory.getLog(CompetitionSetupServiceImpl.class);
	
	@Autowired
	private CompetitionService competitionService;

    private Map<CompetitionSetupSection, CompetitionSetupFormPopulator> formPopulators;
	
	private Map<CompetitionSetupSection, CompetitionSetupSectionSaver> sectionSavers;

	private Map<CompetitionSetupSection, CompetitionSetupSectionModelPopulator> modelPopulators;

	
	@Autowired
	public void setCompetitionSetupFormPopulators(Collection<CompetitionSetupFormPopulator> populators) {
		formPopulators = populators.stream().collect(Collectors.toMap(p -> p.sectionToFill(), Function.identity()));
	}
	
	@Autowired
	public void setCompetitionSetupSectionSavers(Collection<CompetitionSetupSectionSaver> savers) {
		sectionSavers = savers.stream().collect(Collectors.toMap(p -> p.sectionToSave(), Function.identity()));
	}
	
	@Autowired
	public void setCompetitionSetupSectionModelPopulators(Collection<CompetitionSetupSectionModelPopulator> populators) {
		modelPopulators = populators.stream().collect(Collectors.toMap(p -> p.sectionToPopulateModel(), Function.identity()));
	}
	
	@Override
	public void populateCompetitionSectionModelAttributes(Model model, CompetitionResource competitionResource,
			CompetitionSetupSection section) {
		
		populateGeneralModelAttributes(model, competitionResource, section);
		CompetitionSetupSectionModelPopulator populator = modelPopulators.get(section);
		
		if(populator != null) {
			populator.populateModel(model, competitionResource);
		}
	}

	@Override
	public CompetitionSetupForm getSectionFormData(CompetitionResource competitionResource,
			CompetitionSetupSection section) {
		
		CompetitionSetupFormPopulator populator = formPopulators.get(section);
		if(populator == null) {
			LOG.error("unable to populate form for section " + section);
			throw new IllegalArgumentException();
		}
		
		return populator.populateForm(competitionResource);
	}

	@Override
	public List<Error> autoSaveCompetitionSetupSection(CompetitionResource competitionResource, CompetitionSetupSection section, String fieldName, String value) {

		CompetitionSetupSectionSaver saver = sectionSavers.get(section);
		if(saver == null) {
			LOG.error("unable to save section " + section);
			throw new IllegalArgumentException();
		}

		return saver.autoSaveSectionField(competitionResource, fieldName, value);
	}

	@Override
	public List<Error> saveCompetitionSetupSection(CompetitionSetupForm competitionSetupForm,
			CompetitionResource competitionResource, CompetitionSetupSection section) {
		
		CompetitionSetupSectionSaver saver = sectionSavers.get(section);
		if(saver == null || !saver.supportsForm(competitionSetupForm.getClass())) {
			LOG.error("unable to save section " + section);
			throw new IllegalArgumentException();
		}
		
		List<Error> errors = saver.saveSection(competitionResource, competitionSetupForm);
		
		if(errors.isEmpty()) {
			competitionService.setSetupSectionMarkedAsComplete(competitionResource.getId(), section);
		}
		
		return errors;
	}

	@Override
	public boolean isCompetitionReadyToOpen(CompetitionResource competitionResource) {
		if (competitionResource.getCompetitionStatus() != CompetitionResource.Status.COMPETITION_SETUP
				&& competitionResource.getStartDate().isAfter(LocalDateTime.now())) {
			return false;
		}
		Optional<CompetitionSetupSection> notDoneSection = getRequiredSectionsForReadyToOpen().stream().filter(section ->
				(!competitionResource.getSectionSetupStatus().containsKey(section) ||
						!competitionResource.getSectionSetupStatus().get(section))).findFirst();

		return !notDoneSection.isPresent() ;
	}

	@Override
	public void setCompetitionAsReadyToOpen(Long competitionId) {
		CompetitionResource competitionResource = competitionService.getById(competitionId);
		if (competitionResource.getCompetitionStatus() == CompetitionResource.Status.READY_TO_OPEN) {
			return;
		}

		if (isCompetitionReadyToOpen(competitionResource)) {
			competitionService.markAsSetup(competitionId);
		} else {
			LOG.error("Requesting to set a competition (id:" + competitionId + ") as Read to Open, But the competition is not ready to open yet. " +
					"Please check all the madatory sections are done");
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void setCompetitionAsCompetitionSetup(Long competitionId) {
		competitionService.returnToSetup(competitionId);
	}

	private List<CompetitionSetupSection> getRequiredSectionsForReadyToOpen() {
		List<CompetitionSetupSection> requiredSections = new ArrayList<>();
		requiredSections.add(CompetitionSetupSection.INITIAL_DETAILS);
		requiredSections.add(CompetitionSetupSection.ADDITIONAL_INFO);
		requiredSections.add(CompetitionSetupSection.ELIGIBILITY);
		requiredSections.add(CompetitionSetupSection.MILESTONES);
		requiredSections.add(CompetitionSetupSection.APPLICATION_FORM);
		return requiredSections;
	}

	private void populateGeneralModelAttributes(Model model, CompetitionResource competitionResource, CompetitionSetupSection section) {
		List<CompetitionSetupSection> completedSections = competitionService
				.getCompletedCompetitionSetupSectionStatusesByCompetitionId(competitionResource.getId());

		boolean editable = !completedSections.contains(section);
		model.addAttribute("editable", editable);

		model.addAttribute("competition", competitionResource);
		model.addAttribute("currentSection", section);
		model.addAttribute("currentSectionFragment", "section-" + section.getPath());

		model.addAttribute("allSections", CompetitionSetupSection.values());
		model.addAttribute("allCompletedSections", completedSections);
		model.addAttribute("subTitle",
				(competitionResource.getCode() != null ? competitionResource.getCode() : "Unknown") + ": "
						+ (competitionResource.getName() != null ? competitionResource.getName() : "Unknown"));
	}

}
