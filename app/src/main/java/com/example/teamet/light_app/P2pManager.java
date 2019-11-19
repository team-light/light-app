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
                showToast(successMsg("discoverPeers"));
            }

            @Override
            public void onFailure(int reason) {
                showToast(failureMsg("discoverPeers"));
            }
        });
    }

    public void stopPeerDiscovery() {
        manager.stopPeerDiscovery(channel,  new WifiP2pManager.ActionListener () {
            @Override
            public void onSuccess() {
                showToast(successMsg("stopPeerDiscovery"));
            }

            @Override
            public void onFailure(int reason) {
                showToast(failureMsg("stopPeerDiscovery"));
            }
        });
    }

    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                showToast(successMsg("Connect"));
            }

            @Override
            public void onFailure(int reason) {
                showToast(failureMsg("Connect"));
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
        } );
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        ((Switch)activity.findViewById(R.id.switch1)).setChecked(isEnabled);
    }

    private void showToast(String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }

    // エラーメッセージ生成
    private String failureMsg(String fnName) {
        return fnName + "(...) failure.";
    }

    // 成功メッセージ生成
    private String successMsg(String fnName) {
        return fnName + "(...) success.";
    }
}
