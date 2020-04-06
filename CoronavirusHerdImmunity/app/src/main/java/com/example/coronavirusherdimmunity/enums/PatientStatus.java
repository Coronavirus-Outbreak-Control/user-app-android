package com.example.coronavirusherdimmunity.enums;

import com.example.coronavirusherdimmunity.CovidApplication;
import com.example.coronavirusherdimmunity.R;

import java.util.HashMap;
import java.util.Map;

public enum PatientStatus {
    //{0: normal, 1: infected, 2:suspect, 3: healed, 4: quarantine_light, 5: quarantine_warning, 6:quarantine_alert}
    NORMAL(R.string.status_no_risk, R.color.colorTextDark, 0),
    INFECTED(R.string.status_infected, R.color.green, 1),
    SUSPECT(R.string.status_suspect, R.color.yellow, 2),
    HEALED(R.string.status_healed, R.color.orange, 3),
    QUARANTINE(R.string.status_quarantine, R.color.red, 4),
    QUARANTINE_WARNING(R.string.status_quarantine, R.color.red, 5),// TODO: 06/04/2020 Update status string
    QUARANTINE_ALERT(R.string.status_quarantine, R.color.red, 6)
    ;

    private int intValue;
    private int colorValue;
    private int stringValue;
    private static Map<Integer, PatientStatus> map = new HashMap<Integer, PatientStatus>();
    static {
        for (PatientStatus enu : PatientStatus.values()) {
            map.put(enu.intValue, enu);
        }
    }

    private PatientStatus(int toString, int color, int value) {
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

    public static PatientStatus valueOf(int value) {
        return map.get(value);
    }
}