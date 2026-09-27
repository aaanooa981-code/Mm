package com.najmspace.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;

public class MiniAppRuntimeActivity extends Activity {
    private WebView web;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        final String path=getIntent().getStringExtra("path");
        final String title=getIntent().getStringExtra("title");

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(7,15,27));

        LinearLayout bar=new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(12,6,12,6);

        TextView back=new TextView(this);
        back.setText("‹");
        back.setTextColor(Color.WHITE);
        back.setTextSize(34);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                if(web!=null && web.canGoBack())web.goBack(); else finish();
            }
        });
        bar.addView(back,new LinearLayout.LayoutParams(60,50));

        TextView name=new TextView(this);
        name.setText(title==null?"Najm Mini App":title);
        name.setTextColor(Color.WHITE);
        name.setTextSize(19);
        bar.addView(name,new LinearLayout.LayoutParams(0,50,1));

        TextView badge=new TextView(this);
        badge.setText("INTERNAL");
        badge.setTextColor(Color.rgb(100,205,255));
        badge.setTextSize(12);
        badge.setGravity(Gravity.CENTER);
        bar.addView(badge,new LinearLayout.LayoutParams(100,50));

        root.addView(bar,new LinearLayout.LayoutParams(-1,56));

        web=new WebView(this);
        WebSettings s=web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient());
        web.setBackgroundColor(Color.rgb(8,16,29));
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1));

        setContentView(root);

        if(path!=null){
            File f=new File(path);
            if(f.exists())web.loadUrl("file://"+f.getAbsolutePath());
        }
    }

    @Override public void onBackPressed(){
        if(web!=null && web.canGoBack())web.goBack(); else super.onBackPressed();
    }

    @Override protected void onDestroy(){
        if(web!=null){web.stopLoading();web.destroy();}
        super.onDestroy();
    }
}
