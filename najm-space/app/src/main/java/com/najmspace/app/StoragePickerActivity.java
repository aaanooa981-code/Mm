package com.najmspace.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class StoragePickerActivity extends Activity {
    private LinearLayout list;
    private TextView path;
    private File current;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16,10,16,8);
        root.setBackgroundColor(Color.rgb(7,15,27));

        TextView title=new TextView(this);
        title.setText("اختيار مجلد التخزين");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        path=new TextView(this);
        path.setTextColor(Color.rgb(110,195,255));
        path.setTextSize(13);
        path.setPadding(0,8,0,8);
        root.addView(path);

        Button choose=new Button(this);
        choose.setText("استخدام هذا المجلد");
        choose.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                if(current!=null && current.exists() && current.isDirectory()){
                    File test=new File(current,"NajmSpace");
                    if(!test.exists())test.mkdirs();
                    if(test.exists() && test.canWrite()){
                        NajmStorage.setCustomPath(StoragePickerActivity.this,current.getAbsolutePath());
                        setResult(RESULT_OK);
                        finish();
                    }else{
                        NajmHints.show(StoragePickerActivity.this,"storage_not_writable","التخزين","هذا المجلد غير قابل للكتابة. اختر مجلدًا آخر.");
                    }
                }
            }
        });
        root.addView(choose,new LinearLayout.LayoutParams(-1,52));

        ScrollView scroll=new ScrollView(this);
        list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        root.addView(NajmNavigation.create(this),new LinearLayout.LayoutParams(-1,58));
        setContentView(root);

        File start=new File("/storage");
        if(!start.exists())start=Environment.getExternalStorageDirectory();
        show(start);
    }

    private void show(final File dir){
        if(dir==null || !dir.exists() || !dir.isDirectory())return;
        current=dir;
        path.setText(dir.getAbsolutePath());
        list.removeAllViews();

        File parent=dir.getParentFile();
        if(parent!=null){
            TextView up=row("⬆  ..",true);
            up.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){show(parent);}});
            list.addView(up,new LinearLayout.LayoutParams(-1,48));
        }

        File[] fs;
        try{fs=dir.listFiles();}catch(Exception e){fs=null;}
        if(fs==null)return;

        Arrays.sort(fs,new Comparator<File>(){
            @Override public int compare(File a,File b){return a.getName().compareToIgnoreCase(b.getName());}
        });

        for(final File f:fs){
            if(!f.isDirectory() || f.isHidden())continue;
            TextView t=row("📁  "+f.getName(),false);
            t.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){show(f);}});
            list.addView(t,new LinearLayout.LayoutParams(-1,48));
        }
    }

    private TextView row(String s,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(16);
        t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(12,0,12,0);
        if(bold)t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }
}
