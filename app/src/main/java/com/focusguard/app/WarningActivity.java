package com.focusguard.app;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WarningActivity extends AppCompatActivity {

    private static final String[] TITLES = {
        "Stay Focused!",
        "You're Stronger!",
        "Think About It",
        "Champions Focus!",
        "Your Future Waits!",
        "Final Warning!"
    };

    private static final String[] MESSAGES = {
        "You just opened a distracting app during your focus time.\n\nYou started this session for a reason. Your goals are waiting for you. Close this and get back to what matters!",
        "This is your 2nd attempt to open a distracting app.\n\n\"Discipline is choosing between what you want now and what you want most.\"\n\nYour future self thanks you for staying on track!",
        "Third attempt! Your brain is fighting the urge and you CAN win this battle.\n\n\"The secret of getting ahead is getting started and staying started.\"\n\nEvery minute of focus compounds into success.",
        "Fourth attempt. Champions are not people who never feel tempted - they push through the temptation.\n\n\"Success is the sum of small efforts repeated day in and day out.\"\n\nYou are capable of greatness. Prove it now.",
        "Fifth attempt! You are SO close to breaking through the distraction barrier.\n\nResearch shows the urge to check social media peaks and then fades. Wait it out.\n\n\"In 5 years you will be more disappointed by what you did not do than what you did.\"",
        "Sixth attempt - FINAL WARNING.\n\nYou have tried to open this app 6 times during your focus session. Before you can proceed, you must complete a short mind exercise to reset your brain.\n\nThis is for your own growth. Ready?"
    };

    private static final int[] BG_COLORS = {
        0xFF1A237E, 0xFF1B5E20, 0xFF4A148C, 0xFFB71C1C, 0xFF0D47A1, 0xFF212121
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning);

        String pkg = getIntent().getStringExtra("package_name");
        int warnNum = getIntent().getIntExtra("warning_number", 1);
        int idx = Math.max(0, Math.min(warnNum - 1, 5));

        String appName = pkg != null ? pkg : "this app";
        try {
            PackageManager pm = getPackageManager();
            if (pkg != null)
                appName = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (Exception ignored) {}

        TextView tvTitle    = findViewById(R.id.tvWarnTitle);
        TextView tvCounter  = findViewById(R.id.tvCounter);
        TextView tvMsg      = findViewById(R.id.tvWarnMsg);
        TextView tvApp      = findViewById(R.id.tvAppName);
        Button   btnBack    = findViewById(R.id.btnGoBack);
        Button   btnDismiss = findViewById(R.id.btnDismiss);

        tvTitle.setText(TITLES[idx]);
        tvCounter.setText("Warning " + warnNum + " of 6");
        tvMsg.setText(MESSAGES[idx]);
        tvApp.setText("App: " + appName);

        getWindow().getDecorView().setBackgroundColor(BG_COLORS[idx]);

        btnBack.setOnClickListener(v -> {
            goHome();
            finish();
        });

        if (warnNum < 6) {
            btnDismiss.setText("I'll be quick (Warning " + (warnNum + 1) + " next)");
            btnDismiss.setOnClickListener(v -> finish());
        } else {
            btnDismiss.setText("Complete Mind Challenge");
            btnDismiss.setOnClickListener(v -> {
                android.content.Intent i = new android.content.Intent(this, MindGameActivity.class);
                i.putExtra("package_name", pkg);
                startActivity(i);
                finish();
            });
        }
    }

    private void goHome() {
        FocusAccessibilityService svc = FocusAccessibilityService.getInstance();
        if (svc != null) {
            svc.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME);
        }
    }

    @Override
    public void onBackPressed() {
        goHome();
        finish();
    }
}
