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
import android.widget.ProgressBar;
import android.widget.TextView;

public class InternalBrowserActivity extends Activity {
    private WebView web;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        String url=getIntent().getStringExtra("url");
        String title=getIntent().getStringExtra("title");
        if(url==null) url="https://www.google.com";
        if(title==null) title="Najm Browser";

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(7,15,27));

        LinearLayout bar=new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(16,8,16,8);
        TextView back=new TextView(this);
        back.setText("‹");
        back.setTextColor(Color.WHITE);
        back.setTextSize(34);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                if(web!=null && web.canGoBack()) web.goBack(); else finish();
            }
        });
        bar.addView(back,new LinearLayout.LayoutParams(60,48));

        TextView t=new TextView(this);
        t.setText(title);
        t.setTextColor(Color.WHITE);
        t.setTextSize(19);
        t.setGravity(Gravity.CENTER_VERTICAL);
        bar.addView(t,new LinearLayout.LayoutParams(0,48,1));

        TextView close=new TextView(this);
        close.setText("✕");
        close.setTextColor(Color.WHITE);
        close.setTextSize(20);
        close.setGravity(Gravity.CENTER);
        close.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){finish();}});
        bar.addView(close,new LinearLayout.LayoutParams(60,48));
        root.addView(bar,new LinearLayout.LayoutParams(-1,56));

        final ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        root.addView(progress,new LinearLayout.LayoutParams(-1,4));

        web=new WebView(this);
        web.setBackgroundColor(Color.rgb(8,16,29));
        WebSettings s=web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        if(android.os.Build.VERSION.SDK_INT>=21) s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,String target){
                view.loadUrl(target);
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient(){
            @Override public void onProgressChanged(WebView view,int newProgress){
                progress.setProgress(newProgress);
                progress.setVisibility(newProgress>=100?View.GONE:View.VISIBLE);
            }
        });

        root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(NajmNavigation.create(this),new LinearLayout.LayoutParams(-1,62));
        setContentView(root);
        web.loadUrl(url);
    }

    @Override public void onBackPressed(){
        if(web!=null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }

    @Override protected void onDestroy(){
        if(web!=null){web.stopLoading();web.destroy();}
        super.onDestroy();
    }
}
