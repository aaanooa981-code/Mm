package com.najmspace.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class MapPreviewView extends View {
    private Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private float pulse=0f;

    public MapPreviewView(Context c){super(c);start();}
    public MapPreviewView(Context c, AttributeSet a){super(c,a);start();}

    private void start(){
        post(new Runnable(){
            @Override public void run(){
                pulse += .035f;
                if(pulse>1f) pulse=0f;
                invalidate();
                postDelayed(this,33);
            }
        });
    }

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        int w=getWidth(),h=getHeight();
        c.drawColor(Color.rgb(12,28,47));

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2,w/180f));
        p.setColor(Color.rgb(28,74,112));
        for(int i=0;i<5;i++){
            Path road=new Path();
            road.moveTo(-w*.1f,h*(.22f+i*.16f));
            road.cubicTo(w*.25f,h*(.05f+i*.13f),w*.55f,h*(.32f+i*.11f),w*1.1f,h*(.12f+i*.17f));
            c.drawPath(road,p);
        }
        for(int i=0;i<4;i++){
            Path road=new Path();
            road.moveTo(w*(.08f+i*.24f),-h*.1f);
            road.cubicTo(w*(.20f+i*.20f),h*.35f,w*(.05f+i*.23f),h*.62f,w*(.22f+i*.20f),h*1.1f);
            c.drawPath(road,p);
        }

        p.setStrokeWidth(Math.max(4,w/100f));
        p.setColor(Color.rgb(35,154,245));
        Path route=new Path();
        route.moveTo(w*.12f,h*.82f);
        route.cubicTo(w*.35f,h*.70f,w*.48f,h*.54f,w*.58f,h*.41f);
        route.cubicTo(w*.68f,h*.27f,w*.78f,h*.36f,w*.90f,h*.18f);
        c.drawPath(route,p);

        float cx=w*.58f, cy=h*.41f;
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb((int)(110*(1f-pulse)),45,180,255));
        c.drawCircle(cx,cy,12+38*pulse,p);
        p.setColor(Color.rgb(75,195,255));
        c.drawCircle(cx,cy,9,p);

        Path arrow=new Path();
        arrow.moveTo(cx,cy-24);
        arrow.lineTo(cx-12,cy+14);
        arrow.lineTo(cx,cy+8);
        arrow.lineTo(cx+12,cy+14);
        arrow.close();
        p.setColor(Color.WHITE);
        c.drawPath(arrow,p);
    }
}
