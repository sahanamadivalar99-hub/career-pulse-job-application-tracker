package com.tracker.repository;

import com.tracker.model.ChatMessage;
import com.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByOrderByCreatedAtAsc();
    List<ChatMessage> findAllByUserOrderByCreatedAtAsc(User user);
    void deleteAll();
    void deleteAllByUser(User user);
    List<ChatMessage> findByUserIsNull();
}
