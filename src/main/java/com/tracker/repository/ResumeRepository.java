package com.tracker.repository;

import com.tracker.model.Resume;
import com.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findTopByOrderByUploadDateDesc();
    Optional<Resume> findTopByUserOrderByUploadDateDesc(User user);

    List<Resume> findAllByOrderByUploadDateDesc();
    List<Resume> findAllByUserOrderByUploadDateDesc(User user);

    Optional<Resume> findByIdAndUser(Long id, User user);

    boolean existsByOriginalFileName(String originalFileName);
    boolean existsByOriginalFileNameAndUser(String originalFileName, User user);

    List<Resume> findByUserIsNull();
}
