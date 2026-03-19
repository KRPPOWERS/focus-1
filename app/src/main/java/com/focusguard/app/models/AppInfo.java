package com.focusguard.app.models;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String packageName;
    public String appName;
    public Drawable icon;
    public boolean selected;

    public AppInfo(String packageName, String appName, Drawable icon) {
        this.packageName = packageName;
        this.appName = appName;
        this.icon = icon;
        this.selected = false;
    }
}
