/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.jbak.JbakKeyboard;

import java.lang.reflect.Field;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.jbak.CustomGraphics.CustomButtonDrawable;
import com.jbak.JbakKeyboard.EditSetActivity.EditSet;
import com.jbak.JbakKeyboard.IKeyboard.KbdDesign;
import com.jbak.JbakKeyboard.IKeyboard.Keybrd;
import com.jbak.JbakKeyboard.JbKbd.LatinKey;

public class JbKbdView extends KeyboardView {

    static final int KEYCODE_OPTIONS = -100;
    KeyDrw m_PreviewDrw = new KeyDrw();
    Drawable m_PreviewDrawable;
    static JbKbdView inst;
    /** Высота клавиш */    
    int m_KeyHeight =0;
    StateListDrawable m_KeyBackDrw;
    StateListDrawable m_KeyBackSecondDrw;
    Drawable m_drwKeyBack;
    Drawable m_drwKeyPress;
    TextPaint m_tpPreview;
    int m_KeyTextSz = 20;
    int m_LabelTextSize = 14;
    int m_PreviewTextSize;
/** Состояние - клавиши в верхнем регистре на одну букву. После ввода любого символа - сбрасывается */    
    public static final int STATE_TEMP_SHIFT = 0x0000001;
/** Состояние - включён CAPS_LOCK */    
    public static final int STATE_CAPS_LOCK = 0x0000002;
/** Состояние - вибрируем при коротком нажатии */    
    public static final int STATE_VIBRO_SHORT = 0x0000004;
/** Состояние - вибрируем при долгом нажатии */    
    public static final int STATE_VIBRO_LONG = 0x0000008;
    /** Состояние - звуки при каждом нажатии */    
    public static final int STATE_SOUNDS = 0x0000010;
    int m_state = 0;
    int m_PreviewHeight;
    KbdDesign m_curDesign = st.arDesign[st.KBD_DESIGN_STANDARD];
    public JbKbdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JbKbdView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
/** Возвращает текущую клавиатуру типа {@link JbKbd}*/    
    final JbKbd getCurKeyboard()
    {
        return (JbKbd) getKeyboard();
    }
/** Возвращает значение поля f типа int. В случае ошибки возвращает defVal */    
    static int getFieldInt(Field f,Object o,int defVal)
    {
        try{
            f.setAccessible(true);
            return f.getInt(o);
            }
            catch(Throwable e)
            {
                
            }
            return defVal;
    }
/** Инициализация. Берутся значения приватных переменных для задания размера шрифта */    
    void init()
    {
        inst = this;
        int index = st.pref().getInt(st.PREF_KEY_KBD_SKIN, st.KBD_DESIGN_STANDARD);
        if(index<0||index>=st.arDesign.length||st.arDesign[index]==null)
        {
            index = 0;
            st.pref().edit().putInt(st.PREF_KEY_KBD_SKIN, 0).commit();
        }
    	m_curDesign = st.arDesign[index];
    	
        setPreferences();
        int clr = Color.WHITE;
        Field[] af = KeyboardView.class.getDeclaredFields();
        String txtClr="mKeyTextColor";  
        String txtSz = "mKeyTextSize";  
        String labSz="mLabelTextSize";  
        String prevText="mPreviewText";
        String prevTs = "mPreviewTextSizeLarge";
        String ph = "mPreviewHeight";
        String keyBack = "mKeyBackground";
        String shadowRadius = "mShadowRadius";
        if(st.has(m_curDesign.flags, st.DF_BIG_GAP))
          KeyDrw.GAP = KeyDrw.BIG_GAP;
        else
          KeyDrw.GAP = KeyDrw.DEFAULT_GAP;
        if(m_curDesign.drawResId!=0)
            m_KeyBackDrw = (StateListDrawable)getResources().getDrawable(m_curDesign.drawResId);
        if(m_curDesign.m_keyBackground!=null)
        	m_KeyBackDrw = m_curDesign.m_keyBackground.getStateDrawable(); 
        if(m_curDesign.m_kbdFuncKeys!=null&&m_curDesign.m_kbdFuncKeys.m_keyBackground!=null)
        {
            m_KeyBackSecondDrw = m_curDesign.m_kbdFuncKeys.m_keyBackground.getStateDrawable();
            if(m_KeyBackDrw instanceof CustomButtonDrawable)
            {
                ((CustomButtonDrawable)m_KeyBackDrw).setDependentDrawable(m_KeyBackSecondDrw);
            }
        }
        if(m_curDesign.backDrawableRes!=0)
            setBackgroundResource(m_curDesign.backDrawableRes);
        if(m_curDesign.m_kbdBackground!=null)
        	setBackgroundDrawable(m_curDesign.m_kbdBackground.getStateDrawable());
        for(int i=0;i<af.length;i++)
        {
            Field f = af[i];
            if(f.getName().equals(shadowRadius))
            {
              try {
                f.setAccessible(true);
                f.setFloat(this, 0);
              } catch (Throwable e) {
              }
            }
            else if(f.getName().equals(txtClr))
            {
              try{
                  f.setAccessible(true);
                  clr = f.getInt(this);
                }
                catch(Throwable e)
                {
                    
                }
            }
            else if(f.getName().equals(txtSz))
            {
                m_KeyTextSz = getFieldInt(f, this, m_KeyTextSz);
            }
            else if(f.getName().equals(prevTs))
            {
                m_PreviewTextSize = getFieldInt(f, this, 25);
            }
            else if(f.getName().equals(keyBack))
            {
                	try
                	{
	                    f.setAccessible(true);
	                    if(m_curDesign.drawResId==0&&m_curDesign.m_keyBackground==null)
	                    {
	                        m_KeyBackDrw = ((StateListDrawable)f.get(this));
	                    }
	                    else
	                    {
	                        f.set(this, m_KeyBackDrw);
	                    }
                    }
                    catch(Throwable e)
                    {
                        m_KeyBackDrw = null;
                    }
            }
            else if(f.getName().equals(labSz))
            {
                m_LabelTextSize = getFieldInt(f, this, m_LabelTextSize);
            }
            else if(f.getName().equals(ph))
            {
                m_PreviewHeight = getFieldInt(f, this, 80);
            }
            else if(f.getName().equals(prevText))
            {
                try{
                f.setAccessible(true);
                m_tpPreview = ((TextView)f.get(this)).getPaint();
                }
                catch(Throwable e)
                {
                    m_tpPreview = null;
                }
            }
        }
        if(m_KeyBackDrw!=null)
        {
            // Дёргаем фон ненажатой кнопки
            m_drwKeyBack = m_KeyBackDrw.getCurrent();
            int stat[] = m_KeyBackDrw.getState();
            // Дёргаем фон нажатой кнопки
            boolean bSet = m_KeyBackDrw.setState(PRESSED_ENABLED_STATE_SET);
            m_drwKeyPress = m_KeyBackDrw.getCurrent();
            // Возвращаем всё на место
            m_KeyBackDrw.setState(stat);
        }
        st.paint().setDefault(m_KeyTextSz, m_LabelTextSize, m_curDesign, clr);
        st.paint().createFromSettings();
        if(m_tpPreview==null)
        {
            m_tpPreview = new TextPaint(st.paint().main);
        }
        m_tpPreview.setTextSize(m_PreviewTextSize);
    }
    boolean processLongPress(Key key)
    {
        if(ServiceJbKbd.inst!=null)
            ServiceJbKbd.inst.onLongPress(key);
        if(key.codes[0] == Keyboard.KEYCODE_MODE_CHANGE)
        {
            ServiceJbKbd.inst.onOptions();
            return true;
        }
        else if(key.codes[0] == 10)
        {
            st.setSmilesKeyboard();
            invalidateAllKeys();
            return true;
        }
        else if(key.codes[0] == Keyboard.KEYCODE_SHIFT)
        {
            st.setTextEditKeyboard();
            return true;
        }
        else 
        {
            LatinKey lk = (LatinKey)key;
            if(lk.m_kd!=null&&lk.m_kd.txtSmall!=null)
            {
                int cmd = st.getCmdByLabel(lk.m_kd.txtSmall);
                if(cmd!=0)
                    st.kbdCommand(cmd);
                else if(lk.m_kd.txtSmall.equals("tab"))
                {
                    getOnKeyboardActionListener().onText("\t");
                }
                else
                    getOnKeyboardActionListener().onText(lk.m_kd.txtSmall);
                return true;
            }
        }
        return false;
    }
    @Override
    protected boolean onLongPress(Key key) {
        if(processLongPress(key))
            return true;
        return super.onLongPress(key);
    }
    void handleShift()
    {
        int rid = getCurKeyboard().resId;
        if(st.kbdForId(rid)!=null)
        {
            String s = st.pref().getString(st.PREF_KEY_SHIFT_STATE, "0");
            int v = Integer.decode(s);
            if(v>0)
            {
                if(isUpperCase())
                {
                    m_state = st.rem(m_state, STATE_CAPS_LOCK);
                    m_state = st.rem(m_state, STATE_TEMP_SHIFT);
                }
                else
                {
                    m_state|=v==1?STATE_TEMP_SHIFT:STATE_CAPS_LOCK;
                }
                if(v==1)invalidateAllKeys();
            }
            else
            {
                if(st.has(m_state, STATE_TEMP_SHIFT))
                {
                    m_state = st.rem(m_state, STATE_TEMP_SHIFT);
                    m_state|=STATE_CAPS_LOCK;
                }
                else if(st.has(m_state, STATE_CAPS_LOCK))
                {
                    m_state = st.rem(m_state, STATE_CAPS_LOCK);
                }
                else
                {
                    JbKbdView.inst.m_state|=JbKbdView.STATE_TEMP_SHIFT;
                    invalidateAllKeys();
                }
            }
            setShifted(st.has(m_state,STATE_CAPS_LOCK));
//            invalidateAllKeys();
        }
        else
        {
            st.setSymbolKeyboard(rid == R.xml.symbols);
        }
    }
    boolean isUpperCase()
    {
        boolean bCaps = st.has(m_state, STATE_CAPS_LOCK);
        boolean bts = st.has(m_state, STATE_TEMP_SHIFT);
        if(bCaps&&bts)
            return false;
        return bCaps||bts;
    }
    EditSet m_esMainFont;
    EditSet m_esSecondFont;
    EditSet m_esLabelFont;
/** Выставляет настройки клавиатуры из {@link SharedPreferences}*/  
    void setPreferences()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(pref==null)
            return;
        m_state = 0;
        if(pref.getBoolean(st.PREF_KEY_VIBRO_SHORT_KEY, false))
            m_state|=STATE_VIBRO_SHORT;
        if(pref.getBoolean(st.PREF_KEY_VIBRO_LONG_KEY, false))
            m_state|=STATE_VIBRO_LONG;
        if(pref.getBoolean(st.PREF_KEY_SOUND, false))
            m_state|=STATE_SOUNDS;
        boolean bp = pref.getBoolean(st.PREF_KEY_PREVIEW, true);
        setPreviewEnabled(bp);
        WindowManager wm = (WindowManager)st.c().getSystemService(Service.WINDOW_SERVICE);
        boolean bPortrait = true;
        boolean bSet = false;
        if(SetKbdActivity.inst!=null)
        {
            if(SetKbdActivity.inst.m_curAction==st.SET_KEY_HEIGHT_PORTRAIT)
            {
                bPortrait = true;
                bSet = true;
            }
            else if(SetKbdActivity.inst.m_curAction==st.SET_KEY_HEIGHT_LANDSCAPE)
            {
                bPortrait = false;
                bSet = true;
            }
                
        }
        if(!bSet&&wm.getDefaultDisplay().getHeight()<wm.getDefaultDisplay().getWidth())
            bPortrait = false;
        m_KeyHeight = pref.getInt(bPortrait?st.PREF_KEY_HEIGHT_PORTRAIT:st.PREF_KEY_HEIGHT_LANDSCAPE, 0);
    }
    @Override
    public void setKeyboard(Keyboard keyboard) 
    {
        m_state = st.rem(m_state, STATE_CAPS_LOCK);
        m_state = st.rem(m_state, STATE_TEMP_SHIFT);
        super.setKeyboard(keyboard);
        if(getOnKeyboardActionListener() instanceof ServiceJbKbd)
            ServiceJbKbd.inst.onChangeKeyboard();
    }
    @Override
    public void invalidateAllKeys() 
    {
        super.invalidateAllKeys();
    };
    public void handleLangChange()
    {
        String ls[]=st.getLangsArray(st.c());
        int f = st.searchStr(st.getCurLang(), ls);
        String newLang = st.defKbd().lang.name;
        if(f==ls.length-1)
            newLang = ls[0];
        else if(f<ls.length-1)
            newLang = ls[f+1];
        Keybrd k = st.kbdForLangName(newLang);
        setKeyboard(new JbKbd(getContext(), k.resId));
        st.saveCurLang();
        invalidateAllKeys();
    }
    void reload()
    {
    	init();
    	setKeyboard(new JbKbd(getContext(),st.getCurQwertyRes()));
    }
    void onKeyPress(int primaryCode)
    {
        if(isPreviewEnabled())
        {
           LatinKey key = getCurKeyboard().getKeyByCode(primaryCode);
           if(key==null)
               return;
           m_PreviewDrw.set(key,true);
           key.iconPreview = m_PreviewDrw.getDrawable();
        }
    }
}
