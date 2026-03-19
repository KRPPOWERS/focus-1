package com.focusguard.app;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
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
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        tvEmpty.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                Set<String> blocked = prefs.getBlockedApps();
                PackageManager pm = getPackageManager();

                // Get all apps including non-system ones via getLaunchIntent
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
                    if (list.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No apps found. Please grant permissions first.");
                    }
                    tvAppCount.setText(list.size() + " apps found");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    TextView tvEmpty = findViewById(R.id.tvEmpty);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Error loading apps: " + e.getMessage());
                });
            }
        }).start();
    }

    private void filter(String q) {
        filtered.clear();
        if (q.isEmpty()) {
            filtered.addAll(allApps);
        } else {
            for (AppInfo a : allApps)
                if (a.appName.toLowerCase().contains(q.toLowerCase()) ||
                    a.packageName.toLowerCase().contains(q.toLowerCase()))
                    filtered.add(a);
        }
        adapter.notifyDataSetChanged();
    }

    class AppAdapter extends RecyclerView.Adapter<AppAdapter.VH> {
        class VH extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvName, tvPkg;
            CheckBox cbSelect;
            VH(View v) {
                super(v);
                ivIcon   = v.findViewById(R.id.ivAppIcon);
                tvName   = v.findViewById(R.id.tvAppName);
                tvPkg    = v.findViewById(R.id.tvAppPkg);
                cbSelect = v.findViewById(R.id.cbSelect);
            }
        }
        @Override public VH onCreateViewHolder(ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_app, p, false));
        }
        @Override public void onBindViewHolder(VH h, int pos) {
            AppInfo a = filtered.get(pos);
            h.ivIcon.setImageDrawable(a.icon);
            h.tvName.setText(a.appName);
            h.tvPkg.setText(a.packageName);
            h.cbSelect.setChecked(a.selected);
            h.cbSelect.setOnClickListener(v -> a.selected = h.cbSelect.isChecked());
            h.itemView.setOnClickListener(v -> {
                a.selected = !a.selected;
                h.cbSelect.setChecked(a.selected);
            });
        }
        @Override public int getItemCount() { return filtered.size(); }
    }
}
