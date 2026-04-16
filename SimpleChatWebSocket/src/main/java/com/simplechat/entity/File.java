package com.simplechat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class File {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fileId;
    
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;
    
    @Column(nullable = false, length = 255)
    private String fileName;
    
    @Column
    private Long fileSize;
    
    @Column(length = 50)
    private String fileType;
    
    @Column(length = 500)
    private String filePath;
    
    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
    
    @Column(nullable = false, columnDefinition = "datetime2")
    private LocalDateTime uploadedAt = LocalDateTime.now();
    
    @Column
    private Integer downloadCount = 0;

    public File() {}
    public File(Message message, String fileName, User uploadedBy) {
        this.message = message;
        this.fileName = fileName;
        this.uploadedBy = uploadedBy;
    }

    public Integer getFileId() { return fileId; }
    public void setFileId(Integer fileId) { this.fileId = fileId; }
    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
}
