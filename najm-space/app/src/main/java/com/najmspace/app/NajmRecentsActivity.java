package com.najmspace.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;

public class NajmRecentsActivity extends Activity {
    private LinearLayout list;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(7,15,27));
        root.setPadding(16,12,16,8);

        LinearLayout top=new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView title=new TextView(this);
        title.setText("التطبيقات المفتوحة");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        top.addView(title,new LinearLayout.LayoutParams(0,54,1));

        Button clear=new Button(this);
        clear.setText("إغلاق الكل");
        clear.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                NajmRecentStore.clear(NajmRecentsActivity.this);
                refresh();
            }
        });
        top.addView(clear,new LinearLayout.LayoutParams(120,50));
        root.addView(top);

        TextView hint=new TextView(this);
        hint.setText("ⓘ  مثل التطبيقات الأخيرة: اضغط البطاقة للعودة، أو أغلقها من ×");
        hint.setTextColor(Color.rgb(130,185,235));
        hint.setTextSize(13);
        hint.setPadding(0,2,0,8);
        hint.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                NajmHints.show(NajmRecentsActivity.this,"recents","التطبيقات المفتوحة","يعرض Najm Space آخر النوافذ التي استخدمتها. اضغط بطاقة للعودة أو × لإغلاقها من القائمة.");
            }
        });
        root.addView(hint);

        ScrollView scroll=new ScrollView(this);
        list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        root.addView(NajmNavigation.create(this),new LinearLayout.LayoutParams(-1,62));
        setContentView(root);
        refresh();
    }

    @Override protected void onResume(){
        super.onResume();
        refresh();
    }

    private void refresh(){
        if(list==null)return;
        list.removeAllViews();
        ArrayList<NajmRecentStore.Item> items=NajmRecentStore.get(this);

        if(items.size()==0){
            TextView empty=new TextView(this);
            empty.setText("لا توجد تطبيقات مفتوحة داخل Najm Space");
            empty.setTextColor(Color.rgb(150,170,195));
            empty.setTextSize(17);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(10,70,10,10);
            list.addView(empty,new LinearLayout.LayoutParams(-1,-2));
            return;
        }

        for(final NajmRecentStore.Item item:items){
            LinearLayout card=new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(18,16,12,16);
            card.setBackgroundColor(Color.rgb(18,35,56));

            LinearLayout info=new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);

            TextView name=new TextView(this);
            name.setText(item.title);
            name.setTextColor(Color.WHITE);
            name.setTextSize(19);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            info.addView(name);

            TextView sub=new TextView(this);
            sub.setText("NAJM SPACE • مفتوح مؤخرًا");
            sub.setTextColor(Color.rgb(120,185,235));
            sub.setTextSize(12);
            info.addView(sub);

            card.addView(info,new LinearLayout.LayoutParams(0,-2,1));

            Button close=new Button(this);
            close.setText("×");
            close.setTextSize(23);
            close.setOnClickListener(new View.OnClickListener(){
                @Override public void onClick(View v){
                    NajmRecentStore.remove(NajmRecentsActivity.this,item.id);
                    refresh();
                }
            });
            card.addView(close,new LinearLayout.LayoutParams(62,52));

            card.setOnClickListener(new View.OnClickListener(){
                @Override public void onClick(View v){
                    try{
                        Class<?> cls=Class.forName(item.className);
                        Intent i=new Intent(NajmRecentsActivity.this,cls);
                        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                    }catch(Exception e){}
                }
            });

            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.bottomMargin=10;
            list.addView(card,lp);
        }
    }
}
