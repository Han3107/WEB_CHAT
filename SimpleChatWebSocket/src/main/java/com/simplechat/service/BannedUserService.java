package com.simplechat.service;

import com.simplechat.entity.BannedUser;
import com.simplechat.repository.BannedUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BannedUserService {
    
    @Autowired
    private BannedUserRepository bannedUserRepository;
    
    // Cam trai user
    public BannedUser banUser(BannedUser bannedUser) {
        return bannedUserRepository.save(bannedUser);
    }
    
    // Lay cam trai theo ID
    public Optional<BannedUser> getBanById(Integer banId) {
        return bannedUserRepository.findById(banId);
    }
    
    // Kiem tra user co bi cam trai khong
    public Optional<BannedUser> checkIfBanned(Integer userId) {
        return bannedUserRepository.findByUser_UserId(userId);
    }
    
    // Lay tat ca cam trai vinh bien
    public List<BannedUser> getPermanentBans() {
        return bannedUserRepository.findByIsPermanent(true);
    }
    
    // Lay tat ca cam trai tam thoi
    public List<BannedUser> getTemporaryBans() {
        return bannedUserRepository.findByIsPermanent(false);
    }
    
    // Goi ban cho user (tam thoi)
    public BannedUser banUserTemporarily(Integer userId, Integer bannedBy, String reason, LocalDateTime banEnd) {
        BannedUser bannedUser = new BannedUser();
        // Set user and bannedBy (you need to inject UserRepository to fetch these)
        bannedUser.setReason(reason);
        bannedUser.setBanEnd(banEnd);
        bannedUser.setIsPermanent(false);
        return bannedUserRepository.save(bannedUser);
    }
    
    // Goi ban vinh bien
    public BannedUser banUserPermanently(Integer userId, Integer bannedBy, String reason) {
        BannedUser bannedUser = new BannedUser();
        // Set user and bannedBy
        bannedUser.setReason(reason);
        bannedUser.setIsPermanent(true);
        return bannedUserRepository.save(bannedUser);
    }
    
    // Goi ban het han
    public BannedUser unbanUser(Integer banId) {
        Optional<BannedUser> bannedUser = bannedUserRepository.findById(banId);
        if (bannedUser.isPresent()) {
            BannedUser bu = bannedUser.get();
            bannedUserRepository.delete(bu);
        }
        return null;
    }
    
    // Kiem tra ban co con hieu luc khong
    public boolean isBanActive(BannedUser ban) {
        if (ban.getIsPermanent()) {
            return true;
        }
        if (ban.getBanEnd() == null) {
            return false;
        }
        return ban.getBanEnd().isAfter(LocalDateTime.now());
    }
    
    // Lay tat ca ban
    public List<BannedUser> getAllBans() {
        return bannedUserRepository.findAll();
    }
}
