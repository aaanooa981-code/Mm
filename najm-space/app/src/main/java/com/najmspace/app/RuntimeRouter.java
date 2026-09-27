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
        String[] packages={"com.google.android.youtube","com.google.android.youtube.tv"};
        for(String pkg:packages){
            try{
                Intent nativeIntent=a.getPackageManager().getLaunchIntentForPackage(pkg);
                if(nativeIntent!=null){
                    nativeIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    a.startActivity(nativeIntent);
                    return;
                }
            }catch(Exception ignored){}
        }

        android.content.SharedPreferences p=a.getSharedPreferences("najmspace",Activity.MODE_PRIVATE);
        String last=p.getString("youtube_last_url","https://m.youtube.com/");
        Intent i=new Intent(a,InternalBrowserActivity.class);
        i.putExtra("title","YouTube");
        i.putExtra("url",last);
        i.putExtra("youtube_mode",true);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        a.startActivity(i);
    }

    public static void openRuntimeManager(Activity a){
        a.startActivity(new Intent(a,RuntimeManagerActivity.class));
    }
}
