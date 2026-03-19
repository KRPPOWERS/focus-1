package com.focusguard.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.focusguard.app.models.AppInfo;
import com.focusguard.app.utils.PrefsManager;
import java.util.*;

public class AppSelectorActivity extends AppCompatActivity {

    private PrefsManager prefs;
    private List<AppInfo> allApps = new ArrayList<>();
    private List<AppInfo> filtered = new ArrayList<>();
    private AppAdapter adapter;
    private TextView tvAppCount;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selector);
        prefs = new PrefsManager(this);

        RecyclerView rv = findViewById(R.id.rvApps);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppAdapter();
        rv.setAdapter(adapter);

        tvAppCount = findViewById(R.id.tvAppCount);
        tvEmpty = findViewById(R.id.tvEmpty);

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) { filter(s.toString()); }
            public void afterTextChanged(Editable s) {}
        });

        loadApps();

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            Set<String> sel = new HashSet<>();
            for (AppInfo a : allApps) if (a.selected) sel.add(a.packageName);
            prefs.saveBlockedApps(sel);
            Toast.makeText(this, sel.size() + " apps will be blocked", Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void loadApps() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                Set<String> blocked = prefs.getBlockedApps();
                PackageManager pm = getPackageManager();

                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<android.content.pm.ResolveInfo> resolveInfos =
                        pm.queryIntentActivities(mainIntent, 0);

                List<AppInfo> list = new ArrayList<>();
                Set<String> seen = new HashSet<>();

                for (android.content.pm.ResolveInfo ri : resolveInfos) {
                    String pkg = ri.activityInfo.packageName;
                    if (seen.contains(pkg)) continue;
                    if (pkg.equals(getPackageName())) continue;
                    seen.add(pkg);
                    String name = ri.loadLabel(pm).toString();
                    AppInfo info = new AppInfo(pkg, name, ri.loadIcon(pm));
                    info.selected = blocked.contains(pkg);
                    list.add(info);
                }

                list.sort((a, b) -> a.appName.compareToIgnoreCase(b.appName));

                runOnUiThread(() -> {
                    allApps.clear();
                    allApps.addAll(list);
                    filtered.clear();
                    filtered.addAll(list);
                    adapter.notifyDataSetChanged();
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
