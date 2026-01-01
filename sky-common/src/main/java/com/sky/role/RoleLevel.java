package com.sky.role;

public enum RoleLevel {
    STAFF(1),
    MANAGER(5),
    SUPER(9);

    private final int level;

    RoleLevel(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public int getValue() {
        return level;
    }

    public static RoleLevel fromValue(Integer value) {
        if (value == null) return null;
        for (RoleLevel role : values()) {
            if (role.level == value) {
                return role;
            }
        }
        return null;
    }

    public static RoleLevel from(Object v) {
        if (v == null) return STAFF;
        String s = String.valueOf(v);
        // 允许 token 存 "STAFF"/"MANAGER"/"SUPER" 或数字
        try {
            int x = Integer.parseInt(s);
            if (x >= SUPER.level) return SUPER;
            if (x >= MANAGER.level) return MANAGER;
            return STAFF;
        } catch (NumberFormatException e) {
            return RoleLevel.valueOf(s);
        }
    }
}
