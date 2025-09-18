package com.grpc.demo.enums;

public enum Category {
    ROPA(1),
    ALIMENTOS(2),
    JUGUETES(3),
    UTILES_ESCOLARES(4);

    private final int id;

    Category(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String displayName() {
        return this.name();
    }

    public static Category fromId(int id) {
        for (Category category : Category.values()) {
            if (category.getId() == id) {
                return category;
            }
        }
        throw new IllegalArgumentException("No existe una Category con id: " + id);
    }

    public static int idFromName(String categoryName) {
        try {
            return Category.valueOf(categoryName.toUpperCase().replace(" ", "_")).getId();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No existe una Category con nombre: " + categoryName);
        }
    }
}
