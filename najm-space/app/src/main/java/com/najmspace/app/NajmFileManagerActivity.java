package com.najmspace.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class NajmFileManagerActivity extends Activity {
    private static final int PICK_SYSTEM_FILE=401;
    private LinearLayout list;
    private TextView pathView;
    private File current;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16,10,16,8);
        root.setBackgroundColor(Color.rgb(7,15,27));

        TextView title=new TextView(this);
        title.setText("NAJM FILE MANAGER • USB");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        LinearLayout quick=new LinearLayout(this);
        quick.setOrientation(LinearLayout.HORIZONTAL);

        addQuick(quick,"Internal",new File("/storage/emulated/0"));
        addQuick(quick,"Storage",new File("/storage"));
        addQuick(quick,"USB",findUsbRoot());

        Button picker=new Button(this);
        picker.setText("USB/System");
        picker.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){openSystemPicker();}
        });
        quick.addView(picker,new LinearLayout.LayoutParams(0,48,1));

        root.addView(quick,new LinearLayout.LayoutParams(-1,50));

        pathView=new TextView(this);
        pathView.setTextColor(Color.rgb(130,190,245));
        pathView.setTextSize(13);
        pathView.setPadding(4,6,4,6);
        root.addView(pathView);

        ScrollView scroll=new ScrollView(this);
        list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list,new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        root.addView(NajmNavigation.create(this),new LinearLayout.LayoutParams(-1,62));

        setContentView(root);

        File start=new File("/storage/emulated/0");
        if(!start.exists())start=Environment.getExternalStorageDirectory();
        show(start);
    }

    private void addQuick(LinearLayout row,String label,final File target){
        Button b=new Button(this);b.setText(label);
        b.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                if(target!=null && target.exists())show(target);
            }
        });
        row.addView(b,new LinearLayout.LayoutParams(0,48,1));
    }

    private File findUsbRoot(){
        String[] roots={
            "/storage/usbotg",
            "/storage/usb0",
            "/storage/usb1",
            "/storage/USB",
            "/mnt/usb_storage",
            "/mnt/usbhost1",
            "/mnt/usbotg",
            "/mnt/media_rw"
        };
        for(String p:roots){
            File f=new File(p);
            if(f.exists() && f.canRead())return f;
        }
        File storage=new File("/storage");
        File[] fs=storage.listFiles();
        if(fs!=null){
            for(File f:fs){
                String n=f.getName().toLowerCase();
                if((n.contains("usb")||n.contains("otg")) && f.canRead())return f;
            }
        }
        return new File("/storage");
    }

    private void show(final File dir){
        if(dir==null || !dir.exists() || !dir.isDirectory())return;
        current=dir;
        pathView.setText(dir.getAbsolutePath());
        list.removeAllViews();

        File parent=dir.getParentFile();
        if(parent!=null){
            TextView up=rowText("⬆  ..",Color.rgb(210,225,245),true);
            up.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){show(current.getParentFile());}});
            list.addView(up,new LinearLayout.LayoutParams(-1,50));
        }

        File[] files;
        try{files=dir.listFiles();}catch(Exception e){files=null;}
        if(files==null){
            addMessage("لا يمكن قراءة هذا المجلد. جرّب زر USB/System.");
            return;
        }

        Arrays.sort(files,new Comparator<File>(){
            @Override public int compare(File a,File b){
                if(a.isDirectory()!=b.isDirectory())return a.isDirectory()?-1:1;
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });

        for(final File f:files){
            if(f.isHidden())continue;
            String icon=f.isDirectory()?"📁  ":"📄  ";
            int color=f.isDirectory()?Color.rgb(130,195,255):Color.WHITE;
            TextView row=rowText(icon+f.getName(),color,f.isDirectory());
            row.setOnClickListener(new View.OnClickListener(){
                @Override public void onClick(View v){
                    if(f.isDirectory())show(f);
                    else if(f.getName().toLowerCase().endsWith(".apk"))showInstallChoice(f);
                }
            });
            list.addView(row,new LinearLayout.LayoutParams(-1,48));
        }
    }

    private TextView rowText(String s,int color,boolean bold){
        TextView t=new TextView(this);
        t.setText(s);t.setTextColor(color);t.setTextSize(16);t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(12,0,12,0);
        if(bold)t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private void addMessage(String s){
        TextView t=rowText(s,Color.rgb(165,180,205),false);
        t.setPadding(12,30,12,30);
        list.addView(t);
    }

    private void showInstallChoice(final File apk){
        AlertDialog.Builder b=new AlertDialog.Builder(this);
        b.setTitle("تثبيت التطبيق");
        b.setMessage(apk.getName()+"\n\nاختر مكان التثبيت:");
        b.setPositiveButton("تثبيت في Najm Space",new DialogInterface.OnClickListener(){
            @Override public void onClick(DialogInterface d,int w){installIntoNajm(apk);}
        });
        b.setNegativeButton("تثبيت في النظام",new DialogInterface.OnClickListener(){
            @Override public void onClick(DialogInterface d,int w){installIntoSystem(apk);}
        });
        b.setNeutralButton("إلغاء",null);
        b.show();
    }

    private void installIntoNajm(File apk){
        File dir=NajmStorage.appsDir(this);
        File out=new File(dir,safe(apk.getName()));
        copyFile(apk,out);
        AlertDialog.Builder b=new AlertDialog.Builder(this);
        b.setTitle("Najm Space");
        b.setMessage("تم حفظ التطبيق داخل مساحة Najm Space. سيظهر في NAJM APP SPACE ضمن Legacy Container.");
        b.setPositiveButton("فتح NAJM APP SPACE",new DialogInterface.OnClickListener(){
            @Override public void onClick(DialogInterface d,int w){startActivity(new Intent(NajmFileManagerActivity.this,NajmAppsActivity.class));}
        });
        b.setNegativeButton("إغلاق",null);
        b.show();
    }

    private void installIntoSystem(File apk){
        try{
            File cacheDir=new File(getCacheDir(),"install");
            if(!cacheDir.exists())cacheDir.mkdirs();
            File copy=new File(cacheDir,safe(apk.getName()));
            copyFile(apk,copy);
            Uri uri=Uri.parse("content://"+getPackageName()+".apkprovider/"+Uri.encode(copy.getName()));
            Intent i=new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri,"application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);
        }catch(Exception e){
            AlertDialog.Builder b=new AlertDialog.Builder(this);
            b.setTitle("تعذر فتح المثبت");
            b.setMessage("تحقق من السماح بالتثبيت من مصادر غير معروفة.");
            b.setPositiveButton("موافق",null);
            b.show();
        }
    }

    private void copyFile(File inFile,File out){
        try{
            FileInputStream in=new FileInputStream(inFile);
            FileOutputStream fos=new FileOutputStream(out);
            byte[] buf=new byte[8192];int n;
            while((n=in.read(buf))>0)fos.write(buf,0,n);
            fos.close();in.close();
        }catch(Exception ignored){}
    }

    private String safe(String s){return s.replaceAll("[^A-Za-z0-9._-]","_");}

    private void openSystemPicker(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/vnd.android.package-archive");
        try{startActivityForResult(i,PICK_SYSTEM_FILE);}
        catch(Exception e){
            Intent old=new Intent(Intent.ACTION_GET_CONTENT);
            old.setType("application/vnd.android.package-archive");
            startActivityForResult(old,PICK_SYSTEM_FILE);
        }
    }

    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==PICK_SYSTEM_FILE && result==RESULT_OK && data!=null && data.getData()!=null){
            Uri uri=data.getData();
            String name=queryName(uri);
            if(name==null)name="selected_"+System.currentTimeMillis()+".apk";
            File temp=new File(getCacheDir(),safe(name));
            try{
                InputStream in=getContentResolver().openInputStream(uri);
                FileOutputStream out=new FileOutputStream(temp);
                byte[] buf=new byte[8192];int n;
                while((n=in.read(buf))>0)out.write(buf,0,n);
                out.close();in.close();
                showInstallChoice(temp);
            }catch(Exception ignored){}
        }
    }

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
    @Override protected void onResume(){
        super.onResume();
        NajmRecentStore.touch(this,"files","مدير الملفات","com.najmspace.app.NajmFileManagerActivity");
    }

}
