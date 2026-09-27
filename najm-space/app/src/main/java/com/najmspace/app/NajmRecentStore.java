package com.najmspace.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public final class NajmRecentStore {
    private static final String PREF="najm_recents";
    private NajmRecentStore(){}

    public static void touch(Activity a,String id,String title,String className){
        SharedPreferences p=a.getSharedPreferences(PREF,Context.MODE_PRIVATE);
        long now=System.currentTimeMillis();
        p.edit()
            .putString(id+"_title",title)
            .putString(id+"_class",className)
            .putLong(id+"_time",now)
            .apply();
    }

    public static void remove(Context c,String id){
        SharedPreferences p=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);
        p.edit().remove(id+"_title").remove(id+"_class").remove(id+"_time").apply();
    }

    public static void clear(Context c){
        c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static ArrayList<Item> get(Context c){
        SharedPreferences p=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);
        ArrayList<Item> out=new ArrayList<Item>();
        for(Map.Entry<String,?> e:p.getAll().entrySet()){
            String k=e.getKey();
            if(!k.endsWith("_time"))continue;
            String id=k.substring(0,k.length()-5);
            String title=p.getString(id+"_title",id);
            String cls=p.getString(id+"_class","");
            long time=p.getLong(k,0);
            if(cls.length()>0)out.add(new Item(id,title,cls,time));
        }
        Collections.sort(out,new Comparator<Item>(){
            @Override public int compare(Item a,Item b){
                return a.time==b.time?0:(a.time>b.time?-1:1);
            }
        });
        return out;
    }

    public static class Item {
        public final String id,title,className;
        public final long time;
        public Item(String id,String title,String className,long time){
            this.id=id;this.title=title;this.className=className;this.time=time;
        }
    }
}
