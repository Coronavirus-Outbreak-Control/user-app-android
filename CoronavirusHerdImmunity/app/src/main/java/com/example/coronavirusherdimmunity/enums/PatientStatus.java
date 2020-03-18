package com.example.coronavirusherdimmunity.enums;

import java.util.HashMap;
import java.util.Map;

public enum PatientStatus {
    //{0: normal, 1: infected, 2: quarantine, 3: healed, 4: suspect}

    NORMAL("Active", 0),
    INFECTED("Infected", 1),
    QUARANTINE("Quarantine", 2),
    HEALED("Healed", 3),
    SUSPECT("Suspect", 4);

    private int intValue;
    private String stringValue;
    private static Map<Integer, PatientStatus> map = new HashMap<Integer, PatientStatus>();

    static {
        for (PatientStatus enu : PatientStatus.values()) {
            map.put(enu.intValue, enu);
        }
    }

    private PatientStatus(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    public int toInt() {
        return intValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }

    public static PatientStatus valueOf(int value) {
        return map.get(value);
    }
}
