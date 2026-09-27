package com.najmspace.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public final class NajmStorage {
    public static final String AUTO="auto";
    public static final String INTERNAL="internal";
    public static final String EXTERNAL="external";
    public static final String CUSTOM="custom";

    private NajmStorage(){}

    private static SharedPreferences prefs(Context c){
        return c.getSharedPreferences("najmspace",Context.MODE_PRIVATE);
    }

    public static void setMode(Context c,String mode){
        prefs(c).edit().putString("storage_mode",mode).apply();
    }

    public static String getMode(Context c){
        return prefs(c).getString("storage_mode",AUTO);
    }

    public static void setCustomTree(Context c,String uri){
        prefs(c).edit().putString("storage_custom_uri",uri).putString("storage_mode",CUSTOM).apply();
    }

    public static String getCustomTree(Context c){
        return prefs(c).getString("storage_custom_uri","");
    }

    public static File base(Context c){
        String mode=getMode(c);
        File chosen=null;

        if(INTERNAL.equals(mode)){
            chosen=new File(c.getFilesDir(),"NajmSpace");
        }else if(EXTERNAL.equals(mode)){
            File ext=null;
            try{ext=c.getExternalFilesDir(null);}catch(Exception ignored){}
            if(ext!=null)chosen=new File(ext,"NajmSpace");
        }else{
            File ext=null;
            try{ext=c.getExternalFilesDir(null);}catch(Exception ignored){}
            if(ext!=null && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
                chosen=new File(ext,"NajmSpace");
            }else{
                chosen=new File(c.getFilesDir(),"NajmSpace");
            }
        }

        if(chosen==null)chosen=new File(c.getFilesDir(),"NajmSpace");
        if(!chosen.exists())chosen.mkdirs();
        return chosen;
    }

    public static File appsDir(Context c){
        File d=new File(base(c),"apps");
        if(!d.exists())d.mkdirs();
        migrateLegacy(new File(c.getFilesDir(),"najm_apps"),d);
        return d;
    }

    public static File miniAppsDir(Context c){
        File d=new File(base(c),"mini_apps");
        if(!d.exists())d.mkdirs();
        migrateLegacy(new File(c.getFilesDir(),"najm_mini_apps"),d);
        return d;
    }

    public static File downloadsDir(Context c){
        File d=new File(base(c),"downloads");
        if(!d.exists())d.mkdirs();
        return d;
    }

    public static String modeLabel(Context c){
        String m=getMode(c);
        if(INTERNAL.equals(m))return "الذاكرة الداخلية";
        if(EXTERNAL.equals(m))return "الذاكرة الخارجية";
        if(CUSTOM.equals(m))return "مجلد مخصص";
        return "تلقائي";
    }

    public static String locationLabel(Context c){
        File b=base(c);
        String p=b.getAbsolutePath();
        return modeLabel(c)+" • "+(p.contains("/Android/data/")?"مساحة خارجية خاصة بـ Najm Space":"مساحة داخلية");
    }

    public static long freeBytes(Context c){
        try{
            StatFs s=new StatFs(base(c).getAbsolutePath());
            if(android.os.Build.VERSION.SDK_INT>=18)return s.getAvailableBytes();
            return (long)s.getAvailableBlocks()*(long)s.getBlockSize();
        }catch(Exception e){return 0;}
    }

    public static long totalBytes(Context c){
        try{
            StatFs s=new StatFs(base(c).getAbsolutePath());
            if(android.os.Build.VERSION.SDK_INT>=18)return s.getTotalBytes();
            return (long)s.getBlockCount()*(long)s.getBlockSize();
        }catch(Exception e){return 0;}
    }

    public static String format(long b){
        if(b<1024)return b+" B";
        double kb=b/1024.0;
        if(kb<1024)return String.format(java.util.Locale.US,"%.1f KB",kb);
        double mb=kb/1024.0;
        if(mb<1024)return String.format(java.util.Locale.US,"%.1f MB",mb);
        return String.format(java.util.Locale.US,"%.2f GB",mb/1024.0);
    }

    private static void migrateLegacy(File old,File dest){
        try{
            if(old==null || !old.exists() || !old.isDirectory())return;
            File[] fs=old.listFiles();
            if(fs==null)return;
            for(File f:fs){
                File out=new File(dest,f.getName());
                if(f.isDirectory()){
                    if(!out.exists())out.mkdirs();
                    migrateDir(f,out);
                }else if(!out.exists()){
                    copy(f,out);
                }
            }
        }catch(Exception ignored){}
    }

    private static void migrateDir(File from,File to){
        File[] fs=from.listFiles();
        if(fs==null)return;
        for(File f:fs){
            File out=new File(to,f.getName());
            if(f.isDirectory()){
                if(!out.exists())out.mkdirs();
                migrateDir(f,out);
            }else if(!out.exists())copy(f,out);
        }
    }

    private static void copy(File inFile,File out){
        try{
            FileInputStream in=new FileInputStream(inFile);
            FileOutputStream fos=new FileOutputStream(out);
            byte[] buf=new byte[8192];int n;
            while((n=in.read(buf))>0)fos.write(buf,0,n);
            fos.close();in.close();
        }catch(Exception ignored){}
    }
}
