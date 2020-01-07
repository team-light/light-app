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
import android.widget.Switch;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class P2pManager {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Activity activity;
    private boolean isEnabled;
    private List<String> peers = new ArrayList<>();

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
                showToast(successMsg("requestPeers"));

                final List<WifiP2pDevice> ps = new ArrayList<>(peers.getDeviceList());

                P2pManager.this.peers.clear();
                for (WifiP2pDevice p : ps) {
                    P2pManager.this.peers.add(p.deviceName + " (" + p.deviceAddress + ")");
                }

                ListView lv = ((ListView)activity.findViewById(R.id.listview1));
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = ps.get(position).deviceAddress;
                        connect(config);
                    }
                });
                ArrayAdapter adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, P2pManager.this.peers);
                lv.setAdapter(adapter);
            }
        } );
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        ((Switch)activity.findViewById(R.id.switch1)).setChecked(isEnabled);
    }

    public List<String> getPeers() {
        return peers;
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
