package com.HuyHoang.repository;

import com.HuyHoang.Entity.ListeningHistory;
import com.HuyHoang.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, String> {
    List<ListeningHistory> findByUserId(String userId);
    List<ListeningHistory> findByUser(User user);

    Page<ListeningHistory> findByUser(User user, Pageable pageable);
}
