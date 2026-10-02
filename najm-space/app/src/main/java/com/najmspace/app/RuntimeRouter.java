package com.najmspace.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

public final class RuntimeRouter {
    public static final String MODE_WEB="internal_web";
    public static final String MODE_LEGACY="legacy_container";

    private RuntimeRouter(){}

    public static void openWeb(Activity a,String title,String url){
        String t=title==null?"":title.toLowerCase();
        String u=url==null?"":url.toLowerCase();
        if(t.contains("appgallery") || u.contains("appgallery.huawei.com") || u.contains("mobileservices/appgallery")){
            a.startActivity(new Intent(a,HuaweiAppGalleryInstallerActivity.class));
            return;
        }
        Intent i=new Intent(a,InternalBrowserActivity.class);
        i.putExtra("title",title);
        i.putExtra("url",url);
        a.startActivity(i);
    }

    public static void openYouTube(Activity a){
        if(Build.VERSION.SDK_INT<=20){
            NajmLegacyYouTube.open(a);
            return;
        }
        Intent i=new Intent(a,NajmYouTubeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        a.startActivity(i);
    }

    public static void openRuntimeManager(Activity a){
        a.startActivity(new Intent(a,RuntimeManagerActivity.class));
    }
}
