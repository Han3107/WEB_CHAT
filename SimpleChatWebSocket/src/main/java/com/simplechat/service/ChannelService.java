package com.simplechat.service;

import com.simplechat.entity.Channel;
import com.simplechat.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChannelService {
    
    @Autowired
    private ChannelRepository channelRepository;
    
    // Tao kenh
    public Channel createChannel(Channel channel) {
        return channelRepository.save(channel);
    }
    
    // Lay kenh theo ID
    public Optional<Channel> getChannelById(Integer channelId) {
        return channelRepository.findById(channelId);
    }
    
    // Lay tat ca kenh theo loai
    public List<Channel> getChannelsByType(String channelType) {
        return channelRepository.findByChannelType(channelType);
    }
    
    // Lay tat ca kenh dang hoat dong
    public List<Channel> getAllActiveChannels() {
        return channelRepository.findAllActive();
    }
    
    // Lay kenh theo user tao
    public List<Channel> getChannelsByCreator(Integer userId) {
        return channelRepository.findByCreatedBy_UserId(userId);
    }
    
    // Lay tat ca kenh
    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }
    
    // Cap nhat kenh
    public Channel updateChannel(Channel channel) {
        return channelRepository.save(channel);
    }
    
    // Xoa kenh
    public void deleteChannel(Integer channelId) {
        channelRepository.deleteById(channelId);
    }
    
    // Dung/tung kenh
    public Channel toggleChannelStatus(Integer channelId) {
        Optional<Channel> channel = channelRepository.findById(channelId);
        if (channel.isPresent()) {
            Channel c = channel.get();
            c.setIsActive(!c.getIsActive());
            return channelRepository.save(c);
        }
        return null;
    }
}
