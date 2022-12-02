package com.example.scanwifi;

import java.util.ArrayList;

public class info {
    private static ArrayList<String> name;
    private static ArrayList<Double> strength;
    private static int index;


    public info(ArrayList<String> wifiName, ArrayList<Double> wifiStr, int i) {

        name = wifiName;
        strength = wifiStr;
        index = i;

    }

    public void setName(String n, int in){
        name.set(in, n);
    }

    public void setStr(double s, int in){
        strength.set(in, s);
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
