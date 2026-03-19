package com.focusguard.app;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.focusguard.app.models.FocusTimeRange;
import com.focusguard.app.utils.PrefsManager;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private PrefsManager prefs;
    private Switch swEnable;
    private TextView tvStatus, tvRangeCount, tvAppCount, tvActiveRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = new PrefsManager(this);

        swEnable     = findViewById(R.id.swEnable);
        tvStatus     = findViewById(R.id.tvStatus);
        tvRangeCount = findViewById(R.id.tvRangeCount);
        tvAppCount   = findViewById(R.id.tvAppCount);
        tvActiveRange= findViewById(R.id.tvActiveRange);

        swEnable.setOnCheckedChangeListener((btn, checked) -> {
            prefs.setEnabled(checked);
            updateUI();
            if (checked) startMonitorService();
        });

        findViewById(R.id.btnFocusTimes).setOnClickListener(v ->
                startActivity(new Intent(this, FocusTimeActivity.class)));
        findViewById(R.id.btnBlockedApps).setOnClickListener(v ->
                startActivity(new Intent(this, AppSelectorActivity.class)));
        findViewById(R.id.btnPermissions).setOnClickListener(v ->
                startActivity(new Intent(this, PermissionSetupActivity.class)));

        startMonitorService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void startMonitorService() {
        Intent svc = new Intent(this, FocusMonitorService.class);
        ContextCompat.startForegroundService(this, svc);
    }

    private void updateUI() {
        boolean enabled = prefs.isEnabled();
        swEnable.setChecked(enabled);
        boolean accessOk = isAccessibilityEnabled();

        if (!accessOk) {
            tvStatus.setText("⚠️ Accessibility Service not enabled");
            tvStatus.setTextColor(0xFFFF5722);
        } else if (enabled) {
            tvStatus.setText("✅ FocusGuard is ACTIVE");
            tvStatus.setTextColor(0xFF4CAF50);
        } else {
            tvStatus.setText("⏸ FocusGuard is PAUSED");
            tvStatus.setTextColor(0xFFFF9800);
        }

        List<FocusTimeRange> ranges = prefs.getFocusRanges();
        tvRangeCount.setText("Focus Ranges: " + ranges.size() + " / 10");

        Set<String> blocked = prefs.getBlockedApps();
        tvAppCount.setText("Blocked Apps: " + blocked.size());

        String active = "No active focus range now";
        for (FocusTimeRange r : ranges) {
            if (r.isActive()) {
                active = "🎯 FOCUS TIME: " + r.label + " (" + r.getTimeString() + ")";
                break;
            }
        }
        tvActiveRange.setText(active);
    }

    private boolean isAccessibilityEnabled() {
        try {
            int enabled = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            if (enabled != 1) return false;
            String services = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return services != null &&
                   services.contains(getPackageName() + "/.FocusAccessibilityService");
        } catch (Exception e) { return false; }
    }
}
