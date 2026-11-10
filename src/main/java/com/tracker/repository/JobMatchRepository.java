package com.tracker.repository;

import com.tracker.model.JobMatch;
import com.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, Long> {
    List<JobMatch> findAllByOrderByMatchedAtDesc();
    List<JobMatch> findAllByUserOrderByMatchedAtDesc(User user);

    List<JobMatch> findTop5ByOrderByMatchedAtDesc();
    List<JobMatch> findTop5ByUserOrderByMatchedAtDesc(User user);

    List<JobMatch> findByUserIsNull();
}
