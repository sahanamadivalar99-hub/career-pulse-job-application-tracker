package com.tracker.service;

import com.tracker.model.Reminder;
import com.tracker.model.User;
import com.tracker.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private UserService userService;

    public List<Reminder> getAllReminders() {
        return getAllReminders(userService.getCurrentUser());
    }

    public List<Reminder> getAllReminders(User user) {
        return reminderRepository.findAllByUserOrderByReminderDateAscReminderTimeAsc(user);
    }

    public List<Reminder> getUpcomingReminders() {
        return getUpcomingReminders(userService.getCurrentUser());
    }

    public List<Reminder> getUpcomingReminders(User user) {
        return reminderRepository.findUpcomingRemindersByUser(user, LocalDate.now());
    }

    public List<Reminder> getPendingReminders() {
        return getPendingReminders(userService.getCurrentUser());
    }

    public List<Reminder> getPendingReminders(User user) {
        return reminderRepository.findByUserAndCompletedFalseOrderByReminderDateAscReminderTimeAsc(user);
    }

    public Optional<Reminder> getReminderById(Long id) {
        return getReminderById(id, userService.getCurrentUser());
    }

    public Optional<Reminder> getReminderById(Long id, User user) {
        return reminderRepository.findByIdAndUser(id, user);
    }

    @Transactional
    public Reminder saveReminder(Reminder reminder) {
        return saveReminder(reminder, userService.getCurrentUser());
    }

    @Transactional
    public Reminder saveReminder(Reminder reminder, User user) {
        if (reminder.getUser() == null) {
            reminder.setUser(user);
        }
        if (reminder.getReminderDate() == null) {
            reminder.setReminderDate(LocalDate.now().plusDays(1));
        }
        return reminderRepository.save(reminder);
    }

    @Transactional
    public Reminder toggleCompletion(Long id) {
        return toggleCompletion(id, userService.getCurrentUser());
    }

    @Transactional
    public Reminder toggleCompletion(Long id, User user) {
        Reminder reminder = reminderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found or unauthorized for id: " + id));
        reminder.setCompleted(!reminder.isCompleted());
        return reminderRepository.save(reminder);
    }

    @Transactional
    public void deleteReminder(Long id) {
        deleteReminder(id, userService.getCurrentUser());
    }

    @Transactional
    public void deleteReminder(Long id, User user) {
        Reminder reminder = reminderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found or unauthorized for id: " + id));
        reminderRepository.delete(reminder);
    }
}
