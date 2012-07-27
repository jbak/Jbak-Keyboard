package com.jbak.JbakKeyboard;

import android.app.Service;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.jbak.JbakKeyboard.JbKbd.LatinKey;
import com.jbak.ctrl.SameThreadTimer;

public class KeyboardPopup
{
    SameThreadTimer m_tm;
    OwnView m_view;
    public static int m_w = 100;
    public static int m_h = 80;
    boolean m_bShowUnderKey = true;
    WindowManager m_wm;
    boolean m_bShow = false;
    public KeyboardPopup(Context c,int w,int h)
    {
        m_w = w;
        m_h = h;
        m_view = new OwnView(c);
        m_view.setKey(null, false);
        m_wm = (WindowManager)c.getSystemService(Service.WINDOW_SERVICE);
    }
    public void close()
    {
        if(!m_bShow)
            return;
        try{
            m_wm.removeViewImmediate(m_view);
        }
        catch (Throwable e) {
            
        }
    }
    public void addView(int x,int y)
    {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = m_w;
        lp.height = m_h;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    |WindowManager.LayoutParams.FLAG_FULLSCREEN
                    |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    |WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    ;
        lp.gravity = Gravity.LEFT|Gravity.TOP;
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        lp.x = x;
        lp.y = y;
        m_wm.addView(m_view, lp);
    }
    public void show(JbKbdView v,LatinKey key,boolean bLong)
    {
        m_view.setKey(key, bLong);
        int sh = m_view.getResources().getDisplayMetrics().heightPixels;
        int kbdTop = sh-v.getKeyboard().getHeight();
        int xoff = v.getWidth()/2-m_w/2;
        int yoff = kbdTop-m_h-4;
        if(m_bShowUnderKey)
        {
            xoff = Math.min(v.getWidth()-m_w-4, key.x);
            yoff = Math.max(yoff,kbdTop+key.y-m_h-40);
        }
        close();
        m_bShow = true;
        addView(xoff, yoff);
        if(m_tm!=null)
            m_tm.cancel();
        m_tm = new SameThreadTimer(300,0)
        {
            
            @Override
            public void onTimer(SameThreadTimer timer)
            {
                close();
            }
        };
        m_tm.start();
    }
    public void hide()
    {
        m_view.setKey(null, false);
//        if(isShowing())
//            dismiss();
    }
    public static class OwnView extends View
    {
        LatinKey key;
        boolean bLong=false;
        TextPaint m_pt;
        Paint m_bgPaint;
        RectF m_bgRf;
        public OwnView(Context context)
        {
            super(context);
            m_pt = new TextPaint();
            m_pt.setColor(Color.BLACK);
            m_pt.setAntiAlias(true);
            m_bgRf = new RectF(0, 0, m_w-1, m_h-1);
            m_bgPaint = new Paint();
            m_bgPaint.setColor(0xeeffffff);
        }
        public void setKey(LatinKey k,boolean longPress)
        {
            key = k;
            bLong = longPress;
            invalidate();
        }
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
        {
            setMeasuredDimension(m_w, m_h);
        };
        @Override
        public void draw(Canvas canvas)
        {
            //canvas.drawColor(0x99ffffff);
            if(key==null)
                return;
            canvas.drawRoundRect(m_bgRf, 8, 8, m_bgPaint);
            st.paint().bitmapPreview.setStyle(Style.STROKE);
            canvas.drawRoundRect(m_bgRf, 8, 8,st.paint().bitmapPreview);
            st.paint().bitmapPreview.setStyle(Style.FILL);
            
//            canvas.translate(0, 0-m_h/2);
            st.kv().m_PreviewDrw.draw(canvas, st.paint().bitmapPreview);
//            key.m_kd.draw(canvas, m_pt);
        }
    }
}
