package com.simplechat.repository;

import com.simplechat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByChannel_ChannelId(Integer channelId);
    List<Message> findByUser_UserId(Integer userId);
    
    @Query("SELECT m FROM Message m WHERE m.channel.channelId = :channelId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findActiveMessagesByChannel(@Param("channelId") Integer channelId, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.channel.channelId = :channelId AND m.isDeleted = false")
    Integer countActiveMessagesByChannel(@Param("channelId") Integer channelId);
}
