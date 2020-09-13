package com.example.teamet.light_app.network;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.util.Consumer;
import android.util.Log;
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

    public void discoverPeers(Runnable then) {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener () {
            @Override
            public void onSuccess() {
                Log.v("P2pManager", successMsg("discoverPeers"));
                then.run();
            }

            @Override
            public void onFailure(int reason) {
                Log.v("P2pManager", failureMsg("discoverPeers"));
                then.run();
            }
        });
    }

    public void stopPeerDiscovery(Runnable then) {
        manager.stopPeerDiscovery(channel,  new WifiP2pManager.ActionListener () {
            @Override
            public void onSuccess() {
                Log.v("P2pManager", successMsg("stopPeerDiscovery"));
                then.run();
            }

            @Override
            public void onFailure(int reason) {
                Log.v("P2pManager", failureMsg("stopPeerDiscovery"));
                then.run();
            }
        });
    }

    public void connect(WifiP2pConfig config, final Runnable then) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v("P2pManager", successMsg("Connect"));
                then.run();
            }

            @Override
            public void onFailure(int reason) {
                Log.v("P2pManager", failureMsg("Connect"));
                then.run();
            }
        } );
    }

    public void disconnectAll(final Runnable then) {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v("P2pManager", successMsg("disconnectAll"));
                then.run();
            }

            @Override
            public void onFailure(int i) {
                Log.v("P2pManager", failureMsg("disconnectAll"));
                then.run();
            }
        });
    }

   public void requestPeers(final Consumer<List<WifiP2pDevice>> peerListListener) {
        manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                Log.v("P2pManager", successMsg("requestPeers"));
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
                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.accept( wifiP2pInfo.groupOwnerAddress );
                    }
                })).start();
            }
        });
    }

    // エラーメッセージ生成
    private String failureMsg(String fnName) {
        return fnName + "(...) failure.";
    }

    // 成功メッセージ生成
    private String successMsg(String fnName) {
        return fnName + "(...) success.";
    }

    public void requestIsGroupOwner(final Consumer<Boolean> consumer) {
        manager.requestGroupInfo(channel, wifiP2pGroup -> {
            if (wifiP2pGroup == null) {
                Log.v("P2pManager", "wifiP2pGroup == null");
            }
            else {
                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        consumer.accept(wifiP2pGroup.isGroupOwner());
                    }
                })).start();
            }
        });
    }
}
