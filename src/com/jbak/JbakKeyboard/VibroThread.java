package com.jbak.JbakKeyboard;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Vibrator;
import android.provider.Settings;

public class VibroThread extends ContentObserver
{
    Vibrator m_vibro;
    int m_shortVibro = 30;
    int m_longVibro = 10;
    boolean m_bLongVibro = false;
    int m_shortType;
    boolean m_bSilent;
    Context m_c;
    public VibroThread(Context c)
    {
        super(null);
        m_c = c;
        m_c.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.MODE_RINGER), false, this);
        m_vibro = (Vibrator) c.getSystemService(Service.VIBRATOR_SERVICE);
        readSettings();
    }
    void destroy()
    {
        m_c.getContentResolver().unregisterContentObserver(this);
    }
    @Override
    public void onChange(boolean selfChange) 
    {
        m_bSilent = isSilent();
    };
    public void readSettings()
    {
        SharedPreferences p = st.pref();
        m_shortType = Integer.decode(p.getString(st.PREF_KEY_VIBRO_SHORT_TYPE, st.ZERO_STRING));
        m_bLongVibro = p.getBoolean(st.PREF_KEY_VIBRO_LONG_KEY, false);
        m_shortVibro = Integer.decode(p.getString(st.PREF_KEY_VIBRO_SHORT_DURATION, JbKbdPreference.DEF_SHORT_VIBRO));
        m_longVibro = Integer.decode(p.getString(st.PREF_KEY_VIBRO_LONG_DURATION, JbKbdPreference.DEF_LONG_VIBRO));
        m_bSilent = isSilent();
    }
    public void vibro(boolean bLong,boolean bPress)
    {
        if(m_bSilent)
            return;
        if(!bLong)
        {
            if(!bPress&&m_shortType!=1||bPress&&m_shortType!=2)
                return;
        }
        new Thread(bLong?m_runLong:m_runShort).run();
    }
    final boolean isSilent()
    {
        int set = Settings.System.getInt(m_c.getContentResolver(),Settings.System.MODE_RINGER,-1 );
        return set==0;
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
