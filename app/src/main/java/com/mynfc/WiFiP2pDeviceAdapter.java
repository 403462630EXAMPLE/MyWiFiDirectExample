package com.mynfc;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by rjhy on 14-10-31.
 */
public class WiFiP2pDeviceAdapter extends ArrayAdapter<WifiP2pDevice> {
    private Context context;
    public WiFiP2pDeviceAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            holder = new Holder();
            holder.nameText = (TextView) convertView.findViewById(R.id.name);
            holder.statusText = (TextView) convertView.findViewById(R.id.status);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.nameText.setText(getItem(position).deviceName);
        holder.statusText.setText(WiFiDrectActivity.getDeviceStatus(getItem(position).status));
        return convertView;
    }

    @Override
    public WifiP2pDevice getItem(int position) {
        return super.getItem(position);
    }

    class Holder {
        public TextView nameText;
        public TextView statusText;
    }
}
