package com.najmspace.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class SpeedometerView extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float speed = 0f;
    private float shownSpeed = 0f;

    public SpeedometerView(Context c){ super(c); init(); }
    public SpeedometerView(Context c, AttributeSet a){ super(c,a); init(); }

    private void init(){
        paint.setStrokeCap(Paint.Cap.ROUND);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setSpeed(float kmh){
        speed = Math.max(0f, Math.min(220f, kmh));
        animateStep();
    }

    private void animateStep(){
        final float start = shownSpeed;
        final float end = speed;
        final long begin = System.currentTimeMillis();
        post(new Runnable(){
            @Override public void run(){
                float t = Math.min(1f, (System.currentTimeMillis()-begin)/320f);
                shownSpeed = start + (end-start)*(1f-(1f-t)*(1f-t));
                invalidate();
                if(t < 1f) postDelayed(this,16);
            }
        });
    }

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(), h=getHeight();
        float cx=w/2f, cy=h*0.58f;
        float radius=Math.min(w,h)*0.39f;
        RectF arc=new RectF(cx-radius,cy-radius,cx+radius,cy+radius);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(radius*0.12f);
        paint.setColor(Color.rgb(35,55,78));
        c.drawArc(arc,140,260,false,paint);

        paint.setColor(Color.rgb(45,175,255));
        c.drawArc(arc,140,260*(shownSpeed/220f),false,paint);

        for(int i=0;i<=11;i++){
            double a=Math.toRadians(140 + (260.0*i/11.0));
            float r1=radius*0.79f, r2=radius*0.92f;
            float x1=cx+(float)Math.cos(a)*r1;
            float y1=cy+(float)Math.sin(a)*r1;
            float x2=cx+(float)Math.cos(a)*r2;
            float y2=cy+(float)Math.sin(a)*r2;
            paint.setColor(i<=(int)(shownSpeed/20f)?Color.rgb(225,181,82):Color.rgb(100,125,155));
            paint.setStrokeWidth(radius*0.025f);
            c.drawLine(x1,y1,x2,y2,paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(radius*0.62f);
        paint.setFakeBoldText(true);
        c.drawText(String.valueOf(Math.round(shownSpeed)),cx,cy+radius*0.12f,paint);

        paint.setFakeBoldText(false);
        paint.setColor(Color.rgb(175,195,220));
        paint.setTextSize(radius*0.18f);
        c.drawText("km/h",cx,cy+radius*0.42f,paint);

        paint.setColor(Color.rgb(229,181,82));
        paint.setTextSize(radius*0.12f);
        c.drawText("GPS SPEED",cx,cy+radius*0.64f,paint);
    }
}
