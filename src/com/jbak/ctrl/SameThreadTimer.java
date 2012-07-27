package com.jbak.ctrl;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

/** Таймер, который запускается всегда в том же потоке, в котором он был создан*/
public abstract class SameThreadTimer
{
    Timer m_timer;
    public int m_delay;
    public int m_period;
    Handler m_h = new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg) 
        {
            callTimer();
        };
    };
    TimerTask m_tt = new TimerTask()
    {
        @Override
        public void run()
        {
            m_h.sendEmptyMessage(0);
        }
    };
    final void callTimer()
    {
        onTimer(this);
    }
    public SameThreadTimer(int delay,int period)
    {
        m_delay = delay;
        m_period = period;
        m_timer = new Timer();
    }
    public void start()
    {
        if(m_period==0)
            m_timer.schedule(m_tt, m_delay);
        else
            m_timer.schedule(m_tt, m_delay, m_period);
    }
    public void cancel()
    {
        if(m_timer!=null)
        {
            m_timer.cancel();
            m_timer = null;
        }
    }
    public abstract void onTimer(SameThreadTimer timer);
}
