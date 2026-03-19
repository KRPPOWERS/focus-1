package com.focusguard.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import com.focusguard.app.models.FocusTimeRange;
import com.focusguard.app.utils.PrefsManager;
import java.util.List;
import java.util.Set;

public class FocusAccessibilityService extends AccessibilityService {

    private static FocusAccessibilityService sInstance;
    public static FocusAccessibilityService getInstance() { return sInstance; }

    private PrefsManager prefs;
    private String currentPkg = "";
    private long appStartMs = 0;
    // Cooldown: avoid spamming warning activities
    private long lastWarningMs = 0;
    private static final long WARN_COOLDOWN_MS = 2000;

    @Override
    protected void onServiceConnected() {
        sInstance = this;
        prefs = new PrefsManager(this);
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 200;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence pkgCs = event.getPackageName();
        if (pkgCs == null) return;
        String pkg = pkgCs.toString();
        if (pkg.equals(getPackageName())) return; // ignore own app
        if (pkg.equals("android") || pkg.equals("com.android.systemui")) return;

        // Track usage for previous app
        if (!currentPkg.isEmpty() && appStartMs > 0 && !currentPkg.equals(pkg)) {
            long elapsed = System.currentTimeMillis() - appStartMs;
            if (elapsed > 500) { // debounce tiny flashes
                prefs.addUsageMs(currentPkg, elapsed);
                maybeShowUsageWarning(currentPkg);
            }
        }
        currentPkg = pkg;
        appStartMs = System.currentTimeMillis();

        if (!prefs.isEnabled()) return;
        Set<String> blocked = prefs.getBlockedApps();
        if (!blocked.contains(pkg)) return;

        long now = System.currentTimeMillis();
        if (now - lastWarningMs < WARN_COOLDOWN_MS) return;
        lastWarningMs = now;

        if (isInFocusTime()) {
            int count = prefs.getWarnCount(pkg);
            // Push blocked app to background
            performGlobalAction(GLOBAL_ACTION_HOME);
            if (count >= 6) {
                prefs.resetWarnCount(pkg);
                Intent i = new Intent(this, MindGameActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("package_name", pkg);
                startActivity(i);
            } else {
                prefs.incWarnCount(pkg);
                Intent i = new Intent(this, WarningActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("package_name", pkg);
                i.putExtra("warning_number", count + 1);
                startActivity(i);
            }
        }
    }

    private boolean isInFocusTime() {
        List<FocusTimeRange> ranges = prefs.getFocusRanges();
        for (FocusTimeRange r : ranges) if (r.isActive()) return true;
        return false;
    }

    private void maybeShowUsageWarning(String pkg) {
        if (isInFocusTime()) return; // focus-time warnings handled separately
        Set<String> blocked = prefs.getBlockedApps();
        if (!blocked.contains(pkg)) return;
        long totalMs = prefs.getUsageMs(pkg);
        long thresholdMs = prefs.getUsageThresholdMin() * 60_000L;
        // Warn every time threshold is crossed (each additional threshold block)
        long prevMs = totalMs - prefs.getUsageMs(pkg); // already updated
        // Simple: warn once per threshold crossing
        long blocks = totalMs / thresholdMs;
        long prevBlocks = (totalMs - (System.currentTimeMillis() - appStartMs)) / thresholdMs;
        if (blocks > 0 && blocks > prevBlocks) {
            Intent i = new Intent(this, UsageWarningActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("package_name", pkg);
            i.putExtra("usage_minutes", (int)(totalMs / 60_000L));
            startActivity(i);
        }
    }

    @Override
    public void onInterrupt() {}

    @Override
    public boolean onUnbind(Intent intent) {
        sInstance = null;
        return super.onUnbind(intent);
    }
}
