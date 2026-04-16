package com.simplechat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.simplechat.repository.ActivityLogRepository;
import com.simplechat.repository.ChannelMemberRepository;
import com.simplechat.repository.ChannelRepository;
import com.simplechat.repository.MessageRepository;
import com.simplechat.repository.UserRepository;

/**
 * DataLoader - Khởi tạo dữ liệu mẫu khi ứng dụng khởi động
 */
@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChannelRepository channelRepository;
    
    @Autowired
    private ChannelMemberRepository channelMemberRepository;
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Only use existing database data - no demo initialization
        long userCount = userRepository.count();
        System.out.println("✓ Database ready. Users in database: " + userCount);
    }
}
