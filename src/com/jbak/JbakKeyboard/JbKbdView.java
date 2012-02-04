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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.jbak.JbakKeyboard.EditSetActivity.EditSet;
import com.jbak.JbakKeyboard.IKbdSettings.KbdGesture;
import com.jbak.JbakKeyboard.IKeyboard.KbdDesign;
import com.jbak.JbakKeyboard.IKeyboard.Keybrd;
import com.jbak.JbakKeyboard.JbKbd.LatinKey;
import com.jbak.JbakKeyboard.KeyboardGesture.GestureInfo;

public class JbKbdView extends KeyboardView {

    static final int KEYCODE_OPTIONS = -100;
    KeyDrw m_PreviewDrw = new KeyDrw();
    Drawable m_PreviewDrawable;
    static JbKbdView inst;
    /** Высота клавиш */    
    int m_KeyHeight =0;
    int m_KeyTextSz =0;
    StateListDrawable m_KeyBackDrw;
    Drawable m_drwKeyBack;
    Drawable m_drwKeyPress;
    TextPaint m_tpPreview;
    int m_LabelTextSize = 0;
    int m_PreviewTextSize=0;
    KbdGesture m_gestures[]=new KbdGesture[4];
/** Состояние - клавиши в верхнем регистре на одну букву. После ввода любого символа - сбрасывается */    
    public static final int STATE_TEMP_SHIFT    = 0x0000001;
/** Состояние - включён CAPS_LOCK */    
    public static final int STATE_CAPS_LOCK     = 0x0000002;
    public static final int STATE_SOUNDS        = 0x0000004;
    public static final int STATE_GESTURES      = 0x0000008;
    int m_state = 0;
    int m_PreviewHeight=0;
    KeyboardGesture m_gd;
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
            return(f.getInt(o));
        }
        catch(Throwable e)
        {
             st.logEx(e);   
        }
        return defVal;
    }
    Drawable m_defDrawable;
    Drawable m_defBackground;
    String m_designPath;
    OwnKeyboardHandler m_handler;
/** Инициализация. Берутся значения приватных переменных для задания размера шрифта */    
    void init()
    {
        inst = this;
        String path = st.pref().getString(st.PREF_KEY_KBD_SKIN_PATH, st.ZERO_STRING);
        KbdDesign d = st.getSkinByPath(path);
        path = st.getSkinPath(m_curDesign);
        if(!path.equals(m_designPath))
        {
            m_curDesign = d.getDesign();
        }
        setPreferences();
        int clr = Color.WHITE;
        if(st.has(m_curDesign.flags, st.DF_BIG_GAP))
          KeyDrw.GAP = KeyDrw.BIG_GAP;
        else
          KeyDrw.GAP = KeyDrw.DEFAULT_GAP;
        if(m_curDesign.drawResId!=0)
            m_KeyBackDrw = (StateListDrawable)getResources().getDrawable(m_curDesign.drawResId);
        if(m_curDesign.m_keyBackground!=null)
        {
        	m_KeyBackDrw = m_curDesign.m_keyBackground.getStateDrawable(); 
        	KeyDrw.GAP = m_curDesign.m_keyBackground.m_gap+2;
        }
        if(m_curDesign.backDrawableRes!=0)
            setBackgroundResource(m_curDesign.backDrawableRes);
        else if(m_curDesign.m_kbdBackground!=null)
        	setBackgroundDrawable(m_curDesign.m_kbdBackground.getStateDrawable());
        else
        {
            if(m_defBackground==null)
                m_defBackground = getBackground();
            else
                setBackgroundDrawable(m_defBackground);
        }
        Field[] af = KeyboardView.class.getDeclaredFields();
        String txtClr="mKeyTextColor";  
        String txtSz = "mKeyTextSize";  
        String labSz="mLabelTextSize";  
        String prevText="mPreviewText";
        String prevTs = "mPreviewTextSizeLarge";
        String ph = "mPreviewHeight";
        String keyBack = "mKeyBackground";
        String shadowRadius = "mShadowRadius";
        String handler = "mHandler";
        String gd = "mGestureDetector";
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
/** Пытаемся подложить собственный хэндлер */            
            else if(f.getName().equals(handler))
            {
              try {
                f.setAccessible(true);
                m_handler= new OwnKeyboardHandler((Handler)f.get(this),this);
                if(m_handler.m_bSuccessInit)
                    f.set(this,m_handler);
              } catch (Throwable e) {
                  m_handler = null;
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
            else if(f.getName().equals(txtSz)&&m_KeyTextSz==0)
            {
                m_KeyTextSz = getFieldInt(f, this, 20);
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
                    if(m_defDrawable==null)
                        m_defDrawable = (Drawable)f.get(this);
                    if(m_curDesign.drawResId==0&&m_curDesign.m_keyBackground==null)
                    {
                        f.set(this,m_defDrawable);
                        m_KeyBackDrw = (StateListDrawable) m_defDrawable;
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
            else if(f.getName().equals(labSz)&&m_LabelTextSize==0)
            {
                m_LabelTextSize = getFieldInt(f, this, 12);
            }
            else if(f.getName().equals(gd)&&st.has(m_state, STATE_GESTURES))
            {
                try{
                    m_gd = new KeyboardGesture(this);
                    f.setAccessible(true);
                    f.set(this, m_gd);
                }
                catch (Throwable e) {
                }
            }
            else if(f.getName().equals(ph)&&m_PreviewHeight==0)
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
        if(isDefaultDesign()&&m_defDrawable!=null)
        {
            // Дёргаем фон ненажатой кнопки
            try{
                StateListDrawable ds = (StateListDrawable)m_defDrawable;
                m_drwKeyBack = ds.getCurrent();
                int stat[] = m_KeyBackDrw.getState();
                // Дёргаем фон нажатой кнопки
                ds.setState(PRESSED_ENABLED_STATE_SET);
                m_drwKeyPress = ds.getCurrent();
                m_KeyBackDrw.setState(stat);
            }
            catch(Throwable e)
            {
            }
        }
        st.paint().setDefault(m_KeyTextSz, m_LabelTextSize, m_curDesign, clr);
        st.paint().createFromSettings();
        if(m_tpPreview==null)
        {
            m_tpPreview = new TextPaint(st.paint().main);
        }
        m_tpPreview.setTextSize(m_PreviewTextSize);
    }
    boolean processLongPress(LatinKey key)
    {
        if(ServiceJbKbd.inst!=null)
            ServiceJbKbd.inst.onLongPress(key);
        if(key.popupCharacters!=null)
            return false;
        boolean isService = getOnKeyboardActionListener() instanceof ServiceJbKbd;
        if(key.longCode!=0)
        {
            if(isService)
                ServiceJbKbd.inst.processKey(key.longCode);
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
            if(key.m_kd!=null&&key.m_kd.txtSmall!=null)
            {
                if(isService&&key.m_kd.txtSmall.length()==1)
                    ServiceJbKbd.inst.processKey(key.m_kd.txtSmall.charAt(0));
                else
                    getOnKeyboardActionListener().onText(key.m_kd.txtSmall);
                return true;
            }
        }
        return false;
    }
    @Override
    protected boolean onLongPress(Key key) {
        if(isPreviewEnabled()&&m_handler!=null)
        {
            int pos = 0;
            int index = -1;
            for(Key k:getCurKeyboard().getKeys())
            {
                if(k==key)
                {
                    index = pos;
                    break;
                }
                ++pos;
            }
            if(index>-1)
            {
                m_handler.removeMessages(OwnKeyboardHandler.MSG_REMOVE_PREVIEW);
                m_PreviewDrw.set(key,true);
                m_PreviewDrw.m_bLongPreview = true;
                key.iconPreview = m_PreviewDrw.getDrawable();
                m_handler.sendMessageDelayed(m_handler.obtainMessage(OwnKeyboardHandler.MSG_SHOW_PREVIEW, index, 0), 10);
                m_handler.sendMessageDelayed(m_handler.obtainMessage(OwnKeyboardHandler.MSG_REMOVE_PREVIEW), 400);
            }
        }
        if(processLongPress((LatinKey)key))
            return false;
        return super.onLongPress(key);
    }
    void setTempShift(boolean bShift,boolean bInvalidate)
    {
        m_state = st.rem(m_state, STATE_CAPS_LOCK);
        if(bShift)
            m_state|=STATE_TEMP_SHIFT;
        else
            m_state = st.rem(m_state, STATE_TEMP_SHIFT);
        if(isShifted())
            setShifted(false);
        else if(bInvalidate)
            invalidateAllKeys();
        
    }
    void handleShift()
    {
        Keybrd kbd = getCurKeyboard().kbd;
        if(st.isQwertyKeyboard(kbd))
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
            st.setSymbolKeyboard(st.LANG_SYM_KBD.equals(kbd.lang.name));
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
//        if(pref.getBoolean(st.PREF_KEY_VIBRO_SHORT_KEY, false))
//            m_state|=STATE_VIBRO_SHORT;
        if(pref.getBoolean(st.PREF_KEY_USE_GESTURES, false))
            m_state|=STATE_GESTURES;
        if(pref.getBoolean(st.PREF_KEY_SOUND, false))
            m_state|=STATE_SOUNDS;
        boolean bp = pref.getBoolean(st.PREF_KEY_PREVIEW, true);
        setPreviewEnabled(bp);
        boolean bPortrait = true;
        boolean bSet = false;
        m_gestures[GestureInfo.LEFT] = st.getGesture(st.PREF_KEY_GESTURE_LEFT, pref);
        m_gestures[GestureInfo.RIGHT] = st.getGesture(st.PREF_KEY_GESTURE_RIGHT, pref);
        m_gestures[GestureInfo.UP] = st.getGesture(st.PREF_KEY_GESTURE_UP, pref);
        m_gestures[GestureInfo.DOWN] = st.getGesture(st.PREF_KEY_GESTURE_DOWN, pref);
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
        if(!bSet&&st.isLandscape(st.c()))
            bPortrait = false;
        m_KeyHeight = pref.getInt(bPortrait?st.PREF_KEY_HEIGHT_PORTRAIT:st.PREF_KEY_HEIGHT_LANDSCAPE, (int) getResources().getDimension(R.dimen.def_key_height));
    }
    @Override
    public void onDraw(android.graphics.Canvas canvas) 
    {
        m_bStopInvalidate = false;
        super.onDraw(canvas);
    };
    @Override
    public void invalidateAllKeys() 
    {
        if(m_handler!=null)
            m_handler.removeMessages(OwnKeyboardHandler.MSG_INVALIDATE);
        if(ComMenu.inst==null)
            super.invalidateAllKeys();
    };
    @Override
    public void invalidateKey(int keyIndex)
    {
        if(m_handler!=null)
        {
            m_handler.sendMessageDelayed(m_handler.obtainMessage(OwnKeyboardHandler.MSG_INVALIDATE, keyIndex, 0), 50);
            return;
        }
        if(ComMenu.inst==null)
            super.invalidateKey(keyIndex);
    }
    public final boolean isDefaultDesign()
    {
        return m_curDesign==null||m_curDesign.m_keyBackground==null;
    }
    boolean m_bStopInvalidate = false;
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
        if(k==null)
        {
            Toast.makeText(getContext(), "No keyboards for lang "+newLang, 700);
            return;
        }
        setKeyboard(st.loadKeyboard(k));
        st.saveCurLang();
    }
    void reload()
    {
    	init();
    	setKeyboard(st.loadKeyboard(st.getCurQwertyKeybrd()));
    }
    void reloadSkin()
    {
        m_designPath = null;
        init();
        invalidateAllKeys();
    }
    void onKeyPress(int primaryCode)
    {
        if(isPreviewEnabled())
        {
           LatinKey key = getCurKeyboard().getKeyByCode(primaryCode);
           if(key==null)
               return;
           m_PreviewDrw.set(key,true);
           m_PreviewDrw.m_bLongPreview = false;
           key.iconPreview = m_PreviewDrw.getDrawable();
        }
    }
    public boolean isUserInput()
    {
        return getOnKeyboardActionListener() instanceof ServiceJbKbd;
    }
    @Override
    public boolean onTouchEvent(MotionEvent me)
    {
        if(me.getAction()==MotionEvent.ACTION_DOWN)
            Log.w(st.TAG, "down:x="+me.getX()+" y="+me.getY());
        return super.onTouchEvent(me);
    }
    public void gesture(GestureInfo gest)
    {
        if(!isUserInput())
            return;
        KbdGesture g = m_gestures[gest.dir];
        if(g.code!=0)
            st.kbdCommand(g.code);
    }
    @Override
    public boolean setShifted(boolean shifted)
    {
        boolean b = isUpperCase();
        if(shifted&&b||!shifted&&!b)
            return false;
        return super.setShifted(shifted);
    }
    public void trueInvalidateKey(int index)
    {
        super.invalidateKey(index);
    }
}
