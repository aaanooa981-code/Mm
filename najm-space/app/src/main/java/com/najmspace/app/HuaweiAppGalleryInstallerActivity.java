package com.najmspace.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class HuaweiAppGalleryInstallerActivity extends Activity {
    private static final int PICK_APPGALLERY=731;
    private static final String PKG="com.huawei.appmarket";
    private static final String OFFICIAL_URL="https://consumer.huawei.com/sa/mobileservices/appgallery/";

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        route();
    }

    private void route(){
        if(Build.VERSION.SDK_INT<21){
            new AlertDialog.Builder(this)
                .setTitle("HUAWEI AppGallery")
                .setMessage("الإصدار الرسمي الحالي من AppGallery يحتاج Android 5.0 أو أحدث. على Android 4.4 سيفتح Najm Space صفحة المتجر الرسمية بدل تشغيل التطبيق داخل الحاوية.")
                .setPositiveButton("فتح المتجر الرسمي",new DialogInterface.OnClickListener(){
                    @Override public void onClick(DialogInterface d,int w){openOfficialPage();}
                })
                .setNegativeButton("إلغاء",new DialogInterface.OnClickListener(){
                    @Override public void onClick(DialogInterface d,int w){finish();}
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener(){@Override public void onCancel(DialogInterface d){finish();}})
                .show();
            return;
        }

        if(NajmContainer.isInstalled(PKG)){
            NajmContainer.launch(this,PKG);
            finishLater();
            return;
        }

        File imported=findImportedApk();
        if(imported!=null){
            NajmContainer.installAndLaunch(this,imported,PKG,21);
            finishLater();
            return;
        }

        File host=copyFromHostIfAvailable();
        if(host!=null){
            NajmContainer.installAndLaunch(this,host,PKG,21);
            finishLater();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("HUAWEI AppGallery داخل Najm")
            .setMessage("سيتم تثبيت AppGallery داخل الحاوية وليس فتحه كصفحة ويب. إذا كان ملف AppGallery APK موجودًا عندك اختره الآن. وإذا لم يكن موجودًا، نزّله من موقع هواوي الرسمي ثم ارجع واختر الملف.")
            .setPositiveButton("اختيار APK",new DialogInterface.OnClickListener(){
                @Override public void onClick(DialogInterface d,int w){pickApk();}
            })
            .setNegativeButton("تنزيل من هواوي",new DialogInterface.OnClickListener(){
                @Override public void onClick(DialogInterface d,int w){openOfficialPage();}
            })
            .setNeutralButton("إلغاء",new DialogInterface.OnClickListener(){
                @Override public void onClick(DialogInterface d,int w){finish();}
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener(){@Override public void onCancel(DialogInterface d){finish();}})
            .show();
    }

    private File findImportedApk(){
        try{
            File[] files=NajmStorage.appsDir(this).listFiles();
            if(files==null)return null;
            for(File f:files){
                if(!f.isFile()||!f.getName().toLowerCase().endsWith(".apk"))continue;
                PackageInfo pi=getPackageManager().getPackageArchiveInfo(f.getAbsolutePath(),0);
                if(pi!=null&&PKG.equals(pi.packageName))return f;
            }
        }catch(Throwable ignored){}
        return null;
    }

    private File copyFromHostIfAvailable(){
        try{
            ApplicationInfo ai=getPackageManager().getApplicationInfo(PKG,0);
            if(ai==null||ai.sourceDir==null)return null;
            File source=new File(ai.sourceDir);
            if(!source.exists())return null;
            File out=new File(NajmStorage.appsDir(this),"huawei-appgallery-host.apk");
            copyFile(source,out);
            PackageInfo pi=getPackageManager().getPackageArchiveInfo(out.getAbsolutePath(),0);
            if(pi!=null&&PKG.equals(pi.packageName))return out;
        }catch(Throwable ignored){}
        return null;
    }

    private void pickApk(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/vnd.android.package-archive");
        try{startActivityForResult(i,PICK_APPGALLERY);}
        catch(Exception e){
            Intent old=new Intent(Intent.ACTION_GET_CONTENT);
            old.setType("application/vnd.android.package-archive");
            startActivityForResult(old,PICK_APPGALLERY);
        }
    }

    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request!=PICK_APPGALLERY||result!=RESULT_OK||data==null||data.getData()==null){finish();return;}
        try{
            File out=new File(NajmStorage.appsDir(this),"huawei-appgallery.apk");
            InputStream in=getContentResolver().openInputStream(data.getData());
            FileOutputStream fos=new FileOutputStream(out);
            byte[] buf=new byte[32768];int n;
            while((n=in.read(buf))>0)fos.write(buf,0,n);
            fos.flush();fos.close();in.close();

            PackageInfo pi=getPackageManager().getPackageArchiveInfo(out.getAbsolutePath(),0);
            if(pi==null||!PKG.equals(pi.packageName)){
                out.delete();
                new AlertDialog.Builder(this)
                    .setTitle("ملف غير صحيح")
                    .setMessage("الملف المحدد ليس HUAWEI AppGallery. اسم الحزمة المطلوب: "+PKG)
                    .setPositiveButton("موافق",new DialogInterface.OnClickListener(){@Override public void onClick(DialogInterface d,int w){finish();}})
                    .show();
                return;
            }

            NajmContainer.installAndLaunch(this,out,PKG,21);
            finishLater();
        }catch(Exception e){
            new AlertDialog.Builder(this)
                .setTitle("HUAWEI AppGallery")
                .setMessage("تعذر تجهيز ملف المتجر داخل Najm Space.")
                .setPositiveButton("موافق",new DialogInterface.OnClickListener(){@Override public void onClick(DialogInterface d,int w){finish();}})
                .show();
        }
    }

    private void openOfficialPage(){
        try{
            Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(OFFICIAL_URL));
            startActivity(i);
        }catch(Exception e){
            Intent i=new Intent(this,InternalBrowserActivity.class);
            i.putExtra("title","HUAWEI AppGallery");
            i.putExtra("url",OFFICIAL_URL);
            startActivity(i);
        }
        finish();
    }

    private void finishLater(){
        getWindow().getDecorView().postDelayed(new Runnable(){@Override public void run(){if(!isFinishing())finish();}},1200L);
    }

    private void copyFile(File from,File to) throws Exception{
        FileInputStream in=new FileInputStream(from);
        FileOutputStream out=new FileOutputStream(to);
        byte[] buf=new byte[32768];int n;
        while((n=in.read(buf))>0)out.write(buf,0,n);
        out.flush();out.close();in.close();
    }
}
