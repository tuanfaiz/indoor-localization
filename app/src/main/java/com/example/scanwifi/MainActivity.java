package com.example.scanwifi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

//import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private int size = 0;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    EditText txtRoom;
    Button btnSave;
    String roomtxt;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonScan = findViewById(R.id.scanBtn);
        buttonScan.setOnClickListener(view -> scanWifi());

        btnSave = findViewById(R.id.btnSave);
        txtRoom = findViewById(R.id.txtRoom);

        btnSave.setOnClickListener(view -> {

            roomtxt = txtRoom.getText().toString().trim();
            if (roomtxt.isEmpty()){
                Toast.makeText(MainActivity.this, "Please enter room name...", Toast.LENGTH_SHORT).show();
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permissions, WRITE_EXTERNAL_STORAGE_CODE);
                    }
                    else {
                        saveToTxtFile(roomtxt);
                    }
                }
                else {
                    saveToTxtFile(roomtxt);
                }
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

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveToTxtFile(roomtxt);
            }
            else {
                Toast.makeText(this, "Storage permission is required to store data", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void saveToTxtFile(String roomtxt) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());

        try {

            File path = Environment.getExternalStorageDirectory();
            File dir = new File(path + "/My Files/");
            dir.mkdirs();
            String fileName = "RoomFile_" + timeStamp + ".txt";

            File file = new File(dir, fileName);

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(roomtxt);
            bw.close();

            Toast.makeText(this, " is saved to \n" + dir, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
            for (ScanResult scanResult : results) {
                arrayList.add(scanResult.SSID + " - " + scanResult.level);
                adapter.notifyDataSetChanged();
            }
        }
    };
}