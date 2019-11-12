package com.example.teamet.light_app;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.TextView;

public class P2pManager {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Activity activity;
    private boolean isEnabled;

    public P2pManager(Activity activity) {
        this.activity = activity;

        manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(activity, activity.getMainLooper(), null);
    }

    public void discoverPeers() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener () {
            @Override
            public void onSuccess() {
                Toast.makeText(activity, "discoverPeers success.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(activity, "discoverPeers failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void stopPeerDiscovery() {
        manager.stopPeerDiscovery(channel,  new WifiP2pManager.ActionListener () {
            @Override
            public void onSuccess() {
                Toast.makeText(activity, "stopPeerDiscovery success.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(activity, "stopPeerDiscovery failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(activity, "Connect success.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(activity, "Connect failed.", Toast.LENGTH_SHORT).show();
            }
        } );
    }

   public void requestPeers() {
        manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                TextView tv = activity.findViewById(R.id.debugText);
                tv.setText(peers.toString());
            }
        });
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        ((Switch)activity.findViewById(R.id.switch1)).setChecked(isEnabled);
    }
}
