package com.najmspace.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

public class NajmFileManagerActivity extends Activity {
    private static final int PICK_SYSTEM_FILE=401;
    private LinearLayout list;
    private TextView pathView,countView;
    private File current;

    private final int BG0=Color.rgb(5,12,22);
    private final int BG1=Color.rgb(9,23,39);
    private final int PANEL=Color.rgb(14,29,47);
    private final int LINE=Color.rgb(39,66,92);
    private final int SOFT=Color.rgb(166,192,217);
    private final int ACCENT=Color.rgb(78,174,236);

    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    private GradientDrawable bg(int color,int radius,int stroke){
        GradientDrawable g=new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        if(stroke!=0)g.setStroke(dp(1),stroke);
        return g;
    }

    private TextView txt(String s,float size,int color,boolean bold){
        TextView t=new TextView(this);
        t.setText(s);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private void feedback(final View v){
        v.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View view,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN)view.animate().scaleX(.97f).scaleY(.97f).alpha(.82f).setDuration(55).start();
                if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL)view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(100).start();
                return false;
            }
        });
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(12),dp(16),dp(8));
        if(Build.VERSION.SDK_INT>=17)root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{BG0,BG1,Color.rgb(7,16,29)}));

        root.addView(buildHeader(),new LinearLayout.LayoutParams(-1,dp(72)));
        root.addView(buildQuickBar(),new LinearLayout.LayoutParams(-1,dp(58)));

        LinearLayout pathCard=new LinearLayout(this);
        pathCard.setGravity(Gravity.CENTER_VERTICAL);
        pathCard.setPadding(dp(14),0,dp(14),0);
        pathCard.setBackground(bg(Color.rgb(12,27,44),17,LINE));
        pathView=txt("",12,Color.rgb(132,197,244),false);
        pathView.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        pathView.setSingleLine(true);
        pathCard.addView(pathView,new LinearLayout.LayoutParams(0,-1,1));
        countView=txt("",11,SOFT,true);
        countView.setGravity(Gravity.CENTER);
        pathCard.addView(countView,new LinearLayout.LayoutParams(dp(92),-1));
        LinearLayout.LayoutParams pcp=new LinearLayout.LayoutParams(-1,dp(43));
        pcp.topMargin=dp(7);
        root.addView(pathCard,pcp);

        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0,dp(5),0,dp(7));
        scroll.addView(list,new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        root.addView(NajmNavigation.create(this),new LinearLayout.LayoutParams(-1,dp(48)));
        setContentView(root);

        File start=new File("/storage/emulated/0");
        if(!start.exists())start=Environment.getExternalStorageDirectory();
        show(start);
    }

    private View buildHeader(){
        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(15),dp(7),dp(15),dp(7));
        header.setBackground(bg(Color.argb(225,11,24,39),21,LINE));

        LinearLayout titleBox=new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
        TextView title=txt("NAJM FILES",24,Color.WHITE,true);
        title.setGravity(Gravity.RIGHT);
        TextView sub=txt("مدير الملفات و USB",11,SOFT,false);
        sub.setGravity(Gravity.RIGHT);
        titleBox.addView(title,new LinearLayout.LayoutParams(-1,dp(31)));
        titleBox.addView(sub,new LinearLayout.LayoutParams(-1,dp(22)));
        header.addView(titleBox,new LinearLayout.LayoutParams(0,-1,1));

        TextView pick=quickButton("＋ APK",Color.rgb(34,101,150));
        pick.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){openSystemPicker();}});
        header.addView(pick,new LinearLayout.LayoutParams(dp(110),dp(42)));
        return header;
    }

    private View buildQuickBar(){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0,dp(7),0,dp(3));
        addQuick(row,"الهاتف",new File("/storage/emulated/0"),Color.rgb(38,91,139));
        addQuick(row,"التخزين",new File("/storage"),Color.rgb(48,73,112));
        addQuick(row,"USB",findUsbRoot(),Color.rgb(38,111,89));

        TextView picker=quickButton("اختيار ملف",Color.rgb(73,66,111));
        picker.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){openSystemPicker();}});
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(44),1);
        lp.rightMargin=dp(6);
        row.addView(picker,lp);
        return row;
    }

    private TextView quickButton(String label,int color){
        TextView b=txt(label,13,Color.WHITE,true);
        b.setGravity(Gravity.CENTER);
        b.setBackground(bg(color,15,Color.argb(100,118,188,235)));
        b.setClickable(true);b.setFocusable(true);
        feedback(b);
        return b;
    }

    private void addQuick(LinearLayout row,String label,final File target,int color){
        TextView b=quickButton(label,color);
        b.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){if(target!=null&&target.exists())show(target);}
        });
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(44),1);
        lp.rightMargin=dp(6);
        row.addView(b,lp);
    }

    private File findUsbRoot(){
        String[] roots={"/storage/usbotg","/storage/usb0","/storage/usb1","/storage/USB","/mnt/usb_storage","/mnt/usbhost1","/mnt/usbotg","/mnt/media_rw"};
        for(String p:roots){
            File f=new File(p);
            if(f.exists()&&f.canRead())return f;
        }
        File storage=new File("/storage");
        File[] fs=storage.listFiles();
        if(fs!=null){
            for(File f:fs){
                String n=f.getName().toLowerCase();
                if((n.contains("usb")||n.contains("otg"))&&f.canRead())return f;
            }
        }
        return new File("/storage");
    }

    private void show(final File dir){
        if(dir==null||!dir.exists()||!dir.isDirectory())return;
        current=dir;
        pathView.setText(dir.getAbsolutePath());
        list.removeAllViews();

        File parent=dir.getParentFile();
        if(parent!=null){
            TextView up=rowText("↑  رجوع للمجلد السابق","مجلد",Color.rgb(118,194,245),true);
            up.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){File p=current.getParentFile();if(p!=null)show(p);}});
            addRow(up);
        }

        File[] files;
        try{files=dir.listFiles();}catch(Exception e){files=null;}
        if(files==null){
            countView.setText("غير متاح");
            addMessage("لا يمكن قراءة هذا المجلد. جرّب زر اختيار ملف أو USB.");
            return;
        }

        Arrays.sort(files,new Comparator<File>(){
            @Override public int compare(File a,File b){
                if(a.isDirectory()!=b.isDirectory())return a.isDirectory()?-1:1;
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });

        int shown=0;
        for(final File f:files){
            if(f.isHidden())continue;
            String kind=f.isDirectory()?"مجلد":fileKind(f);
            int color=f.isDirectory()?Color.rgb(132,199,248):Color.WHITE;
            TextView row=rowText((f.isDirectory()?"▰  ":"▤  ")+f.getName(),kind,color,f.isDirectory());
            row.setOnClickListener(new View.OnClickListener(){
                @Override public void onClick(View v){
                    if(f.isDirectory())show(f);
                    else if(f.getName().toLowerCase().endsWith(".apk"))showInstallChoice(f);
                }
            });
            addRow(row);
            shown++;
        }
        countView.setText(shown+" عنصر");
        if(shown==0)addMessage("هذا المجلد فارغ.");
    }

    private String fileKind(File f){
        String n=f.getName().toLowerCase();
        if(n.endsWith(".apk"))return "APK • اضغط للتثبيت";
        if(n.endsWith(".zip"))return "ZIP";
        if(n.endsWith(".mp4")||n.endsWith(".mkv")||n.endsWith(".avi"))return "فيديو";
        if(n.endsWith(".mp3")||n.endsWith(".wav")||n.endsWith(".m4a"))return "صوت";
        return "ملف";
    }

    private TextView rowText(String title,String meta,int color,boolean bold){
        TextView t=txt(title+"\n"+meta,14,color,bold);
        t.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
        t.setPadding(dp(14),dp(6),dp(14),dp(6));
        t.setMaxLines(2);
        t.setBackground(bg(PANEL,15,Color.rgb(31,54,76)));
        t.setClickable(true);
        feedback(t);
        return t;
    }

    private void addRow(View row){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));
        lp.bottomMargin=dp(5);
        list.addView(row,lp);
    }

    private void addMessage(String s){
        TextView t=txt(s,14,SOFT,false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(15),dp(24),dp(15),dp(24));
        t.setBackground(bg(Color.rgb(12,25,40),16,LINE));
        list.addView(t,new LinearLayout.LayoutParams(-1,dp(94)));
    }

    private void showInstallChoice(final File apk){
        new AlertDialog.Builder(this)
            .setTitle("تثبيت التطبيق")
            .setMessage(apk.getName()+"\n\nاختر مكان التثبيت:")
            .setPositiveButton("داخل Najm Space",new DialogInterface.OnClickListener(){@Override public void onClick(DialogInterface d,int w){installIntoNajm(apk);}})
            .setNegativeButton("في نظام أندرويد",new DialogInterface.OnClickListener(){@Override public void onClick(DialogInterface d,int w){installIntoSystem(apk);}})
            .setNeutralButton("إلغاء",null)
            .show();
    }

    private void installIntoNajm(File apk){
        try{
            android.content.pm.PackageInfo pi=getPackageManager().getPackageArchiveInfo(apk.getAbsolutePath(),0);
            String pkg=pi==null?null:pi.packageName;
            if(pkg==null||pkg.length()==0){
                new AlertDialog.Builder(this).setTitle("Najm Container").setMessage("تعذر قراءة اسم حزمة التطبيق.").setPositiveButton("موافق",null).show();
                return;
            }
            File dir=NajmStorage.appsDir(this);
            File out=new File(dir,safe(apk.getName()));
            copyFile(apk,out);
            NajmContainer.installAndLaunch(this,out,pkg,0);
        }catch(Exception e){
            new AlertDialog.Builder(this).setTitle("Najm Container").setMessage("تعذر تجهيز التطبيق داخل الحاوية.").setPositiveButton("موافق",null).show();
        }
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
            new AlertDialog.Builder(this).setTitle("تعذر فتح المثبت").setMessage("تحقق من السماح بالتثبيت من مصادر غير معروفة.").setPositiveButton("موافق",null).show();
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
        if(request==PICK_SYSTEM_FILE&&result==RESULT_OK&&data!=null&&data.getData()!=null){
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
            if(c!=null&&c.moveToFirst()){
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
