package com.simplechat.service;

import com.simplechat.entity.File;
import com.simplechat.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FileService {
    
    @Autowired
    private FileRepository fileRepository;
    
    // Luu file
    public File saveFile(File file) {
        return fileRepository.save(file);
    }
    
    // Lay file theo ID
    public Optional<File> getFileById(Integer fileId) {
        return fileRepository.findById(fileId);
    }
    
    // Lay tat ca file cua tin nhan
    public List<File> getFilesByMessage(Integer messageId) {
        return fileRepository.findByMessage_MessageId(messageId);
    }
    
    // Lay tat ca file cua user
    public List<File> getFilesByUser(Integer userId) {
        return fileRepository.findByUploadedBy_UserId(userId);
    }
    
    // Tang download count
    public File incrementDownloadCount(Integer fileId) {
        Optional<File> file = fileRepository.findById(fileId);
        if (file.isPresent()) {
            File f = file.get();
            f.setDownloadCount((f.getDownloadCount() != null ? f.getDownloadCount() : 0) + 1);
            return fileRepository.save(f);
        }
        return null;
    }
    
    // Cap nhat file info
    public File updateFile(File file) {
        return fileRepository.save(file);
    }
    
    // Xoa file
    public void deleteFile(Integer fileId) {
        fileRepository.deleteById(fileId);
    }
    
    // Lay tat ca file
    public List<File> getAllFiles() {
        return fileRepository.findAll();
    }
}
