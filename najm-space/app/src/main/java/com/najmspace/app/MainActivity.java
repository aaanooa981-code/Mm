package com.najmspace.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener {
    private TextView clock,date,gpsState,coords;
    private SpeedometerView speedometer;
    private HomeMapView homeMap;
    private FrameLayout shell,shade;
    private float downY;
    private boolean shadeOpen=false;
    private final Handler handler=new Handler();
    private LocationManager locationManager;
    private double lastLat=24.7136,lastLon=46.6753;
    private static final int REQ_LOCATION=91;

    private final Runnable tick=new Runnable(){
        @Override public void run(){
            Date now=new Date();
            if(clock!=null) clock.setText(new SimpleDateFormat("HH:mm",Locale.US).format(now));
            if(date!=null) date.setText(new SimpleDateFormat("EEEE  yyyy/MM/dd",new Locale("ar")).format(now));
            handler.postDelayed(this,1000);
        }
    };

    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    private GradientDrawable card(int color,int radius,int strokeColor){
        GradientDrawable g=new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radius));
        if(strokeColor!=0) g.setStroke(dp(1),strokeColor);
        return g;
    }

    private TextView label(String text,float size,int color,boolean bold){
        TextView t=new TextView(this); t.setText(text); t.setTextSize(size); t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT_BOLD); return t;
    }

    private TextView tile(String icon,String title,int color){
        TextView t=label(icon+"\n"+title,14,Color.WHITE,true);
        t.setGravity(Gravity.CENTER); t.setPadding(dp(8),dp(8),dp(8),dp(8));
        t.setBackground(card(color,18,Color.argb(70,120,185,255)));
        t.setClickable(true); t.setFocusable(true);
        t.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN)v.animate().scaleX(.93f).scaleY(.93f).setDuration(70).start();
                if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL)v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                return false;
            }
        });
        return t;
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        shell=new FrameLayout(this);
        shell.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(5,13,24),Color.rgb(12,27,47),Color.rgb(7,16,29)}));

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(14),dp(8),dp(14),dp(12));
        shell.addView(root,new FrameLayout.LayoutParams(-1,-1));

        View pull=new View(this);
        pull.setBackgroundColor(Color.TRANSPARENT);
        root.addView(pull,new LinearLayout.LayoutParams(-1,dp(12)));

        LinearLayout status=new LinearLayout(this); status.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=label("✦  Najm Space",20,Color.WHITE,true);
        status.addView(brand,new LinearLayout.LayoutParams(0,dp(38),1));
        TextView indicators=label("⌂   BT   Wi‑Fi   GPS",13,Color.rgb(175,195,220),false);
        indicators.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        status.addView(indicators,new LinearLayout.LayoutParams(0,dp(38),1));
        root.addView(status);

        LinearLayout body=new LinearLayout(this); body.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(body,new LinearLayout.LayoutParams(-1,0,1));

        // left: clock/weather/speed
        LinearLayout left=new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL);
        LinearLayout timeWeather=new LinearLayout(this);

        LinearLayout timeCard=new LinearLayout(this); timeCard.setOrientation(LinearLayout.VERTICAL); timeCard.setGravity(Gravity.CENTER);
        timeCard.setBackground(card(Color.rgb(18,34,54),22,Color.argb(80,70,155,230)));
        clock=label("--:--",40,Color.WHITE,true); date=label("",12,Color.rgb(180,200,225),false);
        timeCard.addView(clock); timeCard.addView(date);
        timeWeather.addView(timeCard,new LinearLayout.LayoutParams(0,dp(122),1));

        LinearLayout weather=new LinearLayout(this); weather.setOrientation(LinearLayout.VERTICAL); weather.setGravity(Gravity.CENTER);
        weather.setBackground(card(Color.rgb(18,34,54),22,Color.argb(80,70,155,230)));
        LinearLayout.LayoutParams wlp=new LinearLayout.LayoutParams(0,dp(122),.75f);wlp.leftMargin=dp(8);
        weather.addView(label("☀  32°",26,Color.rgb(245,192,70),true));
        weather.addView(label("مشمس",13,Color.WHITE,false));
        weather.addView(label("الرياض",12,Color.rgb(180,200,225),false));
        timeWeather.addView(weather,wlp);
        left.addView(timeWeather);

        LinearLayout speedCard=new LinearLayout(this); speedCard.setOrientation(LinearLayout.VERTICAL);speedCard.setGravity(Gravity.CENTER);
        speedCard.setBackground(card(Color.rgb(14,30,48),22,Color.argb(90,70,165,245)));
        LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,0,1);slp.topMargin=dp(8);
        speedometer=new SpeedometerView(this);speedCard.addView(speedometer,new LinearLayout.LayoutParams(-1,0,1));
        gpsState=label("GPS: جاري البحث...",12,Color.rgb(229,181,82),false);gpsState.setGravity(Gravity.CENTER);
        speedCard.addView(gpsState,new LinearLayout.LayoutParams(-1,dp(24)));
        left.addView(speedCard,slp);

        body.addView(left,new LinearLayout.LayoutParams(0,-1,1.05f));

        // center-right: hero, music, map, apps
        LinearLayout center=new LinearLayout(this);center.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(0,-1,2.4f);clp.leftMargin=dp(10);body.addView(center,clp);

        LinearLayout cards=new LinearLayout(this);
        LinearLayout.LayoutParams cardsLp=new LinearLayout.LayoutParams(-1,0,1);
        center.addView(cards,cardsLp);

        LinearLayout music=new LinearLayout(this);music.setOrientation(LinearLayout.VERTICAL);music.setPadding(dp(14),dp(12),dp(14),dp(12));
        music.setBackground(card(Color.rgb(17,36,58),22,Color.argb(80,70,155,230)));
        music.addView(label("الموسيقى",18,Color.WHITE,true));
        music.addView(label("غير مشغل حاليًا",13,Color.rgb(180,200,225),false));
        TextView controls=label("⏮     ▶     ⏭",24,Color.rgb(75,190,255),true);controls.setGravity(Gravity.CENTER);
        music.addView(controls,new LinearLayout.LayoutParams(-1,0,1));
        music.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){openMusic();}});
        cards.addView(music,new LinearLayout.LayoutParams(0,-1,.72f));

        LinearLayout mapCard=new LinearLayout(this);mapCard.setOrientation(LinearLayout.VERTICAL);mapCard.setPadding(dp(10),dp(10),dp(10),dp(8));
        mapCard.setBackground(card(Color.rgb(13,31,51),22,Color.argb(100,55,165,245)));
        LinearLayout.LayoutParams mlp=new LinearLayout.LayoutParams(0,-1,2.15f);mlp.leftMargin=dp(8);cards.addView(mapCard,mlp);
        LinearLayout mapTop=new LinearLayout(this);
        mapTop.setOrientation(LinearLayout.HORIZONTAL);
        mapTop.setGravity(Gravity.CENTER_VERTICAL);

        final EditText mapSearch=new EditText(this);
        mapSearch.setSingleLine(true);
        mapSearch.setHint("ابحث عن مكان...");
        mapSearch.setTextColor(Color.WHITE);
        mapSearch.setHintTextColor(Color.rgb(155,180,205));
        mapSearch.setTextSize(14);
        mapSearch.setBackground(card(Color.rgb(20,42,65),14,Color.argb(80,80,170,235)));
        mapSearch.setPadding(dp(12),0,dp(12),0);
        mapTop.addView(mapSearch,new LinearLayout.LayoutParams(0,dp(40),1));

        TextView searchBtn=tile("⌕","بحث",Color.rgb(36,104,165));
        LinearLayout.LayoutParams sbp=new LinearLayout.LayoutParams(dp(72),dp(40));sbp.leftMargin=dp(6);
        mapTop.addView(searchBtn,sbp);

        TextView gpsBtn=tile("⌖","موقعي",Color.rgb(38,118,92));
        LinearLayout.LayoutParams gbp=new LinearLayout.LayoutParams(dp(72),dp(40));gbp.leftMargin=dp(6);
        mapTop.addView(gpsBtn,gbp);

        mapCard.addView(mapTop,new LinearLayout.LayoutParams(-1,dp(42)));

        homeMap=new HomeMapView(this);mapCard.addView(homeMap,new LinearLayout.LayoutParams(-1,0,1));
        searchBtn.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){homeMap.search(mapSearch.getText().toString());}});
        mapSearch.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override public boolean onEditorAction(TextView v,int actionId,android.view.KeyEvent event){
                homeMap.search(mapSearch.getText().toString());
                return true;
            }
        });
        gpsBtn.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){homeMap.showCurrentLocation();}});
        coords=label("اضغط لفتح الملاحة",11,Color.rgb(175,195,220),false);coords.setGravity(Gravity.CENTER);
        mapCard.addView(coords,new LinearLayout.LayoutParams(-1,dp(22)));
        mapCard.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){openMap();}});

        LinearLayout launchers=new LinearLayout(this);launchers.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams llp=new LinearLayout.LayoutParams(0,-1,.72f);llp.leftMargin=dp(8);cards.addView(launchers,llp);
        TextView store=tile("✦","AppGallery",Color.rgb(178,45,48));
        store.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            RuntimeRouter.openWeb(MainActivity.this,"HUAWEI AppGallery","https://consumer.huawei.com/sa/mobileservices/appgallery/");
        }});
        launchers.addView(store,new LinearLayout.LayoutParams(-1,0,1));

        TextView files=tile("▤","الملفات",Color.rgb(47,116,145));
        LinearLayout.LayoutParams flp=new LinearLayout.LayoutParams(-1,0,1);flp.topMargin=dp(6);launchers.addView(files,flp);
        files.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            startActivity(new Intent(MainActivity.this,NajmFileManagerActivity.class));
        }});

        TextView youtube=tile("▶","YouTube",Color.rgb(200,45,52));
        LinearLayout.LayoutParams ylp=new LinearLayout.LayoutParams(-1,0,1);ylp.topMargin=dp(6);launchers.addView(youtube,ylp);
        youtube.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            RuntimeRouter.openWeb(MainActivity.this,"YouTube","https://m.youtube.com");
        }});

        TextView apps=tile("▦","التطبيقات",Color.rgb(46,97,170));
        LinearLayout.LayoutParams alp=new LinearLayout.LayoutParams(-1,0,1);alp.topMargin=dp(6);launchers.addView(apps,alp);
        apps.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            startActivity(new Intent(MainActivity.this,NajmAppsActivity.class));
        }});

        LinearLayout dock=new LinearLayout(this);dock.setPadding(dp(6),dp(5),dp(6),dp(5));
        dock.setBackground(card(Color.argb(225,15,24,38),24,Color.argb(70,100,165,230)));
        String[] di={"⚙\nالإعدادات","▤\nالملفات","✦\nAppGallery","◎\nBrowser","▦\nApps"};
        for(int i=0;i<di.length;i++){
            final int idx=i;TextView d=tile("",di[i],Color.rgb(31,48,70));d.setText(di[i]);d.setTextSize(12);
            d.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
                if(idx==0)startActivity(new Intent(MainActivity.this,NajmSettingsActivity.class));
                if(idx==1)startActivity(new Intent(MainActivity.this,NajmFileManagerActivity.class));
                if(idx==2)RuntimeRouter.openWeb(MainActivity.this,"HUAWEI AppGallery","https://consumer.huawei.com/sa/mobileservices/appgallery/");
                if(idx==3)RuntimeRouter.openWeb(MainActivity.this,"Najm Browser","https://www.google.com");
                if(idx==4)startActivity(new Intent(MainActivity.this,NajmAppsActivity.class));
            }});
            dock.addView(d,new LinearLayout.LayoutParams(0,dp(60),1));
        }
        LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(-1,dp(64));dlp.topMargin=dp(8);center.addView(dock,dlp);

        buildShade();
        pull.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){downY=e.getRawY();return true;}
                if(e.getAction()==MotionEvent.ACTION_UP){if(e.getRawY()-downY>dp(55))showShade();return true;}
                return true;
            }
        });

        root.addView(NajmNavigation.create(this),new LinearLayout.LayoutParams(-1,dp(54)));
        setContentView(shell);
        animateIn(root);
        NajmHints.show(this,"main_navigation","تلميح","استخدم ◁ للرجوع للتطبيق السابق، ○ للهوم، و▢ للتطبيقات المفتوحة.");
        startLocationIfAllowed();
    }

    private void animateIn(View v){
        boolean enabled=getSharedPreferences("najmspace",MODE_PRIVATE).getBoolean("animations",true);
        if(!enabled){v.setAlpha(1f);v.setTranslationY(0f);return;}
        v.setAlpha(0f);v.setTranslationY(dp(14));
        v.animate().alpha(1f).translationY(0).setDuration(260).start();
    }

    private void startLocationIfAllowed(){
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            gpsState.setText("GPS: يحتاج إذن الموقع");
            NajmHints.show(this,"gps_permission","الموقع","فعّل إذن الموقع من إعدادات Najm Space إذا كنت تريد عداد السرعة والخريطة الحية.");
            return;
        }
        startLocation();
    }

    private void startLocation(){
        try{
            locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                gpsState.setText("GPS: متصل"); locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,900,1f,this);
            }else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                gpsState.setText("GPS: شبكة"); locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1800,8f,this);
            }else gpsState.setText("GPS: غير مفعّل");
        }catch(Exception e){gpsState.setText("GPS: غير متاح");}
    }

    @Override public void onLocationChanged(Location l){
        if(l==null)return;
        lastLat=l.getLatitude();lastLon=l.getLongitude();
        float kmh=l.hasSpeed()?l.getSpeed()*3.6f:0f;
        speedometer.setSpeed(kmh);
        gpsState.setText("GPS: "+(l.getProvider()==null?"متصل":l.getProvider()));
        coords.setText(String.format(Locale.US,"%.4f , %.4f",lastLat,lastLon));
        if(homeMap!=null)homeMap.updateLocation(lastLat,lastLon);
    }
    @Override public void onProviderDisabled(String p){gpsState.setText("GPS: متوقف");}
    @Override public void onProviderEnabled(String p){gpsState.setText("GPS: متصل");}
    @Override public void onStatusChanged(String p,int s,Bundle e){}

    private void openMap(){
        Uri geo=Uri.parse("geo:"+lastLat+","+lastLon+"?q="+lastLat+","+lastLon);
        Intent i=new Intent(Intent.ACTION_VIEW,geo);
        try{startActivity(i);}catch(Exception e){startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://maps.google.com/?q="+lastLat+","+lastLon)));}
    }

    private void openMusic(){
        String[] pkgs={"com.google.android.music","com.spotify.music","com.android.music"};
        for(String p:pkgs){Intent i=getPackageManager().getLaunchIntentForPackage(p);if(i!=null){startActivity(i);return;}}
    }

    private void buildShade(){
        shade=new FrameLayout(this);shade.setBackgroundColor(Color.argb(145,0,0,0));shade.setVisibility(View.GONE);
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(18),dp(14),dp(18),dp(14));
        panel.setBackground(card(Color.rgb(25,35,50),24,Color.argb(100,75,165,245)));
        panel.addView(label("مركز التحكم السريع",21,Color.WHITE,true));

        LinearLayout row=new LinearLayout(this);
        String[] titles={"Wi‑Fi","Bluetooth","الشاشة","الإعدادات"};
        int[] colors={Color.rgb(42,103,160),Color.rgb(49,88,145),Color.rgb(155,110,40),Color.rgb(92,76,130)};
        for(int i=0;i<4;i++){
            final int idx=i;TextView t=tile(i==0?"⌁":i==1?"ᛒ":i==2?"☀":"⚙",titles[i],colors[i]);
            t.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
                if(idx==0)startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                if(idx==1)startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                if(idx==2)startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
                if(idx==3)startActivity(new Intent(MainActivity.this,NajmSettingsActivity.class));
            }});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(74),1);if(i>0)lp.leftMargin=dp(7);row.addView(t,lp);
        }
        LinearLayout.LayoutParams rlp=new LinearLayout.LayoutParams(-1,dp(78));rlp.topMargin=dp(10);panel.addView(row,rlp);

        TextView bl=label("السطوع",13,Color.WHITE,false);panel.addView(bl);
        SeekBar bright=new SeekBar(this);bright.setMax(100);bright.setProgress(70);
        bright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean f){WindowManager.LayoutParams lp=getWindow().getAttributes();lp.screenBrightness=Math.max(.05f,p/100f);getWindow().setAttributes(lp);}
            public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}
        });panel.addView(bright);

        TextView vl=label("الصوت",13,Color.WHITE,false);panel.addView(vl);
        final AudioManager audio=(AudioManager)getSystemService(AUDIO_SERVICE);
        SeekBar vol=new SeekBar(this);vol.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));vol.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
        vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean f){if(f)audio.setStreamVolume(AudioManager.STREAM_MUSIC,p,0);}
            public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}
        });panel.addView(vol);

        FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(-1,dp(272));pp.gravity=Gravity.TOP;pp.leftMargin=dp(12);pp.rightMargin=dp(12);pp.topMargin=dp(5);shade.addView(panel,pp);
        shade.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){downY=e.getRawY();return true;}
                if(e.getAction()==MotionEvent.ACTION_UP){if(e.getRawY()-downY<-dp(45))hideShade();return true;}
                return true;
            }
        });
        shell.addView(shade,new FrameLayout.LayoutParams(-1,-1));
    }

    private void showShade(){
        if(shadeOpen)return;shadeOpen=true;shade.setVisibility(View.VISIBLE);shade.setTranslationY(-dp(290));shade.setAlpha(0f);
        shade.animate().translationY(0).alpha(1f).setDuration(250).start();
    }
    private void hideShade(){
        if(!shadeOpen)return;shadeOpen=false;shade.animate().translationY(-dp(290)).alpha(0f).setDuration(200).withEndAction(new Runnable(){@Override public void run(){shade.setVisibility(View.GONE);}}).start();
    }

    @Override public void onBackPressed(){
        if(shadeOpen){hideShade();return;}
        try{moveTaskToBack(true);}catch(Exception e){super.onBackPressed();}
    }
    @Override protected void onResume(){super.onResume();handler.post(tick);}
    @Override protected void onPause(){handler.removeCallbacks(tick);super.onPause();}
    @Override protected void onDestroy(){try{if(locationManager!=null)locationManager.removeUpdates(this);}catch(Exception ignored){}super.onDestroy();}
}
