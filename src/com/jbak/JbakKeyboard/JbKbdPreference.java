package com.jbak.JbakKeyboard;


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.jbak.ctrl.IntEditor;

public class JbKbdPreference extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
    public static final String DEF_SIZE_CLIPBRD = "20";
    public static final String DEF_SHORT_VIBRO = "30";
    public static final String DEF_LONG_VIBRO = "10";
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        st.upgradeSettings(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_view);
        addPreferencesFromResource(R.xml.preferences);
        setShiftState();
        SharedPreferences p = st.pref(this);
        setSummary(st.PREF_KEY_CLIPBRD_SIZE, R.string.set_key_clipbrd_size_desc, p.getString(st.PREF_KEY_CLIPBRD_SIZE,DEF_SIZE_CLIPBRD ));
        int index = Integer.decode(p.getString(st.PREF_KEY_VIBRO_SHORT_TYPE, st.ONE_STRING));
        setSummary(st.PREF_KEY_VIBRO_SHORT_TYPE, R.string.set_key_short_vibro_desc, strVal(getResources().getStringArray(R.array.vibro_short_type)[index]));
        setSummary(st.PREF_KEY_VIBRO_SHORT_DURATION, R.string.set_key_short_vibro_duration_desc, p.getString(st.PREF_KEY_VIBRO_SHORT_DURATION,DEF_SHORT_VIBRO ));
        setSummary(st.PREF_KEY_VIBRO_LONG_DURATION, R.string.set_key_long_vibro_duration_desc, p.getString(st.PREF_KEY_VIBRO_LONG_DURATION,DEF_LONG_VIBRO));
        st.pref(this).registerOnSharedPreferenceChangeListener(this);
    }
    String strVal(String src)
    {
        return "[ "+src+" ]";
    }
    @Override
    protected void onDestroy()
    {
        st.pref(this).unregisterOnSharedPreferenceChangeListener(this);
        if(JbKbdView.inst!=null)
            JbKbdView.inst.setPreferences();
        super.onDestroy();
    }
    void runSetKbd(int action)
    {
        try{
            Intent in = new Intent(Intent.ACTION_VIEW)
            .setComponent(new ComponentName(this, SetKbdActivity.class))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(st.SET_INTENT_ACTION, action);
            startActivity(in);
        }
        catch(Throwable e)
        {
            st.logEx(e);
        }

    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) 
    {
        String k = preference.getKey();
        Context c = preference.getContext();
        if("intervals".equals(k))
        {
            showIntervalsEditor();
        }
        if("set_skins".equals(k))
        {
            String err = CustomKbdDesign.loadCustomSkins();
            if(err.length()>0)
            {
               Toast.makeText(this, err, 1000).show();
            }
            runSetKbd(st.SET_SELECT_SKIN);
            return true;
        }
        if("pref_port_key_height".equals(k))
        {
            runSetKbd(st.SET_KEY_HEIGHT_PORTRAIT);
            return true;
        }
        if("pref_land_key_height".equals(k))
        {
            runSetKbd(st.SET_KEY_HEIGHT_LANDSCAPE);
            return true;
        }
        if("set_key_main_font".equals(k))
        {
            c.startActivity(
                    new Intent(c,EditSetActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(EditSetActivity.EXTRA_PREF_KEY, st.PREF_KEY_MAIN_FONT)
                    .putExtra(EditSetActivity.EXTRA_DEFAULT_EDIT_SET, st.paint().getDefaultMain().toString())
                );
            
        }
        if("set_key_second_font".equals(k))
         {
             c.startActivity(
                     new Intent(c,EditSetActivity.class)
                     .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                     .putExtra(EditSetActivity.EXTRA_PREF_KEY, st.PREF_KEY_SECOND_FONT)
                     .putExtra(EditSetActivity.EXTRA_DEFAULT_EDIT_SET, st.paint().getDefaultSecond().toString())
                 );
             
         }
        if("set_key_label_font".equals(k))
         {
             c.startActivity(
                     new Intent(c,EditSetActivity.class)
                     .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                     .putExtra(EditSetActivity.EXTRA_PREF_KEY, st.PREF_KEY_LABEL_FONT)
                     .putExtra(EditSetActivity.EXTRA_DEFAULT_EDIT_SET, st.paint().getDefaultLabel().toString())
                 );
             
         }
        if("pref_languages".equals(k))
        {
            st.runAct(LangSetActivity.class,c);
            return true;
        }
        if("fs_editor_set".equals(k))
        {
            getApplicationContext().startActivity(
                    new Intent(getApplicationContext(),EditSetActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(EditSetActivity.EXTRA_PREF_KEY, st.PREF_KEY_EDIT_SETTINGS)
                );
            return true;
        }
        if("about_app".equals(k))
        {
            st.runAct(AboutActivity.class,c);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    void setSummary(String prefName,int summaryStr,String value)
    {
        Preference p = getPreferenceScreen().findPreference(prefName);
        if(p!=null)
        {
            String summary;
            if(summaryStr==0)
            {
                summary = value;
            }
            else
            {
                summary = value+"\n"+getString(summaryStr);
            }
            p.setSummary(summary);
        }
    }
    void setShiftState()
    {
        int v = Integer.decode(st.pref(this).getString(st.PREF_KEY_SHIFT_STATE, "0"));
        setSummary(st.PREF_KEY_SHIFT_STATE,0,getResources().getStringArray(R.array.array_shift_vars)[v]);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if(st.PREF_KEY_SHIFT_STATE.equals(key))
            setShiftState();
        if(st.PREF_KEY_VIBRO_SHORT_TYPE.equals(key))
        {
            int index = Integer.decode(sharedPreferences.getString(st.PREF_KEY_VIBRO_SHORT_TYPE, st.ONE_STRING));
            setSummary(st.PREF_KEY_VIBRO_SHORT_TYPE, R.string.set_key_short_vibro_desc, strVal(getResources().getStringArray(R.array.vibro_short_type)[index]));
        }
        if(st.PREF_KEY_CLIPBRD_SIZE.equals(key))
        {
            if(checkIntValue(key,DEF_SIZE_CLIPBRD))
            {
                try{
                    String v = st.pref(this).getString(key, DEF_SIZE_CLIPBRD);
                    st.stor().CLIPBOARD_LIMIT = Integer.decode(v);
                    setSummary(key, R.string.set_key_clipbrd_size_desc,v);
                }
                catch (Throwable e) {
                }
            }
        }
        if(st.PREF_KEY_VIBRO_LONG_DURATION.equals(key)||st.PREF_KEY_VIBRO_SHORT_DURATION.equals(key))
        {
            String def = st.PREF_KEY_VIBRO_LONG_DURATION.equals(key)?DEF_LONG_VIBRO:DEF_SHORT_VIBRO;
            int desc = st.PREF_KEY_VIBRO_LONG_DURATION.equals(key)?R.string.set_key_long_vibro_duration_desc:R.string.set_key_short_vibro_duration_desc;
            if(checkIntValue(key,def))
            {
                try{
                    String v = st.pref(this).getString(key, def);
                    setSummary(key, desc,v);
                }
                catch (Throwable e) {
                }
            }
        }
    }
    boolean checkIntValue(String key,String defValue)
    {
        String v = st.pref(this).getString(key, "0");
        boolean bOk = true;
        for(int i = v.length()-1;i>=0;i--)
        {
            if(!Character.isDigit(v.charAt(i)))
            {
                bOk = false; 
                break;
            }
        }
        if(!bOk)
        {
            Toast.makeText(this, "Incorrect integer value!", 700).show();
            st.pref(this).edit().putString(key, defValue).commit();
        }
        return bOk;
    }
    void showIntervalsEditor()
    {
        final View v = getLayoutInflater().inflate(R.layout.edit_intervals, null);
        int max = 5000,min = 500;
        int steps[] = new int[]{50,100,100};
        final SharedPreferences p = st.pref(this);
        IntEditor ie = null;
        ie = (IntEditor)v.findViewById(R.id.long_press);
        ie.setMinAndMax(min, max);
        ie.setValue(p.getInt(st.PREF_KEY_LONG_PRESS_INTERVAL, min));
        ie.setSteps(steps);
        
        ie = (IntEditor)v.findViewById(R.id.first_repeat);
        min = 400;
        ie.setMinAndMax(min, max);
        ie.setValue(p.getInt(st.PREF_KEY_REPEAT_FIRST_INTERVAL, min));
        ie.setSteps(steps);

        ie = (IntEditor)v.findViewById(R.id.next_repeat);
        min = 50;
        ie.setMinAndMax(min, max);
        ie.setValue(p.getInt(st.PREF_KEY_REPEAT_NEXT_INTERVAL, min));
        ie.setSteps(steps);
        st.UniObserver obs = new st.UniObserver()
        {
            @Override
            int OnObserver(Object param1, Object param2)
            {
                if(((Integer)param1).intValue()==AlertDialog.BUTTON_POSITIVE)
                {
                    IntEditor ie;
                    Editor e = p.edit();
                    ie = (IntEditor)v.findViewById(R.id.long_press);
                    e.putInt(st.PREF_KEY_LONG_PRESS_INTERVAL, ie.getValue());
                    ie = (IntEditor)v.findViewById(R.id.first_repeat);
                    e.putInt(st.PREF_KEY_REPEAT_FIRST_INTERVAL, ie.getValue());
                    ie = (IntEditor)v.findViewById(R.id.next_repeat);
                    e.putInt(st.PREF_KEY_REPEAT_NEXT_INTERVAL, ie.getValue());
                    e.commit();
                    if(OwnKeyboardHandler.inst!=null)
                        OwnKeyboardHandler.inst.loadFromSettings();
                }
                return 0;
            }
        };
        Dlg.CustomDialog(this, v, getString(R.string.ok), getString(R.string.cancel), null, obs);
    }
}
