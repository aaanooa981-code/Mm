package com.najmspace.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
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
    private GridLayout grid;
    private File appsDir;
    private File miniDir;

    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        appsDir=NajmStorage.appsDir(this);
        miniDir=NajmStorage.miniAppsDir(this);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14),dp(10),dp(14),dp(8));
        root.setBackgroundColor(Color.rgb(8,16,29));

        LinearLayout top=new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView title=new TextView(this);
        title.setText("NAJM APP SPACE");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        top.addView(title,new LinearLayout.LayoutParams(0,dp(52),1));

        Button importBtn=new Button(this);
        importBtn.setText("+ APK");
        importBtn.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){pickApk();}});
        top.addView(importBtn,new LinearLayout.LayoutParams(dp(100),dp(46)));

        Button miniBtn=new Button(this);
        miniBtn.setText("+ Mini");
        miniBtn.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){pickMini();}});
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(dp(100),dp(46));mp.leftMargin=dp(6);
        top.addView(miniBtn,mp);

        root.addView(top);

        TextView hint=new TextView(this);
        hint.setText("ⓘ  اضغط للتلميحات");
        hint.setTextColor(Color.rgb(105,190,255));
        hint.setTextSize(13);
        hint.setPadding(0,0,0,dp(8));
        hint.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                NajmHints.show(NajmAppsActivity.this,"app_grid","تلميح",
                    "التطبيقات تظهر كأيقونات. اضغط للتشغيل، واضغط مطولًا على التطبيق المستورد للحذف أو عرض المعلومات.");
            }
        });
        root.addView(hint);

        ScrollView scroll=new ScrollView(this);
        grid=new GridLayout(this);
        grid.setColumnCount(6);
        grid.setUseDefaultMargins(false);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        grid.setPadding(0,0,0,dp(8));
        scroll.addView(grid,new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        root.addView(NajmNavigation.create(this),new LinearLayout.LayoutParams(-1,dp(62)));

        setContentView(root);
        refresh();
    }

    private void refresh(){
        grid.removeAllViews();

        addShortcut("YouTube","▶",Color.rgb(198,45,55),new View.OnClickListener(){
            @Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"YouTube","https://m.youtube.com");}
        });

        addShortcut("المتصفح","◎",Color.rgb(45,110,180),new View.OnClickListener(){
            @Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"Najm Browser","https://www.google.com");}
        });

        addShortcut("الخرائط","⌖",Color.rgb(36,125,96),new View.OnClickListener(){
            @Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"Najm Maps","https://www.openstreetmap.org");}
        });

        addShortcut("الملفات","▤",Color.rgb(48,118,148),new View.OnClickListener(){
            @Override public void onClick(View v){startActivity(new Intent(NajmAppsActivity.this,NajmFileManagerActivity.class));}
        });

        addShortcut("AppGallery","✦",Color.rgb(180,46,49),new View.OnClickListener(){
            @Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"HUAWEI AppGallery","https://consumer.huawei.com/sa/mobileservices/appgallery/");}
        });

        addShortcut("الإعدادات","⚙",Color.rgb(88,76,132),new View.OnClickListener(){
            @Override public void onClick(View v){startActivity(new Intent(NajmAppsActivity.this,NajmSettingsActivity.class));}
        });

        File[] minis=miniDir.listFiles();
        if(minis!=null){
            int count=1;
            for(final File folder:minis){
                if(!folder.isDirectory())continue;
                final File entry=findEntry(folder);
                if(entry==null)continue;
                addMiniTile(folder,entry,"Mini App "+count);
                count++;
            }
        }

        File[] files=appsDir.listFiles();
        if(files!=null){
            PackageManager pm=getPackageManager();
            for(final File f:files){
                if(!f.getName().toLowerCase().endsWith(".apk"))continue;
                PackageInfo pi=pm.getPackageArchiveInfo(f.getAbsolutePath(),0);
                String appName=f.getName();
                String pkg="";
                Drawable icon=null;

                if(pi!=null){
                    pi.applicationInfo.sourceDir=f.getAbsolutePath();
                    pi.applicationInfo.publicSourceDir=f.getAbsolutePath();
                    try{appName=pm.getApplicationLabel(pi.applicationInfo).toString();}catch(Exception ignored){}
                    try{icon=pm.getApplicationIcon(pi.applicationInfo);}catch(Exception ignored){}
                    pkg=pi.packageName==null?"":pi.packageName;
                }

                final String finalName=appName;
                final String finalPkg=pkg;
                final boolean isAppGallery="com.huawei.appmarket".equals(finalPkg) || finalName.toLowerCase().contains("appgallery");

                View tile=createAppTile(finalName,icon,isAppGallery?"جاهز":"محفوظ");
                tile.setOnClickListener(new View.OnClickListener(){
                    @Override public void onClick(View v){
                        if(isAppGallery){
                            RuntimeRouter.openWeb(NajmAppsActivity.this,"HUAWEI AppGallery","https://consumer.huawei.com/sa/mobileservices/appgallery/");
                        }else{
                            NajmHints.show(NajmAppsActivity.this,"legacy_apk","التطبيق",
                                "هذا الـAPK محفوظ داخل Najm Space. تشغيل APK كامل داخل الحاوية ينتظر Legacy Container.");
                        }
                    }
                });
                tile.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override public boolean onLongClick(View v){
                        showApkOptions(f,finalName,finalPkg);
                        return true;
                    }
                });
                addTile(tile);
            }
        }
    }

    private void addShortcut(String name,String symbol,int color,View.OnClickListener listener){
        LinearLayout tile=createBaseTile(name);
        TextView icon=new TextView(this);
        icon.setText(symbol);
        icon.setTextColor(Color.WHITE);
        icon.setTextSize(34);
        icon.setGravity(Gravity.CENTER);
        icon.setBackgroundColor(color);
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(dp(68),dp(68));
        ilp.topMargin=dp(4);
        tile.addView(icon,ilp);
        addNameLabel(tile,name);
        tile.setOnClickListener(listener);
        addTile(tile);
    }

    private void addMiniTile(final File folder,final File entry,String name){
        LinearLayout tile=createBaseTile(name);
        ImageView icon=new ImageView(this);
        icon.setImageResource(R.mipmap.ic_launcher);
        icon.setPadding(dp(6),dp(6),dp(6),dp(6));
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(dp(68),dp(68));
        ilp.topMargin=dp(4);
        tile.addView(icon,ilp);
        addNameLabel(tile,name);

        tile.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                Intent i=new Intent(NajmAppsActivity.this,MiniAppRuntimeActivity.class);
                i.putExtra("path",entry.getAbsolutePath());
                i.putExtra("title",folder.getName());
                startActivity(i);
            }
        });

        tile.setOnLongClickListener(new View.OnLongClickListener(){
            @Override public boolean onLongClick(View v){
                new AlertDialog.Builder(NajmAppsActivity.this)
                    .setTitle("Mini App")
                    .setItems(new String[]{"تشغيل","حذف"},new DialogInterface.OnClickListener(){
                        @Override public void onClick(DialogInterface d,int which){
                            if(which==0){
                                Intent i=new Intent(NajmAppsActivity.this,MiniAppRuntimeActivity.class);
                                i.putExtra("path",entry.getAbsolutePath());
                                i.putExtra("title",folder.getName());
                                startActivity(i);
                            }else{
                                deleteTree(folder);
                                refresh();
                            }
                        }
                    }).show();
                return true;
            }
        });
        addTile(tile);
    }

    private LinearLayout createAppTile(String name,Drawable iconDrawable,String badge){
        LinearLayout tile=createBaseTile(name);

        if(iconDrawable!=null){
            ImageView icon=new ImageView(this);
            icon.setImageDrawable(iconDrawable);
            icon.setPadding(dp(4),dp(4),dp(4),dp(4));
            LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(dp(68),dp(68));
            ilp.topMargin=dp(4);
            tile.addView(icon,ilp);
        }else{
            ImageView icon=new ImageView(this);
            icon.setImageResource(R.mipmap.ic_launcher);
            icon.setPadding(dp(6),dp(6),dp(6),dp(6));
            LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(dp(68),dp(68));
            ilp.topMargin=dp(4);
            tile.addView(icon,ilp);
        }

        addNameLabel(tile,name);
        if(badge!=null && badge.length()>0){
            TextView state=new TextView(this);
            state.setText(badge);
            state.setTextColor(Color.rgb(110,200,255));
            state.setTextSize(9);
            state.setGravity(Gravity.CENTER);
            tile.addView(state,new LinearLayout.LayoutParams(-1,dp(16)));
        }
        return tile;
    }

    private LinearLayout createBaseTile(String name){
        LinearLayout tile=new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER_HORIZONTAL);
        tile.setPadding(dp(7),dp(6),dp(7),dp(6));

        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.rgb(15,29,47));
        bg.setCornerRadius(dp(20));
        bg.setStroke(dp(1),Color.rgb(34,58,82));
        tile.setBackground(bg);

        tile.setClickable(true);
        tile.setFocusable(true);
        tile.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View v,android.view.MotionEvent e){
                if(e.getAction()==android.view.MotionEvent.ACTION_DOWN){
                    v.animate().scaleX(.95f).scaleY(.95f).alpha(.86f).setDuration(55).start();
                }else if(e.getAction()==android.view.MotionEvent.ACTION_UP || e.getAction()==android.view.MotionEvent.ACTION_CANCEL){
                    v.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(95).start();
                }
                return false;
            }
        });
        return tile;
    }

    private void addNameLabel(LinearLayout tile,String name){
        TextView label=new TextView(this);
        label.setText(name);
        label.setTextColor(Color.WHITE);
        label.setTextSize(12);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(2);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(34));
        lp.topMargin=dp(5);
        tile.addView(label,lp);
    }

    private void addTile(View tile){
        GridLayout.LayoutParams lp=new GridLayout.LayoutParams();
        lp.width=0;
        lp.height=dp(138);
        lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
        lp.setMargins(dp(5),dp(5),dp(5),dp(5));
        grid.addView(tile,lp);
    }

    private void showApkOptions(final File f,String appName,String pkg){
        String detail=(pkg==null||pkg.length()==0)?f.getName():pkg;
        new AlertDialog.Builder(this)
            .setTitle(appName)
            .setMessage(detail)
            .setItems(new String[]{"معلومات","حذف"},new DialogInterface.OnClickListener(){
                @Override public void onClick(DialogInterface d,int which){
                    if(which==0){
                        NajmHints.show(NajmAppsActivity.this,"apk_info","معلومات التطبيق",
                            "الملف محفوظ داخل مساحة Najm Space في:\n"+f.getAbsolutePath());
                    }else{
                        f.delete();
                        refresh();
                    }
                }
            }).show();
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
            File[] fs=f.listFiles();
            if(fs!=null)for(File x:fs)deleteTree(x);
        }
        f.delete();
    }

    @Override protected void onResume(){
        super.onResume();
        NajmRecentStore.touch(this,"apps","NAJM APP SPACE","com.najmspace.app.NajmAppsActivity");
    }
}
