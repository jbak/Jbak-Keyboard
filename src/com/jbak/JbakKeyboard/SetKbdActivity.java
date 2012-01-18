package com.jbak.JbakKeyboard;

import java.util.Vector;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.jbak.JbakKeyboard.IKeyboard.Keybrd;
/** Класс для настроек различных значений клавиатуры, требующих просмотра qwerty-слоя */
public class SetKbdActivity extends Activity
{
    /** Текущий экземпляр класса */
    static SetKbdActivity inst;
    int m_curAction;
    String m_LangName;
    int m_curKbd=-1;
    View m_MainView;
    JbKbdView m_kbd;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        inst = this;
        m_curAction = getIntent().getIntExtra(st.SET_INTENT_ACTION, st.SET_KEY_HEIGHT_PORTRAIT);
        m_MainView = getLayoutInflater().inflate(R.layout.kbd_set, null);
        m_MainView.setBackgroundDrawable(st.getBack());
        SharedPreferences pref = st.pref();
        m_kbd = (JbKbdView)m_MainView.findViewById(R.id.keyboard);
        m_MainView.findViewById(R.id.next).setOnClickListener(m_NextPrevListener);
        m_MainView.findViewById(R.id.prew).setOnClickListener(m_NextPrevListener);
        if(m_curAction==st.SET_KEY_HEIGHT_LANDSCAPE)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setTitle(R.string.set_key_height_landscape);
            st.setQwertyKeyboard(true);
        }
        else if(m_curAction==st.SET_KEY_HEIGHT_PORTRAIT)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setTitle(R.string.set_key_height_portrait);
            st.setQwertyKeyboard(true);
        }
        else if(m_curAction==st.SET_SELECT_SKIN)
        {
        	m_curKbd= st.pref().getInt(st.PREF_KEY_KBD_SKIN, 0);
            m_MainView.findViewById(R.id.key_height).setVisibility(View.GONE);
            m_MainView.findViewById(R.id.select_kbd).setVisibility(View.VISIBLE);
            m_MainView.findViewById(R.id.screen_type).setVisibility(View.GONE);
            String name = st.arDesign[m_curKbd].getName(this);
            if(name==null)
                name = getString(st.arDesign[m_curKbd].nameResId);
            ((TextView)m_MainView.findViewById(R.id.keyboard_name)).setText(name);
            m_kbd.reload();
        }
        else if(m_curAction==st.SET_SELECT_KEYBOARD)
        {
            m_MainView.findViewById(R.id.key_height).setVisibility(View.GONE);
            m_MainView.findViewById(R.id.select_kbd).setVisibility(View.VISIBLE);
            setTitle(R.string.set_select_keyboard);
            m_LangName = getIntent().getStringExtra(st.SET_INTENT_LANG_NAME);
            m_curKbd = -1;
            changeKbd(true);
        }
        int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().addFlags(flags);
        m_kbd.setOnKeyboardActionListener(m_kbdListener);
        if(m_curAction==st.SET_KEY_HEIGHT_PORTRAIT||m_curAction==st.SET_KEY_HEIGHT_LANDSCAPE)
        {
            SeekBar sb = (SeekBar)m_MainView.findViewById(R.id.key_height);
            sb.setProgress(m_kbd.getCurKeyboard().getHeightKey());
            sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
            {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                }
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser)
                {
                    if(fromUser)
                        changeKeyHeight(progress);
                }
            });
        }
        setContentView(m_MainView);
        super.onCreate(savedInstanceState);
    }
    void changeKeyHeight(int height)
    {
        String pname = m_curAction==st.SET_KEY_HEIGHT_PORTRAIT?st.PREF_KEY_HEIGHT_PORTRAIT:st.PREF_KEY_HEIGHT_LANDSCAPE;
        st.pref().edit().putInt(pname, height).commit();
        m_kbd.m_KeyHeight = height;
        m_kbd.reload();
    }
    @Override
    public void onBackPressed()
    {
        if(JbKbdView.inst!=null)
            JbKbdView.inst = null;
        inst = null;
        super.onBackPressed();
    };
    @Override
    protected void onDestroy()
    {
        if(m_curAction==st.SET_SELECT_KEYBOARD)
        {
            
            int pos = ((Spinner)m_MainView.findViewById(R.id.screen_type)).getSelectedItemPosition();
            if(pos==0||pos==1)
                st.pref().edit().putInt(st.PREF_KEY_LANG_KBD_PORTRAIT+m_LangName, m_curKbd).commit();
            if(pos==0||pos==2)
                st.pref().edit().putInt(st.PREF_KEY_LANG_KBD_LANDSCAPE+m_LangName, m_curKbd).commit();
        }
        super.onDestroy();
    }
/** */
    void changeSkin(boolean bNext)
    {
    	if(m_curKbd<0)
    	{
    		m_curKbd = 0;
    	}
    	else if(bNext)
    	{
    	    
    		m_curKbd++;
    		if(m_curKbd>=st.arDesign.length)
    			m_curKbd = 0;
    	}
    	else
    	{
    		--m_curKbd;
    		if(m_curKbd<0)
    		{
    			m_curKbd = st.arDesign.length-1;
    		}
    	}
    	setCurSkin();
    		
    }
    void setCurSkin()
    {
    	st.pref().edit().putInt(st.PREF_KEY_KBD_SKIN, m_curKbd).commit();
    	finish();
    	startActivity(getIntent());
    }
/** Устанавливает в просмотр следующую клавиатуру в массиве {@link IKeyboard#arKbd}*/   
    void changeKbd(boolean bNext)
    {
        Vector<Keybrd> ar = st.getKeybrdArrayByLang(m_LangName);
        if(m_curKbd==-1)
        {
            boolean bLandscape = st.isLandscape(this);
            SharedPreferences pref = st.pref();
            int pv = pref.getInt(st.PREF_KEY_LANG_KBD_PORTRAIT+m_LangName, 0);
            int lv = pref.getInt(st.PREF_KEY_LANG_KBD_LANDSCAPE+m_LangName, 0);
            m_curKbd = bLandscape?lv:pv;
            if(m_curKbd>=ar.size())
                m_curKbd = 0;
            int sel = 0;
            if(pv!=lv)
                sel = bLandscape?2:1;
            ((Spinner)m_MainView.findViewById(R.id.screen_type)).setSelection(sel);
        }
        else
        {
            if(bNext)
            {
            ++m_curKbd;
            if(m_curKbd>=ar.size())
                m_curKbd = 0;
            }
            else
            {
                if(m_curKbd==0)m_curKbd=ar.size()-1;
                else --m_curKbd;
            }
        }
        setKeyboard(ar.elementAt(m_curKbd));
        
    }
/** Устанавливает клавиатуру kbd текущей в просмотре */ 
    void setKeyboard(Keybrd kbd)
    {
        m_kbd.setKeyboard(st.loadKeyboard(kbd));
        ((TextView)m_MainView.findViewById(R.id.keyboard_name)).setText(kbd.getName(this));
    }
    OnKeyboardActionListener m_kbdListener = new OnKeyboardActionListener()
    {
        @Override
        public void swipeUp()
        {
        }
        @Override
        public void swipeRight()
        {
        }
        @Override
        public void swipeLeft()
        {
        }
        @Override
        public void swipeDown()
        {
        }
        @Override
        public void onText(CharSequence text)
        {
        }
        @Override
        public void onRelease(int primaryCode)
        {
        }
        @Override
        public void onPress(int primaryCode)
        {
            st.kv().onKeyPress(primaryCode);
        }
        @Override
        public void onKey(int primaryCode, int[] keyCodes)
        {
            if(st.kv()==null)
                return;
            if(primaryCode==Keyboard.KEYCODE_SHIFT)
            {
                st.kv().handleShift();
            }
            if(primaryCode==IKeyboard.KEYCODE_LANG_CHANGE&&m_curAction!=st.SET_LANGUAGES_SELECTION)
            {
                st.kv().handleLangChange();
            }
        }
    };
    View.OnClickListener m_NextPrevListener = new View.OnClickListener()
    {

		@Override
		public void onClick(View v)
		{
			boolean bNext = v.getId()==R.id.next;
			switch(m_curAction)
			{
				case st.SET_SELECT_KEYBOARD:
				    changeKbd(bNext);
				    break;
				case st.SET_SELECT_SKIN:
					changeSkin(bNext);
					break;
			}
		}
    };
}
