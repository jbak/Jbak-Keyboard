package com.jbak.JbakKeyboard;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jbak.ctrl.IntEditor;
import com.jbak.words.Words;

public class JbKbdPreference extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
    public static final String DEF_SIZE_CLIPBRD = "20";
    public static final String DEF_SHORT_VIBRO = "30";
    public static final String DEF_LONG_VIBRO = "15";
    public static JbKbdPreference inst;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        inst = this;
        st.upgradeSettings(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_view);
        addPreferencesFromResource(R.xml.preferences);
        setShiftState();
        SharedPreferences p = st.pref(this);
        PreferenceScreen ps =getPreferenceScreen(); 
        Preference pr = ps.findPreference(st.PREF_KEY_SAVE);
        pr.setSummary(pr.getSummary().toString()+'\n'+getBackupPath());
        pr = getPreferenceScreen().findPreference(st.PREF_KEY_LOAD);
        pr.setSummary(pr.getSummary().toString()+'\n'+getBackupPath());
        setSummary(st.PREF_KEY_CLIPBRD_SIZE, R.string.set_key_clipbrd_size_desc, p.getString(st.PREF_KEY_CLIPBRD_SIZE,DEF_SIZE_CLIPBRD ));
        int index = Integer.decode(p.getString(st.PREF_KEY_USE_SHORT_VIBRO, st.ONE_STRING));
        
        CharSequence entries[] = st.getGestureEntries(this);
        CharSequence entValues[] = st.getGestureEntryValues(); 
        setGestureList(p, st.PREF_KEY_GESTURE_LEFT, entries, entValues);
        setGestureList(p, st.PREF_KEY_GESTURE_RIGHT, entries, entValues);
        setGestureList(p, st.PREF_KEY_GESTURE_UP, entries, entValues);
        setGestureList(p, st.PREF_KEY_GESTURE_DOWN, entries, entValues);
        setSummary(st.PREF_KEY_USE_SHORT_VIBRO, R.string.set_key_short_vibro_desc, strVal(getResources().getStringArray(R.array.vibro_short_type)[index]));

        index = Integer.decode(p.getString(st.PREF_KEY_PORTRAIT_TYPE, st.ZERO_STRING));
        setSummary(st.PREF_KEY_PORTRAIT_TYPE, R.string.set_key_portrait_input_type_desc, strVal(getResources().getStringArray(R.array.array_input_type)[index]));
        index = Integer.decode(p.getString(st.PREF_KEY_LANSCAPE_TYPE, st.ZERO_STRING));
        setSummary(st.PREF_KEY_LANSCAPE_TYPE, R.string.set_key_landscape_input_type_desc, strVal(getResources().getStringArray(R.array.array_input_type)[index]));
        st.pref(this).registerOnSharedPreferenceChangeListener(this);
    }
    void setGestureList(SharedPreferences p,final String set,CharSequence entries[],CharSequence entValues[])
    {
        ListPreference lp = (ListPreference)getPreferenceScreen().findPreference(set);
        if(lp!=null)
        {
            int index = st.getGestureIndexBySetting(p.getString(set, st.ZERO_STRING));
            if(entries==null)
            {
                lp.setSummary(strVal(st.getGestureEntries(this)[index].toString()));
                return;
            }
            lp.setEntries(entries);
            lp.setEntryValues(entValues);
            lp.setValueIndex(index);
            lp.setSummary(strVal(entries[index].toString()));
        }
    }
    final String strVal(String src)
    {
        return "[ "+src+" ]";
    }
    @Override
    protected void onDestroy()
    {
        inst = null;
        st.pref(this).unregisterOnSharedPreferenceChangeListener(this);
        if(JbKbdView.inst!=null)
            JbKbdView.inst.setPreferences();
        inst = null;
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
        inst = this;
        String k = preference.getKey();
        Context c = this;
        if("vibro_durations".equals(k))
        {
            showVibroDuration();
        }
        if("intervals".equals(k))
        {
            showIntervalsEditor();
        }
        else if(st.PREF_KEY_LOAD.equals(k))
        {
            backup(false);
        }
        else if(st.PREF_KEY_SAVE.equals(k))
        {
            backup(true);
        }
        else if("set_skins".equals(k))
        {
            String err = CustomKbdDesign.loadCustomSkins();
            if(err.length()>0)
            {
               Toast.makeText(this, err, 1000).show();
            }
            runSetKbd(st.SET_SELECT_SKIN);
            return true;
        }
        else if("pref_port_key_height".equals(k))
        {
            runSetKbd(st.SET_KEY_HEIGHT_PORTRAIT);
            return true;
        }
        else if("pref_land_key_height".equals(k))
        {
            runSetKbd(st.SET_KEY_HEIGHT_LANDSCAPE);
            return true;
        }
        else if("set_key_main_font".equals(k))
        {
            c.startActivity(
                    new Intent(c,EditSetActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(EditSetActivity.EXTRA_PREF_KEY, st.PREF_KEY_MAIN_FONT)
                    .putExtra(EditSetActivity.EXTRA_DEFAULT_EDIT_SET, st.paint().getDefaultMain().toString())
                );
            
        }
        else if("set_key_second_font".equals(k))
         {
             c.startActivity(
                     new Intent(c,EditSetActivity.class)
                     .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                     .putExtra(EditSetActivity.EXTRA_PREF_KEY, st.PREF_KEY_SECOND_FONT)
                     .putExtra(EditSetActivity.EXTRA_DEFAULT_EDIT_SET, st.paint().getDefaultSecond().toString())
                 );
             
         }
        else if("set_key_label_font".equals(k))
         {
             c.startActivity(
                     new Intent(c,EditSetActivity.class)
                     .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                     .putExtra(EditSetActivity.EXTRA_PREF_KEY, st.PREF_KEY_LABEL_FONT)
                     .putExtra(EditSetActivity.EXTRA_DEFAULT_EDIT_SET, st.paint().getDefaultLabel().toString())
                 );
             
         }
        else if("pref_languages".equals(k))
        {
            st.runAct(LangSetActivity.class,c);
            return true;
        }
        else if("fs_editor_set".equals(k))
        {
            getApplicationContext().startActivity(
                    new Intent(getApplicationContext(),EditSetActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(EditSetActivity.EXTRA_PREF_KEY, st.PREF_KEY_EDIT_SETTINGS)
                );
            return true;
        }
        else if("about_app".equals(k))
        {
            vocabTest();
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
        if(st.PREF_KEY_GESTURE_LEFT.equals(key)||st.PREF_KEY_GESTURE_RIGHT.equals(key)
            ||st.PREF_KEY_GESTURE_UP.equals(key)||st.PREF_KEY_GESTURE_DOWN.equals(key)
         )
        {
            JbKbdView.inst = null;
            setGestureList(sharedPreferences, key, null, null);
        }
        if(st.PREF_KEY_USE_GESTURES.equals(key))
            JbKbdView.inst = null;
        if(st.PREF_KEY_SHIFT_STATE.equals(key))
            setShiftState();
        if(st.PREF_KEY_PORTRAIT_TYPE.equals(key))
        {
            int index = Integer.decode(sharedPreferences.getString(st.PREF_KEY_PORTRAIT_TYPE, st.ZERO_STRING));
            setSummary(st.PREF_KEY_PORTRAIT_TYPE, R.string.set_key_portrait_input_type_desc, strVal(getResources().getStringArray(R.array.array_input_type)[index]));
        }
        if(st.PREF_KEY_LANSCAPE_TYPE.equals(key))
        {
            int index = Integer.decode(sharedPreferences.getString(st.PREF_KEY_LANSCAPE_TYPE, st.ZERO_STRING));
            setSummary(st.PREF_KEY_LANSCAPE_TYPE, R.string.set_key_portrait_input_type_desc, strVal(getResources().getStringArray(R.array.array_input_type)[index]));
        }
        if(st.PREF_KEY_USE_SHORT_VIBRO.equals(key))
        {
            int index = Integer.decode(sharedPreferences.getString(st.PREF_KEY_USE_SHORT_VIBRO, st.ONE_STRING));
            setSummary(st.PREF_KEY_USE_SHORT_VIBRO, R.string.set_key_short_vibro_desc, strVal(getResources().getStringArray(R.array.vibro_short_type)[index]));
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
/** Настройка интервалов нажатий */    
    void showIntervalsEditor()
    {
        final View v = getLayoutInflater().inflate(R.layout.edit_intervals, null);
        int max = 5000,min = 50;
        int steps[] = new int[]{50,100,100};
        final SharedPreferences p = st.pref(this);
        IntEditor ie = null;
        ie = (IntEditor)v.findViewById(R.id.long_press);
        ie.setMinAndMax(min, max);
        ie.setValue(p.getInt(st.PREF_KEY_LONG_PRESS_INTERVAL, 500));
        ie.setSteps(steps);
        
        ie = (IntEditor)v.findViewById(R.id.first_repeat);
        min = 50;
        ie.setMinAndMax(min, max);
        ie.setValue(p.getInt(st.PREF_KEY_REPEAT_FIRST_INTERVAL, 400));
        ie.setSteps(steps);

        ie = (IntEditor)v.findViewById(R.id.next_repeat);
        min = 50;
        ie.setMinAndMax(min, max);
        ie.setValue(p.getInt(st.PREF_KEY_REPEAT_NEXT_INTERVAL, 50));
        ie.setSteps(steps);
        st.UniObserver obs = new st.UniObserver()
        {
            @Override
            public int OnObserver(Object param1, Object param2)
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
    void showVibroDuration()
    {
        final View v = getLayoutInflater().inflate(R.layout.edit_intervals, null);
        int max = 5000,min = 10;
        ((TextView)v.findViewById(R.id.interval1)).setText(R.string.set_key_short_vibro_duration);
        ((TextView)v.findViewById(R.id.interval2)).setText(R.string.set_key_long_vibro_duration);
        ((TextView)v.findViewById(R.id.interval3)).setText(R.string.set_key_repeat_vibro_duration);
        int steps[] = new int[]{5,10,20};
        final SharedPreferences p = st.pref(this);
        IntEditor ie = null;
        IntEditor.OnChangeValue cv = new IntEditor.OnChangeValue()
        {
            @Override
            public void onChangeIntValue(IntEditor edit)
            {
                VibroThread.getInstance(inst).runForce(edit.getValue());
            }
        };
        ie = (IntEditor)v.findViewById(R.id.long_press);
        ie.setMinAndMax(min, max);
        ie.setValue(Integer.decode(p.getString(st.PREF_KEY_VIBRO_SHORT_DURATION, DEF_LONG_VIBRO)));
        ie.setSteps(steps);
        ie.setOnChangeValue(cv);

        ie = (IntEditor)v.findViewById(R.id.first_repeat);
        ie.setMinAndMax(min, max);
        ie.setValue(Integer.decode(p.getString(st.PREF_KEY_VIBRO_LONG_DURATION, DEF_LONG_VIBRO)));
        ie.setSteps(steps);
        ie.setOnChangeValue(cv);

        ie = (IntEditor)v.findViewById(R.id.next_repeat);
        ie.setMinAndMax(min, max);
        ie.setValue(Integer.decode(p.getString(st.PREF_KEY_VIBRO_REPEAT_DURATION, DEF_LONG_VIBRO)));
        ie.setSteps(steps);
        ie.setOnChangeValue(cv);
        st.UniObserver obs = new st.UniObserver()
        {
            @Override
            public int OnObserver(Object param1, Object param2)
            {
                if(((Integer)param1).intValue()==AlertDialog.BUTTON_POSITIVE)
                {
                    IntEditor ie;
                    Editor e = p.edit();
                    ie = (IntEditor)v.findViewById(R.id.long_press);
                    e.putString(st.PREF_KEY_VIBRO_SHORT_DURATION, ""+ie.getValue());
                    ie = (IntEditor)v.findViewById(R.id.first_repeat);
                    e.putString(st.PREF_KEY_VIBRO_LONG_DURATION, ""+ie.getValue());
                    ie = (IntEditor)v.findViewById(R.id.next_repeat);
                    e.putString(st.PREF_KEY_VIBRO_REPEAT_DURATION, ""+ie.getValue());
                    e.commit();
                    if(VibroThread.inst!=null)
                        VibroThread.inst.readSettings();
                }
                return 0;
            }
        };
        Dlg.CustomDialog(this, v, getString(R.string.ok), getString(R.string.cancel), null, obs);
    }
    final String getBackupPath()
    {
        return st.getSettingsPath()+st.SETTINGS_BACKUP_FILE;
    }
    void backup(final boolean bSave)
    {
        Dlg.yesNoDialog(this, getString(bSave?R.string.set_key_save_pref:R.string.set_key_load_pref)+" ?", new st.UniObserver()
        {
            @Override
            public int OnObserver(Object param1, Object param2)
            {
                if(((Integer)param1).intValue()==AlertDialog.BUTTON_POSITIVE)
                {
                    int ret = prefBackup(bSave);
                    try{
                    if(ret==0)
                        Toast.makeText(getApplicationContext(), "ERROR", 700).show();
                    else if(ret==1)
                        Toast.makeText(getApplicationContext(), R.string.ok, 700).show();
                        if(!bSave)
                        {
                            finish();
                            startActivity(getIntent());
                        }
                    }
                    catch(Throwable e)
                    {
                    }
                }
                return 0;
            }
        });
    }
    int prefBackup(boolean bSave)
    {
        try{
            String path = getBackupPath();
            String prefDir = getFilesDir().getParent()+"/shared_prefs/";
            File ar[] = st.getFilesByExt(new File(prefDir), st.EXT_XML);
            if(ar==null||ar.length==0)
                return 0;
            File f = new File(path);
            FileInputStream in;
            FileOutputStream out;
            if(bSave)
            {
                in = new FileInputStream(ar[0]);
                f.delete();
                out = new FileOutputStream(f);
            }
            else
            {
                if(!f.exists())
                {
                    Toast.makeText(this, "File not exist: "+path, 700).show();
                    return -1;
                }
                out = new FileOutputStream(ar[0]);
                in = new FileInputStream(f);
            }
            byte b[] = new byte[in.available()];
            in.read(b);
            out.write(b);
            out.flush();
            in.close();
            out.close();
            if(!bSave)
            {
                if(JbKbdView.inst!=null)
                    JbKbdView.inst = null;
                if(ServiceJbKbd.inst!=null)
                    ServiceJbKbd.inst.stopSelf();
            }
            return 1;
        }
        catch (Throwable e) {
            st.logEx(e);
        }
        return 0;
    }
    void vocabTest()
    {
        Words w = new Words();
        w.open("ru");
        String test[] = new String[]{"ящ","те"};
        long times []= new long[test.length];
        for(int i=0;i<test.length;i++)
        {
            long time = System.currentTimeMillis();
            String s[] = w.getWords(test[i]);
            time = System.currentTimeMillis()-time;
            times[i]=time;
        }
        long total = 0;
        String log = "Test words: {";
        for(int i=0;i<test.length;i++)
        {
            long time = times[i];
            total+=time;
            log+=test[i]+":"+time;
        }
        log+="} total:"+total;
        Log.w("Words test", log);
    }
}
