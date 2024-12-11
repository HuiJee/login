package com.hjpj.login.util;

public enum UserRole {

    ADMIN("A", "ADMIN"),
    MANAGER("M", "MANAGER"),
    USER("U", "USER"),
    GUEST("G", "GUEST");

    private final String roleKey;
    private final String roleName;

    UserRole(String roleKey, String roleName) {
        this.roleKey = roleKey;
        this.roleName = roleName;
    }

    public static String getUserRole(String roleKey) {
        for(UserRole role : values()) {
            if(role.roleKey.equals(roleKey)) {
                return role.roleName;
            }
        }
        return null;
    }



}
