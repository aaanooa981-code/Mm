package com.najmspace.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView clock, date;
    private FrameLayout shell, shade;
    private LinearLayout root;
    private float downY;
    private boolean shadeOpen = false;
    private final Handler handler = new Handler();

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            Date now = new Date();
            if (clock != null) clock.setText(new SimpleDateFormat("HH:mm", Locale.US).format(now));
            if (date != null) date.setText(new SimpleDateFormat("EEE, dd MMM", Locale.US).format(now));
            handler.postDelayed(this, 1000);
        }
    };

    private int dp(int v){ return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private TextView tile(String icon, String label, int color) {
        TextView v = new TextView(this);
        v.setText(icon + "\n" + label);
        v.setGravity(Gravity.CENTER);
        v.setTextColor(Color.WHITE);
        v.setTextSize(16);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setPadding(dp(8), dp(10), dp(8), dp(10));
        v.setBackground(rounded(color, 20));
        v.setClickable(true);
        v.setFocusable(true);
        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    view.animate().scaleX(.94f).scaleY(.94f).setDuration(80).start();
                } else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(110).start();
                }
                return false;
            }
        });
        return v;
    }

    private void open(Class<?> c){ startActivity(new Intent(this,c)); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        shell = new FrameLayout(this);
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(10,14,24), Color.rgb(29,42,64)}
        );
        shell.setBackground(bg);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(14), dp(24), dp(16));
        shell.addView(root, new FrameLayout.LayoutParams(-1,-1));

        // Swipe handle / top status strip
        TextView pull = new TextView(this);
        pull.setText("  ━━━   اسحب للأسفل لمركز التحكم   ━━━  ");
        pull.setTextColor(Color.rgb(190,205,224));
        pull.setTextSize(12);
        pull.setGravity(Gravity.CENTER);
        pull.setPadding(0, dp(3), 0, dp(6));
        root.addView(pull, new LinearLayout.LayoutParams(-1, dp(30)));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout timeBox = new LinearLayout(this);
        timeBox.setOrientation(LinearLayout.VERTICAL);
        clock = new TextView(this);
        clock.setTextColor(Color.WHITE);
        clock.setTextSize(42);
        clock.setTypeface(Typeface.DEFAULT_BOLD);
        date = new TextView(this);
        date.setTextColor(Color.rgb(170,185,205));
        date.setTextSize(15);
        timeBox.addView(clock);
        timeBox.addView(date);
        top.addView(timeBox, new LinearLayout.LayoutParams(0,-2,1));

        TextView brand = new TextView(this);
        brand.setText("NAJM SPACE");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(23);
        brand.setTypeface(Typeface.DEFAULT_BOLD);
        brand.setGravity(Gravity.RIGHT);
        top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        root.addView(top);

        TextView sub = new TextView(this);
        sub.setText("Smart Car Launcher  •  Compatibility Hub  •  Android 4.4+");
        sub.setTextColor(Color.rgb(120,145,175));
        sub.setTextSize(14);
        sub.setPadding(0,0,0,dp(12));
        root.addView(sub);

        LinearLayout middle = new LinearLayout(this);
        middle.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(22),dp(18),dp(22),dp(18));
        GradientDrawable heroBg = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(34,80,132),Color.rgb(25,40,69)}
        );
        heroBg.setCornerRadius(dp(24));
        hero.setBackground(heroBg);

        TextView heroTitle = new TextView(this);
        heroTitle.setText("Drive smarter");
        heroTitle.setTextColor(Color.WHITE);
        heroTitle.setTextSize(28);
        heroTitle.setTypeface(Typeface.DEFAULT_BOLD);
        hero.addView(heroTitle);

        TextView heroText = new TextView(this);
        heroText.setText("Your apps, media and controls in one lightweight interface.");
        heroText.setTextColor(Color.rgb(215,228,243));
        heroText.setTextSize(15);
        hero.addView(heroText);

        TextView youtube = tile("▶","YouTube",Color.rgb(190,46,46));
        youtube.setOnClickListener(v -> open(CompatibilityActivity.class));
        LinearLayout.LayoutParams yt = new LinearLayout.LayoutParams(-1,dp(80));
        yt.topMargin = dp(14);
        hero.addView(youtube,yt);

        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(0,dp(220),1.2f);
        heroLp.rightMargin=dp(12);
        middle.addView(hero,heroLp);

        LinearLayout quick = new LinearLayout(this);
        quick.setOrientation(LinearLayout.VERTICAL);

        LinearLayout q1 = new LinearLayout(this);
        TextView apps = tile("◉","Apps",Color.rgb(45,93,148));
        apps.setOnClickListener(v -> open(AppsActivity.class));
        TextView control = tile("☰","Control",Color.rgb(57,73,96));
        control.setOnClickListener(v -> showShade());
        q1.addView(apps,new LinearLayout.LayoutParams(0,dp(100),1));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0,dp(100),1);
        cp.leftMargin=dp(10);
        q1.addView(control,cp);

        LinearLayout q2 = new LinearLayout(this);
        TextView browser = tile("◎","Browser",Color.rgb(42,117,94));
        browser.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))));
        TextView settings = tile("⚙","Settings",Color.rgb(98,82,133));
        settings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_SETTINGS)));
        q2.addView(browser,new LinearLayout.LayoutParams(0,dp(100),1));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0,dp(100),1);
        sp.leftMargin=dp(10);
        q2.addView(settings,sp);

        quick.addView(q1);
        LinearLayout.LayoutParams q2p = new LinearLayout.LayoutParams(-1,-2);
        q2p.topMargin=dp(10);
        quick.addView(q2,q2p);

        middle.addView(quick,new LinearLayout.LayoutParams(0,dp(220),1));
        root.addView(middle,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setPadding(dp(8),dp(7),dp(8),dp(7));
        dock.setBackground(rounded(Color.argb(200,24,31,44),24));

        TextView home=tile("⌂","Home",Color.rgb(43,54,72));
        TextView all=tile("◉","Apps",Color.rgb(43,54,72));
        TextView media=tile("▶","Media",Color.rgb(43,54,72));
        TextView ctrl=tile("☰","Control",Color.rgb(43,54,72));
        all.setOnClickListener(v->open(AppsActivity.class));
        media.setOnClickListener(v->open(CompatibilityActivity.class));
        ctrl.setOnClickListener(v->showShade());
        dock.addView(home,new LinearLayout.LayoutParams(0,dp(66),1));
        dock.addView(all,new LinearLayout.LayoutParams(0,dp(66),1));
        dock.addView(media,new LinearLayout.LayoutParams(0,dp(66),1));
        dock.addView(ctrl,new LinearLayout.LayoutParams(0,dp(66),1));
        root.addView(dock);

        buildShade();

        View.OnTouchListener swipe = new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction()==MotionEvent.ACTION_DOWN) {
                    downY=e.getRawY();
                    return true;
                }
                if (e.getAction()==MotionEvent.ACTION_UP) {
                    float dy=e.getRawY()-downY;
                    if(dy>dp(65)) showShade();
                    else if(dy<-dp(65)) hideShade();
                    return true;
                }
                return true;
            }
        };
        pull.setOnTouchListener(swipe);

        setContentView(shell);
    }

    private void buildShade() {
        shade = new FrameLayout(this);
        shade.setBackgroundColor(Color.argb(145,0,0,0));
        shade.setVisibility(View.GONE);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24),dp(16),dp(24),dp(18));
        panel.setBackground(rounded(Color.rgb(30,38,52),26));

        TextView head = new TextView(this);
        head.setText("مركز التحكم السريع     NAJM SPACE");
        head.setTextColor(Color.WHITE);
        head.setTextSize(21);
        head.setTypeface(Typeface.DEFAULT_BOLD);
        head.setGravity(Gravity.RIGHT);
        panel.addView(head);

        LinearLayout row = new LinearLayout(this);
        row.setPadding(0,dp(12),0,dp(12));

        TextView wifi=tile("⌁","Wi-Fi",Color.rgb(42,103,160));
        wifi.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
        TextView bt=tile("ᛒ","Bluetooth",Color.rgb(49,88,145));
        bt.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)));
        TextView display=tile("☀","Display",Color.rgb(143,104,42));
        display.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS)));
        TextView settings=tile("⚙","Settings",Color.rgb(93,76,128));
        settings.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_SETTINGS)));

        row.addView(wifi,new LinearLayout.LayoutParams(0,dp(82),1));
        row.addView(bt,new LinearLayout.LayoutParams(0,dp(82),1));
        row.addView(display,new LinearLayout.LayoutParams(0,dp(82),1));
        row.addView(settings,new LinearLayout.LayoutParams(0,dp(82),1));
        panel.addView(row);

        TextView brightLabel = new TextView(this);
        brightLabel.setText("السطوع");
        brightLabel.setTextColor(Color.WHITE);
        brightLabel.setTextSize(15);
        panel.addView(brightLabel);

        SeekBar bright = new SeekBar(this);
        bright.setMax(100);
        bright.setProgress(70);
        bright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean fromUser){
                WindowManager.LayoutParams lp=getWindow().getAttributes();
                lp.screenBrightness=Math.max(.05f,p/100f);
                getWindow().setAttributes(lp);
            }
            public void onStartTrackingTouch(SeekBar s){}
            public void onStopTrackingTouch(SeekBar s){}
        });
        panel.addView(bright);

        TextView volLabel = new TextView(this);
        volLabel.setText("الصوت");
        volLabel.setTextColor(Color.WHITE);
        volLabel.setTextSize(15);
        panel.addView(volLabel);

        final AudioManager audio=(AudioManager)getSystemService(AUDIO_SERVICE);
        SeekBar volume = new SeekBar(this);
        int max=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume.setMax(max);
        volume.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean fromUser){
                if(fromUser) audio.setStreamVolume(AudioManager.STREAM_MUSIC,p,0);
            }
            public void onStartTrackingTouch(SeekBar s){}
            public void onStopTrackingTouch(SeekBar s){}
        });
        panel.addView(volume);

        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(-1,dp(300));
        pp.gravity=Gravity.TOP;
        pp.leftMargin=dp(16); pp.rightMargin=dp(16); pp.topMargin=dp(8);
        shade.addView(panel,pp);

        shade.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){ downY=e.getRawY(); return true; }
                if(e.getAction()==MotionEvent.ACTION_UP){
                    if(e.getRawY()-downY < -dp(50)) hideShade();
                    return true;
                }
                return true;
            }
        });
        shell.addView(shade,new FrameLayout.LayoutParams(-1,-1));
    }

    private void showShade(){
        if(shadeOpen) return;
        shadeOpen=true;
        shade.setVisibility(View.VISIBLE);
        shade.setTranslationY(-dp(310));
        shade.setAlpha(0f);
        shade.animate().translationY(0).alpha(1f).setDuration(280).start();
    }

    private void hideShade(){
        if(!shadeOpen) return;
        shadeOpen=false;
        shade.animate().translationY(-dp(310)).alpha(0f).setDuration(230)
            .withEndAction(new Runnable(){ public void run(){ shade.setVisibility(View.GONE); } }).start();
    }

    @Override public void onBackPressed(){
        if(shadeOpen) hideShade(); else super.onBackPressed();
    }

    @Override protected void onResume(){ super.onResume(); handler.post(tick); }
    @Override protected void onPause(){ handler.removeCallbacks(tick); super.onPause(); }
}
