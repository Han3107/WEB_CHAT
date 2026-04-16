package com.simplechat.service;

import com.simplechat.entity.UserResponse;
import com.simplechat.repository.UserResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserResponseService {
    
    @Autowired
    private UserResponseRepository userResponseRepository;
    
    // Tao phan hoi tu user
    public UserResponse createResponse(UserResponse response) {
        return userResponseRepository.save(response);
    }
    
    // Lay phan hoi theo ID
    public Optional<UserResponse> getResponseById(Integer responseId) {
        return userResponseRepository.findById(responseId);
    }
    
    // Lay tat ca phan hoi cua task
    public List<UserResponse> getResponsesByTask(Integer taskId) {
        return userResponseRepository.findByTask_TaskId(taskId);
    }
    
    // Lay tat ca phan hoi cua user
    public List<UserResponse> getResponsesByUser(Integer userId) {
        return userResponseRepository.findByUser_UserId(userId);
    }
    
    // Kiem tra user da phan hoi cho task chua
    public boolean hasUserResponded(Integer taskId, Integer userId) {
        return userResponseRepository.findByTask_TaskIdAndUser_UserId(taskId, userId).isPresent();
    }
    
    // Lay phan hoi cu neu co (de update)
    public Optional<UserResponse> getExistingResponse(Integer taskId, Integer userId) {
        return userResponseRepository.findByTask_TaskIdAndUser_UserId(taskId, userId);
    }
    
    // Cap nhat phan hoi (doi y)
    public UserResponse updateResponse(UserResponse response) {
        return userResponseRepository.save(response);
    }
    
    // Xoa phan hoi
    public void deleteResponse(Integer responseId) {
        userResponseRepository.deleteById(responseId);
    }
    
    // Cap nhat phan hoi cho tuy chon khac
    public UserResponse changeResponseOption(Integer taskId, Integer userId, Integer newOptionId, UserResponse response) {
        Optional<UserResponse> existing = getExistingResponse(taskId, userId);
        if (existing.isPresent()) {
            UserResponse ur = existing.get();
            ur.setOption(response.getOption());
            ur.setRespondedAt(java.time.LocalDateTime.now());
            return userResponseRepository.save(ur);
        }
        return createResponse(response);
    }
}
