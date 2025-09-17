package com.grpc.demo.enums;

public enum Role {
    PRESIDENTE(1),
    COORDINADOR(2),
    VOCAL(4),
    VOLUNTARIO(3);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public String authority() {
        return this.name();
    }

    public static Role fromLevel(int level) {
        for (Role role : Role.values()) {
            if (role.getLevel() == level) {
                return role;
            }
        }
        throw new IllegalArgumentException("No existe un Role con level: " + level);
    }

    public static int levelFromName(String roleName) {
        try {
            return Role.valueOf(roleName.toUpperCase()).getLevel();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No existe un Role con nombre: " + roleName);
        }
    }
}
