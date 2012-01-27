package com.jbak.JbakKeyboard;

import java.util.Vector;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;

import com.jbak.CustomGraphics.CustomButtonDrawable;
import com.jbak.JbakKeyboard.EditSetActivity.EditSet;
import com.jbak.JbakKeyboard.IKeyboard.KbdDesign;

public class KeyboardPaints
{
    public static KeyboardPaints inst;
/** Шрифт клавиатуры для основных символов */    
    public Paint main;
/** Шрифт клавиатуры для дополнительных символов */    
    public Paint second;
/** Шрифт клавиатуры для меток */    
    public Paint label;
/** Основной цвет*/    
    public int mainColor = Color.WHITE;
/** Дополнительный цвет*/    
    public int secondColor = st.DEF_COLOR;
    public PorterDuffColorFilter bmpColorFilter = null;
    int m_defaultFontSize = 32;
    int m_defaultLabelSize = 21;
    boolean m_bMainBold = false;
    Vector <BitmapCache> m_arBitmaps = new Vector<BitmapCache>();
    int BitmapCacheSize = 120;
    StateListDrawable funcBackDrawable;
    Paint bitmapNormal;
    Paint bitmapFunc;
    Paint bitmapPreview;
    public KeyboardPaints()
    {
        inst = this;
    }
    void setDefault(int fontSize,int labelSize,KbdDesign design,int defColor)
    {
        mainColor = design.textColor==st.DEF_COLOR?defColor:design.textColor;
        if(design.m_kbdFuncKeys!=null&&design.m_kbdFuncKeys.textColor!=st.DEF_COLOR)
            secondColor = design.m_kbdFuncKeys.textColor;
        else
            secondColor = mainColor;
        m_bMainBold = st.has(design.flags,st.DF_BOLD);
        m_defaultFontSize = fontSize;
        m_defaultLabelSize = labelSize;
        if(design.m_kbdFuncKeys!=null&&design.m_kbdFuncKeys.m_keyBackground!=null)
        {
            funcBackDrawable = design.m_kbdFuncKeys.m_keyBackground.getStateDrawable();
            if(st.kv().m_KeyBackDrw instanceof CustomButtonDrawable)
            {
//                design.m_keyBackground.setDependentback(design.m_kbdFuncKeys.m_keyBackground);
                ((CustomButtonDrawable)st.kv().m_KeyBackDrw).setDependentDrawable(funcBackDrawable);
            }
        }
        else
        {
            funcBackDrawable = null;
        }
        bitmapNormal = new Paint();
        bitmapNormal.setColorFilter(new PorterDuffColorFilter(mainColor, PorterDuff.Mode.SRC_ATOP));
        if(secondColor==mainColor)
            bitmapFunc = bitmapNormal;
        else
        {
            bitmapFunc = new Paint();
            bitmapFunc.setColorFilter(new PorterDuffColorFilter(secondColor, PorterDuff.Mode.SRC_ATOP));
        }
        bitmapPreview = new Paint();
        bitmapPreview.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP));
    }
    public final Paint getBitmapPaint(boolean bPreview,boolean bFunc)
    {
        if(bPreview)
            return bitmapPreview;
        if(bFunc)
            return bitmapFunc;
        return bitmapNormal;
    }
    final EditSet getDefaultMain()
    {
        EditSet es = new EditSet();
        es.fontSize = m_defaultFontSize;
        es.style = 0;
        if(m_bMainBold)
            es.style|=Typeface.BOLD;
        return es;
    }
    final EditSet getDefaultSecond()
    {
        EditSet es = new EditSet();
     // создаем second        
         es.fontSize = m_defaultFontSize/2;
         return es;
    }
    final EditSet getDefaultLabel()
    {
        EditSet es = new EditSet();
         es.fontSize = m_defaultLabelSize;
         es.style=Typeface.BOLD;
         return es;
    }
    final void createFromSettings()
    {
        EditSet es = new EditSet();
        if(!es.load(st.PREF_KEY_MAIN_FONT)||es.isDefault())
            main = getDefaultMain().getTextPaint();
        else
            main = es.getTextPaint();
        if(!es.load(st.PREF_KEY_SECOND_FONT)||es.isDefault())
            second = getDefaultSecond().getTextPaint();
        else
        {
            second = es.getTextPaint(true);
            
        }
        if(!es.load(st.PREF_KEY_LABEL_FONT)||es.isDefault())
            label = getDefaultLabel().getTextPaint();
        else
            label = es.getTextPaint();
    }
    void addBitmap(BitmapCache c)
    {
        if(m_arBitmaps.size()==BitmapCacheSize)
            m_arBitmaps.remove(0);
        m_arBitmaps.add(c);
    }
    BitmapDrawable getBitmap(String path)
    {
        for(BitmapCache bc:m_arBitmaps)
        {
            if(path.equals(bc.path))
                return bc.bd;
        }
        try{
            BitmapDrawable bd = (BitmapDrawable)BitmapDrawable.createFromPath(path);
            if(bd!=null)
                addBitmap(new BitmapCache(path, bd));
            return bd;
        }
        catch (Throwable e) {
        }
        return null;
    }
    BitmapDrawable getBitmap(int id)
    {
        for(BitmapCache bc:m_arBitmaps)
        {
            if(bc.resId==id)
                return bc.bd;
        }
        BitmapDrawable bd =(BitmapDrawable)st.c().getResources().getDrawable(id);
        addBitmap(new BitmapCache(id, bd));
        return bd;
    }
    public static class BitmapCache
    {
        public BitmapCache(String path,BitmapDrawable b)
        {
            this.path = path;
            bd = b;
        }
        public BitmapCache(int id,BitmapDrawable b)
        {
            resId = id;
            bd = b;
        }
        int resId = 0;
        String path;
        BitmapDrawable bd;
    }
}
