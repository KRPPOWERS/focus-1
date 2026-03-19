package com.focusguard.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PermissionSetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_setup);

        findViewById(R.id.btnAccessibility).setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        findViewById(R.id.btnUsageStats).setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));

        findViewById(R.id.btnOverlay).setOnClickListener(v -> {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        // Accessibility
        TextView tvA = findViewById(R.id.tvAccessStatus);
        boolean accessOk = isAccessibilityEnabled();
        tvA.setText(accessOk ? "GRANTED" : "NOT GRANTED - Tap button below");
        tvA.setTextColor(accessOk ? 0xFF4CAF50 : 0xFFFF5722);

        // How-to text
        TextView tvHow = findViewById(R.id.tvAccessHow);
        if (!accessOk) {
            tvHow.setText("Steps:\n1. Tap 'Open Accessibility Settings'\n2. Scroll down to find 'FocusGuard'\n3. Tap it and toggle ON\n4. Tap OK on the confirmation");
            tvHow.setVisibility(android.view.View.VISIBLE);
        } else {
            tvHow.setVisibility(android.view.View.GONE);
        }

        // Usage Stats
        TextView tvU = findViewById(R.id.tvUsageStatus);
        boolean usageOk = hasUsageAccess();
        tvU.setText(usageOk ? "GRANTED" : "NOT GRANTED - Tap button below");
        tvU.setTextColor(usageOk ? 0xFF4CAF50 : 0xFFFF5722);

        // Overlay
        TextView tvO = findViewById(R.id.tvOverlayStatus);
        boolean overlayOk = Settings.canDrawOverlays(this);
        tvO.setText(overlayOk ? "GRANTED" : "NOT GRANTED - Tap button below");
        tvO.setTextColor(overlayOk ? 0xFF4CAF50 : 0xFFFF5722);

        // Overall status
        TextView tvAll = findViewById(R.id.tvAllStatus);
        if (accessOk && usageOk && overlayOk) {
            tvAll.setText("All permissions granted! FocusGuard is ready.");
            tvAll.setTextColor(0xFF4CAF50);
        } else {
            int missing = (accessOk ? 0 : 1) + (usageOk ? 0 : 1) + (overlayOk ? 0 : 1);
            tvAll.setText(missing + " permission(s) still needed");
            tvAll.setTextColor(0xFFFF5722);
        }
    }

    private boolean isAccessibilityEnabled() {
        try {
            int e = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            String s = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return e == 1 && s != null &&
                   s.contains(getPackageName() + "/.FocusAccessibilityService");
        } catch (Exception e) { return false; }
    }

    private boolean hasUsageAccess() {
        try {
            android.app.AppOpsManager aom =
                    (android.app.AppOpsManager) getSystemService(APP_OPS_SERVICE);
            int mode = aom.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
            return mode == android.app.AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) { return false; }
    }
}
