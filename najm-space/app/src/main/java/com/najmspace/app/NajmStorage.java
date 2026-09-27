package com.najmspace.app;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public final class NajmStorage {
    private NajmStorage(){}

    public static File base(Context c){
        File ext=null;
        try{ ext=c.getExternalFilesDir(null); }catch(Exception ignored){}
        File base;
        if(ext!=null && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            base=new File(ext,"NajmSpace");
        }else{
            base=new File(c.getFilesDir(),"NajmSpace");
        }
        if(!base.exists())base.mkdirs();
        return base;
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

    public static String locationLabel(Context c){
        File b=base(c);
        String p=b.getAbsolutePath();
        return p.contains("/Android/data/") ? "مساحة التخزين الخارجية الخاصة بـ Najm Space" : "مساحة التخزين الداخلية";
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
        double gb=mb/1024.0;
        return String.format(java.util.Locale.US,"%.2f GB",gb);
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
