package com.jbak.JbakKeyboard;

import com.jbak.JbakKeyboard.JbKbd.LatinKey;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;

/** Собственная картинка для клавиш. Генерируется на основе {@link Key#label}*/    
class KeyDrw extends RectShape
{
    public static final String DRW_PREFIX = "d_"; 
    Bitmap bmp;
    String txtMain;
    String txtSmall;
    boolean m_bPreview = false;
    Rect rb;
    public static int GAP = 5;
    public static final int DELIM = 2;
    public static final int DEFAULT_GAP = 5;
    public static final int BIG_GAP = 8;
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
//    public KeyDrw(CharSequence lab,int w,int h,boolean bPreview)
//    {
//        m_bPreview = bPreview;
//        rb = new Rect(0,0,w,h);
//    }
//    public KeyDrw(Bitmap bmp,int w,int h,boolean bPreview)
//    {
//        m_bPreview = bPreview;
//        this.bmp = bmp;
//        rb = new Rect(0,0,w,h);
//    }
    final void drawSingleBitmap(Canvas canvas,float x,float y,Paint paint)
    {
      int color = st.kv().m_curDesign.textColor;
      if(m_bPreview)
      {
          paint.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP));
      }
      else if(color!=st.DEF_COLOR)
    	  paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
      canvas.drawBitmap(bmp, x,y, paint);
    }
    @Override
    public void draw(Canvas canvas, Paint paint)
    {
        if(txtMain==null&&bmp==null||JbKbdView.inst==null)
            return;
//      Rect rb = canvas.getClipBounds();
        boolean bPrev = st.kv().isPreviewEnabled();
        Paint p1 = JbKbdView.inst.m_tpMainKey;
        if(txtMain==null&&bmp!=null)
        {
// Рисуем просто картинку, без текста (по центру)
            if(!m_bPreview)
                canvas.translate(0-rb.width()/2, 0-rb.height()/2);
            drawSingleBitmap(canvas,rb.width()/2-bmp.getWidth()/2,rb.height()/2-bmp.getHeight()/2, paint);
            return;
        }
        if(m_bPreview)
        {
//Рисуем просмотр               
            p1 = JbKbdView.inst.m_tpPreview;
            float mw = p1.measureText(txtMain);
            float y = rb.height()/2+(0-p1.ascent())/2;
            float x = rb.width()/2-mw/2;
            canvas.drawText(txtMain, x, y, p1);
            return;
        }
//Если текст длинее 1 символа - считаем меткой и рисуем с помощью JbKbdView.m_tpLabel 
        if(txtMain.length()>1)
        {
            p1 = st.kv().m_tpLabel;
        }
//canvas сдвинут к середине, вернём его в позицию 0,0           
        canvas.translate(0-rb.width()/2, 0-rb.height()/2);
        Paint p2 = JbKbdView.inst.m_tpSmallKey;
        if(txtMain.length()==1)
        {
            if(JbKbdView.inst.isUpperCase())
                txtMain = txtMain.toUpperCase();
            else
                txtMain = txtMain.toLowerCase();
        }
        Rect b = new Rect();
        p1.getTextBounds(txtMain, 0, txtMain.length(), b);
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
        w1 = (int) p1.measureText(txtMain);
        int fh = GAP*2+h1+h2+DELIM; // Полная высота
        if(fh>rb.height())
        {
            // Всё изображение не уместится
            int x2 = rb.width()-GAP-w2;
            if(bmp!=null)
            {
                drawSingleBitmap(canvas, x2, GAP+3, p2);
            }
            else if(txtSmall!=null)
            {
                canvas.drawText(txtSmall, x2, GAP+a2, p2);
            }
            int x = rb.width()/2-w1/2;
            while(x+w1>x2-DELIM&&x>GAP)
                --x;
            canvas.drawText(txtMain, x, rb.height()-GAP-d1, p1);
        }
        else
        {
            // Всё изображение умещается
            if(bmp!=null)
            {
              drawSingleBitmap(canvas, rb.width()/2-w2/2, GAP+3, p2);
            }
            else if(txtSmall!=null)
            {
                canvas.drawText(txtSmall, rb.width()/2-w2/2, GAP+a2, p2);
            }
            canvas.drawText(txtMain, rb.width()/2-w1/2, rb.height()-GAP-d1, p1);
            
        }
    }
}
