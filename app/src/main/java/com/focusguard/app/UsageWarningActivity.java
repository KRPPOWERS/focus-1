package com.focusguard.app;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UsageWarningActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_warning);

        String pkg  = getIntent().getStringExtra("package_name");
        int    mins = getIntent().getIntExtra("usage_minutes", 30);

        String appName = pkg != null ? pkg : "this app";
        try {
            PackageManager pm = getPackageManager();
            if (pkg != null)
                appName = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (Exception ignored) {}

        String timeStr = mins >= 60
                ? (mins / 60) + "h " + (mins % 60) + "m"
                : mins + " minutes";

        TextView tvTitle  = findViewById(R.id.tvUsageTitle);
        TextView tvTime   = findViewById(R.id.tvUsageTime);
        TextView tvMsg    = findViewById(R.id.tvUsageMsg);
        Button   btnClose = findViewById(R.id.btnClose);

        tvTitle.setText("Screen Time Alert");
        tvTime.setText("You have spent " + timeStr + " on " + appName + " today");

        String message = "That's " + timeStr + " of your precious time on " + appName + " today.\n\n"
                + "\"Time is the most valuable thing a man can spend.\" - Theophrastus\n\n"
                + "Consider what else you could achieve with that time. Your goals, your health, "
                + "your relationships - all deserve more of you than a social media feed.\n\n"
                + "You have the power to choose how you spend every next minute. Choose wisely.";
        tvMsg.setText(message);

        btnClose.setOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
