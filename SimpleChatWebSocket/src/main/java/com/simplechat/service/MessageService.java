package com.simplechat.service;

import com.simplechat.entity.Message;
import com.simplechat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    // Tao tin nhan
    public Message createMessage(Message message) {
        message.setCreatedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }
    
    // Lay tin nhan theo ID
    public Optional<Message> getMessageById(Integer messageId) {
        return messageRepository.findById(messageId);
    }
    
    // Lay tat ca tin nhan cua kenh
    public List<Message> getMessagesByChannel(Integer channelId) {
        return messageRepository.findByChannel_ChannelId(channelId);
    }
    
    // Lay tin nhan paging co phan trang
    public Page<Message> getActiveMessagesByChannel(Integer channelId, Pageable pageable) {
        return messageRepository.findActiveMessagesByChannel(channelId, pageable);
    }
    
    // Lay tin nhan theo user
    public List<Message> getMessagesByUser(Integer userId) {
        return messageRepository.findByUser_UserId(userId);
    }
    
    // Dem tin nhan hoat dong cua kenh
    public Integer countActiveMessages(Integer channelId) {
        return messageRepository.countActiveMessagesByChannel(channelId);
    }
    
    // Cap nhat tin nhan
    public Message updateMessage(Message message) {
        message.setEditedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }
    
    // Xoa lan (soft delete) tin nhan
    public Message softDeleteMessage(Integer messageId, Integer deletedBy) {
        Optional<Message> message = messageRepository.findById(messageId);
        if (message.isPresent()) {
            Message m = message.get();
            m.setIsDeleted(true);
            m.setDeletedAt(LocalDateTime.now());
            m.setDeletedBy(m.getDeletedBy());
            return messageRepository.save(m);
        }
        return null;
    }
    
    // Khoi phuc tin nhan
    public Message restoreMessage(Integer messageId) {
        Optional<Message> message = messageRepository.findById(messageId);
        if (message.isPresent()) {
            Message m = message.get();
            m.setIsDeleted(false);
            m.setDeletedAt(null);
            m.setDeletedBy(null);
            return messageRepository.save(m);
        }
        return null;
    }
    
    // Xoa vinh bien tin nhan
    public void permanentlyDeleteMessage(Integer messageId) {
        messageRepository.deleteById(messageId);
    }
}
