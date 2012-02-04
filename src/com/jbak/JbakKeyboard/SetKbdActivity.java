package com.jbak.JbakKeyboard;

import java.util.Vector;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jbak.JbakKeyboard.IKeyboard.KbdDesign;
import com.jbak.JbakKeyboard.IKeyboard.Keybrd;
import com.jbak.ctrl.IntEditor;
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
    int m_curSkin;
/** Текущий тип экрана, для которого выбирается клава. 0- оба типа, 1 - портрет, 2 - ландшафт*/    
    int m_screenType;
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
            String path = st.pref().getString(st.PREF_KEY_KBD_SKIN_PATH, st.ZERO_STRING);
            int pos = 0;
            for(KbdDesign kd:st.arDesign)
            {
                if(st.getSkinPath(kd).equals(path))
                {
                    m_curKbd = pos;
                    break;
                }
                ++pos;
            }
        	m_curSkin = m_curKbd;
            m_MainView.findViewById(R.id.set_height).setVisibility(View.GONE);
            m_MainView.findViewById(R.id.select_kbd).setVisibility(View.VISIBLE);
            m_MainView.findViewById(R.id.screen_type).setVisibility(View.GONE);
            String name = st.arDesign[m_curKbd].getName(this);
            ((TextView)m_MainView.findViewById(R.id.keyboard_name)).setText(name);
            m_MainView.findViewById(R.id.save).setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    m_curSkin = m_curKbd;
                    Toast.makeText(inst, R.string.settings_saved, 700).show();
                    finish();
                }
            });
            m_kbd.reload();
        }
        else if(m_curAction==st.SET_SELECT_KEYBOARD)
        {
            m_MainView.findViewById(R.id.set_height).setVisibility(View.GONE);
            m_MainView.findViewById(R.id.select_kbd).setVisibility(View.VISIBLE);
            m_MainView.findViewById(R.id.save).setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    saveKeyboard();
                    Toast.makeText(inst, R.string.settings_saved, 700).show();
                }
            });
            m_MainView.findViewById(R.id.screen_type).setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                   showScreenTypes();
                }
            });
                    
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
            final IntEditor sb = (IntEditor)m_MainView.findViewById(R.id.key_height);
            sb.setSteps(new int[]{2,4,8});
            String p = m_curAction==st.SET_KEY_HEIGHT_PORTRAIT?st.PREF_KEY_HEIGHT_PORTRAIT:st.PREF_KEY_HEIGHT_LANDSCAPE;
            sb.setMinAndMax(20, 150);
            int defHeight = st.getDefaultKeyHeight(inst);
            int val = st.pref().getInt(p,defHeight);
            sb.setOnChangeValue(new IntEditor.OnChangeValue()
            {
                @Override
                public void onChangeIntValue(IntEditor edit)
                {
                    changeKeyHeight(edit.getValue());
                }
            });
            sb.setValue(val);
            ((Button)m_MainView.findViewById(R.id.default_size)).setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    sb.setValue((int) v.getContext().getResources().getDimension(R.dimen.def_key_height));
                }
            });
        }
        setContentView(m_MainView);
        super.onCreate(savedInstanceState);
    }
    void showScreenTypes()
    {
        st.UniObserver obs = new st.UniObserver()
        {
            @Override
            public int OnObserver(Object param1, Object param2)
            {
                setScreenType(((Integer)param1).intValue());
                return 0;
            }
        };
        ArrayAdapter<String>adapt = new ArrayAdapter<String>(inst, android.R.layout.select_dialog_item, getResources().getStringArray(R.array.screen_type_vars));
        Dlg.CustomMenu(inst, adapt, null, obs);
    }
    void setScreenType(int sel)
    {
        m_screenType = sel;
        ((TextView)m_MainView.findViewById(R.id.screen_type))
        .setText(getResources().getStringArray(R.array.screen_type_vars)[sel]);
    }
    void changeKeyHeight(int height)
    {
        String pname = m_curAction==st.SET_KEY_HEIGHT_PORTRAIT?st.PREF_KEY_HEIGHT_PORTRAIT:st.PREF_KEY_HEIGHT_LANDSCAPE;
        st.pref().edit().putInt(pname, height).commit();
        m_kbd.m_KeyHeight = height;
        m_kbd.reload();
    }
    void saveKeyboard()
    {
        if(m_curAction==st.SET_SELECT_KEYBOARD)
        {
            String path = st.getKeybrdArrayByLang(m_LangName).elementAt(m_curKbd).path;
            if(m_screenType==0||m_screenType==1)
                st.pref().edit().putString(st.PREF_KEY_LANG_KBD_PORTRAIT+m_LangName, path).commit();
            if(m_screenType==0||m_screenType==2)
                st.pref().edit().putString(st.PREF_KEY_LANG_KBD_LANDSCAPE+m_LangName, path).commit();
            
        }
    }
    @Override
    protected void onDestroy()
    {
        if(m_curAction==st.SET_SELECT_SKIN)
        {
            st.pref(inst).edit().putString(st.PREF_KEY_KBD_SKIN_PATH, st.getSkinPath(st.arDesign[m_curSkin])).commit();
        }
        m_kbd.setOnKeyboardActionListener(null);
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
        KbdDesign kd = st.arDesign[m_curKbd];
        ((TextView)m_MainView.findViewById(R.id.keyboard_name)).setText(kd.getName(inst));
    	st.pref().edit().putString(st.PREF_KEY_KBD_SKIN_PATH,st.getSkinPath(kd)).commit();
    	m_kbd.reloadSkin();
    }
/** Устанавливает в просмотр следующую клавиатуру в массиве {@link IKeyboard#arKbd}*/   
    void changeKbd(boolean bNext)
    {
        Vector<Keybrd> ar = st.getKeybrdArrayByLang(m_LangName);
        if(m_curKbd==-1)
        {
            boolean bLandscape = st.isLandscape(this);
            SharedPreferences pref = st.pref();
            String pv = pref.getString(st.PREF_KEY_LANG_KBD_PORTRAIT+m_LangName, "");
            String lv = pref.getString(st.PREF_KEY_LANG_KBD_LANDSCAPE+m_LangName,"");
            String t = bLandscape?lv:pv;
            int pos = 0;
            for(Keybrd k:ar)
            {
                if(t.length()==0||t.equals(k.path))
                {
                   m_curKbd = pos; 
                   break;
                }
                pos++;
            }
            if(m_curKbd<0)
            {
                m_curKbd = 0;
            }
            int sel = 0;
            if(!pv.equals(lv))
                sel = bLandscape?2:1;
            setScreenType(sel);
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
            if(primaryCode==st.CMD_LANG_CHANGE&&m_curAction!=st.SET_LANGUAGES_SELECTION)
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
