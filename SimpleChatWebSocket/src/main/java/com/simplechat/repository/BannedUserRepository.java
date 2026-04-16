package com.simplechat.repository;

import com.simplechat.entity.BannedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BannedUserRepository extends JpaRepository<BannedUser, Integer> {
    Optional<BannedUser> findByUser_UserId(Integer userId);
    List<BannedUser> findByIsPermanent(Boolean isPermanent);
}
