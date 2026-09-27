package com.najmspace.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.Settings;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class NajmSettingsActivity extends Activity {
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    private TextView heading(String s){
        TextView t=new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(21);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0,dp(14),0,dp(8));
        return t;
    }

    private Switch option(String label, boolean checked){
        Switch s=new Switch(this);
        s.setText(label);
        s.setTextColor(Color.WHITE);
        s.setTextSize(16);
        s.setGravity(Gravity.CENTER_VERTICAL);
        s.setPadding(dp(12),dp(8),dp(12),dp(8));
        s.setChecked(checked);
        return s;
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        SharedPreferences prefs=getSharedPreferences("najm",MODE_PRIVATE);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24),dp(18),dp(24),dp(18));
        root.setBackgroundColor(Color.rgb(13,18,28));

        TextView title=heading("إعدادات NAJM SPACE");
        title.setTextSize(28);
        title.setGravity(Gravity.RIGHT);
        root.addView(title);

        TextView version=new TextView(this);
        version.setText("V0.3  •  Android 4.4+");
        version.setTextColor(Color.rgb(140,160,185));
        version.setTextSize(14);
        root.addView(version);

        root.addView(heading("الواجهة"));

        Switch anim=option("تفعيل الأنميشن",prefs.getBoolean("animations",true));
        anim.setOnCheckedChangeListener((CompoundButton b1,boolean c)->prefs.edit().putBoolean("animations",c).apply());
        root.addView(anim);

        Switch dark=option("الوضع الداكن",prefs.getBoolean("dark",true));
        dark.setOnCheckedChangeListener((CompoundButton b1,boolean c)->prefs.edit().putBoolean("dark",c).apply());
        root.addView(dark);

        Switch clock=option("إظهار الساعة والتاريخ",prefs.getBoolean("clock",true));
        clock.setOnCheckedChangeListener((CompoundButton b1,boolean c)->prefs.edit().putBoolean("clock",c).apply());
        root.addView(clock);

        root.addView(heading("النظام"));

        TextView sys=new TextView(this);
        sys.setText("فتح إعدادات أندرويد");
        sys.setTextColor(Color.WHITE);
        sys.setTextSize(17);
        sys.setPadding(dp(14),dp(16),dp(14),dp(16));
        sys.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_SETTINGS)));
        root.addView(sys);

        TextView about=new TextView(this);
        about.setText("\nNajm Space يجمع الواجهة، درج التطبيقات، مركز التحكم وطبقة التوافق في مكان واحد.");
        about.setTextColor(Color.rgb(150,167,190));
        about.setTextSize(14);
        root.addView(about);

        setContentView(root);
        root.setAlpha(0f);
        root.animate().alpha(1f).setDuration(250).start();
    }
}
