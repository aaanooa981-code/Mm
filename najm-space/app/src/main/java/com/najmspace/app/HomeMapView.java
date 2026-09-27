package com.najmspace.app;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Locale;

public class HomeMapView extends WebView {
    private double lat=24.7136, lon=46.6753;
    private long lastLoad=0;

    public HomeMapView(Context c){super(c);init();}
    public HomeMapView(Context c, AttributeSet a){super(c,a);init();}

    private void init(){
        setBackgroundColor(Color.rgb(13,31,51));
        WebSettings s=getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        setWebViewClient(new WebViewClient());
        loadMap(lat,lon,true);
    }

    public void updateLocation(double newLat,double newLon){
        double dLat=Math.abs(newLat-lat), dLon=Math.abs(newLon-lon);
        lat=newLat;lon=newLon;
        long now=SystemClock.elapsedRealtime();
        if(now-lastLoad>10000 && (dLat>.00020 || dLon>.00020)){
            loadMap(lat,lon,false);
        }
    }

    private void loadMap(double la,double lo,boolean force){
        double span=.012;
        double left=lo-span, right=lo+span, bottom=la-span*.65, top=la+span*.65;
        String url=String.format(Locale.US,
            "https://www.openstreetmap.org/export/embed.html?bbox=%f%%2C%f%%2C%f%%2C%f&layer=mapnik&marker=%f%%2C%f",
            left,bottom,right,top,la,lo);
        lastLoad=SystemClock.elapsedRealtime();
        loadUrl(url);
    }
}
