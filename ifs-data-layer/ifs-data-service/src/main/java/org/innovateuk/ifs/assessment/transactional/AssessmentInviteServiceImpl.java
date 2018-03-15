package org.innovateuk.ifs.assessment.transactional;

import org.innovateuk.ifs.assessment.mapper.CompetitionInviteMapper;
import org.innovateuk.ifs.category.domain.Category;
import org.innovateuk.ifs.category.domain.InnovationArea;
import org.innovateuk.ifs.category.mapper.InnovationAreaMapper;
import org.innovateuk.ifs.category.repository.InnovationAreaRepository;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.repository.CompetitionRepository;
import org.innovateuk.ifs.invite.domain.Participant;
import org.innovateuk.ifs.invite.domain.ParticipantStatus;
import org.innovateuk.ifs.invite.domain.competition.AssessmentInvite;
import org.innovateuk.ifs.invite.domain.competition.AssessmentParticipant;
import org.innovateuk.ifs.invite.domain.competition.CompetitionParticipant;
import org.innovateuk.ifs.invite.domain.competition.RejectionReason;
import org.innovateuk.ifs.invite.mapper.ParticipantStatusMapper;
import org.innovateuk.ifs.invite.repository.AssessmentInviteRepository;
import org.innovateuk.ifs.invite.repository.CompetitionParticipantRepository;
import org.innovateuk.ifs.invite.repository.RejectionReasonRepository;
import org.innovateuk.ifs.invite.resource.*;
import org.innovateuk.ifs.notifications.resource.ExternalUserNotificationTarget;
import org.innovateuk.ifs.notifications.resource.Notification;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.notifications.resource.SystemNotificationSource;
import org.innovateuk.ifs.notifications.service.NotificationTemplateRenderer;
import org.innovateuk.ifs.notifications.service.senders.NotificationSender;
import org.innovateuk.ifs.profile.domain.Profile;
import org.innovateuk.ifs.profile.repository.ProfileRepository;
import org.innovateuk.ifs.security.LoggedInUserSupplier;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.innovateuk.ifs.category.resource.CategoryType.INNOVATION_AREA;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.*;
import static org.innovateuk.ifs.commons.service.ServiceResult.*;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.*;
import static org.innovateuk.ifs.invite.constant.InviteStatus.*;
import static org.innovateuk.ifs.invite.domain.Invite.generateInviteHash;
import static org.innovateuk.ifs.invite.domain.ParticipantStatus.*;
import static org.innovateuk.ifs.invite.domain.competition.CompetitionParticipantRole.ASSESSOR;
import static org.innovateuk.ifs.util.CollectionFunctions.mapWithIndex;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.innovateuk.ifs.util.StringFunctions.plainTextToHtml;
import static org.innovateuk.ifs.util.StringFunctions.stripHtml;
import static org.springframework.data.domain.Sort.Direction.ASC;

/**
 * Service for managing {@link AssessmentInvite}s.
 */
@Service
@Transactional
public class AssessmentInviteServiceImpl implements AssessmentInviteService {

    private static final String WEB_CONTEXT = "/assessment";
    private static final DateTimeFormatter inviteFormatter = ofPattern("d MMMM yyyy");
    private static final DateTimeFormatter detailsFormatter = ofPattern("d MMM yyyy");

    @Autowired
    private AssessmentInviteRepository assessmentInviteRepository;

    @Autowired
    private CompetitionParticipantRepository competitionParticipantRepository;

    @Autowired
    private RejectionReasonRepository rejectionReasonRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private InnovationAreaRepository innovationAreaRepository;

    @Autowired
    private CompetitionInviteMapper competitionInviteMapper;

    @Autowired
    private InnovationAreaMapper innovationAreaMapper;

    @Autowired
    private ParticipantStatusMapper participantStatusMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private NotificationSender notificationSender;

    @Autowired
    private NotificationTemplateRenderer renderer;

    @Autowired
    private SystemNotificationSource systemNotificationSource;

    @Autowired
    private LoggedInUserSupplier loggedInUserSupplier;

    @Value("${ifs.web.baseURL}")
    private String webBaseUrl;

    enum Notifications {
        INVITE_ASSESSOR,
        INVITE_ASSESSOR_GROUP
    }

    @Override
    public ServiceResult<AssessorInvitesToSendResource> getAllInvitesToSend(long competitionId) {
        return getCompetition(competitionId).andOnSuccess(competition -> {
            List<AssessmentInvite> invites = assessmentInviteRepository.getByCompetitionIdAndStatus(competition.getId(), CREATED);

            List<String> recipients = simpleMap(invites, AssessmentInvite::getName);
            recipients.sort(String::compareTo);

            return serviceSuccess(new AssessorInvitesToSendResource(
                    recipients,
                    competition.getId(),
                    competition.getName(),
                    getInvitePreviewContent(competition)
            ));
        });
    }

    @Override
    public ServiceResult<AssessorInvitesToSendResource> getAllInvitesToResend(long competitionId, List<Long> inviteIds) {
        return getCompetition(competitionId).andOnSuccess(competition -> {

            List<AssessmentInvite> invites = assessmentInviteRepository.getByIdIn(inviteIds);
            List<String> recipients = simpleMap(invites, AssessmentInvite::getName);
            recipients.sort(String::compareTo);

            return serviceSuccess(new AssessorInvitesToSendResource(
                    recipients,
                    competition.getId(),
                    competition.getName(),
                    getInvitePreviewContent(competition)
            ));
        });
    }

    @Override
    public ServiceResult<AssessorInvitesToSendResource> getInviteToSend(long inviteId) {
        return getById(inviteId).andOnSuccess(invite ->
                serviceSuccess(new AssessorInvitesToSendResource(
                        singletonList(invite.getName()),
                        invite.getTarget().getId(),
                        invite.getTarget().getName(),
                        getInviteContent(invite)
                ))
        );
    }

    private String getInviteContent(AssessmentInvite invite) {
        NotificationTarget notificationTarget = new ExternalUserNotificationTarget("", "");
        Competition competition = invite.getTarget();

        return getInviteContent(notificationTarget, asMap(
                "competitionName", competition.getName(),
                "acceptsDate", competition.getAssessorAcceptsDate().format(inviteFormatter),
                "deadlineDate", competition.getAssessorDeadlineDate().format(inviteFormatter),
                "name", invite.getName(),
                "inviteUrl", format("%s/invite/competition/%s", webBaseUrl + WEB_CONTEXT, invite.getHash())
        ));
    }

    private String getInvitePreviewContent(Competition competition) {
        NotificationTarget notificationTarget = new ExternalUserNotificationTarget("", "");

        return getInvitePreviewContent(notificationTarget, asMap(
                "competitionName", competition.getName(),
                "acceptsDate", competition.getAssessorAcceptsDate().format(inviteFormatter),
                "deadlineDate", competition.getAssessorDeadlineDate().format(inviteFormatter)
        ));
    }

    @Override
    public ServiceResult<CompetitionInviteResource> getInvite(String inviteHash) {
        return getByHashIfOpen(inviteHash)
                .andOnSuccessReturn(competitionInviteMapper::mapToResource);
    }

    @Override
    public ServiceResult<CompetitionInviteResource> openInvite(String inviteHash) {
        return getByHashIfOpen(inviteHash)
                .andOnSuccessReturn(this::openInvite)
                .andOnSuccessReturn(competitionInviteMapper::mapToResource);
    }

    @Override
    public ServiceResult<Void> acceptInvite(String inviteHash, UserResource currentUser) {
        final User user = userRepository.findOne(currentUser.getId());
        return getParticipantByInviteHash(inviteHash)
                .andOnSuccess(p -> accept(p, user))
                .andOnSuccessReturnVoid();
    }

    @Override
    public ServiceResult<Void> rejectInvite(String inviteHash, RejectionReasonResource rejectionReason, Optional<String> rejectionComment) {
        return getRejectionReason(rejectionReason)
                .andOnSuccess(reason -> getParticipantByInviteHash(inviteHash)
                        .andOnSuccess(invite -> reject(invite, reason, rejectionComment)))
                .andOnSuccessReturnVoid();
    }

    @Override
    public ServiceResult<Boolean> checkExistingUser(@P("inviteHash") String inviteHash) {
        return getByHash(inviteHash).andOnSuccessReturn(invite -> {
            if (invite.getUser() != null) {
                return TRUE;
            }

            return userRepository.findByEmail(invite.getEmail()).isPresent();
        });
    }

    @Override
    public ServiceResult<AvailableAssessorPageResource> getAvailableAssessors(long competitionId, Pageable pageable, Optional<Long> innovationArea) {
        final Page<User> pagedResult = innovationArea.map(i -> assessmentInviteRepository.findAssessorsByCompetitionAndInnovationArea(competitionId, i, pageable))
                .orElse(assessmentInviteRepository.findAssessorsByCompetition(competitionId, pageable));

        return serviceSuccess(new AvailableAssessorPageResource(
                pagedResult.getTotalElements(),
                pagedResult.getTotalPages(),
                simpleMap(pagedResult.getContent(), this::mapToAvailableAssessorResource),
                pagedResult.getNumber(),
                pagedResult.getSize()
        ));
    }

    @Override
    public ServiceResult<List<Long>> getAvailableAssessorIds(long competitionId, Optional<Long> innovationArea) {

        List<User> result = innovationArea.map(innovationAreaId -> assessmentInviteRepository.findAssessorsByCompetitionAndInnovationArea(
                competitionId, innovationAreaId
        )).orElseGet(() -> assessmentInviteRepository.findAssessorsByCompetition(competitionId));

        return serviceSuccess(simpleMap(result, User::getId));
    }

    private AvailableAssessorResource mapToAvailableAssessorResource(User assessor) {
        Profile profile = profileRepository.findOne(assessor.getProfileId());

        AvailableAssessorResource availableAssessor = new AvailableAssessorResource();
        availableAssessor.setId(assessor.getId());
        availableAssessor.setEmail(assessor.getEmail());
        availableAssessor.setName(assessor.getName());
        availableAssessor.setBusinessType(profile.getBusinessType());
        availableAssessor.setCompliant(profile.isCompliant(assessor));
        availableAssessor.setInnovationAreas(simpleMap(profile.getInnovationAreas(), innovationAreaMapper::mapToResource));

        return availableAssessor;
    }

    @Override
    public ServiceResult<AssessorCreatedInvitePageResource> getCreatedInvites(long competitionId, Pageable pageable) {
        return getInvitePageResource(competitionId, pageable);
    }

    @Override
    public ServiceResult<CompetitionInviteStatisticsResource> getInviteStatistics(long competitionId) {
        CompetitionInviteStatisticsResource statisticsResource = new CompetitionInviteStatisticsResource();
        statisticsResource.setInvited(assessmentInviteRepository.countByCompetitionIdAndStatusIn(competitionId, EnumSet.of(OPENED, SENT)));
        statisticsResource.setInviteList(assessmentInviteRepository.countByCompetitionIdAndStatusIn(competitionId, EnumSet.of(CREATED)));
        statisticsResource.setAccepted(competitionParticipantRepository.countByCompetitionIdAndRoleAndStatus(competitionId, ASSESSOR, ACCEPTED));
        statisticsResource.setDeclined(competitionParticipantRepository.countByCompetitionIdAndRoleAndStatus(competitionId, ASSESSOR, REJECTED));
        return serviceSuccess(statisticsResource);
    }

    @Override
    public ServiceResult<AssessorInviteOverviewPageResource> getInvitationOverview(long competitionId,
                                                                                   Pageable pageable,
                                                                                   Optional<Long> innovationArea,
                                                                                   List<ParticipantStatus> statuses,
                                                                                   Optional<Boolean> compliant) {
        Page<AssessmentParticipant> pagedResult;

        if (innovationArea.isPresent() || compliant.isPresent()) {
            // We want to avoid performing the potentially expensive join on Profile if possible
            pagedResult = competitionParticipantRepository.getAssessorsByCompetitionAndInnovationAreaAndStatusContainsAndCompliant(
                    competitionId,
                    innovationArea.orElse(null),
                    statuses,
                    compliant.orElse(null),
                    pageable
            );
        } else {
            pagedResult = competitionParticipantRepository.getAssessorsByCompetitionAndStatusContains(
                    competitionId,
                    statuses,
                    pageable
            );
        }

        List<AssessorInviteOverviewResource> inviteOverviews = simpleMap(
                pagedResult.getContent(),
                participant -> {
                    AssessorInviteOverviewResource assessorInviteOverview = new AssessorInviteOverviewResource();
                    assessorInviteOverview.setName(participant.getInvite().getName());
                    assessorInviteOverview.setStatus(participantStatusMapper.mapToResource(participant.getStatus()));
                    assessorInviteOverview.setDetails(getDetails(participant));
                    assessorInviteOverview.setInviteId(participant.getInvite().getId());

                    if (participant.getUser() != null) {
                        Profile profile = profileRepository.findOne(participant.getUser().getProfileId());

                        assessorInviteOverview.setId(participant.getUser().getId());
                        assessorInviteOverview.setBusinessType(profile.getBusinessType());
                        assessorInviteOverview.setCompliant(profile.isCompliant(participant.getUser()));
                        assessorInviteOverview.setInnovationAreas(simpleMap(profile.getInnovationAreas(), innovationAreaMapper::mapToResource));
                    } else {
                        assessorInviteOverview.setInnovationAreas(singletonList(
                                innovationAreaMapper.mapToResource(participant.getInvite().getInnovationArea())
                        ));
                    }

                    return assessorInviteOverview;
                });

        return serviceSuccess(new AssessorInviteOverviewPageResource(
                pagedResult.getTotalElements(),
                pagedResult.getTotalPages(),
                inviteOverviews,
                pagedResult.getNumber(),
                pagedResult.getSize()
        ));
    }

    @Override
    public ServiceResult<List<Long>> getAssessorsNotAcceptedInviteIds(long competitionId,
                                                                      Optional<Long> innovationArea,
                                                                      List<ParticipantStatus> statuses,
                                                                      Optional<Boolean> compliant) {
        List<AssessmentParticipant> participants;

        if (innovationArea.isPresent() || compliant.isPresent()) {
            // We want to avoid performing the potentially expensive join on Profile if possible
            participants = competitionParticipantRepository.getAssessorsByCompetitionAndInnovationAreaAndStatusContainsAndCompliant(
                    competitionId,
                    innovationArea.orElse(null),
                    statuses,
                    compliant.orElse(null));
        } else {
            participants = competitionParticipantRepository.getAssessorsByCompetitionAndStatusContains(
                    competitionId,
                    statuses);
        }

        return serviceSuccess(simpleMap(participants, participant -> participant.getInvite().getId()));
    }

    @Override
    public ServiceResult<CompetitionInviteResource> inviteUser(NewUserStagedInviteResource stagedInvite) {
        return getByEmailAndCompetition(stagedInvite.getEmail(), stagedInvite.getCompetitionId()).handleSuccessOrFailure(
                failure -> getCompetition(stagedInvite.getCompetitionId())
                        .andOnSuccess(competition -> getInnovationArea(stagedInvite.getInnovationAreaId())
                                .andOnSuccess(innovationArea ->
                                        inviteUserToCompetition(
                                                stagedInvite.getName(),
                                                stagedInvite.getEmail(),
                                                competition,
                                                innovationArea
                                        )
                                )
                        )
                        .andOnSuccessReturn(competitionInviteMapper::mapToResource),
                success -> serviceFailure(Error.globalError(
                        "validation.competitionAssessmentInvite.create.email.exists",
                        singletonList(stagedInvite.getEmail())
                ))
        );
    }

    @Override
    public ServiceResult<Void> inviteNewUsers(List<NewUserStagedInviteResource> newUserStagedInvites, long competitionId) {
        return getCompetition(competitionId).andOnSuccessReturn(competition ->
                mapWithIndex(newUserStagedInvites, (index, invite) ->
                        getByEmailAndCompetitionWithValidation(invite.getEmail(), competitionId).handleSuccessOrFailure(
                                failure -> getInnovationArea(invite.getInnovationAreaId())
                                        .andOnSuccess(innovationArea ->
                                                inviteUserToCompetition(invite.getName(), invite.getEmail(), competition, innovationArea)
                                        )
                                        .andOnFailure(() -> serviceFailure(Error.fieldError(
                                                "invites[" + index + "].innovationArea",
                                                invite.getInnovationAreaId(),
                                                "validation.competitionAssessmentInvite.create.innovationArea.required"
                                                ))
                                        ),
                                success -> serviceFailure(Error.fieldError(
                                        "invites[" + index + "].email",
                                        invite.getEmail(),
                                        "validation.competitionAssessmentInvite.create.email.exists"
                                ))
                        )
                ))
                .andOnSuccess(list -> aggregate(list))
                .andOnSuccessReturnVoid();
    }

    private String getDetails(CompetitionParticipant participant) {
        String details = null;

        if (participant.getStatus() == REJECTED) {
            details = format("Invite declined as %s", lowerCase(participant.getRejectionReason().getReason()));
        } else if (participant.getStatus() == PENDING) {
            if (participant.getInvite().getSentOn() != null) {
                details = format("Invite sent: %s", participant.getInvite().getSentOn().format(detailsFormatter));
            }
        }

        return details;
    }

    private ServiceResult<InnovationArea> getInnovationArea(long innovationCategoryId) {
        return find(innovationAreaRepository.findOne(innovationCategoryId), notFoundError(Category.class, innovationCategoryId, INNOVATION_AREA));
    }

    private ServiceResult<AssessmentInvite> inviteUserToCompetition(String name, String email, Competition competition, InnovationArea innovationArea) {
        return serviceSuccess(
                assessmentInviteRepository.save(new AssessmentInvite(name, email, generateInviteHash(), competition, innovationArea))
        );
    }

    @Override
    public ServiceResult<CompetitionInviteResource> inviteUser(ExistingUserStagedInviteResource stagedInvite) {
        return getUserById(stagedInvite.getUserId())
                .andOnSuccess(user -> inviteUserToCompetition(user, stagedInvite.getCompetitionId()))
                .andOnSuccessReturn(competitionInviteMapper::mapToResource);
    }

    @Override
    public ServiceResult<Void> inviteUsers(List<ExistingUserStagedInviteResource> stagedInvites) {
        return serviceSuccess(mapWithIndex(stagedInvites, (i, invite) ->
                getUserById(invite.getUserId()).andOnSuccess(user ->
                        getByEmailAndCompetition(user.getEmail(), invite.getCompetitionId()).andOnFailure(() ->
                                inviteUserToCompetition(user, invite.getCompetitionId())
                        )))).andOnSuccessReturnVoid();
    }

    private ServiceResult<AssessmentInvite> inviteUserToCompetition(User user, long competitionId) {
        return getCompetition(competitionId)
                .andOnSuccessReturn(
                        competition -> assessmentInviteRepository.save(new AssessmentInvite(user, generateInviteHash(), competition))
                );
    }

    private ServiceResult<Competition> getCompetition(long competitionId) {
        return find(competitionRepository.findOne(competitionId), notFoundError(Competition.class, competitionId));
    }

    private ServiceResult<User> getUserByEmail(String email) {
        return find(userRepository.findByEmail(email), notFoundError(User.class, email));
    }

    private ServiceResult<User> getUserById(long id) {
        return find(userRepository.findOne(id), notFoundError(User.class, id));
    }

    @Override
    public ServiceResult<Void> sendAllInvites(long competitionId, AssessorInviteSendResource assessorInviteSendResource) {
        return getCompetition(competitionId).andOnSuccess(competition -> {

            String customTextPlain = stripHtml(assessorInviteSendResource.getContent());
            String customTextHtml = plainTextToHtml(customTextPlain);

            return ServiceResult.processAnyFailuresOrSucceed(simpleMap(
                    assessmentInviteRepository.getByCompetitionIdAndStatus(competition.getId(), CREATED),
                    invite -> {
                        competitionParticipantRepository.save(
                                new AssessmentParticipant(invite.send(loggedInUserSupplier.get(), ZonedDateTime.now()))
                        );

                        if (invite.isNewAssessorInvite()) {
                            userRepository.findByEmail(invite.getEmail()).ifPresent(this::addAssessorRoleToUser);
                        }

                        return sendInviteNotification(
                                assessorInviteSendResource.getSubject(),
                                inviteFormatter,
                                customTextPlain,
                                customTextHtml,
                                invite,
                                Notifications.INVITE_ASSESSOR_GROUP
                        );
                    }
            ));
        });
    }

    @Override
    public ServiceResult<Void> resendInvite(long inviteId, AssessorInviteSendResource assessorInviteSendResource) {
        return getParticipantByInviteId(inviteId)
                .andOnSuccess(participant ->
                        resendInviteNotification(participant.getInvite().sendOrResend(loggedInUserSupplier.get(), ZonedDateTime.now()), assessorInviteSendResource)
                )
                .andOnSuccessReturnVoid();
    }

    @Override
    public ServiceResult<Void> resendInvites(List<Long> inviteIds, AssessorInviteSendResource assessorInviteSendResource) {

        String customTextPlain = stripHtml(assessorInviteSendResource.getContent());
        String customTextHtml = plainTextToHtml(customTextPlain);

        return ServiceResult.processAnyFailuresOrSucceed(simpleMap(
                assessmentInviteRepository.getByIdIn(inviteIds),
                invite -> sendInviteNotification(
                        assessorInviteSendResource.getSubject(),
                        inviteFormatter,
                        customTextPlain,
                        customTextHtml,
                        invite.sendOrResend(loggedInUserSupplier.get(), ZonedDateTime.now()),
                        Notifications.INVITE_ASSESSOR_GROUP
                )
        ));
    }

    private ServiceResult<Notification> resendInviteNotification(AssessmentInvite invite, AssessorInviteSendResource assessorInviteSendResource) {
        // Strip any HTML that may have been added to the content by the user.
        String bodyPlain = stripHtml(assessorInviteSendResource.getContent());

        // HTML'ify the plain content to add line breaks.
        String bodyHtml = plainTextToHtml(bodyPlain);

        NotificationTarget recipient = new ExternalUserNotificationTarget(invite.getName(), invite.getEmail());
        Notification notification = new Notification(systemNotificationSource, singletonList(recipient),
                Notifications.INVITE_ASSESSOR, asMap(
                "subject", assessorInviteSendResource.getSubject(),
                "bodyPlain", bodyPlain,
                "bodyHtml", bodyHtml
        ));

        return notificationSender.sendNotification(notification);
    }

    private ServiceResult<Void> sendInviteNotification(String subject,
                                                       DateTimeFormatter formatter,
                                                       String customTextPlain,
                                                       String customTextHtml,
                                                       AssessmentInvite invite,
                                                       Notifications notificationType) {
        NotificationTarget recipient = new ExternalUserNotificationTarget(invite.getName(), invite.getEmail());
        Notification notification = new Notification(
                systemNotificationSource,
                recipient,
                notificationType,
                asMap(
                        "subject", subject,
                        "name", invite.getName(),
                        "competitionName", invite.getTarget().getName(),
                        "acceptsDate", invite.getTarget().getAssessorAcceptsDate().format(formatter),
                        "deadlineDate", invite.getTarget().getAssessorDeadlineDate().format(formatter),
                        "inviteUrl", format("%s/invite/competition/%s", webBaseUrl + WEB_CONTEXT, invite.getHash()),
                        "customTextPlain", customTextPlain,
                        "customTextHtml", customTextHtml
                ));

        return notificationSender.sendNotification(notification).andOnSuccessReturnVoid();
    }

    private void addAssessorRoleToUser(User user) {
        user.addRole(Role.ASSESSOR);
    }

    @Override
    public ServiceResult<Void> deleteInvite(String email, long competitionId) {
        return getByEmailAndCompetition(email, competitionId).andOnSuccess(this::deleteInvite);
    }

    @Override
    public ServiceResult<Void> deleteAllInvites(long competitionId) {
        return find(competitionRepository.findOne(competitionId), notFoundError(Competition.class, competitionId))
                .andOnSuccessReturnVoid(competition ->
                        assessmentInviteRepository.deleteByCompetitionIdAndStatus(competition.getId(), CREATED));
    }

    private ServiceResult<AssessmentInvite> getByHash(String inviteHash) {
        return find(assessmentInviteRepository.getByHash(inviteHash), notFoundError(AssessmentInvite.class, inviteHash));
    }

    private ServiceResult<AssessmentInvite> getById(long id) {
        return find(assessmentInviteRepository.findOne(id), notFoundError(AssessmentInvite.class, id));
    }

    private ServiceResult<AssessmentParticipant> getParticipantByInviteId(long inviteId) {
        return find(competitionParticipantRepository.getByInviteId(inviteId), notFoundError(CompetitionParticipant.class, inviteId));
    }

    private String getInviteContent(NotificationTarget notificationTarget, Map<String, Object> arguments) {
        return renderer.renderTemplate(systemNotificationSource, notificationTarget, "invite_assessor_editable_text.txt",
                arguments).getSuccess();
    }

    private String getInvitePreviewContent(NotificationTarget notificationTarget, Map<String, Object> arguments) {
        return renderer.renderTemplate(systemNotificationSource, notificationTarget, "invite_assessor_preview_text.txt",
                arguments).getSuccess();
    }

    private ServiceResult<AssessmentInvite> getByEmailAndCompetition(String email, long competitionId) {
        return find(assessmentInviteRepository.getByEmailAndCompetitionId(email, competitionId), notFoundError(AssessmentInvite.class, email, competitionId));
    }

    private ServiceResult<Void> getByEmailAndCompetitionWithValidation(String email, long competitionId) {

        Pageable pageable = new PageRequest(0, 20, new Sort(ASC, "name"));

        ServiceResult<AssessorCreatedInvitePageResource> resource = getInvitePageResource(competitionId, pageable);

        List<String> existingEmails = resource.getSuccess().getContent().stream()
                .map(AssessorCreatedInviteResource::getEmail)
                .collect(Collectors.toList());

        List<String> userIsAlreadyInvitedList = existingEmails.stream()
                    .filter(e -> e.equals(email))
                    .collect(Collectors.toList());

        Optional<User> userExists = userRepository.findByEmail(email);

        boolean userIsAlreadyInvited = userIsAlreadyInvitedList.isEmpty();

        if (assessmentInviteRepository.getByEmailAndCompetitionId(email, competitionId) != null || !userIsAlreadyInvited || userExists.isPresent()) {
            return ServiceResult.serviceSuccess();
        } else {
            return ServiceResult.serviceFailure(new Error(USERS_DUPLICATE_EMAIL_ADDRESS, email));
        }
}

    private ServiceResult<Void> deleteInvite(AssessmentInvite invite) {
        if (invite.getStatus() != CREATED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_INVITE_CANNOT_DELETE_ONCE_SENT, invite.getEmail()));
        }

        assessmentInviteRepository.delete(invite);
        return serviceSuccess();
    }

    private ServiceResult<AssessmentInvite> getByHashIfOpen(String inviteHash) {
        return getByHash(inviteHash).andOnSuccess(invite -> {

            if (!EnumSet.of(READY_TO_OPEN, IN_ASSESSMENT, CLOSED, OPEN).contains(invite.getTarget().getCompetitionStatus())) {
                return ServiceResult.serviceFailure(new Error(COMPETITION_INVITE_EXPIRED, invite.getTarget().getName()));
            }

            CompetitionParticipant participant = competitionParticipantRepository.getByInviteHash(inviteHash);

            if (participant == null) {
                return serviceSuccess(invite);
            }

            if (participant.getStatus() == ACCEPTED || participant.getStatus() == REJECTED) {
                return ServiceResult.serviceFailure(new Error(COMPETITION_INVITE_CLOSED, invite.getTarget().getName()));
            }
            return serviceSuccess(invite);
        });
    }

    private AssessmentInvite openInvite(AssessmentInvite invite) {
        return assessmentInviteRepository.save(invite.open());
    }

    private ServiceResult<AssessmentParticipant> getParticipantByInviteHash(String inviteHash) {
        return find(competitionParticipantRepository.getByInviteHash(inviteHash), notFoundError(CompetitionParticipant.class, inviteHash));
    }

    private ServiceResult<AssessmentParticipant> accept(AssessmentParticipant participant, User user) {
        if (participant.getInvite().getStatus() != OPENED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_UNOPENED_INVITE, getInviteCompetitionName(participant)));
        } else if (participant.getStatus() == ACCEPTED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_ALREADY_ACCEPTED_INVITE, getInviteCompetitionName(participant)));
        } else if (participant.getStatus() == REJECTED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_ALREADY_REJECTED_INVITE, getInviteCompetitionName(participant)));
        } else {
            return
                    applyInnovationAreaToUserProfile(participant, user)
                            .andOnSuccessReturn(() -> participant.acceptAndAssignUser(user));
        }
    }

    private ServiceResult<Participant> applyInnovationAreaToUserProfile(AssessmentParticipant participant, User user) {
        if (participant.getInvite().isNewAssessorInvite()) {
            return getProfileForUser(user).andOnSuccessReturn(
                    profile -> {
                        profile.addInnovationArea(participant.getInvite().getInnovationArea());
                        return participant;
                    }
            );
        } else {
            return serviceSuccess(participant);
        }
    }

    private ServiceResult<Profile> getProfileForUser(User user) {
        return find(profileRepository.findOne(user.getProfileId()), notFoundError(Profile.class, user.getProfileId()));
    }

    private ServiceResult<CompetitionParticipant> reject(AssessmentParticipant participant, RejectionReason rejectionReason, Optional<String> rejectionComment) {
        if (participant.getInvite().getStatus() != OPENED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_REJECT_UNOPENED_INVITE, getInviteCompetitionName(participant)));
        } else if (participant.getStatus() == ACCEPTED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_REJECT_ALREADY_ACCEPTED_INVITE, getInviteCompetitionName(participant)));
        } else if (participant.getStatus() == REJECTED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_REJECT_ALREADY_REJECTED_INVITE, getInviteCompetitionName(participant)));
        } else {
            return serviceSuccess(participant.reject(rejectionReason, rejectionComment));
        }
    }

    private ServiceResult<RejectionReason> getRejectionReason(final RejectionReasonResource rejectionReason) {
        return find(rejectionReasonRepository.findOne(rejectionReason.getId()), notFoundError(RejectionReason.class, rejectionReason.getId()));
    }

    private String getInviteCompetitionName(AssessmentParticipant participant) {
        return participant.getInvite().getTarget().getName();
    }

    private boolean isUserCompliant(AssessmentInvite competitionAssessmentInvite) {
        if (competitionAssessmentInvite == null || competitionAssessmentInvite.getUser() == null) {
            return false;
        }
        Profile profile = profileRepository.findOne(competitionAssessmentInvite.getUser().getProfileId());
        return profile.isCompliant(competitionAssessmentInvite.getUser());
    }

    private List<InnovationAreaResource> getInnovationAreasForInvite(AssessmentInvite assessmentInvite) {
        if (assessmentInvite.isNewAssessorInvite()) {
            return singletonList(innovationAreaMapper.mapToResource(assessmentInvite.getInnovationArea()));
        } else {
            return profileRepository.findOne(assessmentInvite.getUser().getProfileId()).getInnovationAreas().stream()
                    .map(innovationAreaMapper::mapToResource)
                    .collect(toList());
        }
    }

    private ServiceResult<AssessorCreatedInvitePageResource> getInvitePageResource(long competitionId, Pageable pageable) {
        Page<AssessmentInvite> pagedResult = assessmentInviteRepository.getByCompetitionIdAndStatus(competitionId, CREATED, pageable);

        List<AssessorCreatedInviteResource> createdInvites = simpleMap(
                pagedResult.getContent(),
                competitionInvite -> {
                    AssessorCreatedInviteResource assessorCreatedInvite = new AssessorCreatedInviteResource();
                    assessorCreatedInvite.setName(competitionInvite.getName());
                    assessorCreatedInvite.setInnovationAreas(getInnovationAreasForInvite(competitionInvite));
                    assessorCreatedInvite.setCompliant(isUserCompliant(competitionInvite));
                    assessorCreatedInvite.setEmail(competitionInvite.getEmail());
                    assessorCreatedInvite.setInviteId(competitionInvite.getId());

                    if (competitionInvite.getUser() != null) {
                        assessorCreatedInvite.setId(competitionInvite.getUser().getId());
                    }

                    return assessorCreatedInvite;
                }
        );

        return serviceSuccess(new AssessorCreatedInvitePageResource(
                pagedResult.getTotalElements(),
                pagedResult.getTotalPages(),
                createdInvites,
                pagedResult.getNumber(),
                pagedResult.getSize()
        ));
    }
}
