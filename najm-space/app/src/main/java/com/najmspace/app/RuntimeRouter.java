package com.najmspace.app;

import android.app.Activity;
import android.content.Intent;

public final class RuntimeRouter {
    public static final String MODE_WEB="internal_web";
    public static final String MODE_LEGACY="legacy_container";

    private RuntimeRouter(){}

    public static void openWeb(Activity a,String title,String url){
        Intent i=new Intent(a,InternalBrowserActivity.class);
        i.putExtra("title",title);
        i.putExtra("url",url);
        a.startActivity(i);
    }

    public static void openYouTube(Activity a){
        Intent i=new Intent(a,NajmYouTubeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        a.startActivity(i);
    }

    public static void openRuntimeManager(Activity a){
        a.startActivity(new Intent(a,RuntimeManagerActivity.class));
    }
}
