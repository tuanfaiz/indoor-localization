package com.example.scanwifi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.PrintWriter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.gson.*;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private Button connect;

    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private int size = 0;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    private OkHttpClient okHttpClient;

    EditText txtRoom;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonScan = findViewById(R.id.scanBtn);
        connect = findViewById(R.id.connect);
        txtRoom = findViewById(R.id.txtRoom);

        buttonScan.setOnClickListener(view -> scanWifi());
        okHttpClient = new OkHttpClient();
        JsonObject jsonObj = new JsonObject();

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String roomtxt = txtRoom.getText().toString();
                int x = info.getIndex();

                ArrayList<String> tempName = new ArrayList<>();
                ArrayList<Double> tempStrength = new ArrayList<>();
                tempName = info.getName();
                tempStrength = info.getStr();

                JsonArray jsonWifi = new Gson().toJsonTree(tempName).getAsJsonArray();
                JsonArray jsonStrength = new Gson().toJsonTree(tempStrength).getAsJsonArray();
                jsonObj.add("Wifi Name", jsonWifi);
                jsonObj.add("Strength", jsonStrength);

                RequestBody formbody = new FormBody.Builder().add("Output", String.valueOf(jsonObj)).build();

                try (FileWriter out = new FileWriter("C:/Users/tuanf/AndroidStudioProjects/json/wifi.json")) {
                    out.write(jsonObj.toString());
                    out.flush();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(jsonObj);

                Request request = new Request.Builder().url("http://192.168.1.9:5000/debug").post(formbody).build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "server down", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                        if(response.body().toString().equals(roomtxt)){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    response.close();
                                    Toast.makeText(getApplicationContext(), "data received", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

            }
        });

        listView = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if(!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wi-Fi is disabled ... You need to enabled it", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
        scanWifi();

    }

    private void scanWifi() {
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning Wi-Fi", Toast.LENGTH_SHORT).show();
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);

            List<ScanResult> results = wifiManager.getScanResults();
            ArrayList<String> WifiName = new ArrayList<String>();
            ArrayList<Double> WifiStr = new ArrayList<Double>();
            int i = 0;

            for (ScanResult scanResult : results) {
                arrayList.add(scanResult.SSID + " - " + scanResult.level);

                WifiName.add(scanResult.SSID);
                WifiStr.add(Double.valueOf(scanResult.level));
                adapter.notifyDataSetChanged();
                i++;
            }
            new info(WifiName, WifiStr, (i-1));

        }
    };
}

