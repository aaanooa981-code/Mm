package com.najmspace.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public class NajmSettingsActivity extends Activity {
    private SharedPreferences prefs;
    private LinearLayout content;

    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("najmspace",MODE_PRIVATE);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.rgb(7,15,27));

        LinearLayout side=new LinearLayout(this);
        side.setOrientation(LinearLayout.VERTICAL);
        side.setPadding(dp(10),dp(12),dp(10),dp(12));
        side.setBackgroundColor(Color.rgb(10,22,38));

        TextView title=txt("⚙\nNAJM SETTINGS",19,Color.WHITE,true);
        title.setGravity(Gravity.CENTER);
        side.addView(title,new LinearLayout.LayoutParams(-1,dp(92)));

        addNav(side,"عام",0);
        addNav(side,"المظهر",1);
        addNav(side,"التطبيقات",2);
        addNav(side,"الأذونات",3);
        addNav(side,"التوافق",4);
        addNav(side,"التخزين",5);
        addNav(side,"النظام",6);
        addNav(side,"حول",7);

        root.addView(side,new LinearLayout.LayoutParams(dp(190),-1));

        ScrollView scroll=new ScrollView(this);
        content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24),dp(16),dp(24),dp(22));
        scroll.addView(content,new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll,new LinearLayout.LayoutParams(0,-1,1));

        setContentView(root);
        showSection(0);
    }

    private void addNav(LinearLayout parent, final String name, final int section){
        TextView t=txt(name,16,Color.rgb(220,230,245),false);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(dp(14),0,0,0);
        t.setBackgroundColor(Color.rgb(15,33,54));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));lp.bottomMargin=dp(6);
        parent.addView(t,lp);
        t.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){showSection(section);}});
    }

    private TextView txt(String s,float size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private TextView heading(String s){
        TextView t=txt(s,27,Color.WHITE,true);
        t.setPadding(0,0,0,dp(12));
        return t;
    }

    private TextView sub(String s){
        TextView t=txt(s,14,Color.rgb(160,183,210),false);
        t.setPadding(0,0,0,dp(14));
        return t;
    }

    private Switch toggle(String label,String key,boolean def){
        Switch s=new Switch(this);
        s.setText(label);s.setTextColor(Color.WHITE);s.setTextSize(16);s.setPadding(dp(12),dp(9),dp(12),dp(9));
        s.setChecked(prefs.getBoolean(key,def));
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override public void onCheckedChanged(CompoundButton b,boolean c){prefs.edit().putBoolean(key,c).apply();}
        });
        return s;
    }

    private Button action(String text,final View.OnClickListener l){
        Button b=new Button(this);b.setText(text);b.setTextSize(15);b.setOnClickListener(l);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.bottomMargin=dp(8);
        content.addView(b,lp);return b;
    }

    private void showSection(int s){
        content.removeAllViews();

        if(s==0){
            content.addView(heading("الإعدادات العامة"));
            content.addView(sub("إعدادات Najm Space الأساسية."));
            content.addView(toggle("تشغيل Najm Space كواجهة رئيسية", "launcher_mode", true));
            content.addView(toggle("تشغيل تلقائي عند فتح الجهاز", "auto_start", true));
            content.addView(toggle("إظهار الساعة والتاريخ", "clock", true));
            content.addView(toggle("تفعيل الأنميشن", "animations", true));
        }

        if(s==1){
            content.addView(heading("المظهر"));
            content.addView(sub("تخصيص شكل الواجهة دون تغيير إعدادات أندرويد."));
            content.addView(toggle("الوضع الداكن", "dark", true));
            content.addView(toggle("تأثير الزجاج", "glass", true));
            content.addView(toggle("الحركات عند الضغط", "press_animation", true));
            content.addView(toggle("الخريطة المتحركة في الرئيسية", "map_animation", true));
        }

        if(s==2){
            content.addView(heading("التطبيقات"));
            content.addView(sub("إدارة التطبيقات المحفوظة داخل مساحة Najm Space."));
            action("فتح NAJM APP SPACE",new View.OnClickListener(){@Override public void onClick(View v){startActivity(new Intent(NajmSettingsActivity.this,NajmAppsActivity.class));}});
            content.addView(toggle("تشغيل الخدمات المتوافقة داخل Najm Space", "internal_runtime", true));
            content.addView(toggle("إخفاء تطبيقات النظام من قائمة Najm", "hide_system_apps", true));
        }

        if(s==3){
            content.addView(heading("الأذونات"));
            content.addView(sub("راجع أذونات الموقع والملفات وتثبيت التطبيقات."));
            action("فتح شاشة أذونات Najm Space",new View.OnClickListener(){@Override public void onClick(View v){startActivity(new Intent(NajmSettingsActivity.this,PermissionActivity.class));}});
            action("إعدادات أذونات أندرويد",new View.OnClickListener(){@Override public void onClick(View v){startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));}});
        }

        if(s==4){
            content.addView(heading("التوافق"));
            content.addView(sub("حدد طريقة تشغيل الخدمات على الأجهزة القديمة."));
            content.addView(toggle("YouTube داخل Najm Space", "youtube_internal", true));
            content.addView(toggle("المتصفح الداخلي", "browser_internal", true));
            content.addView(toggle("الخرائط الداخلية/الخفيفة", "maps_internal", true));
            content.addView(toggle("السماح بالوضع الخارجي عند فشل الداخلي", "fallback_external", true));
        }

        if(s==5){
            content.addView(heading("التخزين"));
            content.addView(sub("ملفات APK المستوردة تحفظ داخل مساحة التطبيق الخاصة."));
            action("فتح التطبيقات المحفوظة",new View.OnClickListener(){@Override public void onClick(View v){startActivity(new Intent(NajmSettingsActivity.this,NajmAppsActivity.class));}});
            action("مسح التطبيقات المستوردة",new View.OnClickListener(){@Override public void onClick(View v){clearImported();}});
        }

        if(s==6){
            content.addView(heading("النظام"));
            content.addView(sub("اختصارات لإعدادات النظام عند الحاجة."));
            action("Wi‑Fi",new View.OnClickListener(){@Override public void onClick(View v){startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));}});
            action("Bluetooth",new View.OnClickListener(){@Override public void onClick(View v){startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));}});
            action("الشاشة",new View.OnClickListener(){@Override public void onClick(View v){startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));}});
            action("إعدادات أندرويد",new View.OnClickListener(){@Override public void onClick(View v){startActivity(new Intent(Settings.ACTION_SETTINGS));}});
        }

        if(s==7){
            content.addView(heading("حول Najm Space"));
            content.addView(sub("Najm Space V0.8\nAndroid 4.4+\nواجهة سيارة + App Space + Compatibility Runtime"));
            TextView note=txt("محرك تشغيل APK الافتراضي الكامل على Android 4.4.2 ما زال قيد التطوير. الخدمات التي يمكن تشغيلها داخليًا تعمل عبر Internal Runtime.",15,Color.rgb(205,215,230),false);
            content.addView(note);
        }

        content.setAlpha(0f);content.animate().alpha(1f).setDuration(180).start();
    }

    private void clearImported(){
        java.io.File d=new java.io.File(getFilesDir(),"najm_apps");
        java.io.File[] fs=d.listFiles();
        if(fs!=null)for(java.io.File f:fs)f.delete();
    }
}
