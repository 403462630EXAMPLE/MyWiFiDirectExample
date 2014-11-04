package com.mynfc;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class WiFiDrectActivity extends Activity implements ListView.OnItemClickListener {

    private final IntentFilter intentFilter = new IntentFilter();
    public WifiP2pManager wifiP2pManager;
    public WifiP2pManager.Channel channel;
    private ListView listView;
    private TextView myNameView;
    private TextView myStatusView;
    private LinearLayout container;
    private TextView ipView;
    private TextView isOwnView;
    private Button disconnect;
    private Button send;

    private WiFiP2pDeviceAdapter adapter;
    private WiFiDirectBroadcastReceiver receiver;


    public void setIsWiFiP2pEnabled(boolean isWiFiP2pEnabled) {
        this.isWiFiP2pEnabled = isWiFiP2pEnabled;
    }

    public boolean isWiFiP2pEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_drect);
        listView = (ListView) findViewById(R.id.list_view);
        myNameView = (TextView) findViewById(R.id.my_name);
        myStatusView = (TextView) findViewById(R.id.my_status);

        ipView = (TextView) findViewById(R.id.ip);
        isOwnView = (TextView) findViewById(R.id.is_own);
        container = (LinearLayout) findViewById(R.id.container);
        disconnect = (Button) findViewById(R.id.disconnect);
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDrectActivity.this, "断开连接成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(WiFiDrectActivity.this, "断开连接失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        adapter = new WiFiP2pDeviceAdapter(this, R.layout.list_item);
        receiver = new WiFiDirectBroadcastReceiver(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wifiP2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
    }

    public void setThisDevice(WifiP2pDevice wifiP2pDevice) {
        myNameView.setText(wifiP2pDevice.deviceName);
        myStatusView.setText(getDeviceStatus(wifiP2pDevice.status));
    }

    public void requestPeers() {
        if (wifiP2pManager == null) {
            return ;
        }
        wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                ArrayList<WifiP2pDevice> list = new ArrayList<WifiP2pDevice>();
                list.addAll(peers.getDeviceList());
                adapter.clear();
                adapter.addAll(list);
            }
        });
    }

    public void requestConnectionInfo(boolean isConnected) {
        if (isConnected) {
            container.setVisibility(View.VISIBLE);
            wifiP2pManager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo info) {
                    ipView.setText("ip:" + info.groupOwnerAddress);
                    if (info.groupFormed && info.isGroupOwner) {
                        isOwnView.setText("own group:yes");
                    } else {
                        isOwnView.setText("own group:no");
                    }
                }
            });
        } else {
            container.setVisibility(View.GONE);
        }

    }

//    public void resetData() {
//        adapter.clear();
//    }

    public static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wi_fi_drect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.direct_discover) {
            if (isWiFiP2pEnabled) {
                wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDrectActivity.this, "discoverPeers success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(WiFiDrectActivity.this, "discoverPeers failure", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "不支持Wifi direct功能", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        connect(adapter.getItem(position));
    }

    public void connect(WifiP2pDevice wifiP2pDevice) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiP2pDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WiFiDrectActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDrectActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
