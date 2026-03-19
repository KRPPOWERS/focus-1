package com.focusguard.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.focusguard.app.models.FocusTimeRange;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrefsManager {
    private static final String PREF_NAME    = "FocusGuardPrefs";
    private static final String KEY_RANGES   = "focus_ranges";
    private static final String KEY_BLOCKED  = "blocked_apps";
    private static final String KEY_ENABLED  = "service_enabled";
    private static final String KEY_WARN_PFX = "warn_";
    private static final String KEY_USE_PFX  = "use_ms_";
    private static final String KEY_THRESHOLD= "usage_threshold_min";
    private static final String KEY_NEXT_ID  = "next_range_id";

    private final SharedPreferences p;
    private final Gson gson = new Gson();

    public PrefsManager(Context ctx) {
        p = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Focus ranges (max 10)
    public void saveFocusRanges(List<FocusTimeRange> ranges) {
        p.edit().putString(KEY_RANGES, gson.toJson(ranges)).apply();
    }
    public List<FocusTimeRange> getFocusRanges() {
        String json = p.getString(KEY_RANGES, null);
        if (json == null) return new ArrayList<>();
        Type t = new TypeToken<List<FocusTimeRange>>(){}.getType();
        List<FocusTimeRange> list = gson.fromJson(json, t);
        return list != null ? list : new ArrayList<>();
    }
    public int nextRangeId() {
        int id = p.getInt(KEY_NEXT_ID, 1);
        p.edit().putInt(KEY_NEXT_ID, id + 1).apply();
        return id;
    }

    // Blocked apps
    public void saveBlockedApps(Set<String> pkgs) {
        p.edit().putStringSet(KEY_BLOCKED, new HashSet<>(pkgs)).apply();
    }
    public Set<String> getBlockedApps() {
        Set<String> set = p.getStringSet(KEY_BLOCKED, null);
        return set != null ? new HashSet<>(set) : new HashSet<>();
    }

    // Service toggle
    public void setEnabled(boolean v) { p.edit().putBoolean(KEY_ENABLED, v).apply(); }
    public boolean isEnabled() { return p.getBoolean(KEY_ENABLED, true); }

    // Warning counts per package (reset daily)
    public int getWarnCount(String pkg) { return p.getInt(KEY_WARN_PFX + pkg, 0); }
    public void incWarnCount(String pkg) {
        p.edit().putInt(KEY_WARN_PFX + pkg, getWarnCount(pkg) + 1).apply();
    }
    public void resetWarnCount(String pkg) { p.edit().remove(KEY_WARN_PFX + pkg).apply(); }
    public void resetAllWarnCounts() {
        SharedPreferences.Editor ed = p.edit();
        for (String k : p.getAll().keySet()) if (k.startsWith(KEY_WARN_PFX)) ed.remove(k);
        ed.apply();
    }

    // Usage time (ms) per package
    public long getUsageMs(String pkg) { return p.getLong(KEY_USE_PFX + pkg, 0); }
    public void addUsageMs(String pkg, long ms) {
        p.edit().putLong(KEY_USE_PFX + pkg, getUsageMs(pkg) + ms).apply();
    }
    public void resetAllUsage() {
        SharedPreferences.Editor ed = p.edit();
        for (String k : p.getAll().keySet()) if (k.startsWith(KEY_USE_PFX)) ed.remove(k);
        ed.apply();
    }

    // Usage threshold in minutes before showing warning outside focus time
    public int getUsageThresholdMin() { return p.getInt(KEY_THRESHOLD, 30); }
    public void setUsageThresholdMin(int min) { p.edit().putInt(KEY_THRESHOLD, min).apply(); }
}
