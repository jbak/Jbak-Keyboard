package com.jbak.JbakKeyboard;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.jbak.JbakKeyboard.JbKbd.LatinKey;

import android.animation.TimeInterpolator;
import android.os.Handler;
import android.os.Message;

public class KeyCustomRepeat
{
    public static final int MSG_KEY_REPEAT = 1;
    public static final int NAN = -20000;
    public int m_repeat = 0;
    public int m_repeatCode = NAN;
    public long m_repeatTime = 0;
    int arLongPress[] = new int[20];
    Handler m_handler;
    public KeyCustomRepeat()
    {
        fillLongPress(NAN);
        m_handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) 
            {
                if(st.kv()!=null)
                {
                    int code = msg.what;
                    int index = findLongPress(code);
                    if(index>-1)
                    {
                        arLongPress[index]=NAN;
                        st.kv().processLongPress(st.curKbd().getKeyByCode(code));
                    }
                }
            }
        };
    }
    void setRepeat(int repeat)
    {
        m_repeat = repeat;
    }
    boolean onPress(int code)
    {
        if(m_repeat==0||JbKbdView.inst==null)
            return false;
        LatinKey lk = st.curKbd().getKeyByCode(code);
        if(lk.repeatable)
        {
            m_repeatCode = code;
            m_repeatTime = 0;
        }
        else
        {
            addLongPress(code);
        }
        return true;
    }
    boolean onKey(int code)
    {
        if(m_repeat==0)
            return false;
        if(code!=m_repeatCode)
            return true;
        long tm = System.currentTimeMillis();
        if(tm-m_repeatTime>=m_repeat)
        {
            m_repeatTime = tm;
            return false;
        }
        return true;
    }
    boolean onRelease(int code)
    {
        int index = findLongPress(code);
        if(index>-1)
        {
            m_handler.removeMessages(code);
            arLongPress[index]=NAN;
            ServiceJbKbd.inst.processKey(code);
        }
        return true;
    }
    void fillLongPress(int val)
    {
        for(int i=0;i<arLongPress.length;i++)
            arLongPress[i]=val;
    }
    int findLongPress(int val)
    {
        int pos = 0;
        for(int v:arLongPress)
        {
            if(v==val)
                return pos;
            ++pos;
        }
        return -1;
    }
    void addLongPress(int val)
    {
        m_handler.sendMessageDelayed(m_handler.obtainMessage(val), m_repeat);
        for(int i=0;i<arLongPress.length;i++)
        {
            if(arLongPress[i]==NAN)
            {
                arLongPress[i]=val;
                return;
            }
        }
    }
    void resetLongPress()
    {
        m_handler.removeMessages(MSG_KEY_REPEAT);
        fillLongPress(NAN);
    }
}
