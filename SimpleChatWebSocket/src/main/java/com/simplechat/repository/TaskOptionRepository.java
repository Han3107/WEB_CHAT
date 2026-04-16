package com.simplechat.repository;

import com.simplechat.entity.TaskOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskOptionRepository extends JpaRepository<TaskOption, Integer> {
    List<TaskOption> findByTask_TaskId(Integer taskId);
}
