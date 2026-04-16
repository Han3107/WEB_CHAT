package com.simplechat.repository;

import com.simplechat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByStatus(String status);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = 'active'")
    List<User> findActiveByRole(@Param("role") String role);
}
