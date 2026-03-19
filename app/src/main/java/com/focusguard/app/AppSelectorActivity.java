name: Build Debug APK

on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Install Gradle 8.7
        run: |
          wget -q https://services.gradle.org/distributions/gradle-8.7-bin.zip -O /tmp/gradle.zip
          unzip -q /tmp/gradle.zip -d /opt/gradle
          echo "/opt/gradle/gradle-8.7/bin" >> $GITHUB_PATH

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Download Gradle Wrapper JAR
        run: curl -sL "https://github.com/gradle/gradle/raw/v8.7.0/gradle/wrapper/gradle-wrapper.jar" -o gradle/wrapper/gradle-wrapper.jar

      - name: Rewrite AppSelectorActivity
        run: |
          mkdir -p app/src/main/java/com/focusguard/app
          cat > app/src/main/java/com/focusguard/app/AppSelectorActivity.java << 'JAVAEOF'
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
                  tvAppCount = findViewById(R.id.tvAppCount);
                  tvEmpty = findViewById(R.id.tvEmpty);
                  RecyclerView rv = findViewById(R.id.rvApps);
                  rv.setLayoutManager(new LinearLayoutManager(this));
                  adapter = new AppAdapter();
                  rv.setAdapter(adapter);
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
                          List<android.content.pm.ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
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
                              tvAppCount.setText(list.size() + " apps found");
                              if (list.isEmpty()) {
                                  tvEmpty.setVisibility(View.VISIBLE);
                                  tvEmpty.setText("No apps found. Grant permissions first.");
                              }
                          });
                      } catch (Exception e) {
                          runOnUiThread(() -> {
                              findViewById(R.id.progressBar).setVisibility(View.GONE);
                              tvEmpty.setVisibility(View.VISIBLE);
                              tvEmpty.setText("Error: " + e.getMessage());
                          });
                      }
                  }).start();
              }
              private void filter(String q) {
                  filtered.clear();
                  if (q.isEmpty()) { filtered.addAll(allApps); }
                  else { for (AppInfo a : allApps) if (a.appName.toLowerCase().contains(q.toLowerCase())) filtered.add(a); }
                  adapter.notifyDataSetChanged();
              }
              class AppAdapter extends RecyclerView.Adapter<AppAdapter.VH> {
                  class VH extends RecyclerView.ViewHolder {
                      ImageView ivIcon; TextView tvName, tvPkg; CheckBox cbSelect;
                      VH(View v) {
                          super(v);
                          ivIcon = v.findViewById(R.id.ivAppIcon);
                          tvName = v.findViewById(R.id.tvAppName);
                          tvPkg  = v.findViewById(R.id.tvAppPkg);
                          cbSelect = v.findViewById(R.id.cbSelect);
                      }
                  }
                  @Override public VH onCreateViewHolder(ViewGroup p, int t) {
                      return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_app, p, false));
                  }
                  @Override public void onBindViewHolder(VH h, int pos) {
                      AppInfo a = filtered.get(pos);
                      h.ivIcon.setImageDrawable(a.icon);
                      h.tvName.setText(a.appName);
                      h.tvPkg.setText(a.packageName);
                      h.cbSelect.setChecked(a.selected);
                      h.cbSelect.setOnClickListener(v -> a.selected = h.cbSelect.isChecked());
                      h.itemView.setOnClickListener(v -> { a.selected = !a.selected; h.cbSelect.setChecked(a.selected); });
                  }
                  @Override public int getItemCount() { return filtered.size(); }
              }
          }
          JAVAEOF

      - name: Fix unicode in Java files
        run: |
          cat > /tmp/fix.py << ENDOFSCRIPT
          import os, glob
          for path in glob.glob("app/src/**/*.java", recursive=True):
              with open(path, "r", encoding="utf-8", errors="ignore") as f:
                  content = f.read()
              new = content
              new = new.replace("\u201c", '\\"').replace("\u201d", '\\"')
              new = new.replace("\u2018", "'").replace("\u2019", "'")
              new = new.replace("\u2013", "-").replace("\u2014", "-")
              if new != content:
                  with open(path, "w", encoding="utf-8") as f:
                      f.write(new)
                  print("Fixed: " + path)
          print("Done")
          ENDOFSCRIPT
          python3 /tmp/fix.py

      - name: Verify Gradle version
        run: gradle --version

      - name: Build Debug APK
        run: gradle assembleDebug --stacktrace

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: FocusGuard-debug
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 30
