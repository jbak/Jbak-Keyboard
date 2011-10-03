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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.jbak.JbakKeyboard.JbKbd.LatinKey;

public class JbKbdView extends KeyboardView {

    static final int KEYCODE_OPTIONS = -100;
    static JbKbdView inst;
    public JbKbdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public JbKbdView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
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
    void init(AttributeSet attrs)
    {
    	setPreferences();
    	inst = this;
    	int clr = 12345;
    	Field[] af = KeyboardView.class.getDeclaredFields();
    	String txtClr="mKeyTextColor";	
    	String txtSz = "mKeyTextSize";	
    	String labSz="mLabelTextSize";	
    	String prevText="mPreviewText";
    	String prevTs = "mPreviewTextSizeLarge";
    	String ph = "mPreviewHeight";
    	String keyBack = "mKeyBackground";
    	for(int i=0;i<af.length;i++)
    	{
    		Field f = af[i];
    		if(f.getName().equals(txtClr))
    		{
    			clr = getFieldInt(f, this, clr);
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
    			try{
        			f.setAccessible(true);
        			m_KeyBackDrw = ((StateListDrawable)f.get(this));
//	        			m_KeyBackDrw = (StateListDrawable)getContext().getResources().getDrawable(R.drawable.keydrw);
//	        			f.set(this, m_KeyBackDrw);
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
    	
    	m_tpMainKey = new TextPaint();
    	m_tpMainKey.setAntiAlias(true);
    	m_tpMainKey.setDither(true);
    	m_tpMainKey.setColor(clr);
//    	m_tpMainKey.setTextAlign(Align.LEFT);
    	m_tpMainKey.setTextSize(m_KeyTextSz); //sz=22
    	m_tpSmallKey = new TextPaint(m_tpMainKey);
    	m_tpSmallKey.setTextSize(m_KeyTextSz/2);
    	m_tpLabel = new TextPaint(m_tpMainKey);
    	m_tpLabel.setTextSize(m_LabelTextSize);
    	m_tpLabel.setTypeface(Typeface.create(m_tpMainKey.getTypeface(), Typeface.BOLD));
    	if(m_tpPreview==null)
    	{
    		m_tpPreview = new TextPaint(m_tpMainKey);
    	}
    	m_tpPreview.setTextSize(m_PreviewTextSize);

    }

	@Override
    protected boolean onLongPress(Key key) {

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
            return super.onLongPress(key);
        }
    }
	void handleShift()
	{
		int rid = getCurKeyboard().resId;
		if(st.langForId(rid)!=null)
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
	    	}
			setShifted(st.has(m_state,STATE_CAPS_LOCK));
	    	invalidateAllKeys();
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
/** Выставляет настройки клавиатуры из {@link SharedPreferences}*/	
	void setPreferences()
	{
    	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
    	if(pref!=null)
    	{
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
	}
	
/** Высота клавиш */	
	int m_KeyHeight =0;
    Paint m_tpMainKey;
    Paint m_tpSmallKey;
    Paint m_tpLabel;
    StateListDrawable m_KeyBackDrw;
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
    int m_state = 0;
    int m_PreviewHeight;
}
