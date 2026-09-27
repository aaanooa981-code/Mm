package com.najmspace.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class NajmNavigation {
    private NajmNavigation(){}

    public static LinearLayout create(final Activity a){
        LinearLayout bar=new LinearLayout(a);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        int d=(int)(a.getResources().getDisplayMetrics().density+.5f);
        bar.setPadding(10*d,5*d,10*d,5*d);
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.rgb(10,18,30));
        bg.setCornerRadius(20*d);
        bg.setStroke(Math.max(1,d),Color.rgb(31,54,78));
        bar.setBackground(bg);

        TextView back=item(a,"◁","رجوع");
        back.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                if(a instanceof MainActivity){
                    try{a.moveTaskToBack(true);}catch(Exception e){a.finish();}
                }else{
                    a.onBackPressed();
                }
            }
        });

        TextView home=item(a,"○","هوم");
        home.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                Intent i=new Intent(a,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                a.startActivity(i);
            }
        });

        TextView recents=item(a,"▢","المفتوحة");
        recents.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                a.startActivity(new Intent(a,NajmRecentsActivity.class));
            }
        });

        bar.addView(back,new LinearLayout.LayoutParams(0,58,1));
        bar.addView(home,new LinearLayout.LayoutParams(0,58,1));
        bar.addView(recents,new LinearLayout.LayoutParams(0,58,1));
        return bar;
    }

    private static TextView item(Context c,String icon,String label){
        TextView t=new TextView(c);
        t.setText(icon+"\n"+label);
        t.setTextColor(Color.WHITE);
        t.setTextSize(13);
        t.setGravity(Gravity.CENTER);
        t.setClickable(true);
        t.setFocusable(true);
        return t;
    }
}
