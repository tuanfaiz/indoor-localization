package com.example.scanwifi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    info wifiInfo = new info();

    private Button connect;
    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    EditText txtRoom;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Declare related component
        buttonScan = findViewById(R.id.scanBtn);
        connect = findViewById(R.id.connect);
        listView = findViewById(R.id.wifiList);

        // set listener to each related button
        buttonScan.setOnClickListener(view -> scanWifi());
        connect.setOnClickListener(view -> {
            try {
                sendData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // check if wifi available
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wi-Fi is disabled ... You need to enabled it", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }
    }

    private void scanWifi() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            System.out.println("Please check permission.");
        }

        Toast.makeText(this, "Scanning Wi-Fi", Toast.LENGTH_SHORT).show();
    }

    private void sendData() throws IOException {
        Toast.makeText(this, "Sending Wi-Fi", Toast.LENGTH_SHORT).show();

        Gson gson = new Gson();
        HttpMethods httpMethods = new HttpMethods();


        String json = gson.toJson(wifiInfo.getWifiData());
        String response = httpMethods.post("http://192.168.0.180:5000/post", json);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("Stopping receiver");
        unregisterReceiver(wifiReceiver);
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                scanSuccess();
            }
        }
    };

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();

        HashMap<String, ArrayList> wifiData = new HashMap<String, ArrayList>();

        ArrayList<String> roomName = new ArrayList<String>();
        ArrayList<String> bssid = new ArrayList<String>();
        ArrayList<Integer> level = new ArrayList<Integer>();
        for (int i = 0; i < results.size(); i++) {
            bssid.add(results.get(i).BSSID);
            level.add(results.get(i).level);
        }

        wifiData.put("BSSID", bssid);
        wifiData.put("LEVEL", level);
        wifiInfo.setWifiData(wifiData);
    }
}

