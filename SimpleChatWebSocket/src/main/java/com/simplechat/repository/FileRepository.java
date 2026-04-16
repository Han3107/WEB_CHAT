package com.simplechat.repository;

import com.simplechat.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Integer> {
    List<File> findByMessage_MessageId(Integer messageId);
    List<File> findByUploadedBy_UserId(Integer userId);
}
