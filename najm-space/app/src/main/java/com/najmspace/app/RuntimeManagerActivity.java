package com.najmspace.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;

public class RuntimeManagerActivity extends Activity {
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20),dp(14),dp(20),dp(14));
        root.setBackgroundColor(Color.rgb(7,15,27));

        root.addView(txt("NAJM RUNTIME ENGINE V1.0",27,Color.WHITE,true));
        TextView sub=txt("محركات التشغيل داخل Najm Space",14,Color.rgb(160,185,215),false);
        sub.setPadding(0,4,0,12);root.addView(sub);

        ScrollView scroll=new ScrollView(this);
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);scroll.addView(list);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        addEngine(list,"Mini App Runtime","جاهز","HTML • CSS • JavaScript • ZIP","تشغيل محلي كامل داخل Najm Space بدون تثبيت",Color.rgb(30,165,92));
        addEngine(list,"Internal Web Runtime","جاهز","YouTube • Browser • Maps","تشغيل الخدمات داخل نافذة Najm Space",Color.rgb(30,145,92));
        addEngine(list,"Legacy APK Container","قيد التطوير","Android APK","ملفات APK تبقى داخل مساحة Najm ولا تُثبت تلقائيًا على النظام",Color.rgb(196,142,42));
        addEngine(list,"System Fallback","اختياري","فتح خارجي عند الحاجة","يمكن تعطيله من الإعدادات",Color.rgb(62,102,160));

        int apk=0,mini=0;
        File ad=new File(getFilesDir(),"najm_apps");File[] af=ad.listFiles();
        if(af!=null)for(File f:af)if(f.getName().toLowerCase().endsWith(".apk"))apk++;
        File md=new File(getFilesDir(),"najm_mini_apps");File[] mf=md.listFiles();
        if(mf!=null)for(File f:mf)if(f.isDirectory())mini++;

        TextView stats=txt("Mini Apps: "+mini+"    •    APK محفوظة: "+apk,17,Color.WHITE,true);
        stats.setPadding(0,dp(16),0,dp(8));list.addView(stats);
        list.addView(txt("V1.0 يضيف أول محرك تطبيقات داخلي نملكه بالكامل. APK العشوائي ما زال يحتاج Legacy Container مستقل ومتوافق مع Android 4.4.",14,Color.rgb(185,200,220),false));

        root.addView(NajmNavigation.create(this),new LinearLayout.LayoutParams(-1,62));
        setContentView(root);
    }

    private void addEngine(LinearLayout parent,String name,String state,String supports,String desc,int stateColor){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(14),dp(12),dp(14),dp(12));box.setBackgroundColor(Color.rgb(16,33,54));
        box.addView(txt(name,19,Color.WHITE,true));
        TextView s=txt("● "+state,14,stateColor,true);s.setPadding(0,4,0,4);box.addView(s);
        box.addView(txt(supports,14,Color.rgb(95,190,255),false));
        box.addView(txt(desc,13,Color.rgb(165,185,210),false));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(10);parent.addView(box,lp);
    }

    private TextView txt(String s,float size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT_BOLD);return t;
    }
}
