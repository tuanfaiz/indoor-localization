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
import java.util.HashMap;
import java.util.List;
import java.io.FileWriter;
import java.io.PrintWriter;
import okhttp3.OkHttpClient;
import com.google.gson.*;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private int size = 0;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    private Button connect;
    private OkHttpClient okHttpClient;

    info wifiInfo = new info();
    EditText txtRoom;
    EditText coordinates;
    EditText notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Declare related component
        buttonScan = findViewById(R.id.scanBtn);
        connect = findViewById(R.id.connect);
        txtRoom = findViewById(R.id.txtRoom);
        coordinates = findViewById(R.id.txtCoordinates);

        okHttpClient = new OkHttpClient();
        JsonObject jsonObj = new JsonObject();

        //Set listener to each related button
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
            }
        });

        listView = findViewById(R.id.wifiList);
        wifiManager = (WifiManager)
                getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    sendData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Check if wifi available
        if(!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wi-Fi is disabled ... You need to enabled it", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
    }

    private void scanWifi() {
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }

    private void sendData() throws IOException {
        Toast.makeText(this, "Sending Wi-Fi", Toast.LENGTH_SHORT).show();

        Gson gson = new Gson();
        HttpMethods httpMethods = new HttpMethods();

        String json = gson.toJson(wifiInfo.getWifiData());
        String response = httpMethods.post("http://192.168.1.9:5000/post", json);
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
        ArrayList<String> ssid = new ArrayList<>();
        ArrayList<String> bssid = new ArrayList<>();
        ArrayList<Integer> level = new ArrayList<>();
        ArrayList<String> name = new ArrayList<>();
        ArrayList<String> cords = new ArrayList<>();
        ArrayList<String> note = new ArrayList<>();


        txtRoom = (EditText) findViewById(R.id.txtRoom);
        coordinates = (EditText) findViewById(R.id.txtCoordinates);
        notes = (EditText) findViewById(R.id.txtNotes);
        String room_name = txtRoom.getText().toString();
        String croom = coordinates.getText().toString();
        String nota = notes.getText().toString();

        for (ScanResult scanResult : results) {
            arrayList.add(scanResult.SSID + " - " + scanResult.level);
            adapter.notifyDataSetChanged();
        }

        for (int i = 0; i < results.size(); i++) {
            name.add(room_name);
            ssid.add(results.get(i).SSID);
            bssid.add(results.get(i).BSSID);
            level.add(results.get(i).level);
            cords.add(croom);
            note.add(nota);
        }

        wifiData.put("NAME", name);
        wifiData.put("SSID", ssid);
        wifiData.put("BSSID", bssid);
        wifiData.put("LEVEL", level);
        wifiData.put("COORDINATES", cords);
        wifiData.put("NOTES", note);
        wifiInfo.setWifiData(wifiData);
        System.out.println(wifiData);

    }
}

