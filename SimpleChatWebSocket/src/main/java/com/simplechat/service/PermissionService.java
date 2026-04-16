package com.simplechat.service;

import com.simplechat.entity.Permission;
import com.simplechat.entity.RolePermission;
import com.simplechat.repository.PermissionRepository;
import com.simplechat.repository.RolePermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PermissionService {
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    
    // Tao permission
    public Permission createPermission(Permission permission) {
        return permissionRepository.save(permission);
    }
    
    // Lay permission theo ID
    public Optional<Permission> getPermissionById(Integer permissionId) {
        return permissionRepository.findById(permissionId);
    }
    
    // Lay permission theo ten
    public Optional<Permission> getPermissionByName(String permissionName) {
        return permissionRepository.findByPermissionName(permissionName);
    }
    
    // Lay tat ca permission
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
    
    // Cap nhat permission
    public Permission updatePermission(Permission permission) {
        return permissionRepository.save(permission);
    }
    
    // Xoa permission
    public void deletePermission(Integer permissionId) {
        permissionRepository.deleteById(permissionId);
    }
    
    // Them permission cho role
    public RolePermission addPermissionToRole(String role, Integer permissionId) {
        Optional<Permission> permission = permissionRepository.findById(permissionId);
        if (permission.isPresent()) {
            RolePermission rp = new RolePermission();
            rp.setRole(role);
            rp.setPermission(permission.get());
            return rolePermissionRepository.save(rp);
        }
        return null;
    }
    
    // Lay tat ca permission cua role
    public List<Permission> getPermissionsByRole(String role) {
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRole(role);
        return rolePermissions.stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toList());
    }
    
    // Kiem tra role co permission khong
    public boolean hasPermission(String role, String permissionName) {
        List<Permission> permissions = getPermissionsByRole(role);
        return permissions.stream()
                .anyMatch(p -> p.getPermissionName().equals(permissionName));
    }
    
    // Xoa permission khoi role
    public void removePermissionFromRole(String role, Integer permissionId) {
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRole(role);
        RolePermission toDelete = rolePermissions.stream()
                .filter(rp -> rp.getPermission().getPermissionId().equals(permissionId))
                .findFirst()
                .orElse(null);
        if (toDelete != null) {
            rolePermissionRepository.delete(toDelete);
        }
    }
    
    // Lay tat ca RolePermission
    public List<RolePermission> getAllRolePermissions() {
        return rolePermissionRepository.findAll();
    }
}
