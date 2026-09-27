package com.najmspace.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class PermissionActivity extends Activity {
    private static final int REQ=77;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(36,24,36,24);
        root.setBackgroundColor(Color.rgb(7,15,27));

        TextView title=new TextView(this);
        title.setText("إعداد Najm Space");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView body=new TextView(this);
        body.setText("\nيطلب Najm Space الأذونات الأساسية مرة واحدة فقط.\n\n• الموقع: عداد السرعة والخريطة الحية\n• الملفات: استيراد التطبيقات والملفات\n\nإذن تثبيت التطبيقات في النظام يظهر فقط عندما تختار التثبيت في النظام.\n");
        body.setTextColor(Color.rgb(190,207,228));
        body.setTextSize(17);
        body.setGravity(Gravity.RIGHT);
        root.addView(body);

        Button allow=new Button(this);
        allow.setText("منح الأذونات والمتابعة");
        allow.setTextSize(18);
        root.addView(allow,new LinearLayout.LayoutParams(-1,58));
        allow.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){ requestNeeded(); }
        });

        setContentView(root);
    }

    private void requestNeeded(){
        if(Build.VERSION.SDK_INT>=23){
            ArrayList<String> p=new ArrayList<String>();
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.ACCESS_FINE_LOCATION);
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            if(Build.VERSION.SDK_INT<=32 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            if(Build.VERSION.SDK_INT<=28 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(!p.isEmpty()){
                requestPermissions(p.toArray(new String[p.size()]),REQ);
                return;
            }
        }
        finishSetup();
    }

    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){\n        super.onRequestPermissionsResult(r,p,g);\n        if(r==REQ) finishSetup();\n    }\n\n    private void finishSetup(){
        SharedPreferences sp=getSharedPreferences("najmspace",MODE_PRIVATE);
        sp.edit().putBoolean("permissions_intro_done",true).apply();
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
}
