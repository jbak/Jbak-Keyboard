package com.jbak.JbakKeyboard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;

import com.jbak.JbakKeyboard.JbKbd.LatinKey;

/** Собственная картинка для клавиш. Генерируется на основе {@link Key#label}*/    
class KeyDrw extends RectShape
{
    public static final String DRW_PREFIX = "d_"; 
    Bitmap bmp;
    String txtMain;
    String txtSmall;
    boolean m_bPreview = false;
    boolean m_bLongPreview = false;
    boolean m_bSmallLabel = false;
    Rect rb;
    boolean m_bFunc = false;
    public static int GAP = 5;
    public static final int DELIM = 2;
    public static final int DEFAULT_GAP = 5;
    public static final int BIG_GAP = 8;
    DrwCache m_c;
/** Конструктор
* @param key Клавиша из которой создаётся рисовалка */
    public KeyDrw(Keyboard.Key key)
    {
        set(key, false);
    }
/** Пустой конструктор */    
    public KeyDrw()
    {
    }
    public Drawable getDrawable()
    {
        ShapeDrawable drw = new ShapeDrawable(this);
        drw.setBounds(rb);
        return drw;
    }
    public final void setSmallLabel(boolean small)
    {
        m_bSmallLabel = small;
    }
    public final void setFuncKey(boolean func)
    {
        m_bFunc = func;
    }
    final void useTextColor(Paint pt)
    {
        int c = m_bFunc?st.paint().secondColor:st.paint().mainColor;
        if(c!=st.DEF_COLOR)
            pt.setColor(c);
    }
//    @Override
//    protected void onResize(float width, float height) 
//    {
//        super.onResize(width, height);
//        if(m_bPreview&&st.paint().previewBack!=null)
//        {
//            st.paint().previewBack.setBounds(new Rect(0,0,(int)width,(int)height));
//        }
//    };
    void set(Keyboard.Key key, boolean bPreview)
    {
        m_bPreview = bPreview;
        if(key.icon!=null)
        {
            if(key.icon instanceof BitmapDrawable)
            {
                this.bmp = ((BitmapDrawable)key.icon).getBitmap();
            }
            else if(key.icon instanceof ShapeDrawable)
            {
                KeyDrw kd = ((LatinKey)key).m_kd;
                txtMain = kd.txtMain;
                txtSmall = kd.txtSmall;
                bmp = kd.bmp;
            }
        }
        else
        {
            bmp = null;
            CharSequence lab = key.label;
            int f = lab.toString().indexOf('\n');
            if(f>-1)
            {
                txtSmall = lab.subSequence(0, f).toString();
                bmp = st.getBitmapByCmd(st.getCmdByLabel(txtSmall));
                txtMain = lab.subSequence(f+1, lab.length()).toString();
            }
            else
            {
                txtMain = lab.toString();
                txtSmall = null;
            }
        }
        if(m_bPreview)
            rb = new Rect(0, 0, st.kv().m_PreviewHeight, st.kv().m_PreviewHeight);
        else    
            rb = new Rect(0,0,key.width,key.height);

    }
    final void drawFuncBackground(Canvas canvas)
    {
        if(m_bFunc&&st.paint().funcBackDrawable!=null)
            st.paint().funcBackDrawable.draw(canvas);
    }
//    @Override
//    public void draw(Canvas canvas, Paint paint)
//    {
//        if(txtMain==null&&bmp==null||JbKbdView.inst==null)
//            return;
////      Rect rb = canvas.getClipBounds();
//        Paint p1 = st.paint().main;
//        if(txtMain==null&&bmp!=null)
//        {
//// Рисуем просто картинку, без текста (по центру)
//            if(!m_bPreview)
//                canvas.translate(0-rb.width()/2, 0-rb.height()/2);
//            drawFuncBackground(canvas);
//            drawSingleBitmap(canvas,rb.width()/2-bmp.getWidth()/2,rb.height()/2-bmp.getHeight()/2, paint);
//            return;
//        }
//        if(m_bPreview)
//        {
////Рисуем просмотр     
//            if(txtMain!=null)
//            {
//                p1 = JbKbdView.inst.m_tpPreview;
//                float mw = p1.measureText(txtMain);
//                float y = rb.height()/2+(0-p1.ascent())/2;
//                float x = rb.width()/2-mw/2;
//                if(txtMain.length()>1)
//                {
//                    p1.setTextSize(st.kv().m_PreviewTextSize/2);
//                }
//                canvas.drawText(txtMain, x, y, p1);
//            }
//            return;
//        }
////Если текст длинее 1 символа - считаем меткой и рисуем с помощью JbKbdView.m_tpLabel 
//        if(m_bSmallLabel)
//        {
//            p1 = st.paint().second;
//        }
//        else if(txtMain.length()>1)
//        {
//            p1 = st.paint().label;
//        }
//        useTextColor(p1);
////canvas сдвинут к середине, вернём его в позицию 0,0           
//        canvas.translate(0-rb.width()/2, 0-rb.height()/2);
//        drawFuncBackground(canvas);
//        Paint p2 = st.paint().second;
//        useTextColor(p2);
//        if(txtMain.length()==1)
//        {
//            if(JbKbdView.inst.isUpperCase())
//                txtMain = txtMain.toUpperCase();
//            else
//                txtMain = txtMain.toLowerCase();
//        }
//        Rect b = new Rect();
//        p1.getTextBounds(txtMain, 0, txtMain.length(), b);
//        int a1 = (int) (0-p1.ascent());
//        int a2 = (int) (0-p2.ascent());
//        int d1 = (int) p1.descent();
//        int d2 = (int) p2.descent();
//        int w1,h1=d1+a1,w2,h2;
//        if(bmp!=null)
//        {
//            h2 = bmp.getHeight();
//            w2 = bmp.getWidth();
//        }
//        else if(txtSmall!=null)
//        {
//            h2 = a2+d2;
//            w2 = (int) p2.measureText(txtSmall);
//        }
//        else
//        {
//            h2 = w2 = 0;
//        }
//        w1 = (int) p1.measureText(txtMain);
//        int fh = GAP*2+h1+h2+DELIM; // Полная высота
//        if(fh>rb.height())
//        {
//            // Изображение основного текста и доп. символов не умещается в высоту
//            int x2 = rb.width()-GAP-w2;
//            if(bmp!=null)
//            {
//                drawSingleBitmap(canvas, x2, GAP+3, p2);
//            }
//            else if(txtSmall!=null)
//            {
//                canvas.drawText(txtSmall, x2, GAP+a2, p2);
//            }
//            if(txtMain!=null)
//            {
//                int x = rb.width()/2-w1/2;
//                while(x+w1>x2-DELIM&&x>GAP)
//                    --x;
//                canvas.drawText(txtMain, x, rb.height()-GAP-d1-1, p1);
//            }
//        }
//        else
//        {
//            // Всё изображение умещается
//            if(bmp!=null)
//            {
//              drawSingleBitmap(canvas, rb.width()/2-w2/2, GAP+3, p2);
//            }
//            else if(txtSmall!=null)
//            {
//                canvas.drawText(txtSmall, rb.width()/2-w2/2, GAP+a2, p2);
//            }
//            int y = h2+GAP+a1;
//            int dy = (rb.height()-GAP-y)/2;
//            if(dy<4)
//                dy = 0;
//            canvas.drawText(txtMain, rb.width()/2-w1/2, y+dy, p1);
//        }
//    }
    static class DrwCache
    {
        String mainLower;
        String mainUpper;
        float m_xMainLower;
        float m_yMainLower;
        float m_xMainUpper;
        float m_yMainUpper;
        float m_xSmall;
        float m_ySmall;
    };
    final int horzX(int minX,int preferX,int mainTextWidth,int secondTextPos)
    {
        if(preferX+mainTextWidth+DELIM<=secondTextPos)
            return preferX;
        return st.max(minX, secondTextPos-mainTextWidth-DELIM);
    }
    final void buildCache()
    {
        if(txtMain==null&&bmp==null||JbKbdView.inst==null)
            return;
        m_c = new DrwCache();
//      Rect rb = canvas.getClipBounds();
        Paint p1 = st.paint().main;
        if(txtMain==null&&bmp!=null)
        {
// Рисуем просто картинку, без текста (по центру)
            m_c.m_xMainLower = rb.width()/2-bmp.getWidth()/2;
            m_c.m_yMainLower = rb.height()/2-bmp.getHeight()/2;
            return;
        }
        if(m_bPreview)
        {
//Рисуем просмотр     
            if(txtMain!=null)
            {
                p1 = JbKbdView.inst.m_tpPreview;
                float mw = p1.measureText(txtMain);
                m_c.m_yMainLower = rb.height()/2+(0-p1.ascent())/2;
                m_c.m_xMainLower = rb.width()/2-mw/2;
            }
            return;
        }
//Если текст длинее 1 символа - считаем меткой и рисуем с помощью JbKbdView.m_tpLabel 
        if(m_bSmallLabel)
        {
            p1 = st.paint().second;
        }
        else if(txtMain.length()>1)
        {
            p1 = st.paint().label;
        }
//canvas сдвинут к середине, вернём его в позицию 0,0           
        Paint p2 = st.paint().second;
        if(txtMain.length()==1)
        {
            m_c.mainLower = txtMain.toLowerCase();
            m_c.mainUpper = txtMain.toUpperCase();
        }
        else
        {
            m_c.mainLower = txtMain;
            m_c.mainUpper = txtMain;
        }
        int a1 = (int) (0-p1.ascent());
        int a2 = (int) (0-p2.ascent());
        int d1 = (int) p1.descent();
        int d2 = (int) p2.descent();
        int w1,h1=d1+a1,w2,h2;
        if(bmp!=null)
        {
            h2 = bmp.getHeight();
            w2 = bmp.getWidth();
        }
        else if(txtSmall!=null)
        {
            h2 = a2+d2;
            w2 = (int) p2.measureText(txtSmall);
        }
        else
        {
            h2 = w2 = 0;
        }
        w1 = (int) p1.measureText(m_c.mainLower);
        int fh = st.paint().padding.top+st.paint().padding.left+h1+h2+DELIM; // Полная высота
        if(fh>rb.height())
        {
            // Изображение основного текста и доп. символов не умещается в высоту
            int x2 = rb.width()-st.paint().padding.right-w2;
            if(bmp!=null)
            {
                m_c.m_xSmall = x2;
                m_c.m_ySmall = GAP+3;
            }
            else if(txtSmall!=null)
            {
                m_c.m_xSmall = x2;
                m_c.m_ySmall = GAP+a2;
            }
            if(txtMain!=null)
            {
                m_c.m_xMainLower = horzX(st.paint().padding.left,rb.width()/2-w1/2,w1,rb.width()-st.paint().padding.right-w2);
                m_c.m_yMainLower = rb.height()-st.paint().padding.bottom-d1;
                if(m_c.mainUpper==m_c.mainLower)
                {
                    m_c.m_xMainUpper = m_c.m_xMainLower;
                }
                else
                {
                    w1 = (int) p1.measureText(m_c.mainUpper);
                    m_c.m_xMainUpper = horzX(st.paint().padding.left,rb.width()/2-w1/2,w1,rb.width()-st.paint().padding.right-w2);
                }
                m_c.m_yMainUpper = m_c.m_yMainLower;
            }
        }
        else
        {
            // Всё изображение умещается
            if(bmp!=null)
            {
                m_c.m_xSmall = rb.width()/2-w2/2;
                m_c.m_ySmall = st.paint().padding.top;
            }
            else if(txtSmall!=null)
            {
                m_c.m_xSmall = rb.width()/2-w2/2;
                m_c.m_ySmall = st.paint().padding.top+a2;
            }
            int y = h2+DELIM+a1;
            int dy = (rb.height()-st.paint().padding.bottom-y)/2;
            if(dy<4)
                dy = 0;
            m_c.m_xMainLower = rb.width()/2-w1/2;
            m_c.m_yMainLower = y+dy;
            m_c.m_yMainUpper = m_c.m_yMainLower;
            if(m_c.mainUpper!=m_c.mainLower)
            {
                m_c.m_xMainUpper = rb.width()/2-p1.measureText(m_c.mainUpper)/2;
            }
            else
            {
                m_c.m_xMainUpper = m_c.m_xMainLower;
            }
        }
    }
    @Override
    public void draw(Canvas canvas, Paint paint)
    {
        if(m_c==null)
            buildCache();
        if(m_c==null||txtMain==null&&bmp==null||JbKbdView.inst==null)
            return;
        Paint p1 = st.paint().main;
        if(txtMain==null&&bmp!=null)
        {
// Рисуем просто картинку, без текста (по центру)
            if(!m_bPreview)
                canvas.translate(0-rb.width()/2, 0-rb.height()/2);
            drawFuncBackground(canvas);
            canvas.drawBitmap(bmp, m_c.m_xMainLower,m_c.m_yMainLower, st.paint().getBitmapPaint(m_bPreview, m_bFunc));
            return;
        }
        if(m_bPreview)
        {
//Рисуем просмотр     
            String text = m_bLongPreview?txtSmall:txtMain;
//            st.paint().previewBack.draw(canvas);
            if(text!=null)
            {
                p1 = JbKbdView.inst.m_tpPreview;
                if(text.length()>1)
                {
                    p1.setTextSize(st.kv().m_PreviewTextSize/2);
                }
                else
                {
                    p1.setTextSize(st.kv().m_PreviewTextSize);
                }
                canvas.drawText(text, m_c.m_xMainLower, m_c.m_yMainLower, p1);
            }
            return;
        }
//Если текст длинее 1 символа - считаем меткой и рисуем с помощью JbKbdView.m_tpLabel 
        boolean bUp = JbKbdView.inst.isUpperCase(); 
        if(m_bSmallLabel)
        {
            bUp = false;
            p1 = st.paint().second;
        }
        else if(txtMain.length()>1)
        {
            bUp = false;
            p1 = st.paint().label;
        }
        useTextColor(p1);
//canvas сдвинут к середине, вернём его в позицию 0,0           
        canvas.translate(0-rb.width()/2, 0-rb.height()/2);
        drawFuncBackground(canvas);
        Paint p2 = st.paint().second;
        useTextColor(p2);
        if(bmp!=null)
        {
            if(txtMain==null)
                canvas.drawBitmap(bmp, m_c.m_xMainLower, m_c.m_yMainLower, st.paint().getBitmapPaint(m_bPreview, m_bFunc));
            else
                canvas.drawBitmap(bmp, m_c.m_xSmall, m_c.m_ySmall, st.paint().getBitmapPaint(m_bPreview, m_bFunc));
        }
        if(txtSmall!=null&&bmp==null)
        {
            canvas.drawText(txtSmall,m_c.m_xSmall, m_c.m_ySmall, p2);
        }
        if(bUp)
            canvas.drawText(m_c.mainUpper, m_c.m_xMainUpper, m_c.m_yMainUpper, p1);
        else
            canvas.drawText(m_c.mainLower, m_c.m_xMainLower, m_c.m_yMainLower, p1);
    }

}

