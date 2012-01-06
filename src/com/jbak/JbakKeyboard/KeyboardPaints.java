package com.jbak.JbakKeyboard;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;

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
}
