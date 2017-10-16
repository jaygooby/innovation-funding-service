package org.innovateuk.ifs.competition.repository;

import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.resource.CompetitionOpenQueryResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface CompetitionRepository extends PagingAndSortingRepository<Competition, Long> {

    public static final String LIVE_QUERY = "SELECT c FROM Competition c WHERE CURRENT_TIMESTAMP >= " +
            "(SELECT m.date FROM Milestone m WHERE m.type = 'OPEN_DATE' AND m.competition.id = c.id) AND " +
            "NOT EXISTS (SELECT m.date FROM Milestone m WHERE m.type = 'FEEDBACK_RELEASED' AND m.competition.id = c.id) AND " +
            "c.setupComplete = TRUE AND c.template = FALSE AND c.nonIfs = FALSE";

    public static final String LIVE_COUNT_QUERY = "SELECT COUNT(c) FROM Competition c WHERE CURRENT_TIMESTAMP >= " +
            "(SELECT m.date FROM Milestone m WHERE m.type = 'OPEN_DATE' AND m.competition.id = c.id) AND " +
            "NOT EXISTS (SELECT m.date FROM Milestone m WHERE m.type = 'FEEDBACK_RELEASED' AND m.competition.id = c.id) AND " +
            "c.setupComplete = TRUE AND c.template = FALSE AND c.nonIfs = FALSE";

    // Assume competition cannot be in project setup until at least one application is funded and informed
    public static final String PROJECT_SETUP_QUERY = "SELECT c FROM Competition c WHERE ( " +
            "EXISTS (SELECT a.manageFundingEmailDate  FROM Application a WHERE a.competition.id = c.id AND a.fundingDecision = 'FUNDED' AND a.manageFundingEmailDate IS NOT NULL) " +
            ") AND c.setupComplete = TRUE AND c.template = FALSE AND c.nonIfs = FALSE";

    public static final String PROJECT_SETUP_COUNT_QUERY = "SELECT COUNT(c) FROM Competition c WHERE ( " +
            "EXISTS (SELECT a.manageFundingEmailDate  FROM Application a WHERE a.competition.id = c.id AND a.fundingDecision = 'FUNDED' AND a.manageFundingEmailDate IS NOT NULL) " +
            ") AND c.setupComplete = TRUE AND c.template = FALSE AND c.nonIfs = FALSE";

    public static final String UPCOMING_QUERY = "SELECT c FROM Competition c WHERE (CURRENT_TIMESTAMP <= " +
            "(SELECT m.date FROM Milestone m WHERE m.type = 'OPEN_DATE' AND m.competition.id = c.id) AND c.setupComplete = TRUE) OR " +
            "c.setupComplete = FALSE AND c.template = FALSE AND c.nonIfs = FALSE";

    public static final String UPCOMING_COUNT_QUERY = "SELECT count(c) FROM Competition c WHERE (CURRENT_TIMESTAMP <= " +
            "(SELECT m.date FROM Milestone m WHERE m.type = 'OPEN_DATE' AND m.competition.id = c.id) AND c.setupComplete = TRUE) OR " +
            "c.setupComplete = FALSE AND c.template = FALSE AND c.nonIfs = FALSE";

    public static final String SEARCH_QUERY = "SELECT c FROM Competition c LEFT JOIN c.milestones m LEFT JOIN c.competitionType ct " +
            "WHERE (m.type = 'OPEN_DATE' OR m.type IS NULL) AND (c.name LIKE :searchQuery OR ct.name LIKE :searchQuery) AND c.template = FALSE AND c.nonIfs = FALSE " +
            "ORDER BY m.date";

    /* Innovation leads should not access competitions in states: In preparation, Ready to open, Project setup */
    public static final String SEARCH_QUERY_LEAD_TECHNOLOGIST = "SELECT c FROM Competition c LEFT JOIN c.milestones m LEFT JOIN c.competitionType ct LEFT JOIN c.leadTechnologist u " +
            "WHERE (m.type = 'OPEN_DATE' AND m.date < NOW()) AND (c.name LIKE :searchQuery OR ct.name LIKE :searchQuery) AND c.template = FALSE AND c.nonIfs = FALSE " +
            "AND (c.setupComplete IS NOT NULL AND c.setupComplete != FALSE) " +
            "AND NOT EXISTS (SELECT m.id FROM Milestone m WHERE m.competition.id = c.id AND m.type='FEEDBACK_RELEASED') " +
            "AND u.id = :leadTechnologistUserId " +
            "ORDER BY m.date";

    /* Support users should not be able to access competitions in states: In preparation, Project setup */
    public static final String SEARCH_QUERY_SUPPORT_USER = "SELECT c FROM Competition c LEFT JOIN c.milestones m LEFT JOIN c.competitionType ct " +
            "WHERE (m.type = 'OPEN_DATE' OR m.type IS NULL) AND (c.name LIKE :searchQuery OR ct.name LIKE :searchQuery) AND c.template = FALSE AND c.nonIfs = FALSE " +
            "AND (c.setupComplete IS NOT NULL AND c.setupComplete != FALSE) " +
            "AND NOT EXISTS (SELECT m.id FROM Milestone m WHERE m.competition.id = c.id AND m.type='FEEDBACK_RELEASED') " +
            "ORDER BY m.date";

    public static final String NON_IFS_QUERY = "SELECT c FROM Competition c WHERE nonIfs = TRUE";

    public static final String NON_IFS_COUNT_QUERY = "SELECT count(c) FROM Competition c WHERE nonIfs = TRUE";

    public static final String FEEDBACK_RELEASED_QUERY = "SELECT c FROM Competition c WHERE " +
            "EXISTS (SELECT m.date FROM Milestone m WHERE m.type = 'FEEDBACK_RELEASED' and m.competition.id = c.id) AND " +
            "c.setupComplete = TRUE AND c.template = FALSE AND c.nonIfs = FALSE";

    public static final String FEEDBACK_RELEASED_COUNT_QUERY = "SELECT COUNT(c) FROM Competition c WHERE " +
            "CURRENT_TIMESTAMP >= (SELECT m.date FROM Milestone m WHERE m.type = 'FEEDBACK_RELEASED' and m.competition.id = c.id) AND " +
            "c.setupComplete = TRUE AND c.template = FALSE AND c.nonIfs = FALSE";

    public static final String COUNT_OPEN_QUERIES = "SELECT COUNT(DISTINCT t.classPk) " +
            "FROM Post post " +
            "JOIN Thread t ON t.id = post.thread.id " +
            "JOIN post.author u " +
            "JOIN u.roles roles " +
            "INNER JOIN ProjectFinance pf ON pf.id = t.classPk " +
            "INNER JOIN Project pr ON pr.id = pf.project.id " +
            "INNER JOIN Application a ON a.id = pr.application.id " +
            "WHERE t.className='org.innovateuk.ifs.finance.domain.ProjectFinance' " +
            "    AND TYPE(t) = Query " +
            "    AND roles.name != 'project_finance' " +
            "    AND a.competition.id = :competitionId " +
            "    AND (post.thread.id, post.createdOn) IN ( " +
            "        SELECT p.thread.id, MAX(p.createdOn) " +
            "        FROM Post p " +
            "        WHERE p.thread.id = t.id " +
            "        GROUP BY p.thread.id)";

    public static final String GET_OPEN_QUERIES = "SELECT NEW org.innovateuk.ifs.competition.resource.CompetitionOpenQueryResource(pr.application.id, o.id, o.name, pr.id, pr.name) " +
            "FROM Post post " +
            "JOIN Thread t ON t.id = post.thread.id " +
            "JOIN post.author u " +
            "JOIN u.roles roles " +
            "INNER JOIN ProjectFinance pf ON pf.id = t.classPk " +
            "INNER JOIN Project pr ON pr.id = pf.project.id " +
            "INNER JOIN Application a ON a.id = pr.application.id " +
            "INNER JOIN Organisation o ON o.id = pf.organisation.id " +
            "WHERE t.className='org.innovateuk.ifs.finance.domain.ProjectFinance' " +
            "    AND TYPE(t) = Query " +
            "    AND roles.name != 'project_finance' " +
            "    AND a.competition.id = :competitionId " +
            "    AND (post.thread.id, post.createdOn) IN ( " +
            "        SELECT p.thread.id, MAX(p.createdOn) " +
            "        FROM Post p " +
            "        WHERE p.thread.id = t.id " +
            "        GROUP BY p.thread.id) " +
            "GROUP BY pr.application.id, o.id, o.name, pr.id, pr.name " +
            "ORDER BY pr.application.id, o.name";

    @Query(LIVE_QUERY)
    List<Competition> findLive();

    @Query(LIVE_COUNT_QUERY)
    Long countLive();

    @Query(PROJECT_SETUP_QUERY)
    List<Competition> findProjectSetup();

    @Query(PROJECT_SETUP_COUNT_QUERY)
    Long countProjectSetup();

    @Query(UPCOMING_QUERY)
    List<Competition> findUpcoming();

    @Query(UPCOMING_COUNT_QUERY)
    Long countUpcoming();

    @Query(NON_IFS_QUERY)
    List<Competition> findNonIfs();

    @Query(NON_IFS_COUNT_QUERY)
    Long countNonIfs();

    @Query(SEARCH_QUERY)
    Page<Competition> search(@Param("searchQuery") String searchQuery, Pageable pageable);

    @Query(SEARCH_QUERY_LEAD_TECHNOLOGIST)
    Page<Competition> searchForLeadTechnologist(@Param("searchQuery") String searchQuery, @Param("leadTechnologistUserId") Long leadTechnologistUserId, Pageable pageable);

    @Query(SEARCH_QUERY_SUPPORT_USER)
    Page<Competition> searchForSupportUser(@Param("searchQuery") String searchQuery, Pageable pageable);

    List<Competition> findByName(String name);

    Competition findById(Long id);

    List<Competition> findByIdIsIn(List<Long> ids);

    @Override
    List<Competition> findAll();

    List<Competition> findByCodeLike(String code);

    Competition findByTemplateForTypeId(Long id);

    List<Competition> findByInnovationSectorCategoryId(Long id);

    @Query(FEEDBACK_RELEASED_QUERY)
    List<Competition> findFeedbackReleased();

    @Query(FEEDBACK_RELEASED_COUNT_QUERY)
    Long countFeedbackReleased();

    @Query(COUNT_OPEN_QUERIES)
    Long countOpenQueries(@Param("competitionId") Long competitionId);

    @Query(GET_OPEN_QUERIES)
    List<CompetitionOpenQueryResource> getOpenQueryByCompetition(@Param("competitionId") long competitionId);

}
