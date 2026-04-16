package com.simplechat.service;

import com.simplechat.entity.User;
import com.simplechat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // Tao user
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    // Lay user theo ID
    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }
    
    // Lay user theo username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    // Lay user theo email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    // Lay tat ca user co role
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    // Lay tat ca user active va co role
    public List<User> getActiveUsersByRole(String role) {
        return userRepository.findActiveByRole(role);
    }
    
    // Cap nhat user
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    // Xoa user
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }
    
    // Lay tat ca user
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
