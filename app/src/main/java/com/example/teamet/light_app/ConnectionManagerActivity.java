package com.example.teamet.light_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.util.Consumer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManagerActivity extends AppCompatActivity {
    final IntentFilter intentFilter = new IntentFilter();
    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // onWifiP2pEnabled();
                }
                else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                    // onWifiP2pDisabled();
                }

                Toast.makeText(ConnectionManagerActivity.this, "P2P state changed : " + state, Toast.LENGTH_SHORT).show();
            }
        }
    };

    P2pManager pm;

    private List<String> peers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_manager);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        pm = new P2pManager(this);

        final ListView lv = (ListView)findViewById(R.id.listview1);

        Switch switchButton = findViewById(R.id.switch1);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pm.discoverPeers();
                }
                else {
                    pm.stopPeerDiscovery();
                }
            }
        });

        ((Button)findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pm.requestPeers(new Consumer<List<WifiP2pDevice>>() {
                    @Override
                    public void accept(final List<WifiP2pDevice> ps) {
                        peers.clear();
                        for (WifiP2pDevice p : ps) {
                            peers.add(p.deviceName + " (" + p.deviceAddress + ")");
                        }

                        ListView lv = ((ListView)findViewById(R.id.listview1));
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                WifiP2pConfig config = new WifiP2pConfig();
                                config.deviceAddress = ps.get(position).deviceAddress;
                                pm.connect(config);
                            }
                        });
                        ArrayAdapter adapter = new ArrayAdapter<>(ConnectionManagerActivity.this, android.R.layout.simple_list_item_1, peers);
                        lv.setAdapter(adapter);
                    }
                });
                pm.requestIPAddr(new Consumer<InetAddress>() {
                    @Override
                    public void accept(InetAddress inetAddress) {
                        TextView tv = (TextView) findViewById(R.id.textView);
                        if (inetAddress == null) {
                            tv.setText("Unknown owner IP address");
                        }
                        else {
                            tv.setText(inetAddress.toString());
                        }
                    }
                });
            }
        });

        List<String> init = new ArrayList<String>();
        // init.add("あいうえお");
        lv.setAdapter( new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, init ) );


    }

    private void onStartDiscovery() {
        pm.setIsEnabled(true);
    }

    private void onStopDiscovery() {
        pm.setIsEnabled(false);
    }

    private void setIsEnabled(boolean isEnabled) {
        pm.setIsEnabled(isEnabled);
        ((Switch)this.findViewById(R.id.switch1)).setChecked(isEnabled);
    }

    public void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
