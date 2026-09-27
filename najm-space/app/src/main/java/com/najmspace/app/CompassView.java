package com.najmspace.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class CompassView extends View {
    private final Paint p=new Paint(1);
    private float azimuth=0f;

    public CompassView(Context c){ super(c); p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); }

    public void setAzimuth(float a){ azimuth=a; invalidate(); }

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f;
        float r=Math.min(w,h)*.36f;

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2f,w*.012f));
        p.setColor(Color.rgb(74,148,210));
        c.drawCircle(cx,cy,r,p);

        p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(Math.min(w,h)*.17f);
        p.setColor(Color.WHITE);
        c.drawText("N",cx,cy-r+ p.getTextSize(),p);

        c.save();
        c.rotate(-azimuth,cx,cy);
        p.setColor(Color.rgb(230,70,70));
        android.graphics.Path n=new android.graphics.Path();
        n.moveTo(cx,cy-r*.75f);
        n.lineTo(cx-r*.13f,cy+r*.08f);
        n.lineTo(cx+r*.13f,cy+r*.08f);
        n.close();
        c.drawPath(n,p);
        p.setColor(Color.rgb(210,220,235));
        android.graphics.Path s=new android.graphics.Path();
        s.moveTo(cx,cy+r*.72f);
        s.lineTo(cx-r*.11f,cy-r*.05f);
        s.lineTo(cx+r*.11f,cy-r*.05f);
        s.close();
        c.drawPath(s,p);
        c.restore();

        p.setTextSize(Math.min(w,h)*.12f);
        p.setColor(Color.rgb(185,205,228));
        c.drawText(((int)azimuth)+"°",cx,cy+r*.98f,p);
    }
}
