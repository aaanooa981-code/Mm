package com.najmspace.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class NajmRecentsActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(7,15,27));
        root.setPadding(16,12,16,10);

        TextView title=new TextView(this);
        title.setText("التطبيقات المفتوحة داخل Najm Space");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        root.addView(title);

        TextView hint=new TextView(this);
        hint.setText("اضغط للعودة إلى المساحة أو افتح أحد أقسام Najm Space.");
        hint.setTextColor(Color.rgb(165,185,210));
        hint.setTextSize(14);
        hint.setPadding(0,6,0,10);
        root.addView(hint);

        ScrollView scroll=new ScrollView(this);
        LinearLayout list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        add(list,"الرئيسية",MainActivity.class);
        add(list,"NAJM APP SPACE",NajmAppsActivity.class);
        add(list,"الإعدادات",NajmSettingsActivity.class);
        add(list,"Runtime Engine",RuntimeManagerActivity.class);

        root.addView(NajmNavigation.create(this),new LinearLayout.LayoutParams(-1,62));
        setContentView(root);
    }

    private void add(LinearLayout list,String name,final Class<?> cls){
        Button b=new Button(this);
        b.setText(name);
        b.setTextSize(17);
        b.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){startActivity(new Intent(NajmRecentsActivity.this,cls));}
        });
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,54);
        lp.bottomMargin=8;
        list.addView(b,lp);
    }
}
