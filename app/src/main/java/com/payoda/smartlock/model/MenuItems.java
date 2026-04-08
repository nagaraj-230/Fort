package com.payoda.smartlock.model;

import androidx.fragment.app.Fragment;

public class MenuItems {
    private Fragment fragment;
    private String title;
    private int icon;
    private int count;

    public MenuItems(Fragment fragment, String title, int icon) {
        this.fragment = fragment;
        this.title = title;
        this.icon = icon;
    }

    public MenuItems(Fragment fragment, String title, int icon, int count) {
        this.fragment = fragment;
        this.title = title;
        this.icon = icon;
        this.count = count;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
