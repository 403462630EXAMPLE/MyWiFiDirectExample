package com.mynfc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by rjhy on 14-10-31.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WiFiDrectActivity activity;

    public WiFiDirectBroadcastReceiver(WiFiDrectActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                activity.setIsWiFiP2pEnabled(true);
            } else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                activity.setIsWiFiP2pEnabled(false);
            }

        } else if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
            activity.requestPeers();
        } else if (action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
//            if () {
                activity.requestConnectionInfo(networkInfo.isConnected());
//            } else {
////                activity.re
//            }
        } else if (action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)) {
            activity.setThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }
}
