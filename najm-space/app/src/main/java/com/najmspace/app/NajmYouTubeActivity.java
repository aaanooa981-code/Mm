package com.najmspace.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;

public class NajmYouTubeActivity extends Activity {
    private static final int REQ_VOICE=301;
    private WebView web;
    private FrameLayout root;
    private LinearLayout topBar;
    private ProgressBar progress;
    private EditText search;
    private View customView;
    private WebChromeClient.CustomViewCallback customCallback;

    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    private GradientDrawable bg(int color,int radius,int stroke){
        GradientDrawable g=new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        if(stroke!=0)g.setStroke(dp(1),stroke);
        return g;
    }

    private TextView button(String text){
        TextView v=new TextView(this);
        v.setText(text);
        v.setTextColor(Color.WHITE);
        v.setTextSize(17);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setGravity(Gravity.CENTER);
        v.setClickable(true);
        v.setFocusable(true);
        v.setBackground(bg(Color.rgb(26,38,55),13,Color.rgb(45,65,88)));
        return v;
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        root=new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        LinearLayout page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        root.addView(page,new FrameLayout.LayoutParams(-1,-1));

        topBar=new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(8),dp(4),dp(8),dp(4));
        topBar.setBackgroundColor(Color.rgb(8,14,22));

        TextView back=button("‹");
        back.setTextSize(30);
        back.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View v){
                if(web!=null && web.canGoBack())web.goBack(); else finish();
            }
        });
        topBar.addView(back,new LinearLayout.LayoutParams(dp(48),dp(38)));

        TextView logo=new TextView(this);
        logo.setText("▶  YouTube Lite");
        logo.setTextColor(Color.WHITE);
        logo.setTextSize(16);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        logo.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lpLogo=new LinearLayout.LayoutParams(dp(142),dp(38));
        lpLogo.leftMargin=dp(6);
        topBar.addView(logo,lpLogo);

        search=new EditText(this);
        search.setSingleLine(true);
        search.setHint("ابحث في YouTube...");
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(Color.rgb(150,165,184));
        search.setTextSize(14);
        search.setPadding(dp(12),0,dp(12),0);
        search.setBackground(bg(Color.rgb(20,30,43),15,Color.rgb(46,63,83)));
        LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(0,dp(38),1);
        slp.leftMargin=dp(8);
        topBar.addView(search,slp);

        TextView mic=button("🎤");
        LinearLayout.LayoutParams mip=new LinearLayout.LayoutParams(dp(48),dp(38));
        mip.leftMargin=dp(6);
        topBar.addView(mic,mip);

        TextView go=button("⌕");
        LinearLayout.LayoutParams gip=new LinearLayout.LayoutParams(dp(48),dp(38));
        gip.leftMargin=dp(6);
        topBar.addView(go,gip);

        TextView home=button("⌂");
        LinearLayout.LayoutParams hip=new LinearLayout.LayoutParams(dp(48),dp(38));
        hip.leftMargin=dp(6);
        topBar.addView(home,hip);

        TextView close=button("✕");
        LinearLayout.LayoutParams cip=new LinearLayout.LayoutParams(dp(48),dp(38));
        cip.leftMargin=dp(6);
        topBar.addView(close,cip);

        page.addView(topBar,new LinearLayout.LayoutParams(-1,dp(46)));

        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        page.addView(progress,new LinearLayout.LayoutParams(-1,dp(3)));

        web=new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        if(Build.VERSION.SDK_INT>=19)web.setLayerType(View.LAYER_TYPE_HARDWARE,null);

        WebSettings s=web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setLoadsImagesAutomatically(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        if(Build.VERSION.SDK_INT>=17)s.setMediaPlaybackRequiresUserGesture(false);
        if(Build.VERSION.SDK_INT>=21)s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,String url){
                view.loadUrl(url);
                return true;
            }
            @Override public void onPageFinished(WebView view,String url){
                super.onPageFinished(view,url);
                progress.setVisibility(View.GONE);
                if(url!=null && url.contains("youtube")){
                    getSharedPreferences("najmspace",MODE_PRIVATE).edit().putString("youtube_last_url",url).apply();
                }
            }
        });

        web.setWebChromeClient(new WebChromeClient(){
            @Override public void onProgressChanged(WebView view,int newProgress){
                progress.setProgress(newProgress);
                progress.setVisibility(newProgress>=100?View.GONE:View.VISIBLE);
            }
            @Override public void onShowCustomView(View view,CustomViewCallback callback){
                if(customView!=null){callback.onCustomViewHidden();return;}
                customView=view;
                customCallback=callback;
                web.setVisibility(View.GONE);
                topBar.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(-1,-1);
                root.addView(view,fp);
                if(Build.VERSION.SDK_INT>=19){
                    root.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN|
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    );
                }
            }
            @Override public void onHideCustomView(){
                hideCustomVideo();
            }
        });

        page.addView(web,new LinearLayout.LayoutParams(-1,0,1));

        go.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){doSearch();}});
        search.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override public boolean onEditorAction(TextView v,int actionId,android.view.KeyEvent event){
                doSearch(); return true;
            }
        });
        mic.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){startVoice();}});
        home.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){web.loadUrl("https://m.youtube.com/");}});
        close.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){finish();}});

        setContentView(root);

        String last=getSharedPreferences("najmspace",MODE_PRIVATE).getString("youtube_last_url","https://m.youtube.com/");
        web.loadUrl(last);
    }

    private void doSearch(){
        String q=search.getText().toString().trim();
        if(q.length()==0)return;
        web.loadUrl("https://m.youtube.com/results?search_query="+Uri.encode(q));
    }

    private void startVoice(){
        try{
            Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ar-SA");
            i.putExtra(RecognizerIntent.EXTRA_PROMPT,"قل ما تريد البحث عنه");
            startActivityForResult(i,REQ_VOICE);
        }catch(Exception e){
            NajmHints.show(this,"youtube_voice","البحث الصوتي","خدمة التعرف على الصوت غير متاحة على هذه الشاشة.");
        }
    }

    @Override protected void onActivityResult(int req,int result,Intent data){
        super.onActivityResult(req,result,data);
        if(req==REQ_VOICE && result==RESULT_OK && data!=null){
            ArrayList<String> r=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(r!=null && !r.isEmpty()){
                search.setText(r.get(0));
                doSearch();
            }
        }
    }

    private void hideCustomVideo(){
        if(customView==null)return;
        root.removeView(customView);
        customView=null;
        if(customCallback!=null)customCallback.onCustomViewHidden();
        customCallback=null;
        web.setVisibility(View.VISIBLE);
        topBar.setVisibility(View.VISIBLE);
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override public void onBackPressed(){
        if(customView!=null){hideCustomVideo();return;}
        if(web!=null && web.canGoBack()){web.goBack();return;}
        super.onBackPressed();
    }

    @Override protected void onResume(){
        super.onResume();
        if(web!=null){
            web.onResume();
            web.resumeTimers();
        }
        NajmRecentStore.touch(this,"youtube","YouTube Lite","com.najmspace.app.NajmYouTubeActivity");
    }

    @Override protected void onPause(){
        if(web!=null){
            web.onPause();
            web.pauseTimers();
        }
        super.onPause();
    }

    @Override protected void onDestroy(){
        if(web!=null){
            web.stopLoading();
            web.loadUrl("about:blank");
            web.destroy();
        }
        super.onDestroy();
    }
}
