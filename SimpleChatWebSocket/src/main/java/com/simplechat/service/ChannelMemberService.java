package com.simplechat.service;

import com.simplechat.entity.ChannelMember;
import com.simplechat.repository.ChannelMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChannelMemberService {
    
    @Autowired
    private ChannelMemberRepository channelMemberRepository;
    
    // Them thanh vien vao kenh
    public ChannelMember addMemberToChannel(ChannelMember channelMember) {
        return channelMemberRepository.save(channelMember);
    }
    
    // Lay thanh vien theo ID
    public Optional<ChannelMember> getMemberById(Integer memberId) {
        return channelMemberRepository.findById(memberId);
    }
    
    // Lay tat ca thanh vien cua kenh
    public List<ChannelMember> getMembersByChannel(Integer channelId) {
        return channelMemberRepository.findByChannel_ChannelId(channelId);
    }
    
    // Lay tat ca kenh cua user
    public List<ChannelMember> getChannelsByUser(Integer userId) {
        return channelMemberRepository.findByUser_UserId(userId);
    }
    
    // Kiem tra user co la thanh vien cua kenh
    public boolean isMemberOfChannel(Integer channelId, Integer userId) {
        return channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(channelId, userId).isPresent();
    }
    
    // Dem so thanh vien cua kenh
    public Integer countMembersInChannel(Integer channelId) {
        return channelMemberRepository.countMembersByChannel(channelId);
    }
    
    // Cap nhat role cua thanh vien
    public ChannelMember updateMemberRole(Integer channelId, Integer userId, String newRole) {
        Optional<ChannelMember> member = channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(channelId, userId);
        if (member.isPresent()) {
            ChannelMember cm = member.get();
            cm.setRole(newRole);
            return channelMemberRepository.save(cm);
        }
        return null;
    }
    
    // Xoa thanh vien khoi kenh
    public void removeMemberFromChannel(Integer channelId, Integer userId) {
        Optional<ChannelMember> member = channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(channelId, userId);
        if (member.isPresent()) {
            channelMemberRepository.delete(member.get());
        }
    }
    
    // Cap nhat last read (doc tin nhan den dau)
    public ChannelMember updateLastRead(Integer channelId, Integer userId) {
        Optional<ChannelMember> member = channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(channelId, userId);
        if (member.isPresent()) {
            ChannelMember cm = member.get();
            cm.setLastReadAt(java.time.LocalDateTime.now());
            return channelMemberRepository.save(cm);
        }
        return null;
    }
}
