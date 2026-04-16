package com.simplechat.service;

import com.simplechat.entity.Task;
import com.simplechat.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    // Tao task (vote/quiz)
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }
    
    // Lay task theo ID
    public Optional<Task> getTaskById(Integer taskId) {
        return taskRepository.findById(taskId);
    }
    
    // Lay tat ca task cua kenh
    public List<Task> getTasksByChannel(Integer channelId) {
        return taskRepository.findByChannel_ChannelId(channelId);
    }
    
    // Lay task theo loai (vote/quiz)
    public List<Task> getTasksByType(String taskType) {
        return taskRepository.findByTaskType(taskType);
    }
    
    // Lay task theo trang thai (active/closed)
    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }
    
    // Lay task dang hoat dong cua kenh
    public List<Task> getActiveTasksByChannel(Integer channelId) {
        return taskRepository.findActiveTasksByChannel(channelId);
    }
    
    // Cap nhat task
    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }
    
    // Dong task (change status to closed)
    public Task closeTask(Integer taskId) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isPresent()) {
            Task t = task.get();
            t.setStatus("closed");
            return taskRepository.save(t);
        }
        return null;
    }
    
    // Mo task (change status to active)
    public Task openTask(Integer taskId) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isPresent()) {
            Task t = task.get();
            t.setStatus("active");
            return taskRepository.save(t);
        }
        return null;
    }
    
    // Xoa task
    public void deleteTask(Integer taskId) {
        taskRepository.deleteById(taskId);
    }
    
    // Lay tat ca task
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
}
