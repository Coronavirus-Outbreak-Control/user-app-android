package com.example.coronavirusherdimmunity.enums;

import com.example.coronavirusherdimmunity.CovidApplication;
import com.example.coronavirusherdimmunity.R;

import java.util.HashMap;
import java.util.Map;

public enum ApplicationStatus {

    ACTIVE(R.string.status_active, R.color.green, 0),
    INACTIVE(R.string.status_inactive, R.color.red, 1);

    private int intValue;
    private int colorValue;
    private int stringValue;

    private static Map<Integer, ApplicationStatus> map = new HashMap<Integer, ApplicationStatus>();
    static {
        for (ApplicationStatus enu : ApplicationStatus.values()) {
            map.put(enu.intValue, enu);
        }
    }

    private ApplicationStatus(int toString, int color, int value) {
        stringValue = toString;
        colorValue = color;
        intValue = value;
    }

    public int toInt() {
        return intValue;
    }

    @Override
    public String toString() {
        return CovidApplication.getContext().getResources().getString(stringValue);
    }

    public int getColorValue() {
        return colorValue;
    }

    public int getColor() {
        return CovidApplication.getContext().getResources().getColor(colorValue);
    }

    public static ApplicationStatus valueOf(int value) {
        return map.get(value);
    }

}
