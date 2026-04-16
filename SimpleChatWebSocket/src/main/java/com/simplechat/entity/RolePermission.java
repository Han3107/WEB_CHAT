package com.simplechat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "role_permissions")
public class RolePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rolePermissionId;
    
    @Column(nullable = false, length = 50)
    private String role;
    
    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    public RolePermission() {}
    public RolePermission(String role, Permission permission) {
        this.role = role;
        this.permission = permission;
    }

    public Integer getRolePermissionId() { return rolePermissionId; }
    public void setRolePermissionId(Integer rolePermissionId) { this.rolePermissionId = rolePermissionId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Permission getPermission() { return permission; }
    public void setPermission(Permission permission) { this.permission = permission; }
}
