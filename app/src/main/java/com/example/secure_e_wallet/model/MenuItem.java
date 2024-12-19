package com.example.secure_e_wallet.model;

import android.graphics.drawable.Drawable;

public class MenuItem {
    private int id;
    private String title;
    private Drawable icon;

    public MenuItem(int id, String title, Drawable icon) {
        this.id = id;
        this.title = title;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Drawable getIcon() {
        return icon;
    }
}
