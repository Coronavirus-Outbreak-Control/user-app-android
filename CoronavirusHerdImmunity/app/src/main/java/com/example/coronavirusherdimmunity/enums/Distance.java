package com.example.coronavirusherdimmunity.enums;

import java.util.HashMap;
import java.util.Map;

public enum Distance {

    IMMEDIATE("i", 0),
    NEAR("n", 1),
    FAR("f", 2);

    private int intValue;
    private String stringValue;
    private static Map<Integer, Distance> map = new HashMap<Integer, Distance>();

    static {
        for (Distance enu : Distance.values()) {
            map.put(enu.intValue, enu);
        }
    }

    private Distance(String toString, int value) {
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

    public static Distance valueOf(int value) {
        return map.get(value);
    }
}
