package com.simplechat.repository;

import com.simplechat.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Integer> {
    List<Channel> findByChannelType(String channelType);
    List<Channel> findByIsActive(Boolean isActive);
    List<Channel> findByCreatedBy_UserId(Integer userId);
    Optional<Channel> findByInviteCode(String inviteCode);

    @Query("SELECT c FROM Channel c WHERE c.isActive = true ORDER BY c.createdAt DESC")
    List<Channel> findAllActive();
}
