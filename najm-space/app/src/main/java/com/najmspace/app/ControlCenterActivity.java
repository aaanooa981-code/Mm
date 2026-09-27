package com.najmspace.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ControlCenterActivity extends Activity {
    private Button makeButton(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(21);
        return b;
    }

    @Override public void onCreate(Bundle z) {
        super.onCreate(z);
        LinearLayout l = new LinearLayout(this);
        l.setPadding(30,30,30,30);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(Color.rgb(16,20,28));

        TextView t = new TextView(this);
        t.setText("Control Center");
        t.setTextSize(30);
        t.setTextColor(Color.WHITE);
        l.addView(t);

        Button wifi = makeButton("Wi-Fi Settings");
        wifi.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); }
        });
        l.addView(wifi);

        Button bt = makeButton("Bluetooth Settings");
        bt.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)); }
        });
        l.addView(bt);

        Button display = makeButton("Display Settings");
        display.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS)); }
        });
        l.addView(display);

        setContentView(l);
    }
}
