package com.najmspace.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

public class SplashActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(7,14,28));
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.mipmap.ic_launcher);
        box.addView(icon, new LinearLayout.LayoutParams(180,180));

        TextView title = new TextView(this);
        title.setText("NAJM SPACE");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        box.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Private App Space • Smart Car Experience");
        sub.setTextColor(Color.rgb(120,175,235));
        sub.setTextSize(15);
        sub.setGravity(Gravity.CENTER);
        box.addView(sub);

        root.addView(box, new FrameLayout.LayoutParams(-1,-1));
        setContentView(root);

        icon.setScaleX(.55f); icon.setScaleY(.55f); icon.setAlpha(0f);
        title.setAlpha(0f); sub.setAlpha(0f);
        icon.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(650).start();
        title.animate().alpha(1f).setStartDelay(250).setDuration(450).start();
        sub.animate().alpha(1f).setStartDelay(420).setDuration(400).start();

        new Handler().postDelayed(new Runnable(){
            @Override public void run(){
                boolean done=getSharedPreferences("najmspace",MODE_PRIVATE).getBoolean("permissions_intro_done",false);
                startActivity(new Intent(SplashActivity.this, done ? MainActivity.class : PermissionActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        },1200);
    }
}
