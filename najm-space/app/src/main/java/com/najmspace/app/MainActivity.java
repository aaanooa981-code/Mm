package com.najmspace.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView clock;
    private final Handler handler = new Handler();
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (clock != null) clock.setText(new SimpleDateFormat("HH:mm", Locale.US).format(new Date()));
            handler.postDelayed(this, 1000);
        }
    };

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(20);
        b.setMinHeight(84);
        return b;
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 16, 24, 16);
        root.setBackgroundColor(Color.rgb(16, 20, 28));

        clock = new TextView(this);
        clock.setTextColor(Color.WHITE);
        clock.setTextSize(42);
        root.addView(clock);

        TextView title = new TextView(this);
        title.setText("NAJM SPACE   •   Android 4.4+");
        title.setTextColor(Color.LTGRAY);
        title.setTextSize(18);
        root.addView(title);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        Button youtube = makeButton("YouTube");
        youtube.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(MainActivity.this, CompatibilityActivity.class)); }
        });
        row.addView(youtube, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button apps = makeButton("Apps");
        apps.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(MainActivity.this, AppsActivity.class)); }
        });
        row.addView(apps, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button control = makeButton("Control");
        control.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(MainActivity.this, ControlCenterActivity.class)); }
        });
        row.addView(control, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button browser = makeButton("Browser");
        browser.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))); }
        });
        row.addView(browser, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        root.addView(row);

        TextView note = new TextView(this);
        note.setText("\nNajm Space V0.1 — lightweight car launcher and compatibility shell");
        note.setTextColor(Color.GRAY);
        note.setTextSize(16);
        root.addView(note);

        setContentView(root);
    }

    @Override protected void onResume() { super.onResume(); handler.post(tick); }
    @Override protected void onPause() { handler.removeCallbacks(tick); super.onPause(); }
}
