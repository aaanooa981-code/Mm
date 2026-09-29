package com.najmspace.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.morgoo.droidplugin.core.PluginDirHelper;
import android.content.pm.PackageInfo;
import android.os.Build;

import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.compat.PackageManagerCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public final class NajmContainer {
    private static final String REVANCED_PKG="app.revanced.manager.flutter";
    private static volatile boolean revancedWatcherRunning=false;
    private NajmContainer(){}

    public static boolean isReady(){
        try{return PluginManager.getInstance().isConnected();}
        catch(Throwable e){return false;}
    }

    public static void installAndLaunch(final Activity activity, final File apk, final String packageName, final int minApi){
        if(activity==null || apk==null || !apk.exists() || packageName==null || packageName.length()==0){
            show(activity,"Najm Container","ملف التطبيق غير صالح أو تعذر قراءة اسم الحزمة.");
            return;
        }

        if(minApi>0 && Build.VERSION.SDK_INT<minApi){
            show(activity,"غير متوافق",
                "هذا التطبيق يحتاج Android "+apiName(minApi)+" أو أحدث. الحاوية تمنع تدخل مثبت النظام، لكنها لا تضيف APIs غير موجودة في إصدار أندرويد نفسه.");
            return;
        }

        new Thread(new Runnable(){
            @Override public void run(){
                try{
                    final PluginManager pm=PluginManager.getInstance();
                    pm.waitForConnected(7000);
                    if(!pm.isConnected()){
                        show(activity,"Najm Container","تعذر تشغيل محرك الحاوية. أعد فتح Najm Space وجرب مرة أخرى.");
                        return;
                    }

                    PackageInfo existing=null;
                    try{existing=pm.getPackageInfo(packageName,0);}catch(Throwable ignored){}

                    if(existing==null){
                        final int result=pm.installPackage(apk.getAbsolutePath(),0);
                        if(result!=PackageManagerCompat.INSTALL_SUCCEEDED &&
                           result!=PackageManagerCompat.INSTALL_FAILED_ALREADY_EXISTS){
                            show(activity,"Najm Container","تعذر تثبيت التطبيق داخل الحاوية. رمز النتيجة: "+result);
                            return;
                        }
                    }

                    activity.runOnUiThread(new Runnable(){
                        @Override public void run(){launch(activity,packageName);}
                    });
                }catch(final Throwable e){
                    show(activity,"Najm Container","تعذر تشغيل التطبيق داخل الحاوية: "+shortMessage(e));
                }
            }
        },"NajmContainerInstall").start();
    }

    public static void launch(final Activity activity, final String packageName){
        if(REVANCED_PKG.equals(packageName))startRevancedOutputWatcher(activity.getApplicationContext());
        try{
            Intent i=activity.getPackageManager().getLaunchIntentForPackage(packageName);
            if(i==null){
                show(activity,"Najm Container","تمت إضافة التطبيق للحاوية لكن تعذر العثور على شاشة التشغيل الرئيسية.");
                return;
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(i);
        }catch(Throwable e){
            show(activity,"Najm Container","تعذر فتح التطبيق داخل الحاوية: "+shortMessage(e));
        }
    }

    public static boolean isInstalled(String packageName){
        try{
            PluginManager pm=PluginManager.getInstance();
            if(!pm.isConnected())return false;
            return pm.getPackageInfo(packageName,0)!=null;
        }catch(Throwable e){return false;}
    }

    public static void startRevancedOutputWatcher(final Context context){
        if(context==null || revancedWatcherRunning)return;
        revancedWatcherRunning=true;
        final Context app=context.getApplicationContext();

        Thread t=new Thread(new Runnable(){
            @Override public void run(){
                long deadline=System.currentTimeMillis()+(45L*60L*1000L);
                long lastLen=-1L;
                long lastModified=-1L;
                int stable=0;
                try{
                    File pluginData=new File(PluginDirHelper.getPluginDataDir(app,REVANCED_PKG));
                    File output=new File(pluginData,"files/ui_ephemeral/installer/output.apk");

                    while(System.currentTimeMillis()<deadline){
                        try{
                            if(output.exists() && output.isFile() && output.length()>100000L){
                                long len=output.length();
                                long mod=output.lastModified();
                                if(len==lastLen && mod==lastModified)stable++;
                                else stable=0;
                                lastLen=len;
                                lastModified=mod;

                                long stamp=(mod*31L)+len;
                                long done=app.getSharedPreferences("najmspace",Context.MODE_PRIVATE)
                                    .getLong("revanced_output_installed_stamp",-1L);

                                if(stable>=3 && stamp!=done){
                                    if(installRevancedOutput(app,output,stamp)){
                                        stable=0;
                                    }
                                }
                            }else{
                                stable=0;
                                lastLen=-1L;
                                lastModified=-1L;
                            }
                            Thread.sleep(1500L);
                        }catch(InterruptedException e){
                            break;
                        }catch(Throwable ignored){
                            try{Thread.sleep(2000L);}catch(Exception e){break;}
                        }
                    }
                }finally{
                    revancedWatcherRunning=false;
                }
            }
        },"NajmRevancedOutputWatcher");
        t.setDaemon(true);
        t.start();
    }

    private static boolean installRevancedOutput(final Context app,File output,long stamp){
        try{
            PackageInfo pi=app.getPackageManager().getPackageArchiveInfo(output.getAbsolutePath(),0);
            if(pi==null || pi.packageName==null || pi.packageName.length()==0)return false;

            File apps=NajmStorage.appsDir(app);
            String safe=pi.packageName.replaceAll("[^A-Za-z0-9._-]","_");
            File saved=new File(apps,"patched_"+safe+".apk");
            copyFile(output,saved);

            PluginManager pm=PluginManager.getInstance();
            pm.waitForConnected(5000);
            if(!pm.isConnected())return false;

            int result=pm.installPackage(saved.getAbsolutePath(),PackageManagerCompat.INSTALL_REPLACE_EXISTING);
            if(result==PackageManagerCompat.INSTALL_SUCCEEDED ||
               result==PackageManagerCompat.INSTALL_FAILED_ALREADY_EXISTS){
                app.getSharedPreferences("najmspace",Context.MODE_PRIVATE).edit()
                    .putLong("revanced_output_installed_stamp",stamp)
                    .putString("revanced_last_installed_package",pi.packageName)
                    .apply();

                final String name=loadArchiveLabel(app,pi,saved);
                new Handler(Looper.getMainLooper()).post(new Runnable(){
                    @Override public void run(){
                        Toast.makeText(app,"تم تثبيت "+name+" داخل Najm Space",Toast.LENGTH_LONG).show();
                    }
                });
                return true;
            }
        }catch(Throwable ignored){}
        return false;
    }

    private static String loadArchiveLabel(Context app,PackageInfo pi,File apk){
        try{
            pi.applicationInfo.sourceDir=apk.getAbsolutePath();
            pi.applicationInfo.publicSourceDir=apk.getAbsolutePath();
            CharSequence label=app.getPackageManager().getApplicationLabel(pi.applicationInfo);
            if(label!=null && label.length()>0)return label.toString();
        }catch(Throwable ignored){}
        return pi.packageName==null?"التطبيق":pi.packageName;
    }

    private static void copyFile(File from,File to) throws Exception{
        FileInputStream in=new FileInputStream(from);
        FileOutputStream out=new FileOutputStream(to);
        byte[] buf=new byte[32768];
        int n;
        while((n=in.read(buf))>0)out.write(buf,0,n);
        out.flush();
        out.close();
        in.close();
    }

    private static String apiName(int api){
        if(api==26)return "8.0";
        if(api==23)return "6.0";
        if(api==21)return "5.0";
        if(api==19)return "4.4";
        return "(API "+api+")";
    }

    private static String shortMessage(Throwable e){
        String m=e==null?null:e.getMessage();
        if(m==null || m.trim().length()==0)return e==null?"خطأ غير معروف":e.getClass().getSimpleName();
        return m.length()>160?m.substring(0,160):m;
    }

    private static void show(final Activity a, final String title, final String message){
        if(a==null)return;
        a.runOnUiThread(new Runnable(){
            @Override public void run(){
                if(a.isFinishing())return;
                new AlertDialog.Builder(a)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("موافق",null)
                    .show();
            }
        });
    }
}
