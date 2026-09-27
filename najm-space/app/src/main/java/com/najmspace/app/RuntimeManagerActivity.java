package com.najmspace.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
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

        TextView title=txt("NAJM RUNTIME ENGINE",27,Color.WHITE,true);
        root.addView(title);
        TextView sub=txt("حالة محركات التشغيل داخل Najm Space",14,Color.rgb(160,185,215),false);
        sub.setPadding(0,4,0,12);
        root.addView(sub);

        ScrollView scroll=new ScrollView(this);
        LinearLayout list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        addEngine(list,"Internal Web Runtime","جاهز","YouTube • Browser • Maps","يشغّل الخدمات داخل نافذة Najm Space نفسها",Color.rgb(30,145,92));
        addEngine(list,"Legacy APK Container","قيد الدمج","APK محفوظ داخل Najm App Space","يحتاج محرك Virtual Container مرخّص ومتوافق مع Android 4.4",Color.rgb(196,142,42));
        addEngine(list,"System Fallback","اختياري","فتح خارجي عند فشل الداخلي","يمكن تعطيله من الإعدادات",Color.rgb(62,102,160));

        File dir=new File(getFilesDir(),"najm_apps");
        int count=0;
        File[] fs=dir.listFiles();
        if(fs!=null) for(File f:fs) if(f.getName().toLowerCase().endsWith(".apk")) count++;

        TextView apps=txt("APK محفوظة داخل Najm Space: "+count,17,Color.WHITE,true);
        apps.setPadding(0,dp(18),0,dp(8));
        list.addView(apps);

        TextView note=txt("التطبيقات المستوردة لا تُثبت على أندرويد تلقائيًا. تبقى في مجلد Najm الخاص حتى يتوفر لها محرك تشغيل داخلي مناسب.",14,Color.rgb(185,200,220),false);
        list.addView(note);

        setContentView(root);
    }

    private void addEngine(LinearLayout parent,String name,String state,String supports,String desc,int stateColor){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14),dp(12),dp(14),dp(12));
        box.setBackgroundColor(Color.rgb(16,33,54));

        TextView nameV=txt(name,19,Color.WHITE,true);
        box.addView(nameV);

        TextView stateV=txt("● "+state,14,stateColor,true);
        stateV.setPadding(0,4,0,4);
        box.addView(stateV);

        box.addView(txt(supports,14,Color.rgb(95,190,255),false));
        box.addView(txt(desc,13,Color.rgb(165,185,210),false));

        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(10);
        parent.addView(box,lp);
    }

    private TextView txt(String s,float size,int color,boolean bold){
        TextView t=new TextView(this);
        t.setText(s);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }
}
