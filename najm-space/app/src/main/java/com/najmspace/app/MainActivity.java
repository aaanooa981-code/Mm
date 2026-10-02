package com.najmspace.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener, SensorEventListener {
    private TextView clock,date,gpsState,coords,topClock,driveState;
    private SpeedometerView speedometer;
    private HomeMapView homeMap;
    private CompassView compassView;
    private EditText mapSearch;
    private SensorManager sensorManager;
    private Sensor orientationSensor;
    private FrameLayout shell,shade;
    private LinearLayout navigationBar;
    private long lastBottomTap=0;
    private float downY;
    private boolean shadeOpen=false;
    private final Handler handler=new Handler();
    private LocationManager locationManager;
    private boolean locationListening=false;
    private double lastLat=24.7136,lastLon=46.6753;
    private static final int REQ_LOCATION=91;
    private static final int REQ_VOICE=92;

    private final int BG0=Color.rgb(4,10,18);
    private final int BG1=Color.rgb(8,20,34);
    private final int PANEL=Color.rgb(13,27,44);
    private final int PANEL2=Color.rgb(17,34,53);
    private final int LINE=Color.rgb(36,65,91);
    private final int SOFT=Color.rgb(169,194,219);
    private final int ACCENT=Color.rgb(71,174,238);
    private final int GREEN=Color.rgb(96,217,159);

    private final Runnable tick=new Runnable(){
        @Override public void run(){
            Date now=new Date();
            String time=new SimpleDateFormat("HH:mm",Locale.US).format(now);
            if(clock!=null)clock.setText(time);
            if(topClock!=null)topClock.setText(time);
            if(date!=null)date.setText(new SimpleDateFormat("EEEE  yyyy/MM/dd",new Locale("ar")).format(now));
            handler.postDelayed(this,1000);
        }
    };

    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    private GradientDrawable card(int color,int radius,int strokeColor){
        GradientDrawable g=new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        if(strokeColor!=0)g.setStroke(dp(1),strokeColor);
        return g;
    }

    private TextView label(String text,float size,int color,boolean bold){
        TextView t=new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private void pressFeedback(final View v){
        v.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View view,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){
                    view.animate().scaleX(.94f).scaleY(.94f).alpha(.84f).setDuration(55).start();
                }else if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
                    view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(105).start();
                }
                return false;
            }
        });
    }

    private TextView tile(String icon,String title,int color){
        TextView t=label(icon+"\n"+title,13,Color.WHITE,true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(6),dp(6),dp(6),dp(6));
        t.setBackground(card(color,18,Color.argb(100,108,184,235)));
        t.setClickable(true);
        t.setFocusable(true);
        pressFeedback(t);
        return t;
    }

    private TextView dockTile(String icon,String title){
        TextView t=label(icon+"  "+title,12,Color.WHITE,true);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(true);
        t.setBackground(card(Color.rgb(24,43,63),16,Color.argb(100,74,116,151)));
        t.setClickable(true);
        t.setFocusable(true);
        pressFeedback(t);
        return t;
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        shell=new FrameLayout(this);
        shell.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{BG0,BG1,Color.rgb(7,16,28)}));

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12),dp(8),dp(12),dp(7));
        if(Build.VERSION.SDK_INT>=17)root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        shell.addView(root,new FrameLayout.LayoutParams(-1,-1));

        View pull=new View(this);
        pull.setBackgroundColor(Color.TRANSPARENT);
        root.addView(pull,new LinearLayout.LayoutParams(-1,dp(8)));

        root.addView(buildTopBar(),new LinearLayout.LayoutParams(-1,dp(54)));

        LinearLayout body=new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bodyLp=new LinearLayout.LayoutParams(-1,0,1);
        bodyLp.topMargin=dp(9);
        root.addView(body,bodyLp);

        LinearLayout driver=buildDriverPanel();
        body.addView(driver,new LinearLayout.LayoutParams(0,-1,1.03f));

        FrameLayout map=buildMapPanel();
        LinearLayout.LayoutParams mapLp=new LinearLayout.LayoutParams(0,-1,2.42f);
        mapLp.rightMargin=dp(10);
        body.addView(map,mapLp);

        LinearLayout quick=buildQuickPanel();
        LinearLayout.LayoutParams quickLp=new LinearLayout.LayoutParams(0,-1,.95f);
        quickLp.rightMargin=dp(10);
        body.addView(quick,quickLp);

        LinearLayout dock=buildDock();
        LinearLayout.LayoutParams dockLp=new LinearLayout.LayoutParams(-1,dp(58));
        dockLp.topMargin=dp(9);
        root.addView(dock,dockLp);

        buildShade();
        pull.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){downY=e.getRawY();return true;}
                if(e.getAction()==MotionEvent.ACTION_UP){if(e.getRawY()-downY>dp(45))showShade();return true;}
                return true;
            }
        });

        navigationBar=NajmNavigation.create(this);
        boolean navHidden=getSharedPreferences("najmspace",MODE_PRIVATE).getBoolean("nav_hidden",false);
        navigationBar.setVisibility(navHidden?View.GONE:View.VISIBLE);
        root.addView(navigationBar,new LinearLayout.LayoutParams(-1,dp(40)));

        View navHotZone=new View(this);
        navHotZone.setBackgroundColor(Color.TRANSPARENT);
        navHotZone.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_UP){
                    long now=System.currentTimeMillis();
                    if(now-lastBottomTap<420){toggleNavigationBar();lastBottomTap=0;}else lastBottomTap=now;
                    return true;
                }
                return true;
            }
        });
        root.addView(navHotZone,new LinearLayout.LayoutParams(-1,dp(8)));

        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        if(sensorManager!=null)orientationSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        setContentView(shell);
        animateIn(root);
        startLocationIfAllowed();
    }

    private View buildTopBar(){
        LinearLayout bar=new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(14),dp(5),dp(12),dp(5));
        bar.setBackground(card(Color.argb(226,10,23,38),20,LINE));

        LinearLayout brandBox=new LinearLayout(this);
        brandBox.setOrientation(LinearLayout.VERTICAL);
        brandBox.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
        TextView brand=label("NAJM SPACE",20,Color.WHITE,true);
        brand.setGravity(Gravity.RIGHT);
        TextView sub=label("واجهة القيادة الذكية",10,SOFT,false);
        sub.setGravity(Gravity.RIGHT);
        brandBox.addView(brand,new LinearLayout.LayoutParams(-1,dp(27)));
        brandBox.addView(sub,new LinearLayout.LayoutParams(-1,dp(16)));
        bar.addView(brandBox,new LinearLayout.LayoutParams(0,-1,1.1f));

        driveState=label("●  جاهز",11,GREEN,true);
        driveState.setGravity(Gravity.CENTER);
        driveState.setBackground(card(Color.rgb(15,48,43),14,Color.rgb(39,86,73)));
        LinearLayout.LayoutParams dsp=new LinearLayout.LayoutParams(dp(108),dp(36));
        dsp.rightMargin=dp(8);
        bar.addView(driveState,dsp);

        topClock=label("--:--",25,Color.WHITE,true);
        topClock.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tcp=new LinearLayout.LayoutParams(dp(94),dp(40));
        tcp.rightMargin=dp(8);
        bar.addView(topClock,tcp);

        TextView control=label("⌄",26,ACCENT,true);
        control.setGravity(Gravity.CENTER);
        control.setBackground(card(Color.rgb(20,43,65),14,Color.rgb(42,82,112)));
        control.setClickable(true);
        pressFeedback(control);
        control.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){showShade();}});
        bar.addView(control,new LinearLayout.LayoutParams(dp(48),dp(40)));
        return bar;
    }

    private LinearLayout buildDriverPanel(){
        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(9),dp(9),dp(9),dp(9));
        panel.setBackground(card(PANEL,22,LINE));

        TextView title=label("بيانات القيادة",13,SOFT,true);
        title.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        panel.addView(title,new LinearLayout.LayoutParams(-1,dp(24)));

        speedometer=new SpeedometerView(this);
        panel.addView(speedometer,new LinearLayout.LayoutParams(-1,0,1));

        gpsState=label("GPS  •  جاري البحث...",11,Color.rgb(232,187,86),true);
        gpsState.setGravity(Gravity.CENTER);
        gpsState.setBackground(card(Color.rgb(18,39,57),12,Color.rgb(41,72,96)));
        LinearLayout.LayoutParams glp=new LinearLayout.LayoutParams(-1,dp(28));
        glp.topMargin=dp(5);
        panel.addView(gpsState,glp);

        LinearLayout lower=new LinearLayout(this);
        lower.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lowerLp=new LinearLayout.LayoutParams(-1,dp(84));
        lowerLp.topMargin=dp(7);
        panel.addView(lower,lowerLp);

        LinearLayout timeBox=new LinearLayout(this);
        timeBox.setOrientation(LinearLayout.VERTICAL);
        timeBox.setGravity(Gravity.CENTER);
        timeBox.setBackground(card(PANEL2,17,Color.rgb(38,65,89)));
        clock=label("--:--",28,Color.WHITE,true);
        clock.setGravity(Gravity.CENTER);
        date=label("",9,SOFT,false);
        date.setGravity(Gravity.CENTER);
        timeBox.addView(clock,new LinearLayout.LayoutParams(-1,dp(43)));
        timeBox.addView(date,new LinearLayout.LayoutParams(-1,dp(30)));
        lower.addView(timeBox,new LinearLayout.LayoutParams(0,-1,1));

        compassView=new CompassView(this);
        LinearLayout.LayoutParams compassLp=new LinearLayout.LayoutParams(dp(86),-1);
        compassLp.rightMargin=dp(7);
        lower.addView(compassView,compassLp);
        return panel;
    }

    private FrameLayout buildMapPanel(){
        FrameLayout mapCard=new FrameLayout(this);
        mapCard.setBackground(card(Color.rgb(11,29,47),22,Color.argb(150,58,133,184)));

        homeMap=new HomeMapView(this);
        mapCard.addView(homeMap,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout mapTop=new LinearLayout(this);
        mapTop.setOrientation(LinearLayout.HORIZONTAL);
        mapTop.setGravity(Gravity.CENTER_VERTICAL);
        mapTop.setPadding(dp(8),dp(7),dp(8),dp(5));
        mapTop.setBackground(card(Color.argb(190,7,19,32),17,Color.argb(95,75,147,195)));

        TextView navTitle=label("⌖  الملاحة",12,Color.WHITE,true);
        navTitle.setGravity(Gravity.CENTER);
        mapTop.addView(navTitle,new LinearLayout.LayoutParams(dp(92),dp(40)));

        mapSearch=new EditText(this);
        mapSearch.setSingleLine(true);
        mapSearch.setHint("ابحث عن وجهة...");
        mapSearch.setTextColor(Color.WHITE);
        mapSearch.setHintTextColor(Color.rgb(184,204,223));
        mapSearch.setTextSize(13);
        mapSearch.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
        mapSearch.setBackground(card(Color.argb(220,14,32,50),15,Color.argb(120,100,175,224)));
        mapSearch.setPadding(dp(12),0,dp(12),0);
        LinearLayout.LayoutParams searchLp=new LinearLayout.LayoutParams(0,dp(40),1);
        searchLp.rightMargin=dp(6);
        mapTop.addView(mapSearch,searchLp);

        TextView micBtn=tile("●","",Color.rgb(40,83,116));
        TextView searchBtn=tile("⌕","",Color.rgb(35,100,151));
        TextView gpsBtn=tile("⌖","",Color.rgb(36,113,88));
        TextView extBtn=tile("↗","",Color.rgb(62,78,112));
        TextView[] buttons={micBtn,searchBtn,gpsBtn,extBtn};
        for(int i=0;i<buttons.length;i++){
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(43),dp(40));
            if(i>0)bp.rightMargin=dp(5);
            mapTop.addView(buttons[i],bp);
        }

        FrameLayout.LayoutParams mtp=new FrameLayout.LayoutParams(-1,dp(54));
        mtp.gravity=Gravity.TOP;
        mtp.leftMargin=dp(8);mtp.rightMargin=dp(8);mtp.topMargin=dp(7);
        mapCard.addView(mapTop,mtp);

        coords=label("الموقع الحالي",10,Color.WHITE,true);
        coords.setGravity(Gravity.CENTER);
        coords.setBackground(card(Color.argb(205,8,22,36),12,Color.argb(80,94,159,205)));
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(dp(170),dp(27));
        cp.gravity=Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
        cp.bottomMargin=dp(8);
        mapCard.addView(coords,cp);

        searchBtn.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){homeMap.search(mapSearch.getText().toString());}});
        micBtn.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){startVoiceSearch();}});
        gpsBtn.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){homeMap.showCurrentLocation();}});
        extBtn.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){openMap();}});
        mapSearch.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override public boolean onEditorAction(TextView v,int actionId,android.view.KeyEvent event){
                homeMap.search(mapSearch.getText().toString());
                return true;
            }
        });
        return mapCard;
    }

    private LinearLayout buildQuickPanel(){
        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(8),dp(8),dp(8),dp(8));
        panel.setBackground(card(PANEL,22,LINE));

        TextView heading=label("الوصول السريع",13,SOFT,true);
        heading.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        panel.addView(heading,new LinearLayout.LayoutParams(-1,dp(28)));

        addQuickRow(panel,
            quickButton("▶","YouTube",Color.rgb(183,43,51),0),
            quickButton("♫","الموسيقى",Color.rgb(84,65,145),1));
        addQuickRow(panel,
            quickButton("▦","التطبيقات",Color.rgb(44,96,161),2),
            quickButton("▤","الملفات",Color.rgb(42,108,135),3));
        addQuickRow(panel,
            quickButton("◎","المتصفح",Color.rgb(39,95,145),4),
            quickButton("⚙","الإعدادات",Color.rgb(79,71,117),5));

        TextView container=label("●  NAJM CONTAINER\nمساحة التطبيقات جاهزة",11,GREEN,true);
        container.setGravity(Gravity.CENTER);
        container.setBackground(card(Color.rgb(14,45,42),16,Color.rgb(39,84,74)));
        container.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){startActivity(new Intent(MainActivity.this,NajmAppsActivity.class));}});
        pressFeedback(container);
        LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,0,.8f);
        clp.topMargin=dp(6);
        panel.addView(container,clp);
        return panel;
    }

    private TextView quickButton(String icon,String name,int color,final int action){
        TextView b=tile(icon,name,color);
        b.setTextSize(12);
        b.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){runAction(action);}});
        return b;
    }

    private void addQuickRow(LinearLayout parent,View a,View b){
        LinearLayout row=new LinearLayout(this);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,0,1);
        rp.topMargin=dp(5);
        parent.addView(row,rp);
        row.addView(a,new LinearLayout.LayoutParams(0,-1,1));
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,-1,1);
        bp.rightMargin=dp(5);
        row.addView(b,bp);
    }

    private LinearLayout buildDock(){
        LinearLayout dock=new LinearLayout(this);
        dock.setGravity(Gravity.CENTER);
        dock.setPadding(dp(7),dp(6),dp(7),dp(6));
        dock.setBackground(card(Color.argb(235,12,25,40),21,Color.argb(110,67,111,146)));

        String[] icons={"▶","⌖","♫","▦","▤","⚙"};
        String[] names={"YouTube","خرائط","موسيقى","تطبيقات","ملفات","إعدادات"};
        int[] actions={0,6,1,2,3,5};
        for(int i=0;i<names.length;i++){
            final int a=actions[i];
            TextView t=dockTile(icons[i],names[i]);
            t.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){runAction(a);}});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);
            if(i>0)lp.rightMargin=dp(5);
            dock.addView(t,lp);
        }
        return dock;
    }

    private void runAction(int action){
        if(action==0)RuntimeRouter.openYouTube(this);
        else if(action==1)openMusic();
        else if(action==2)startActivity(new Intent(this,NajmAppsActivity.class));
        else if(action==3)startActivity(new Intent(this,NajmFileManagerActivity.class));
        else if(action==4)RuntimeRouter.openWeb(this,"Najm Browser","https://www.google.com");
        else if(action==5)startActivity(new Intent(this,NajmSettingsActivity.class));
        else if(action==6)openMap();
    }

    private void startVoiceSearch(){
        try{
            Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ar-SA");
            i.putExtra(RecognizerIntent.EXTRA_PROMPT,"قل اسم المكان");
            startActivityForResult(i,REQ_VOICE);
        }catch(Exception e){
            NajmHints.show(this,"voice_search_unavailable","البحث الصوتي","خدمة التعرف على الصوت غير متاحة على هذه الشاشة.");
        }
    }

    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==REQ_VOICE && result==RESULT_OK && data!=null){
            java.util.ArrayList<String> results=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(results!=null && !results.isEmpty()){
                String q=results.get(0);
                if(mapSearch!=null)mapSearch.setText(q);
                if(homeMap!=null)homeMap.search(q);
            }
        }
    }

    @Override public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType()==Sensor.TYPE_ORIENTATION && compassView!=null){
            float az=event.values[0];
            if(az<0)az+=360f;
            compassView.setAzimuth(az);
        }
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}

    private void toggleNavigationBar(){
        if(navigationBar==null)return;
        boolean hide=navigationBar.getVisibility()==View.VISIBLE;
        navigationBar.setVisibility(hide?View.GONE:View.VISIBLE);
        getSharedPreferences("najmspace",MODE_PRIVATE).edit().putBoolean("nav_hidden",hide).apply();
    }

    private void animateIn(View v){
        boolean enabled=getSharedPreferences("najmspace",MODE_PRIVATE).getBoolean("animations",true);
        if(!enabled){v.setAlpha(1f);v.setTranslationY(0f);return;}
        v.setAlpha(0f);v.setTranslationY(dp(12));
        v.animate().alpha(1f).translationY(0).setDuration(260).start();
    }

    private void startLocationIfAllowed(){
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            if(gpsState!=null)gpsState.setText("GPS  •  يحتاج إذن الموقع");
            if(driveState!=null){driveState.setText("●  GPS OFF");driveState.setTextColor(Color.rgb(242,185,92));}
            return;
        }
        startLocation();
    }

    private void startLocation(){
        try{
            locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
            if(locationManager!=null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                gpsState.setText("GPS  •  متصل");
                if(driveState!=null){driveState.setText("●  GPS READY");driveState.setTextColor(GREEN);}
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,900,1f,this);
                locationListening=true;
            }else if(locationManager!=null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                gpsState.setText("GPS  •  شبكة");
                if(driveState!=null)driveState.setText("●  LOCATION");
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1800,8f,this);
                locationListening=true;
            }else{
                gpsState.setText("GPS  •  غير مفعّل");
                if(driveState!=null){driveState.setText("●  GPS OFF");driveState.setTextColor(Color.rgb(242,185,92));}
            }
        }catch(Exception e){
            if(gpsState!=null)gpsState.setText("GPS  •  غير متاح");
        }
    }

    @Override public void onLocationChanged(Location l){
        if(l==null)return;
        lastLat=l.getLatitude();lastLon=l.getLongitude();
        float kmh=l.hasSpeed()?l.getSpeed()*3.6f:0f;
        if(speedometer!=null)speedometer.setSpeed(kmh);
        if(gpsState!=null)gpsState.setText("GPS  •  "+(l.getProvider()==null?"متصل":l.getProvider()));
        if(driveState!=null){driveState.setText("●  GPS READY");driveState.setTextColor(GREEN);}
        if(coords!=null)coords.setText(String.format(Locale.US,"%.4f , %.4f",lastLat,lastLon));
        if(homeMap!=null)homeMap.updateLocation(lastLat,lastLon);
    }
    @Override public void onProviderDisabled(String p){if(gpsState!=null)gpsState.setText("GPS  •  متوقف");}
    @Override public void onProviderEnabled(String p){if(gpsState!=null)gpsState.setText("GPS  •  متصل");}
    @Override public void onStatusChanged(String p,int s,Bundle e){}

    private void openMap(){
        Uri geo=Uri.parse("geo:"+lastLat+","+lastLon+"?q="+lastLat+","+lastLon);
        Intent i=new Intent(Intent.ACTION_VIEW,geo);
        try{startActivity(i);}catch(Exception e){RuntimeRouter.openWeb(this,"Najm Maps","https://www.openstreetmap.org");}
    }

    private void openMusic(){
        String[] pkgs={"com.spotify.music","com.google.android.music","com.android.music","com.sec.android.app.music"};
        for(String p:pkgs){
            Intent i=getPackageManager().getLaunchIntentForPackage(p);
            if(i!=null){startActivity(i);return;}
        }
        RuntimeRouter.openWeb(this,"Najm Music","https://music.youtube.com");
    }

    private void buildShade(){
        shade=new FrameLayout(this);
        shade.setBackgroundColor(Color.argb(150,0,0,0));
        shade.setVisibility(View.GONE);

        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16),dp(12),dp(16),dp(12));
        panel.setBackground(card(Color.rgb(18,31,47),24,Color.argb(130,75,156,211)));

        LinearLayout head=new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=label("مركز التحكم",21,Color.WHITE,true);
        head.addView(title,new LinearLayout.LayoutParams(0,dp(40),1));
        TextView close=label("×",28,SOFT,true);
        close.setGravity(Gravity.CENTER);
        close.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){hideShade();}});
        head.addView(close,new LinearLayout.LayoutParams(dp(48),dp(40)));
        panel.addView(head);

        LinearLayout row1=new LinearLayout(this);
        String[] titles1={"Wi-Fi","Bluetooth","GPS","USB"};
        int[] colors1={Color.rgb(42,103,160),Color.rgb(49,88,145),Color.rgb(38,118,92),Color.rgb(47,116,145)};
        for(int i=0;i<4;i++){
            final int idx=i;
            TextView t=tile(i==0?"⌁":i==1?"B":i==2?"⌖":"▤",titles1[i],colors1[i]);
            t.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
                if(idx==0)startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                if(idx==1)startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                if(idx==2)startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                if(idx==3)startActivity(new Intent(MainActivity.this,NajmFileManagerActivity.class));
            }});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(67),1);
            if(i>0)lp.rightMargin=dp(6);
            row1.addView(t,lp);
        }
        LinearLayout.LayoutParams r1lp=new LinearLayout.LayoutParams(-1,dp(70));
        r1lp.topMargin=dp(8);
        panel.addView(row1,r1lp);

        LinearLayout row2=new LinearLayout(this);
        String[] titles2={"التطبيقات","YouTube","الملفات","الإعدادات"};
        int[] actions={2,0,3,5};
        int[] colors2={Color.rgb(48,90,145),Color.rgb(180,44,52),Color.rgb(47,106,135),Color.rgb(86,73,124)};
        for(int i=0;i<4;i++){
            final int action=actions[i];
            TextView t=tile(i==0?"▦":i==1?"▶":i==2?"▤":"⚙",titles2[i],colors2[i]);
            t.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){hideShade();runAction(action);}});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(64),1);
            if(i>0)lp.rightMargin=dp(6);
            row2.addView(t,lp);
        }
        LinearLayout.LayoutParams r2lp=new LinearLayout.LayoutParams(-1,dp(67));
        r2lp.topMargin=dp(6);
        panel.addView(row2,r2lp);

        TextView bl=label("السطوع",12,SOFT,false);
        LinearLayout.LayoutParams blp=new LinearLayout.LayoutParams(-1,dp(22));blp.topMargin=dp(8);panel.addView(bl,blp);
        SeekBar bright=new SeekBar(this);bright.setMax(100);bright.setProgress(70);
        bright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean f){WindowManager.LayoutParams lp=getWindow().getAttributes();lp.screenBrightness=Math.max(.05f,p/100f);getWindow().setAttributes(lp);}
            public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}
        });panel.addView(bright,new LinearLayout.LayoutParams(-1,dp(34)));

        TextView vl=label("الصوت",12,SOFT,false);panel.addView(vl,new LinearLayout.LayoutParams(-1,dp(22)));
        final AudioManager audio=(AudioManager)getSystemService(AUDIO_SERVICE);
        SeekBar vol=new SeekBar(this);
        vol.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        vol.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
        vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean f){if(f)audio.setStreamVolume(AudioManager.STREAM_MUSIC,p,0);}
            public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}
        });panel.addView(vol,new LinearLayout.LayoutParams(-1,dp(34)));

        FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(-1,dp(356));
        pp.gravity=Gravity.TOP;pp.leftMargin=dp(10);pp.rightMargin=dp(10);pp.topMargin=dp(5);
        shade.addView(panel,pp);
        shade.setOnTouchListener(new View.OnTouchListener(){
            @Override public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){downY=e.getRawY();return true;}
                if(e.getAction()==MotionEvent.ACTION_UP){if(e.getRawY()-downY<-dp(40))hideShade();return true;}
                return true;
            }
        });
        shell.addView(shade,new FrameLayout.LayoutParams(-1,-1));
    }

    private void showShade(){
        if(shadeOpen)return;
        shadeOpen=true;
        shade.setVisibility(View.VISIBLE);
        shade.setTranslationY(-dp(365));
        shade.setAlpha(0f);
        shade.animate().translationY(0).alpha(1f).setDuration(230).start();
    }

    private void hideShade(){
        if(!shadeOpen)return;
        shadeOpen=false;
        shade.animate().translationY(-dp(365)).alpha(0f).setDuration(190).withEndAction(new Runnable(){
            @Override public void run(){shade.setVisibility(View.GONE);}
        }).start();
    }

    @Override public void onBackPressed(){
        if(shadeOpen){hideShade();return;}
        try{moveTaskToBack(true);}catch(Exception e){super.onBackPressed();}
    }

    @Override protected void onResume(){
        super.onResume();
        handler.post(tick);
        NajmRecentStore.touch(this,"home","الرئيسية","com.najmspace.app.MainActivity");
        if(homeMap!=null)homeMap.onResume();
        if(!locationListening)startLocationIfAllowed();
        if(sensorManager!=null && orientationSensor!=null)sensorManager.registerListener(this,orientationSensor,SensorManager.SENSOR_DELAY_UI);
    }

    @Override protected void onPause(){
        handler.removeCallbacks(tick);
        if(sensorManager!=null)sensorManager.unregisterListener(this);
        if(homeMap!=null)homeMap.onPause();
        try{
            if(locationManager!=null && locationListening){
                locationManager.removeUpdates(this);
                locationListening=false;
            }
        }catch(Exception ignored){}
        super.onPause();
    }

    @Override protected void onDestroy(){
        try{if(locationManager!=null)locationManager.removeUpdates(this);}catch(Exception ignored){}
        super.onDestroy();
    }
}
