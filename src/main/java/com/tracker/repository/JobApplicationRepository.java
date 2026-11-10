package com.tracker.repository;

import com.tracker.model.ApplicationStatus;
import com.tracker.model.JobApplication;
import com.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    long countByStatus(ApplicationStatus status);
    long countByUser(User user);
    long countByUserAndStatus(User user, ApplicationStatus status);

    List<JobApplication> findAllByOrderByApplicationDateDesc();
    List<JobApplication> findAllByUserOrderByApplicationDateDesc(User user);
    List<JobApplication> findAllByUser(User user);

    List<JobApplication> findTop5ByOrderByCreatedAtDesc();
    List<JobApplication> findTop5ByUserOrderByCreatedAtDesc(User user);

    List<JobApplication> findByStatusOrderByApplicationDateDesc(ApplicationStatus status);
    List<JobApplication> findByUserAndStatusOrderByApplicationDateDesc(User user, ApplicationStatus status);

    Optional<JobApplication> findByIdAndUser(Long id, User user);

    @Query("SELECT a FROM JobApplication a WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(a.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.jobRole) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR a.status = :status) " +
           "ORDER BY a.applicationDate DESC")
    List<JobApplication> searchApplications(@Param("keyword") String keyword, @Param("status") ApplicationStatus status);

    @Query("SELECT a FROM JobApplication a WHERE a.user = :user AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(a.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.jobRole) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR a.status = :status) " +
           "ORDER BY a.applicationDate DESC")
    List<JobApplication> searchApplicationsByUser(@Param("user") User user, @Param("keyword") String keyword, @Param("status") ApplicationStatus status);

    @Query("SELECT a FROM JobApplication a WHERE a.status = 'INTERVIEW' OR a.interviewDate IS NOT NULL ORDER BY a.interviewDate ASC")
    List<JobApplication> findUpcomingInterviews();

    @Query("SELECT a FROM JobApplication a WHERE a.user = :user AND (a.status = 'INTERVIEW' OR a.interviewDate IS NOT NULL) ORDER BY a.interviewDate ASC")
    List<JobApplication> findUpcomingInterviewsByUser(@Param("user") User user);

    boolean existsByCompanyNameAndJobRole(String companyName, String jobRole);
    boolean existsByCompanyNameAndJobRoleAndUser(String companyName, String jobRole, User user);

    List<JobApplication> findByUserIsNull();

    @Query("SELECT a.companyName, COUNT(a) FROM JobApplication a GROUP BY a.companyName ORDER BY COUNT(a) DESC")
    List<Object[]> getTopCompanies();

    @Query("SELECT FUNCTION('MONTH', a.applicationDate) as monthNum, COUNT(a) as total " +
           "FROM JobApplication a " +
           "WHERE a.applicationDate >= :startDate " +
           "GROUP BY FUNCTION('MONTH', a.applicationDate) " +
           "ORDER BY monthNum ASC")
    List<Object[]> getMonthlyApplicationCounts(@Param("startDate") LocalDate startDate);
}
