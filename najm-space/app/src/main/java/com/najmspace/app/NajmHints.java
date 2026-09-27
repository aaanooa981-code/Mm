package com.najmspace.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class NajmHints {
    private NajmHints(){}

    public static void show(final Activity a, final String key, String title, String message){
        final SharedPreferences p=a.getSharedPreferences("najmspace",Activity.MODE_PRIVATE);
        if(p.getBoolean("hint_off_"+key,false))return;

        LinearLayout box=new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad=(int)(18*a.getResources().getDisplayMetrics().density);
        box.setPadding(pad,pad/2,pad,pad/2);

        TextView t=new TextView(a);
        t.setText(message);
        t.setTextSize(16);
        box.addView(t);

        final CheckBox never=new CheckBox(a);
        never.setText("عدم إظهار هذا التلميح مرة أخرى");
        box.addView(never);

        new AlertDialog.Builder(a)
            .setTitle(title)
            .setView(box)
            .setPositiveButton("فهمت",new DialogInterface.OnClickListener(){
                @Override public void onClick(DialogInterface d,int w){
                    if(never.isChecked())p.edit().putBoolean("hint_off_"+key,true).apply();
                }
            }).show();
    }

    public static void reset(Activity a){
        SharedPreferences p=a.getSharedPreferences("najmspace",Activity.MODE_PRIVATE);
        SharedPreferences.Editor e=p.edit();
        for(String k:p.getAll().keySet()){
            if(k.startsWith("hint_off_"))e.remove(k);
        }
        e.apply();
    }
}
