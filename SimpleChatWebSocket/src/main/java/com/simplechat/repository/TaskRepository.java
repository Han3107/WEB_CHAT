package com.simplechat.repository;

import com.simplechat.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByChannel_ChannelId(Integer channelId);
    List<Task> findByTaskType(String taskType);
    List<Task> findByStatus(String status);
    
    @Query("SELECT t FROM Task t WHERE t.channel.channelId = :channelId AND t.status = 'active' ORDER BY t.createdAt DESC")
    List<Task> findActiveTasksByChannel(@Param("channelId") Integer channelId);
}
