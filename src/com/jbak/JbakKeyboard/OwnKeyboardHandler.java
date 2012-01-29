package com.jbak.JbakKeyboard;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.SharedPreferences;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class OwnKeyboardHandler extends Handler
{
    int repeatInterval = 0;
    int deltaLongPress = 0;
    int deltaRepeatStart = 0;
    public static final int MSG_SHOW_PREVIEW = 1;
    public static final int MSG_REMOVE_PREVIEW = 2;
    public static final int MSG_REPEAT = 3;
    public static final int MSG_LONGPRESS = 4;
    Handler m_existHandler;
    TextView m_PreviewText;
    Method m_showKey;
    Method m_repeatKey;
    Method m_openPopupIfRequired;
    KeyboardView m_kv;
    public boolean m_bSuccessInit;
    public static OwnKeyboardHandler inst;
    public OwnKeyboardHandler(Handler exist,KeyboardView kv)
    {
        super();
        inst = this;
        m_kv = kv;
        m_existHandler = exist;
        m_bSuccessInit = init();
        loadFromSettings();
    }
    void loadFromSettings()
    {
        SharedPreferences p = st.pref(m_kv.getContext());
        int dl = p.getInt(st.PREF_KEY_LONG_PRESS_INTERVAL, 500);
        deltaLongPress = dl>=500?dl-500:0;
        dl = p.getInt(st.PREF_KEY_REPEAT_FIRST_INTERVAL, 400);
        deltaRepeatStart = dl>=400?dl-400:0;
        repeatInterval =  p.getInt(st.PREF_KEY_REPEAT_NEXT_INTERVAL, 50);
    }
    boolean init()
    {
        try{
            m_showKey = KeyboardView.class.getDeclaredMethod("showKey", int.class);
            m_repeatKey = KeyboardView.class.getDeclaredMethod("repeatKey");
            m_openPopupIfRequired = KeyboardView.class.getDeclaredMethod("openPopupIfRequired",MotionEvent.class);
            m_openPopupIfRequired.setAccessible(true);
            m_repeatKey.setAccessible(true);
            m_showKey.setAccessible(true);
            Field f = KeyboardView.class.getDeclaredField("mPreviewText");
            f.setAccessible(true);
            m_PreviewText = (TextView) f.get(m_kv);
            return true;
        }
        catch (Throwable e) {
        }
        return false;
    }
    void invokeShowKey(int key)
    {
        try{
            if(m_showKey!=null)
            {
                m_showKey.invoke(m_kv, key);
            }
        }
        catch(Throwable e)
        {
            st.logEx(e);
        }
    }
    @Override
    public void handleMessage(Message msg)
    {
        try{
            switch (msg.what) 
            {
                case MSG_SHOW_PREVIEW:
                    invokeShowKey(msg.arg1);
                    break;
                case MSG_REMOVE_PREVIEW:
                    if(m_PreviewText!=null)
                        m_PreviewText.setVisibility(View.INVISIBLE);
                    break;
                case MSG_REPEAT:
                    {
                        if(m_repeatKey!=null)
                        {
                            try{
                            m_repeatKey.invoke(m_kv);
                            }
                            catch (Throwable e) {
                                st.logEx(e);
                            }
                        }
                        Message repeat = Message.obtain(this, MSG_REPEAT);
                        sendMessageDelayed(repeat, repeatInterval);                        
                    }
                    break;
                case MSG_LONGPRESS:
                    if(deltaLongPress>0&&msg.arg1==0)
                    {
                        sendMessageDelayed(obtainMessage(MSG_LONGPRESS,1,1, msg.obj),deltaLongPress);
                        return;
                    }
                    if(m_openPopupIfRequired!=null)
                    {
                        m_openPopupIfRequired.invoke(m_kv, (MotionEvent) msg.obj);
                    }
                    break;
            }
        }
        catch (Throwable e) {
            st.logEx(e);
        }
    }
        
}
