package com.example.teamet.light_app;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.util.Consumer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class P2pManager {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Context context;
    private boolean isEnabled;

    public P2pManager(Context context) {
        this.context = context;

        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(context, context.getMainLooper(), null);
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

   public void requestPeers(final Consumer<List<WifiP2pDevice>> peerListListener) {
        manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                showToast(successMsg("requestPeers"));
                final List<WifiP2pDevice> ps = new ArrayList<>(peers.getDeviceList());
                peerListListener.accept(ps);
            }
        } );
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void requestIPAddr(final Consumer<InetAddress> callback) {
        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                callback.accept( wifiP2pInfo.groupOwnerAddress );
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
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
