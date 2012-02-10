package com.jbak.JbakKeyboard;

import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;

import com.jbak.CustomGraphics.CustomButtonDrawable;
import com.jbak.JbakKeyboard.EditSetActivity.EditSet;
import com.jbak.JbakKeyboard.IKeyboard.KbdDesign;

public class KeyboardPaints
{
    public static final int VAL_KEY_HEIGHT_PORTRAIT =1;
    public static final int VAL_KEY_HEIGHT_LANDSCAPE =2;
    public static final int VAL_TEXT_SIZE_MAIN =3;
    public static final int VAL_TEXT_SIZE_SYMBOL =4;
    public static final int VAL_TEXT_SIZE_LABEL =5;
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
    boolean m_bMainBold = false;
    Vector <BitmapCache> m_arBitmaps = new Vector<BitmapCache>();
    int BitmapCacheSize = 120;
    StateListDrawable funcBackDrawable;
    Paint bitmapNormal;
    Paint bitmapFunc;
    Paint bitmapPreview;
    Paint bitmapNoColor;
    Rect padding = new Rect();
    public KeyboardPaints()
    {
        inst = this;
    }
    void setDefault(KbdDesign design,int defColor)
    {
        mainColor = design.textColor==st.DEF_COLOR?defColor:design.textColor;
        if(design.m_kbdFuncKeys!=null&&design.m_kbdFuncKeys.textColor!=st.DEF_COLOR)
            secondColor = design.m_kbdFuncKeys.textColor;
        else
            secondColor = mainColor;
        m_bMainBold = st.has(design.flags,st.DF_BOLD);
//        if(design.m_keyBackground!=null)
//        {
//            previewBack = design.m_keyBackground.clone().getStateDrawable();
//        }
//        else
//        {
//            previewBack = st.kv().m_drwKeyBack;
//        }
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
        bitmapNoColor = new Paint();
        bitmapPreview = new Paint();
        bitmapPreview.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP));
        st.kv().m_KeyBackDrw.getPadding(padding);
        padding.offset(2,2);
    }
    public final Paint getBitmapPaint(KeyDrw d)
    {
        if(d.m_bNoColorIcon)
            return bitmapNoColor;
        if(d.m_bPreview)
            return bitmapPreview;
        if(d.m_bFunc)
            return bitmapFunc;
        return bitmapNormal;
    }
    final EditSet getDefaultMain()
    {
        EditSet es = new EditSet();
        es.fontSize = getValue(st.c(), null, VAL_TEXT_SIZE_MAIN);
        es.style = 0;
        if(m_bMainBold)
            es.style|=Typeface.BOLD;
        return es;
    }
    final EditSet getDefaultSecond()
    {
        EditSet es = new EditSet();
     // создаем second        
        es.fontSize = getValue(st.c(), null, VAL_TEXT_SIZE_SYMBOL);
         return es;
    }
    final EditSet getDefaultLabel()
    {
        EditSet es = new EditSet();
        es.fontSize = getValue(st.c(), null, VAL_TEXT_SIZE_LABEL);
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
    public static final int getScreen(Context c,boolean bPortrait)
    {
        DisplayMetrics dm = c.getResources().getDisplayMetrics(); 
        if(bPortrait)
            return st.max(dm.widthPixels,dm.heightPixels);
        return st.min(dm.widthPixels,dm.heightPixels);
    }
/** Перевод процентов от размера экрана в пиксели
*@param c Контекст 
*@param bPortrait - true - для расчёта используется большее значение ширины/высоты, false - меньшее 
*@param val Значение для перевода
*@return
 */
    public static final float pixelToPerc(Context c,boolean bPortrait,float val)
    {
        float sh = getScreen(c, bPortrait);
        return val/sh;
    }
    /** Преобразует значение в процентах от высоты экрана в пиксели
    *@param c Контекст
    *@param val Значение а процентах от высоты экрана
    *@param bPortrait - true - для расчёта используется большее значение ширины/высоты, false - меньшее 
    *@param bEven true - вернуть четное значение
    *@return Размер в пикселях
     */
        public static int percToPixel(Context c,boolean bPortrait,float val,boolean bEven)
        {
            float sh = getScreen(c, bPortrait);
            float ret = val*sh;
            if(!bEven)
                return (int)ret;
            int r = (int)ret;
            if(r%2>0)
                return r+1;
            return r;
        }
    public static float getDefValue(int type)
    {
        switch(type)
        {
            case VAL_KEY_HEIGHT_PORTRAIT:
                return (float) 0.1;
            case VAL_KEY_HEIGHT_LANDSCAPE:
                return (float) 0.12;
            case VAL_TEXT_SIZE_MAIN:
                return (float) 0.04;
            case VAL_TEXT_SIZE_SYMBOL:
                return (float) 0.025;
            case VAL_TEXT_SIZE_LABEL:
                return (float) 0.03;
        }
        return 0;
    }
    public static int getValue(Context c,SharedPreferences p,int type)
    {
        switch(type)
        {
            case VAL_KEY_HEIGHT_PORTRAIT:
                return percToPixel(c, true,p.getFloat(st.PREF_KEY_HEIGHT_PORTRAIT_PERC, getDefValue(type)),true);
            case VAL_KEY_HEIGHT_LANDSCAPE:
                return percToPixel(c, false,p.getFloat(st.PREF_KEY_HEIGHT_LANDSCAPE_PERC, getDefValue(type)),true);
            case VAL_TEXT_SIZE_MAIN:
            case VAL_TEXT_SIZE_SYMBOL:
            case VAL_TEXT_SIZE_LABEL:
                return percToPixel(c, true,getDefValue(type),false);
        }
        return 0;
    }
}
