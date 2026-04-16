package com.simplechat.repository;

import com.simplechat.entity.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Integer> {
    List<ChannelMember> findByChannel_ChannelId(Integer channelId);
    List<ChannelMember> findByUser_UserId(Integer userId);
    Optional<ChannelMember> findByChannel_ChannelIdAndUser_UserId(Integer channelId, Integer userId);
    
    @Query("SELECT COUNT(cm) FROM ChannelMember cm WHERE cm.channel.channelId = :channelId")
    Integer countMembersByChannel(@Param("channelId") Integer channelId);
}
