package com.focusguard.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.focusguard.app.utils.PrefsManager;

public class MidnightReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        PrefsManager prefs = new PrefsManager(ctx);
        prefs.resetAllWarnCounts();
        prefs.resetAllUsage();
    }
}
