package com.tracker.repository;

import com.tracker.model.Reminder;
import com.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findAllByOrderByReminderDateAscReminderTimeAsc();
    List<Reminder> findAllByUserOrderByReminderDateAscReminderTimeAsc(User user);

    List<Reminder> findByCompletedFalseOrderByReminderDateAscReminderTimeAsc();
    List<Reminder> findByUserAndCompletedFalseOrderByReminderDateAscReminderTimeAsc(User user);

    Optional<Reminder> findByIdAndUser(Long id, User user);

    @Query("SELECT r FROM Reminder r WHERE r.completed = false AND r.reminderDate >= :today ORDER BY r.reminderDate ASC, r.reminderTime ASC")
    List<Reminder> findUpcomingReminders(LocalDate today);

    @Query("SELECT r FROM Reminder r WHERE r.user = :user AND r.completed = false AND r.reminderDate >= :today ORDER BY r.reminderDate ASC, r.reminderTime ASC")
    List<Reminder> findUpcomingRemindersByUser(@Param("user") User user, @Param("today") LocalDate today);

    long countByCompletedFalse();
    long countByUserAndCompletedFalse(User user);

    boolean existsByTitle(String title);
    boolean existsByTitleAndUser(String title, User user);

    List<Reminder> findByUserIsNull();
}
