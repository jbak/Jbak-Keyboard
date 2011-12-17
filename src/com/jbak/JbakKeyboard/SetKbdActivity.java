package com.jbak.JbakKeyboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
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
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        inst = this;
        m_curAction = getIntent().getIntExtra(st.SET_INTENT_ACTION, st.SET_KEY_HEIGHT_PORTRAIT);
        m_MainView = getLayoutInflater().inflate(R.layout.set_sizes, null);
        SharedPreferences pref = st.pref();
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
            m_MainView.findViewById(R.id.key_height).setVisibility(View.INVISIBLE);
            m_MainView.findViewById(R.id.select_kbd).setVisibility(View.VISIBLE);
            ((TextView)m_MainView.findViewById(R.id.keyboard_name)).setText(st.arDesign[m_curKbd].nameResId);
            JbKbdView.inst.setKeyboard(new JbKbd(st.c(),st.getCurQwertyRes()));
        }
        else if(m_curAction==st.SET_SELECT_KEYBOARD)
        {
            m_MainView.findViewById(R.id.key_height).setVisibility(View.INVISIBLE);
            m_MainView.findViewById(R.id.select_kbd).setVisibility(View.VISIBLE);
            setTitle(R.string.set_select_keyboard);
            m_LangName = getIntent().getStringExtra(st.SET_INTENT_LANG_NAME);
            int idLang = pref.getInt(st.PREF_KEY_LANG_KBD+m_LangName, -1);
            if(idLang>=0)
                setKeyboard(st.arKbd[idLang]);
            else
                nextKbd();
        }
        int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().addFlags(flags);
        JbKbdView.inst.setOnKeyboardActionListener(m_kbdListener);
        if(m_curAction==st.SET_KEY_HEIGHT_PORTRAIT||m_curAction==st.SET_KEY_HEIGHT_LANDSCAPE)
        {
            SeekBar sb = (SeekBar)m_MainView.findViewById(R.id.key_height);
            sb.setProgress(JbKbdView.inst.getCurKeyboard().getHeightKey());
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
        JbKbdView.inst.m_KeyHeight = height;
        st.setQwertyKeyboard(true);
    }
    @Override
    protected void onDestroy()
    {
        if(m_curAction==st.SET_SELECT_KEYBOARD)
        {
            st.pref().edit().putInt(st.PREF_KEY_LANG_KBD+m_LangName, m_curKbd).commit();
        }
        JbKbdView.inst = null;
        inst = null;
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
    void nextKbd()
    {
        int f = -1;
        int pos = 0;
        int fpos = -1;
        for(Keybrd k:st.arKbd)
        {
            if(k.lang.name.equals(m_LangName))
            {
                if((m_curKbd==-1||f>=0))
                {
                    setKeyboard(k);
                    return;
                }
                else if(m_curKbd==k.kbdCode)
                    f=pos;
                if(fpos<0)
                    fpos = pos;
            }
            ++pos;
        }
        if(fpos>=0)
            setKeyboard(st.arKbd[fpos]);
    }
/** Устанавливает в просмотр предыдущую клавиатуру в массиве {@link IKeyboard#arKbd}*/  
    void prevKbd()
    {
        int f = -1;
        int pos = 0;
        int fpos = -1;
        for(int i=st.arKbd.length-1;i>=0;i--)
        {
            Keybrd k = st.arKbd[i];
            if(k.lang.name.equals(m_LangName))
            {
                if((m_curKbd==-1||f>=0))
                {
                    setKeyboard(k);
                    return;
                }
                else if(m_curKbd==k.kbdCode)
                    f=pos;
                if(fpos<0)
                    fpos = pos;
            }
            ++pos;
        }
        if(fpos>=0)
            setKeyboard(st.arKbd[fpos]);
    }
/** Устанавливает клавиатуру kbd текущей в просмотре */ 
    void setKeyboard(Keybrd kbd)
    {
        JbKbdView.inst.setKeyboard(new JbKbd(st.c(),kbd.resId));
        m_curKbd = kbd.kbdCode;
        ((TextView)m_MainView.findViewById(R.id.keyboard_name)).setText(kbd.resName);
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
					if(bNext) 
						nextKbd();
					else 
						prevKbd();
					break;
				case st.SET_SELECT_SKIN:
					changeSkin(bNext);
					break;
			}
		}
    };
}
