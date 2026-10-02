package com.najmspace.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NajmAppsActivity extends Activity {
    private static final int PICK_APK=201;
    private static final int PICK_MINI=202;

    private GridLayout grid;
    private File appsDir;
    private File miniDir;
    private EditText search;
    private TextView summary;
    private String filter="";

    private final int BG_A=Color.rgb(5,12,22);
    private final int BG_B=Color.rgb(9,23,39);
    private final int CARD=Color.rgb(14,28,45);
    private final int CARD_2=Color.rgb(19,37,58);
    private final int LINE=Color.rgb(41,67,92);
    private final int TEXT_SOFT=Color.rgb(170,193,217);
    private final int ACCENT=Color.rgb(76,171,235);

    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    private GradientDrawable bg(int color,int radius,int stroke){
        GradientDrawable g=new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        if(stroke!=0)g.setStroke(dp(1),stroke);
        return g;
    }

    private TextView text(String value,float size,int color,boolean bold){
        TextView t=new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        appsDir=NajmStorage.appsDir(this);
        miniDir=NajmStorage.miniAppsDir(this);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(14),dp(18),dp(10));
        if(Build.VERSION.SDK_INT>=17)root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{BG_A,BG_B,Color.rgb(7,16,29)}));

        root.addView(buildHeader(),new LinearLayout.LayoutParams(-1,dp(78)));
        root.addView(buildTools(),new LinearLayout.LayoutParams(-1,dp(58)));

        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        grid=new GridLayout(this);
        grid.setColumnCount(5);
        grid.setUseDefaultMargins(false);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        grid.setPadding(0,dp(4),0,dp(10));
        scroll.addView(grid,new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout nav=NajmNavigation.create(this);
        LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,dp(50));
        np.topMargin=dp(4);
        root.addView(nav,np);

        setContentView(root);
        refresh();
    }

    private View buildHeader(){
        LinearLayout top=new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(14),dp(6),dp(14),dp(6));
        top.setBackground(bg(Color.argb(220,11,24,39),22,LINE));

        LinearLayout titleBox=new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
        TextView title=text("NAJM SPACE",26,Color.WHITE,true);
        title.setGravity(Gravity.RIGHT);
        TextView sub=text("مساحة التطبيقات داخل السيارة",12,TEXT_SOFT,false);
        sub.setGravity(Gravity.RIGHT);
        titleBox.addView(title,new LinearLayout.LayoutParams(-1,dp(34)));
        titleBox.addView(sub,new LinearLayout.LayoutParams(-1,dp(24)));
        top.addView(titleBox,new LinearLayout.LayoutParams(0,-1,1));

        TextView status=text("●  CONTAINER",11,Color.rgb(121,226,167),true);
        status.setGravity(Gravity.CENTER);
        status.setBackground(bg(Color.rgb(16,49,44),16,Color.rgb(43,91,77)));
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(126),dp(38));
        sp.leftMargin=dp(10);
        top.addView(status,sp);
        return top;
    }

    private View buildTools(){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0,dp(7),0,dp(5));

        search=new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(14);
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(Color.rgb(126,151,177));
        search.setHint("ابحث عن تطبيق...");
        search.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
        search.setPadding(dp(15),0,dp(15),0);
        search.setBackground(bg(Color.argb(235,16,31,49),18,LINE));
        search.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){filter=s==null?"":s.toString().trim();refresh();}
            @Override public void afterTextChanged(Editable e){}
        });
        row.addView(search,new LinearLayout.LayoutParams(0,dp(44),1.6f));

        summary=text("",11,TEXT_SOFT,false);
        summary.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sump=new LinearLayout.LayoutParams(0,dp(44),.72f);
        sump.rightMargin=dp(8);
        row.addView(summary,sump);

        TextView addApk=action("＋ APK",Color.rgb(31,93,139));
        addApk.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){pickApk();}});
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(104),dp(44));
        ap.rightMargin=dp(8);
        row.addView(addApk,ap);

        TextView addMini=action("＋ Mini",Color.rgb(52,72,112));
        addMini.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){pickMini();}});
        row.addView(addMini,new LinearLayout.LayoutParams(dp(104),dp(44)));
        return row;
    }

    private TextView action(String label,int color){
        TextView t=text(label,13,Color.WHITE,true);
        t.setGravity(Gravity.CENTER);
        t.setClickable(true);
        t.setFocusable(true);
        t.setBackground(bg(color,16,Color.argb(120,135,200,245)));
        touchFeedback(t);
        return t;
    }

    private void touchFeedback(final View v){
        v.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View view,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN)view.animate().scaleX(.95f).scaleY(.95f).alpha(.82f).setDuration(55).start();
                if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL)view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(110).start();
                return false;
            }
        });
    }

    private boolean visible(String name){
        if(filter==null||filter.length()==0)return true;
        return name!=null && name.toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US));
    }

    private void refresh(){
        if(grid==null)return;
        grid.removeAllViews();
        int shown=0;

        shown+=addShortcutIfVisible("YouTube","▶",Color.rgb(196,43,54),new View.OnClickListener(){@Override public void onClick(View v){RuntimeRouter.openYouTube(NajmAppsActivity.this);}});
        shown+=addShortcutIfVisible("ReVanced","R",Color.rgb(198,39,58),new View.OnClickListener(){@Override public void onClick(View v){openBundledReVanced();}});
        shown+=addShortcutIfVisible("المتصفح","◎",Color.rgb(42,104,172),new View.OnClickListener(){@Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"Najm Browser","https://www.google.com");}});
        shown+=addShortcutIfVisible("الخرائط","⌖",Color.rgb(34,119,93),new View.OnClickListener(){@Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"Najm Maps","https://www.openstreetmap.org");}});
        shown+=addShortcutIfVisible("الملفات","▤",Color.rgb(43,106,135),new View.OnClickListener(){@Override public void onClick(View v){startActivity(new Intent(NajmAppsActivity.this,NajmFileManagerActivity.class));}});
        shown+=addShortcutIfVisible("AppGallery","✦",Color.rgb(162,47,53),new View.OnClickListener(){@Override public void onClick(View v){RuntimeRouter.openWeb(NajmAppsActivity.this,"HUAWEI AppGallery","https://consumer.huawei.com/sa/mobileservices/appgallery/");}});
        shown+=addShortcutIfVisible("الإعدادات","⚙",Color.rgb(76,69,122),new View.OnClickListener(){@Override public void onClick(View v){startActivity(new Intent(NajmAppsActivity.this,NajmSettingsActivity.class));}});

        File[] minis=miniDir.listFiles();
        if(minis!=null){
            int count=1;
            for(final File folder:minis){
                if(!folder.isDirectory())continue;
                final File entry=findEntry(folder);
                if(entry==null)continue;
                String name="Mini App "+count++;
                if(!visible(name))continue;
                addMiniTile(folder,entry,name);
                shown++;
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
                if(!visible(appName))continue;
                final String finalName=appName;
                final String finalPkg=pkg;
                View tile=createAppTile(finalName,icon,"داخل Najm");
                tile.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
                    if(finalPkg.length()==0){NajmHints.show(NajmAppsActivity.this,"bad_apk","التطبيق","تعذر قراءة اسم حزمة هذا الـAPK.");return;}
                    NajmContainer.installAndLaunch(NajmAppsActivity.this,f,finalPkg,0);
                }});
                tile.setOnLongClickListener(new View.OnLongClickListener(){@Override public boolean onLongClick(View v){showApkOptions(f,finalName,finalPkg);return true;}});
                addTile(tile);
                shown++;
            }
        }

        if(summary!=null)summary.setText(filter.length()==0?(shown+" تطبيق"): ("النتائج: "+shown));
        if(shown==0){
            TextView empty=text("لا توجد نتائج\nجرّب كلمة بحث أخرى",17,TEXT_SOFT,true);
            empty.setGravity(Gravity.CENTER);
            GridLayout.LayoutParams lp=new GridLayout.LayoutParams();
            lp.width=dp(330);lp.height=dp(150);lp.columnSpec=GridLayout.spec(0,5);
            grid.addView(empty,lp);
        }
    }

    private int addShortcutIfVisible(String name,String symbol,int color,View.OnClickListener listener){
        if(!visible(name))return 0;
        LinearLayout tile=createBaseTile();
        FrameLayout iconShell=new FrameLayout(this);
        iconShell.setBackground(bg(color,24,Color.argb(90,255,255,255)));
        TextView icon=text(symbol,38,Color.WHITE,true);
        icon.setGravity(Gravity.CENTER);
        iconShell.addView(icon,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(dp(76),dp(76));
        ilp.topMargin=dp(8);
        tile.addView(iconShell,ilp);
        addNameLabel(tile,name);
        TextView type=text("أساسي",9,Color.rgb(109,188,240),false);
        type.setGravity(Gravity.CENTER);
        tile.addView(type,new LinearLayout.LayoutParams(-1,dp(16)));
        tile.setOnClickListener(listener);
        addTile(tile);
        return 1;
    }

    private void addMiniTile(final File folder,final File entry,String name){
        LinearLayout tile=createBaseTile();
        FrameLayout iconShell=new FrameLayout(this);
        iconShell.setBackground(bg(Color.rgb(57,83,118),24,Color.argb(80,255,255,255)));
        ImageView icon=new ImageView(this);
        icon.setImageResource(R.mipmap.ic_launcher);
        icon.setPadding(dp(12),dp(12),dp(12),dp(12));
        iconShell.addView(icon,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(dp(76),dp(76));ilp.topMargin=dp(8);tile.addView(iconShell,ilp);
        addNameLabel(tile,name);
        TextView state=text("Mini App",9,Color.rgb(172,196,222),false);state.setGravity(Gravity.CENTER);tile.addView(state,new LinearLayout.LayoutParams(-1,dp(16)));
        tile.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){Intent i=new Intent(NajmAppsActivity.this,MiniAppRuntimeActivity.class);i.putExtra("path",entry.getAbsolutePath());i.putExtra("title",folder.getName());startActivity(i);}});
        tile.setOnLongClickListener(new View.OnLongClickListener(){@Override public boolean onLongClick(View v){
            new AlertDialog.Builder(NajmAppsActivity.this).setTitle("Mini App").setItems(new String[]{"تشغيل","حذف"},new DialogInterface.OnClickListener(){@Override public void onClick(DialogInterface d,int which){if(which==0){Intent i=new Intent(NajmAppsActivity.this,MiniAppRuntimeActivity.class);i.putExtra("path",entry.getAbsolutePath());i.putExtra("title",folder.getName());startActivity(i);}else{deleteTree(folder);refresh();}}}).show();
            return true;
        }});
        addTile(tile);
    }

    private LinearLayout createAppTile(String name,Drawable iconDrawable,String badge){
        LinearLayout tile=createBaseTile();
        FrameLayout iconShell=new FrameLayout(this);
        iconShell.setBackground(bg(Color.rgb(25,44,66),24,Color.argb(80,130,184,230)));
        ImageView icon=new ImageView(this);
        if(iconDrawable!=null)icon.setImageDrawable(iconDrawable);else icon.setImageResource(R.mipmap.ic_launcher);
        icon.setPadding(dp(7),dp(7),dp(7),dp(7));
        iconShell.addView(icon,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(dp(76),dp(76));ilp.topMargin=dp(8);tile.addView(iconShell,ilp);
        addNameLabel(tile,name);
        TextView state=text(badge,9,Color.rgb(99,201,246),false);state.setGravity(Gravity.CENTER);tile.addView(state,new LinearLayout.LayoutParams(-1,dp(16)));
        return tile;
    }

    private LinearLayout createBaseTile(){
        LinearLayout tile=new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER_HORIZONTAL);
        tile.setPadding(dp(8),dp(5),dp(8),dp(5));
        tile.setBackground(bg(CARD,22,LINE));
        tile.setClickable(true);
        tile.setFocusable(true);
        touchFeedback(tile);
        return tile;
    }

    private void addNameLabel(LinearLayout tile,String name){
        TextView label=text(name,13,Color.WHITE,true);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(2);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(38));
        lp.topMargin=dp(6);
        tile.addView(label,lp);
    }

    private void addTile(View tile){
        GridLayout.LayoutParams lp=new GridLayout.LayoutParams();
        lp.width=0;
        lp.height=dp(150);
        lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
        lp.setMargins(dp(6),dp(6),dp(6),dp(6));
        grid.addView(tile,lp);
    }

    private void openBundledReVanced(){
        final String pkg="app.revanced.manager.flutter";
        if(NajmContainer.isInstalled(pkg)){NajmContainer.launch(this,pkg);return;}
        try{
            File preloadDir=new File(getCacheDir(),"najm_container");
            if(!preloadDir.exists())preloadDir.mkdirs();
            File out=new File(preloadDir,"revanced-manager-2.6.0.apk");
            if(!out.exists() || out.length()<46000000L){
                InputStream in=getAssets().open("preload/revanced-manager-2.6.0.apk");
                FileOutputStream fos=new FileOutputStream(out);
                byte[] buf=new byte[32768];int n;while((n=in.read(buf))>0)fos.write(buf,0,n);
                fos.flush();fos.close();in.close();
            }
            NajmContainer.installAndLaunch(this,out,pkg,26);
        }catch(Exception e){new AlertDialog.Builder(this).setTitle("ReVanced Manager").setMessage("تعذر تجهيز التطبيق داخل Najm Container: "+e.getMessage()).setPositiveButton("موافق",null).show();}
    }

    private void showApkOptions(final File f,String appName,String pkg){
        String detail=(pkg==null||pkg.length()==0)?f.getName():pkg;
        new AlertDialog.Builder(this).setTitle(appName).setMessage(detail).setItems(new String[]{"معلومات","حذف"},new DialogInterface.OnClickListener(){@Override public void onClick(DialogInterface d,int which){if(which==0){NajmHints.show(NajmAppsActivity.this,"apk_info","معلومات التطبيق","الملف محفوظ داخل مساحة Najm Space في:\n"+f.getAbsolutePath());}else{f.delete();refresh();}}}).show();
    }

    private void pickApk(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/vnd.android.package-archive");
        try{startActivityForResult(i,PICK_APK);}catch(Exception e){Intent old=new Intent(Intent.ACTION_GET_CONTENT);old.setType("application/vnd.android.package-archive");startActivityForResult(old,PICK_APK);}
    }

    private void pickMini(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");
        try{startActivityForResult(i,PICK_MINI);}catch(Exception e){Intent old=new Intent(Intent.ACTION_GET_CONTENT);old.setType("*/*");startActivityForResult(old,PICK_MINI);}
    }

    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK||data==null||data.getData()==null)return;
        if(request==PICK_APK)importApk(data.getData());
        if(request==PICK_MINI)importMini(data.getData());
    }

    private void importApk(Uri uri){
        String name=queryName(uri);if(name==null||!name.toLowerCase().endsWith(".apk"))name="app_"+System.currentTimeMillis()+".apk";
        copy(uri,new File(appsDir,safe(name)));refresh();
    }

    private void importMini(Uri uri){
        String name=queryName(uri);if(name==null)name="mini_"+System.currentTimeMillis()+".html";
        String low=name.toLowerCase();
        if(low.endsWith(".zip")){
            File pkg=new File(miniDir,"mini_"+System.currentTimeMillis());pkg.mkdirs();
            try{
                InputStream raw=getContentResolver().openInputStream(uri);ZipInputStream zin=new ZipInputStream(raw);ZipEntry e;byte[] buf=new byte[8192];
                while((e=zin.getNextEntry())!=null){
                    String n=e.getName().replace("\\","/");if(n.contains("..")||n.startsWith("/"))continue;
                    File out=new File(pkg,n);String base=pkg.getCanonicalPath()+File.separator;if(!out.getCanonicalPath().startsWith(base))continue;
                    if(e.isDirectory()){out.mkdirs();continue;}File parent=out.getParentFile();if(parent!=null)parent.mkdirs();
                    FileOutputStream fos=new FileOutputStream(out);int len;while((len=zin.read(buf))>0)fos.write(buf,0,len);fos.close();
                }
                zin.close();
            }catch(Exception ignored){}
        }else{
            File folder=new File(miniDir,"mini_"+System.currentTimeMillis());folder.mkdirs();copy(uri,new File(folder,"index.html"));
        }
        refresh();
    }

    private void copy(Uri uri,File out){
        try{InputStream in=getContentResolver().openInputStream(uri);FileOutputStream fos=new FileOutputStream(out);byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)fos.write(buf,0,n);fos.close();in.close();}catch(Exception ignored){}
    }

    private String safe(String name){return name.replaceAll("[^A-Za-z0-9._-]","_");}

    private String queryName(Uri uri){
        Cursor c=null;try{c=getContentResolver().query(uri,null,null,null,null);if(c!=null&&c.moveToFirst()){int idx=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(idx>=0)return c.getString(idx);}}catch(Exception ignored){}finally{if(c!=null)c.close();}return null;
    }

    private File findEntry(File folder){
        File direct=new File(folder,"index.html");if(direct.exists())return direct;File[] fs=folder.listFiles();if(fs==null)return null;
        for(File f:fs){if(f.isDirectory()){File r=findEntry(f);if(r!=null)return r;}else{String n=f.getName().toLowerCase();if(n.equals("index.htm")||n.equals("index.html"))return f;}}return null;
    }

    private void deleteTree(File f){if(f.isDirectory()){File[] fs=f.listFiles();if(fs!=null)for(File x:fs)deleteTree(x);}f.delete();}

    @Override protected void onResume(){super.onResume();NajmRecentStore.touch(this,"apps","NAJM APP SPACE","com.najmspace.app.NajmAppsActivity");}
}
