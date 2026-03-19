package com.focusguard.app;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.focusguard.app.models.FocusTimeRange;
import com.focusguard.app.utils.PrefsManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FocusTimeActivity extends AppCompatActivity {

    private PrefsManager prefs;
    private List<FocusTimeRange> ranges;
    private RangeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_time);
        prefs = new PrefsManager(this);
        ranges = prefs.getFocusRanges();

        RecyclerView rv = findViewById(R.id.rvRanges);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RangeAdapter();
        rv.setAdapter(adapter);

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            if (ranges.size() >= 10) {
                Toast.makeText(this, "Maximum 10 focus ranges allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddEditDialog(null);
        });
    }

    private void save() {
        prefs.saveFocusRanges(ranges);
        adapter.notifyDataSetChanged();
    }

    private void showAddEditDialog(FocusTimeRange existing) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_focus_time, null);
        EditText etLabel = v.findViewById(R.id.etLabel);
        TextView tvStart = v.findViewById(R.id.tvStartTime);
        TextView tvEnd   = v.findViewById(R.id.tvEndTime);
        CheckBox[] dayCbs = {
            v.findViewById(R.id.cbSun), v.findViewById(R.id.cbMon),
            v.findViewById(R.id.cbTue), v.findViewById(R.id.cbWed),
            v.findViewById(R.id.cbThu), v.findViewById(R.id.cbFri),
            v.findViewById(R.id.cbSat)
        };
        Switch swEnabled = v.findViewById(R.id.swEnabled);

        final int[] sh = {9}, sm = {0}, eh = {17}, em = {0};

        if (existing != null) {
            etLabel.setText(existing.label);
            sh[0] = existing.startHour; sm[0] = existing.startMinute;
            eh[0] = existing.endHour;   em[0] = existing.endMinute;
            for (int d : existing.days) dayCbs[d].setChecked(true);
            swEnabled.setChecked(existing.enabled);
        } else {
            // default: Mon-Fri
            for (int d : new int[]{1,2,3,4,5}) dayCbs[d].setChecked(true);
            swEnabled.setChecked(true);
        }

        Runnable updateLabels = () -> {
            tvStart.setText(String.format("Start: %02d:%02d", sh[0], sm[0]));
            tvEnd.setText(String.format("End:   %02d:%02d", eh[0], em[0]));
        };
        updateLabels.run();

        tvStart.setOnClickListener(x -> new TimePickerDialog(this, (tp, h, min) -> {
            sh[0] = h; sm[0] = min; updateLabels.run();
        }, sh[0], sm[0], true).show());

        tvEnd.setOnClickListener(x -> new TimePickerDialog(this, (tp, h, min) -> {
            eh[0] = h; em[0] = min; updateLabels.run();
        }, eh[0], em[0], true).show());

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Add Focus Range" : "Edit Focus Range")
                .setView(v)
                .setPositiveButton("Save", (d, w) -> {
                    String label = etLabel.getText().toString().trim();
                    if (label.isEmpty()) label = "Focus Session";
                    FocusTimeRange r = existing != null ? existing : new FocusTimeRange();
                    if (existing == null) r.id = prefs.nextRangeId();
                    r.label = label;
                    r.startHour = sh[0]; r.startMinute = sm[0];
                    r.endHour   = eh[0]; r.endMinute   = em[0];
                    r.days.clear();
                    for (int i = 0; i < 7; i++) if (dayCbs[i].isChecked()) r.days.add(i);
                    r.enabled = swEnabled.isChecked();
                    if (existing == null) ranges.add(r);
                    save();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    class RangeAdapter extends RecyclerView.Adapter<RangeAdapter.VH> {
        class VH extends RecyclerView.ViewHolder {
            TextView tvLabel, tvTime, tvDays;
            Switch swEnabled;
            ImageButton btnEdit, btnDelete;
            VH(View v) {
                super(v);
                tvLabel  = v.findViewById(R.id.tvRangeLabel);
                tvTime   = v.findViewById(R.id.tvRangeTime);
                tvDays   = v.findViewById(R.id.tvRangeDays);
                swEnabled= v.findViewById(R.id.swRangeEnabled);
                btnEdit  = v.findViewById(R.id.btnEdit);
                btnDelete= v.findViewById(R.id.btnDelete);
            }
        }
        @Override public VH onCreateViewHolder(ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_focus_time, p, false));
        }
        @Override public void onBindViewHolder(VH h, int pos) {
            FocusTimeRange r = ranges.get(pos);
            h.tvLabel.setText((r.isActive() ? "🟢 " : "") + r.label);
            h.tvTime.setText(r.getTimeString());
            h.tvDays.setText(r.getDaysString());
            h.swEnabled.setChecked(r.enabled);
            h.swEnabled.setOnCheckedChangeListener((b, v) -> { r.enabled = v; save(); });
            h.btnEdit.setOnClickListener(v -> showAddEditDialog(r));
            h.btnDelete.setOnClickListener(v ->
                    new AlertDialog.Builder(FocusTimeActivity.this)
                        .setTitle("Delete Range")
                        .setMessage("Delete: " + r.label)
                        .setPositiveButton("Delete", (d, w) -> { ranges.remove(pos); save(); })
                        .setNegativeButton("Cancel", null).show());
        }
        @Override public int getItemCount() { return ranges.size(); }
    }
}
