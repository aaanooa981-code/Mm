package com.najmspace.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class NajmAppsActivity extends Activity {
    private static final int PICK_APK=201;
    private LinearLayout list;
    private File appsDir;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        appsDir=new File(getFilesDir(),"najm_apps");
        if(!appsDir.exists()) appsDir.mkdirs();

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20,14,20,14);
        root.setBackgroundColor(Color.rgb(8,16,29));

        TextView title=new TextView(this);
        title.setText("NAJM APP SPACE");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView desc=new TextView(this);
        desc.setText("التطبيقات والخدمات هنا تعمل من مساحة Najm Space. الخدمات المتوافقة تفتح داخل البرنامج نفسه، وملفات APK تبقى داخل مساحة Najm إلى أن يدعمها محرك الحاوية.");
        desc.setTextColor(Color.rgb(170,190,215));
        desc.setTextSize(14);
        desc.setPadding(0,6,0,10);
        root.addView(desc);

        Button youtube=new Button(this); youtube.setText("▶ YouTube داخل Najm Space"); youtube.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"YouTube","https://m.youtube.com");}}); root.addView(youtube,new LinearLayout.LayoutParams(-1,54));

        Button browser=new Button(this); browser.setText("◎ المتصفح داخل Najm Space"); browser.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"Najm Browser","https://www.google.com");}}); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,54);bp.topMargin=8;root.addView(browser,bp);

        Button maps=new Button(this); maps.setText("⌖ الخرائط داخل Najm Space"); maps.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"Najm Maps","https://www.openstreetmap.org");}}); LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,54);mp.topMargin=8;root.addView(maps,mp);

        Button runtime=new Button(this); runtime.setText("⚙ حالة محرك التشغيل"); runtime.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){RuntimeRouter.openRuntimeManager(NajmAppsActivity.this);}}); LinearLayout.LayoutParams rp0=new LinearLayout.LayoutParams(-1,54);rp0.topMargin=8;root.addView(runtime,rp0);

        Button importBtn=new Button(this);
        importBtn.setText("+ استيراد APK إلى Najm Space");
        importBtn.setTextSize(17);
        root.addView(importBtn,new LinearLayout.LayoutParams(-1,54));
        importBtn.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){ pickApk(); }
        });

        ScrollView scroll=new ScrollView(this);
        list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list,new ScrollView.LayoutParams(-1,-2));
        LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,0,1);slp.topMargin=10;
        root.addView(scroll,slp);

        setContentView(root);
        refresh();
    }

    private void pickApk(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/vnd.android.package-archive");
        try{ startActivityForResult(i,PICK_APK); }
        catch(Exception e){
            Intent old=new Intent(Intent.ACTION_GET_CONTENT);
            old.setType("application/vnd.android.package-archive");
            startActivityForResult(old,PICK_APK);
        }
    }

    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==PICK_APK && result==RESULT_OK && data!=null && data.getData()!=null){
            importApk(data.getData());
        }
    }

    private void importApk(Uri uri){
        String name=queryName(uri);
        if(name==null || !name.toLowerCase().endsWith(".apk")) name="app_"+System.currentTimeMillis()+".apk";
        name=name.replaceAll("[^A-Za-z0-9._-]","_");
        File out=new File(appsDir,name);
        try{
            InputStream in=getContentResolver().openInputStream(uri);
            FileOutputStream fos=new FileOutputStream(out);
            byte[] buf=new byte[8192]; int n;
            while((n=in.read(buf))>0) fos.write(buf,0,n);
            fos.close(); in.close();
        }catch(Exception e){}
        refresh();
    }

    private String queryName(Uri uri){
        Cursor c=null;
        try{
            c=getContentResolver().query(uri,null,null,null,null);
            if(c!=null && c.moveToFirst()){
                int idx=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if(idx>=0)return c.getString(idx);
            }
        }catch(Exception ignored){}
        finally{if(c!=null)c.close();}
        return null;
    }

    private void refresh(){
        list.removeAllViews();
        File[] files=appsDir.listFiles();
        if(files==null || files.length==0){
            TextView empty=new TextView(this);
            empty.setText("لا توجد تطبيقات داخل Najm Space حتى الآن");
            empty.setTextColor(Color.rgb(150,170,195));
            empty.setTextSize(16);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(10,50,10,10);
            list.addView(empty);
            return;
        }

        PackageManager pm=getPackageManager();
        for(final File f:files){
            if(!f.getName().toLowerCase().endsWith(".apk"))continue;
            LinearLayout row=new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(14,12,14,12);
            row.setBackgroundColor(Color.rgb(17,34,54));

            LinearLayout info=new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            PackageInfo pi=pm.getPackageArchiveInfo(f.getAbsolutePath(),0);
            String appName=f.getName();
            String pkg="APK محفوظ داخل Najm Space";
            if(pi!=null){
                pi.applicationInfo.sourceDir=f.getAbsolutePath();
                pi.applicationInfo.publicSourceDir=f.getAbsolutePath();
                try{appName=pm.getApplicationLabel(pi.applicationInfo).toString();}catch(Exception ignored){}
                pkg=pi.packageName;
            }
            TextView n=new TextView(this);n.setText(appName);n.setTextColor(Color.WHITE);n.setTextSize(17);n.setTypeface(Typeface.DEFAULT_BOLD);
            TextView p=new TextView(this);p.setText(pkg);p.setTextColor(Color.rgb(150,175,205));p.setTextSize(12);
            info.addView(n);info.addView(p);
            row.addView(info,new LinearLayout.LayoutParams(0,-2,1));

            Button remove=new Button(this);remove.setText("حذف");
            remove.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){f.delete();refresh();}});
            TextView mode=new TextView(this); mode.setText("Legacy Container"); mode.setTextColor(Color.rgb(235,185,80)); mode.setTextSize(12); mode.setGravity(Gravity.CENTER); row.addView(mode,new LinearLayout.LayoutParams(130,48));

            row.addView(remove,new LinearLayout.LayoutParams(90,48));

            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.bottomMargin=8;
            list.addView(row,rp);
        }
    }
}
