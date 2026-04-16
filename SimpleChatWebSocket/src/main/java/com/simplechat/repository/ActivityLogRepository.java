package com.simplechat.repository;

import com.simplechat.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {
    List<ActivityLog> findByUser_UserId(Integer userId);
    List<ActivityLog> findByAction(String action);
    
    @Query("SELECT a FROM ActivityLog a WHERE a.createdAt >= :startDate ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentActivities(@Param("startDate") LocalDateTime startDate);
}
