package com.jbak.JbakKeyboard;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

public class VibroThread
{
    Vibrator m_vibro;
    public VibroThread(Context c)
    {
        m_vibro = (Vibrator) c.getSystemService(Service.VIBRATOR_SERVICE);
    }
    public void vibro(boolean bLong)
    {
        new Thread(bLong?m_runLong:m_runShort).run();
    }
    Runnable m_runShort = new Runnable()
    {
        @Override
        public void run()
        {
            m_vibro.vibrate(30);
        }
    };
    Runnable m_runLong = new Runnable()
    {
        @Override
        public void run()
        {
            m_vibro.vibrate(10);
        }
    };
}
