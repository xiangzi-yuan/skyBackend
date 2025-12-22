package com.sky.context;

import com.sky.role.RoleLevel;

public class BaseContext {
    private static final ThreadLocal<Long> currentId = new ThreadLocal<>();
    private static final ThreadLocal<RoleLevel> currentRole = new ThreadLocal<>();

    public static void setCurrentId(Long id) { currentId.set(id); }
    public static Long getCurrentId() { return currentId.get(); }

    public static void setCurrentRole(RoleLevel role) { currentRole.set(role); }
    public static RoleLevel getCurrentRole() { return currentRole.get(); }

    public static void removeCurrentId() { currentId.remove(); }
    public static void removeCurrentRole() { currentRole.remove(); }
}