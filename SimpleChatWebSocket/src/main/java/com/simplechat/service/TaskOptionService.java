package com.simplechat.service;

import com.simplechat.entity.TaskOption;
import com.simplechat.repository.TaskOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskOptionService {
    
    @Autowired
    private TaskOptionRepository taskOptionRepository;
    
    // Tao tuy chon task
    public TaskOption createOption(TaskOption option) {
        return taskOptionRepository.save(option);
    }
    
    // Lay tuy chon theo ID
    public Optional<TaskOption> getOptionById(Integer optionId) {
        return taskOptionRepository.findById(optionId);
    }
    
    // Lay tat ca tuy chon cua task
    public List<TaskOption> getOptionsByTask(Integer taskId) {
        return taskOptionRepository.findByTask_TaskId(taskId);
    }
    
    // Tang vote count
    public TaskOption incrementVoteCount(Integer optionId) {
        Optional<TaskOption> option = taskOptionRepository.findById(optionId);
        if (option.isPresent()) {
            TaskOption opt = option.get();
            opt.setVoteCount((opt.getVoteCount() != null ? opt.getVoteCount() : 0) + 1);
            return taskOptionRepository.save(opt);
        }
        return null;
    }
    
    // Giam vote count
    public TaskOption decrementVoteCount(Integer optionId) {
        Optional<TaskOption> option = taskOptionRepository.findById(optionId);
        if (option.isPresent()) {
            TaskOption opt = option.get();
            int currentCount = opt.getVoteCount() != null ? opt.getVoteCount() : 0;
            if (currentCount > 0) {
                opt.setVoteCount(currentCount - 1);
                return taskOptionRepository.save(opt);
            }
        }
        return null;
    }
    
    // Cap nhat tuy chon
    public TaskOption updateOption(TaskOption option) {
        return taskOptionRepository.save(option);
    }
    
    // Xoa tuy chon
    public void deleteOption(Integer optionId) {
        taskOptionRepository.deleteById(optionId);
    }
}
