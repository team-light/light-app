package com.example.teamet.light_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SubActivity extends AppCompatActivity {
    private Button trans;
    private Button start;
    private TextView res;
    private TextView stat;
    private TextView ip;
    private TextView port;
    private Server sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        trans = findViewById(R.id.transition);
        start = findViewById(R.id.start);
        res = findViewById(R.id.result);
        stat = findViewById(R.id.status);
        ip = findViewById(R.id.ip);
        port = findViewById(R.id.port);

        trans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SubActivity.this, MainActivity.class));
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sv = new Server(ip, port, stat, res, SubActivity.this);
                sv.execute();
            }
        });
    }
}
