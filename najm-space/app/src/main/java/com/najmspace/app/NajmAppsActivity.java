package com.najmspace.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NajmAppsActivity extends Activity {
    private static final int PICK_APK=201;
    private static final int PICK_MINI=202;
    private LinearLayout list;
    private File appsDir;
    private File miniDir;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        appsDir=NajmStorage.appsDir(this);
        miniDir=NajmStorage.miniAppsDir(this);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20,14,20,14);
        root.setBackgroundColor(Color.rgb(8,16,29));

        TextView title=new TextView(this);
        title.setText("NAJM APP SPACE  V1.3");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView desc=new TextView(this);
        desc.setText("Mini Apps تعمل داخل Najm Space مباشرة. ملفات APK العادية تبقى في مساحة Najm إلى أن يكتمل Legacy Container.");
        desc.setTextColor(Color.rgb(170,190,215));
        desc.setTextSize(14);
        desc.setPadding(0,6,0,10);
        root.addView(desc);

        Button youtube=new Button(this);
        youtube.setText("▶ YouTube داخل Najm Space");
        youtube.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"YouTube","https://m.youtube.com");}});
        root.addView(youtube,new LinearLayout.LayoutParams(-1,52));

        Button browser=new Button(this);
        browser.setText("◎ المتصفح داخل Najm Space");
        browser.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"Najm Browser","https://www.google.com");}});
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,52);bp.topMargin=6;root.addView(browser,bp);

        Button maps=new Button(this);
        maps.setText("⌖ الخرائط داخل Najm Space");
        maps.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"Najm Maps","https://www.openstreetmap.org");}});
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,52);mp.topMargin=6;root.addView(maps,mp);

        Button filesBtn=new Button(this); filesBtn.setText("📁 مدير الملفات / USB"); filesBtn.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){startActivity(new Intent(NajmAppsActivity.this,NajmFileManagerActivity.class));}}); LinearLayout.LayoutParams fbp=new LinearLayout.LayoutParams(-1,52);fbp.topMargin=8;root.addView(filesBtn,fbp);

        LinearLayout imports=new LinearLayout(this);
        imports.setOrientation(LinearLayout.HORIZONTAL);

        Button miniBtn=new Button(this);
        miniBtn.setText("+ Mini App HTML/ZIP");
        miniBtn.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){pickMini();}});
        imports.addView(miniBtn,new LinearLayout.LayoutParams(0,52,1));

        Button apkBtn=new Button(this);
        apkBtn.setText("+ APK Legacy");
        apkBtn.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){pickApk();}});
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,52,1);ap.leftMargin=6;
        imports.addView(apkBtn,ap);

        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,52);ip.topMargin=8;
        root.addView(imports,ip);

        Button runtime=new Button(this);
        runtime.setText("⚙ حالة Runtime Engine");
        runtime.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){RuntimeRouter.openRuntimeManager(NajmAppsActivity.this);}});
        LinearLayout.LayoutParams rp0=new LinearLayout.LayoutParams(-1,50);rp0.topMargin=6;root.addView(runtime,rp0);

        ScrollView scroll=new ScrollView(this);
        list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list,new ScrollView.LayoutParams(-1,-2));
        LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,0,1);slp.topMargin=10;
        root.addView(scroll,slp);
        root.addView(NajmNavigation.create(this),new LinearLayout.LayoutParams(-1,62));

        setContentView(root);
        refresh();
    }

    private void pickApk(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/vnd.android.package-archive");
        try{startActivityForResult(i,PICK_APK);}
        catch(Exception e){
            Intent old=new Intent(Intent.ACTION_GET_CONTENT);
            old.setType("application/vnd.android.package-archive");
            startActivityForResult(old,PICK_APK);
        }
    }

    private void pickMini(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        try{startActivityForResult(i,PICK_MINI);}
        catch(Exception e){
            Intent old=new Intent(Intent.ACTION_GET_CONTENT);
            old.setType("*/*");
            startActivityForResult(old,PICK_MINI);
        }
    }

    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK || data==null || data.getData()==null)return;
        if(request==PICK_APK)importApk(data.getData());
        if(request==PICK_MINI)importMini(data.getData());
    }

    private void importApk(Uri uri){
        String name=queryName(uri);
        if(name==null || !name.toLowerCase().endsWith(".apk"))name="app_"+System.currentTimeMillis()+".apk";
        name=safe(name);
        copy(uri,new File(appsDir,name));
        refresh();
    }

    private void importMini(Uri uri){
        String name=queryName(uri);
        if(name==null)name="mini_"+System.currentTimeMillis()+".html";
        String low=name.toLowerCase();

        if(low.endsWith(".zip")){
            File pkg=new File(miniDir,"mini_"+System.currentTimeMillis());
            pkg.mkdirs();
            try{
                InputStream raw=getContentResolver().openInputStream(uri);
                ZipInputStream zin=new ZipInputStream(raw);
                ZipEntry e;
                byte[] buf=new byte[8192];
                while((e=zin.getNextEntry())!=null){
                    String n=e.getName().replace("\\","/");
                    if(n.contains("..") || n.startsWith("/"))continue;
                    File out=new File(pkg,n);
                    String base=pkg.getCanonicalPath()+File.separator;
                    if(!out.getCanonicalPath().startsWith(base))continue;
                    if(e.isDirectory()){out.mkdirs();continue;}
                    File parent=out.getParentFile();if(parent!=null)parent.mkdirs();
                    FileOutputStream fos=new FileOutputStream(out);
                    int len;while((len=zin.read(buf))>0)fos.write(buf,0,len);
                    fos.close();
                }
                zin.close();
            }catch(Exception ignored){}
        }else{
            String safe=safe(name);
            if(!safe.toLowerCase().endsWith(".html") && !safe.toLowerCase().endsWith(".htm"))safe=safe+".html";
            File folder=new File(miniDir,"mini_"+System.currentTimeMillis());
            folder.mkdirs();
            copy(uri,new File(folder,"index.html"));
        }
        refresh();
    }

    private void copy(Uri uri,File out){
        try{
            InputStream in=getContentResolver().openInputStream(uri);
            FileOutputStream fos=new FileOutputStream(out);
            byte[] buf=new byte[8192];int n;
            while((n=in.read(buf))>0)fos.write(buf,0,n);
            fos.close();in.close();
        }catch(Exception ignored){}
    }

    private String safe(String name){return name.replaceAll("[^A-Za-z0-9._-]","_");}

    private String queryName(Uri uri){
        Cursor c=null;
        try{
            c=getContentResolver().query(uri,null,null,null,null);
            if(c!=null && c.moveToFirst()){
                int idx=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if(idx>=0)return c.getString(idx);
            }
        }catch(Exception ignored){}finally{if(c!=null)c.close();}
        return null;
    }

    private void refresh(){
        list.removeAllViews();
        addSectionTitle("MINI APPS — تشغيل داخلي");

        File[] minis=miniDir.listFiles();
        int miniCount=0;
        if(minis!=null){
            for(final File folder:minis){
                if(!folder.isDirectory())continue;
                File entry=findEntry(folder);
                if(entry==null)continue;
                miniCount++;
                final File entryFinal=entry;
                LinearLayout row=row();
                LinearLayout info=info("Mini App "+miniCount,"Internal Runtime • "+entry.getName(),Color.rgb(70,205,130));
                row.addView(info,new LinearLayout.LayoutParams(0,-2,1));

                Button run=new Button(this);run.setText("تشغيل");
                run.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
                    Intent i=new Intent(NajmAppsActivity.this,MiniAppRuntimeActivity.class);
                    i.putExtra("path",entryFinal.getAbsolutePath());
                    i.putExtra("title",folder.getName());
                    startActivity(i);
                }});
                row.addView(run,new LinearLayout.LayoutParams(92,48));

                Button del=new Button(this);del.setText("حذف");
                del.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){deleteTree(folder);refresh();}});
                row.addView(del,new LinearLayout.LayoutParams(86,48));
                addRow(row);
            }
        }
        if(miniCount==0)addEmpty("لا توجد Mini Apps. استورد HTML أو ZIP.");

        addSectionTitle("APK — LEGACY CONTAINER");
        File[] files=appsDir.listFiles();
        int apkCount=0;
        PackageManager pm=getPackageManager();
        if(files!=null){
            for(final File f:files){
                if(!f.getName().toLowerCase().endsWith(".apk"))continue;
                apkCount++;
                PackageInfo pi=pm.getPackageArchiveInfo(f.getAbsolutePath(),0);
                String appName=f.getName(), pkg="APK محفوظ داخل Najm Space";
                if(pi!=null){
                    pi.applicationInfo.sourceDir=f.getAbsolutePath();
                    pi.applicationInfo.publicSourceDir=f.getAbsolutePath();
                    try{appName=pm.getApplicationLabel(pi.applicationInfo).toString();}catch(Exception ignored){}
                    pkg=pi.packageName;
                }
                LinearLayout row=row();
                row.addView(info(appName,pkg+" • Legacy Container Pending",Color.rgb(235,185,80)),new LinearLayout.LayoutParams(0,-2,1));
                Button del=new Button(this);del.setText("حذف");
                del.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){f.delete();refresh();}});
                row.addView(del,new LinearLayout.LayoutParams(86,48));
                addRow(row);
            }
        }
        if(apkCount==0)addEmpty("لا توجد APK محفوظة.");
    }

    private File findEntry(File folder){
        File direct=new File(folder,"index.html");if(direct.exists())return direct;
        File[] fs=folder.listFiles();
        if(fs==null)return null;
        for(File f:fs){
            if(f.isDirectory()){File r=findEntry(f);if(r!=null)return r;}
            else{
                String n=f.getName().toLowerCase();
                if(n.equals("index.htm") || n.equals("index.html"))return f;
            }
        }
        return null;
    }

    private void deleteTree(File f){
        if(f.isDirectory()){
            File[] fs=f.listFiles();if(fs!=null)for(File x:fs)deleteTree(x);
        }
        f.delete();
    }

    private LinearLayout row(){
        LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(12,10,12,10);r.setBackgroundColor(Color.rgb(17,34,54));return r;
    }

    private LinearLayout info(String name,String detail,int color){
        LinearLayout i=new LinearLayout(this);i.setOrientation(LinearLayout.VERTICAL);
        TextView n=new TextView(this);n.setText(name);n.setTextColor(Color.WHITE);n.setTextSize(17);n.setTypeface(Typeface.DEFAULT_BOLD);
        TextView d=new TextView(this);d.setText(detail);d.setTextColor(color);d.setTextSize(12);
        i.addView(n);i.addView(d);return i;
    }

    private void addRow(LinearLayout row){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=7;list.addView(row,lp);
    }

    private void addSectionTitle(String text){
        TextView t=new TextView(this);t.setText(text);t.setTextColor(Color.rgb(100,200,255));t.setTextSize(15);t.setTypeface(Typeface.DEFAULT_BOLD);t.setPadding(0,8,0,7);list.addView(t);
    }

    private void addEmpty(String text){
        TextView e=new TextView(this);e.setText(text);e.setTextColor(Color.rgb(145,165,190));e.setTextSize(14);e.setPadding(8,10,8,16);list.addView(e);
    }
}
