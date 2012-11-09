package com.jbak.JbakKeyboard;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.ClipboardManager;
/** Сервис для забора значений по таймеру */
public class ClipbrdService extends Service
{
    static ClipbrdService inst;
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    @Override
    public void onCreate()
    {
        inst = this;
        m_cm = (ClipboardManager)getSystemService(Service.CLIPBOARD_SERVICE);
        super.onCreate();
        IntentFilter filt = new IntentFilter();
        filt.addAction(Intent.ACTION_SCREEN_ON);
        filt.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(m_recv, filt);
        startTimer();
    }
    @Override
    public void onDestroy()
    {
        inst = null;
        unregisterReceiver(m_recv);
        super.onDestroy();
    }
    void startTimer()
    {
        m_timer = new Timer();
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                checkClipboardString();
            }
        }, CLIPBRD_INTERVAL, CLIPBRD_INTERVAL);
        
    }
    void stopTimer()
    {
        m_timer.cancel();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        
        return START_STICKY;
    }
    void checkClipboardString()
    {
        if(!m_cm.hasText())
        {
            return;
        }
        checkString(m_cm.getText().toString());
    }
    void checkString(String str)
    {
        try{
            if(str.equals(m_sLastClipStr))
            {
                return;
            }
            st.stor().checkClipboardString(str);
            m_sLastClipStr = str;
        }
        catch(Throwable e)
        {
            
        }
    }
    BroadcastReceiver m_recv = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String act = intent.getAction();
            if(Intent.ACTION_SCREEN_ON.equals(act))
            {
                startTimer();
            }
            if(Intent.ACTION_SCREEN_OFF.equals(act))
            {
                stopTimer();
            }
        }
    };
    String m_sLastClipStr;
/** Интервал взятия значений из буфера обмена в милисекундах */ 
    public static final int CLIPBRD_INTERVAL = 5000;
    ClipboardManager m_cm;
    Timer m_timer;
}
