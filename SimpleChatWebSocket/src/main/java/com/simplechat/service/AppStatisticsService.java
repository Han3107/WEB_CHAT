package com.simplechat.service;

import com.simplechat.entity.AppStatistics;
import com.simplechat.repository.AppStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AppStatisticsService {
    
    @Autowired
    private AppStatisticsRepository statisticsRepository;
    
    // Tao statistics moi cho ngay mai
    public AppStatistics createTodayStatistics() {
        AppStatistics stats = new AppStatistics();
        stats.setStatDate(LocalDate.now());
        stats.setTotalUsers(0);
        stats.setActiveUsers(0);
        stats.setTotalChannels(0);
        stats.setTotalMessages(0);
        stats.setTotalVotes(0);
        stats.setTotalQuiz(0);
        stats.setActiveSessions(0);
        return statisticsRepository.save(stats);
    }
    
    // Lay statistics cua ngay
    public Optional<AppStatistics> getStatisticsByDate(LocalDate date) {
        return statisticsRepository.findByStatDate(date);
    }
    
    // Lay statistics hom nay
    public Optional<AppStatistics> getTodayStatistics() {
        return statisticsRepository.findByStatDate(LocalDate.now());
    }
    
    // Cap nhat tong so nguoi dung
    public AppStatistics updateTotalUsers(LocalDate date, Integer count) {
        Optional<AppStatistics> stats = getStatisticsByDate(date);
        if (stats.isPresent()) {
            AppStatistics s = stats.get();
            s.setTotalUsers(count);
            return statisticsRepository.save(s);
        }
        return null;
    }
    
    // Cap nhat so user hoat dong
    public AppStatistics updateActiveUsers(LocalDate date, Integer count) {
        Optional<AppStatistics> stats = getStatisticsByDate(date);
        if (stats.isPresent()) {
            AppStatistics s = stats.get();
            s.setActiveUsers(count);
            return statisticsRepository.save(s);
        }
        return null;
    }
    
    // Cap nhat tong so kenh
    public AppStatistics updateTotalChannels(LocalDate date, Integer count) {
        Optional<AppStatistics> stats = getStatisticsByDate(date);
        if (stats.isPresent()) {
            AppStatistics s = stats.get();
            s.setTotalChannels(count);
            return statisticsRepository.save(s);
        }
        return null;
    }
    
    // Cap nhat tong so tin nhan
    public AppStatistics updateTotalMessages(LocalDate date, Integer count) {
        Optional<AppStatistics> stats = getStatisticsByDate(date);
        if (stats.isPresent()) {
            AppStatistics s = stats.get();
            s.setTotalMessages(count);
            return statisticsRepository.save(s);
        }
        return null;
    }
    
    // Cap nhat tong vote
    public AppStatistics updateTotalVotes(LocalDate date, Integer count) {
        Optional<AppStatistics> stats = getStatisticsByDate(date);
        if (stats.isPresent()) {
            AppStatistics s = stats.get();
            s.setTotalVotes(count);
            return statisticsRepository.save(s);
        }
        return null;
    }
    
    // Cap nhat tong quiz
    public AppStatistics updateTotalQuiz(LocalDate date, Integer count) {
        Optional<AppStatistics> stats = getStatisticsByDate(date);
        if (stats.isPresent()) {
            AppStatistics s = stats.get();
            s.setTotalQuiz(count);
            return statisticsRepository.save(s);
        }
        return null;
    }
    
    // Cap nhat active sessions
    public AppStatistics updateActiveSessions(LocalDate date, Integer count) {
        Optional<AppStatistics> stats = getStatisticsByDate(date);
        if (stats.isPresent()) {
            AppStatistics s = stats.get();
            s.setActiveSessions(count);
            return statisticsRepository.save(s);
        }
        return null;
    }
    
    // Lay tat ca statistics
    public List<AppStatistics> getAllStatistics() {
        return statisticsRepository.findAll();
    }
    
    // Cap nhat tat ca thong ke
    public AppStatistics updateAllStatistics(AppStatistics statistics) {
        return statisticsRepository.save(statistics);
    }
}
