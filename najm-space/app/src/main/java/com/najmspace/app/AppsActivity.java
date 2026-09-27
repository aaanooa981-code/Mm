package com.najmspace.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppsActivity extends Activity {
    private final ArrayList<ResolveInfo> apps = new ArrayList<ResolveInfo>();
    private PackageManager pm;

    private int dp(int v){ return (int)(v*getResources().getDisplayMetrics().density+.5f); }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        pm=getPackageManager();

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20),dp(14),dp(20),dp(14));
        root.setBackgroundColor(Color.rgb(12,17,27));

        TextView title=new TextView(this);
        title.setText("التطبيقات   •   ALL APPS");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        root.addView(title,new LinearLayout.LayoutParams(-1,dp(54)));

        Intent q=new Intent(Intent.ACTION_MAIN,null);
        q.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found=pm.queryIntentActivities(q,0);
        for(ResolveInfo r:found){
            if(!r.activityInfo.packageName.equals(getPackageName())) apps.add(r);
        }
        Collections.sort(apps,new Comparator<ResolveInfo>(){
            public int compare(ResolveInfo a,ResolveInfo c){
                return a.loadLabel(pm).toString().compareToIgnoreCase(c.loadLabel(pm).toString());
            }
        });

        GridView grid=new GridView(this);
        grid.setNumColumns(6);
        grid.setHorizontalSpacing(dp(12));
        grid.setVerticalSpacing(dp(12));
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setClipToPadding(false);
        grid.setPadding(0,dp(6),0,dp(8));
        grid.setAdapter(new AppAdapter());
        grid.setOnItemClickListener((parent,view,pos,id)->launch(pos));
        root.addView(grid,new LinearLayout.LayoutParams(-1,0,1));

        TextView hint=new TextView(this);
        hint.setText("اضغط على أي تطبيق لفتحه  •  زر الرجوع للعودة للرئيسية");
        hint.setTextColor(Color.rgb(135,153,177));
        hint.setTextSize(13);
        hint.setGravity(Gravity.CENTER);
        root.addView(hint,new LinearLayout.LayoutParams(-1,dp(34)));

        setContentView(root);

        Animation fade=new AlphaAnimation(0f,1f);
        fade.setDuration(260);
        root.startAnimation(fade);
    }

    private void launch(int pos){
        ResolveInfo r=apps.get(pos);
        Intent i=pm.getLaunchIntentForPackage(r.activityInfo.packageName);
        if(i!=null){
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }
    }

    private class AppAdapter extends BaseAdapter {
        public int getCount(){return apps.size();}
        public Object getItem(int p){return apps.get(p);}
        public long getItemId(int p){return p;}

        public View getView(final int p, View convert, ViewGroup parent){
            LinearLayout cell=new LinearLayout(AppsActivity.this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(dp(6),dp(8),dp(6),dp(8));
            cell.setMinimumHeight(dp(106));

            ImageView icon=new ImageView(AppsActivity.this);
            Drawable d=apps.get(p).loadIcon(pm);
            icon.setImageDrawable(d);
            cell.addView(icon,new LinearLayout.LayoutParams(dp(54),dp(54)));

            TextView label=new TextView(AppsActivity.this);
            label.setText(apps.get(p).loadLabel(pm));
            label.setTextColor(Color.WHITE);
            label.setTextSize(13);
            label.setGravity(Gravity.CENTER);
            label.setMaxLines(1);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(30));
            lp.topMargin=dp(5);
            cell.addView(label,lp);

            cell.setOnTouchListener((v,e)->{
                if(e.getAction()==0) v.animate().scaleX(.90f).scaleY(.90f).setDuration(70).start();
                else if(e.getAction()==1||e.getAction()==3) v.animate().scaleX(1f).scaleY(1f).setDuration(110).start();
                return false;
            });

            ScaleAnimation pop=new ScaleAnimation(.88f,1f,.88f,1f,Animation.RELATIVE_TO_SELF,.5f,Animation.RELATIVE_TO_SELF,.5f);
            pop.setDuration(160);
            pop.setStartOffset(Math.min(p,18)*18);
            cell.startAnimation(pop);
            return cell;
        }
    }
}
