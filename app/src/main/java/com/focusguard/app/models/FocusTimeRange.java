package com.focusguard.app.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FocusTimeRange implements Serializable {
    public int id;
    public String label;
    public int startHour;
    public int startMinute;
    public int endHour;
    public int endMinute;
    public List<Integer> days; // 0=Sun,1=Mon,2=Tue,3=Wed,4=Thu,5=Fri,6=Sat
    public boolean enabled;

    public FocusTimeRange() {
        days = new ArrayList<>();
        enabled = true;
    }

    public boolean isActive() {
        if (!enabled) return false;
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (!days.contains(today)) return false;
        int nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        int startMin = startHour * 60 + startMinute;
        int endMin = endHour * 60 + endMinute;
        if (endMin <= startMin) { // overnight span
            return nowMin >= startMin || nowMin < endMin;
        }
        return nowMin >= startMin && nowMin < endMin;
    }

    public String getTimeString() {
        return String.format("%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute);
    }

    public String getDaysString() {
        String[] names = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        if (days.size() == 7) return "Every day";
        if (days.isEmpty()) return "No days";
        StringBuilder sb = new StringBuilder();
        for (int d : days) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(names[d]);
        }
        return sb.toString();
    }
}
