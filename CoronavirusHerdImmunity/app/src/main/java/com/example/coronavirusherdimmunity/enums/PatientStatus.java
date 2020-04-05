package com.example.coronavirusherdimmunity.enums;

import com.example.coronavirusherdimmunity.CovidApplication;
import com.example.coronavirusherdimmunity.R;

import java.util.HashMap;
import java.util.Map;

public enum PatientStatus {
    //{0: normal, 1: infected, 2: healed, 3: suspect, 4: lowRisk, 5: mediumRisk, 6: highRisk}
    NORMAL(0, R.string.status_normal, R.string.title_normal, R.string.description_normal, R.color.colorTextDark),
    INFECTED(1, R.string.status_infected, R.string.title_infected, R.string.description_infected, R.color.red),
    HEALED(2, R.string.status_healed, R.string.title_healed, R.string.description_healed, R.color.green),
    SUSPECT(3, R.string.status_suspect, R.string.title_suspect  , R.string.description_suspect, R.color.orange),
    LOW_RISK(4, R.string.status_infected, R.string.title_lowrisk, R.string.description_lowrisk, R.color.lightblue),
    MEDIUM_RISK(5, R.string.status_healed, R.string.title_mediumrisk, R.string.description_mediumrisk, R.color.yellow),
    HIGH_RISK(6, R.string.status_suspect, R.string.title_highrisk, R.string.description_highrisk, R.color.orange);

    private int intValue;
    private int stringValue;
    private int titleValue;
    private int descriptionValue;
    private int colorValue;
    private static Map<Integer, PatientStatus> map = new HashMap<Integer, PatientStatus>();
    static {
        for (PatientStatus enu : PatientStatus.values()) {
            map.put(enu.intValue, enu);
        }
    }

    private PatientStatus(int value, int toString, int title, int description, int color) {
        intValue = value;
        stringValue = toString;
        titleValue = title;
        descriptionValue = description;
        colorValue = color;
    }

    public static PatientStatus valueOf(int value) {
        return map.get(value);
    }

    public int toInt() {
        return intValue;
    }

    @Override
    public String toString() {
        return CovidApplication.getContext().getResources().getString(stringValue);
    }

    public String getTitle() {
        return CovidApplication.getContext().getResources().getString(titleValue);
    }

    public String getDescription() {
        return CovidApplication.getContext().getResources().getString(descriptionValue);
    }

    public int getColorValue() {
        return colorValue;
    }

    public int getColor() {
        return CovidApplication.getContext().getResources().getColor(colorValue);
    }

}