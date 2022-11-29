package com.example.scanwifi;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private String url = "http://" + "10.0.2.2" + ":" + 5000 + "/";
    private String postBodyString;
    private MediaType mediaType;
    private RequestBody requestBody;
    private Button connect;

    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private int size = 0;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    private String POST="POST";
    private String GET="GET";

    EditText txtRoom;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonScan = findViewById(R.id.scanBtn);
        connect = findViewById(R.id.connect);
        buttonScan.setOnClickListener(view -> scanWifi());
        txtRoom = findViewById(R.id.txtRoom);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postRequest("your message here", url);

                int x = info.getIndex();

                System.out.println(x);

                ArrayList<String> tempName = new ArrayList<>();
                tempName = info.getName();
                for (int l = 0; l <= x; l++) {
                    System.out.println(tempName.get(l));
                }

                postRequest(String.valueOf(tempName), url);

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

    private RequestBody buildRequestBody(String msg) {
        postBodyString = msg;
        mediaType = MediaType.parse("text/plain");
        requestBody = RequestBody.create(postBodyString, mediaType);
        return requestBody;
    }

    private void postRequest(String message, String URL) {
        RequestBody requestBody = buildRequestBody(message);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().post(requestBody).url(URL).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        Toast.makeText(MainActivity.this, "Something went wrong:" + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        call.cancel();


                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(MainActivity.this, response.body().string(), Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
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

