package com.najmspace.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;

import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.compat.PackageManagerCompat;

import java.io.File;

public final class NajmContainer {
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
