package com.simplechat.repository;

import com.simplechat.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

    // Tìm quan hệ giữa 2 user (bất kể chiều)
    @Query("SELECT f FROM Friendship f WHERE (f.requester.userId = :a AND f.receiver.userId = :b) OR (f.requester.userId = :b AND f.receiver.userId = :a)")
    Optional<Friendship> findBetween(@Param("a") Integer a, @Param("b") Integer b);

    // Danh sách bạn bè đã accepted
    @Query("SELECT f FROM Friendship f WHERE (f.requester.userId = :uid OR f.receiver.userId = :uid) AND f.status = 'accepted'")
    List<Friendship> findFriends(@Param("uid") Integer uid);

    // Lời mời kết bạn đang chờ (gửi đến mình)
    @Query("SELECT f FROM Friendship f WHERE f.receiver.userId = :uid AND f.status = 'pending'")
    List<Friendship> findPendingReceived(@Param("uid") Integer uid);

    // Lời mời mình đã gửi đi
    @Query("SELECT f FROM Friendship f WHERE f.requester.userId = :uid AND f.status = 'pending'")
    List<Friendship> findPendingSent(@Param("uid") Integer uid);
}
