package com.simplechat.repository;

import com.simplechat.entity.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserResponseRepository extends JpaRepository<UserResponse, Integer> {
    List<UserResponse> findByTask_TaskId(Integer taskId);
    List<UserResponse> findByUser_UserId(Integer userId);
    Optional<UserResponse> findByTask_TaskIdAndUser_UserId(Integer taskId, Integer userId);
}
