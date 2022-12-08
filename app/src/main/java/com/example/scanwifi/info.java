package com.example.scanwifi;

import java.util.ArrayList;
import java.util.HashMap;

public class info {
    private HashMap<String, ArrayList> wifiData;
    private static ArrayList<String> name;
    private static ArrayList<Double> strength;
    private static int index;

    public HashMap<String, ArrayList> getWifiData() {
        return wifiData;
    }

    public void setWifiData(HashMap<String, ArrayList> wifiData) {
        this.wifiData = wifiData;
    }

    public static ArrayList<Double> getStr() {
        return strength;
    }

    public static ArrayList<String> getName() {
        return name;
    }

    public static int getIndex() {
        return index;
    }
}
