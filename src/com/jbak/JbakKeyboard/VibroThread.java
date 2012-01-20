package com.jbak.JbakKeyboard;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;

public class VibroThread
{
    Vibrator m_vibro;
    int m_shortVibro = 30;
    int m_longVibro = 10;
    public VibroThread(Context c)
    {
        m_vibro = (Vibrator) c.getSystemService(Service.VIBRATOR_SERVICE);
        readSettings();
    }
    public void readSettings()
    {
        SharedPreferences p = st.pref();
        m_shortVibro = Integer.decode(p.getString(st.PREF_KEY_VIBRO_SHORT_DURATION, JbKbdPreference.DEF_SHORT_VIBRO));
        m_longVibro = Integer.decode(p.getString(st.PREF_KEY_VIBRO_LONG_DURATION, JbKbdPreference.DEF_LONG_VIBRO));
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
            m_vibro.vibrate(m_shortVibro);
        }
    };
    Runnable m_runLong = new Runnable()
    {
        @Override
        public void run()
        {
            m_vibro.vibrate(m_longVibro);
        }
    };
}
