package com.najmspace.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CompatibilityActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(30,30,30,30);
        l.setBackgroundColor(Color.rgb(16,20,28));

        TextView t = new TextView(this);
        t.setText("YouTube Compatibility Mode");
        t.setTextColor(Color.WHITE);
        t.setTextSize(28);
        l.addView(t);

        Button x = new Button(this);
        x.setText("OPEN YOUTUBE MOBILE");
        x.setTextSize(22);
        x.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.youtube.com")));
            }
        });
        l.addView(x);

        TextView n = new TextView(this);
        n.setText("\nV0.1 uses the browser-compatible YouTube path. Deeper compatibility comes in later versions.");
        n.setTextColor(Color.LTGRAY);
        n.setTextSize(17);
        l.addView(n);

        setContentView(l);
    }
}
