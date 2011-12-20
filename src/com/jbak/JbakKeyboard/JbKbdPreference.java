package com.jbak.JbakKeyboard;

import java.util.Locale;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.jbak.JbakKeyboard.IKeyboard.Keybrd;

public class JbKbdPreference extends PreferenceActivity
{
    @Override
    protected void onDestroy()
    {
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
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
//        getListView().setBackgroundDrawable(st.getBack());
        Preference pref;
        pref = getPreferenceScreen().getPreference(2);
        pref.setOnPreferenceClickListener(new PrefRunSetKbd(st.SET_KEY_HEIGHT_PORTRAIT, this));
        pref = getPreferenceScreen().getPreference(3);
        pref.setOnPreferenceClickListener(new PrefRunSetKbd(st.SET_KEY_HEIGHT_LANDSCAPE, this));
        pref = getPreferenceScreen().getPreference(4);
        pref.setOnPreferenceClickListener(new PrefRunSetKbd(st.SET_LANGUAGES_SELECTION, this));
        pref = getPreferenceScreen().getPreference(6);
        pref.setOnPreferenceClickListener(new PrefRunSetKbd(st.SET_SELECT_SKIN, this));
        pref = getPreferenceScreen().getPreference(9);
        pref.setOnPreferenceClickListener(new PrefRunSetKbd(1000, this));
        pref = getPreferenceScreen().getPreference(8);
        getPreferenceScreen().removePreference(pref);
//        pref.setOnPreferenceClickListener(new PrefRunSetKbd(st.SET_KEYS, this));
    }
    void selectLanguages()
    {
        View v = getLayoutInflater().inflate(R.layout.com_menu, null);
        LinearLayout ll = (LinearLayout)v.findViewById(R.id.com_menu_container);
        v.findViewById(R.id.com_menu_buttons).getLayoutParams().width=0;
        String ls[] = st.getLangsArray(this);
        for(int i=0;i<st.arKbd.length;i++)
        {
            Keybrd lang = st.arKbd[i];
            Locale loc = new Locale(lang.lang.name);
            CheckBox chk = new CheckBox(this);
            chk.setText(loc.getDisplayLanguage());
            chk.setTag(lang.lang.name);
            if(st.searchStr(lang.lang.name, ls)>-1)
                chk.setChecked(true);
            else
                chk.setChecked(false);
            ll.addView(chk);
        }
        st.UniObserver obs  = new st.UniObserver(null,ll)
        {
            @Override
            int OnObserver(Object param1, Object param2)
            {
                if(((Integer)param1).intValue()==AlertDialog.BUTTON_POSITIVE)
                {
                    LinearLayout ll = (LinearLayout)param2;
                    String lc = "";
                    int cnt = ll.getChildCount();
                    for(int i=0;i<cnt;i++)
                    {
                        CheckBox ch = (CheckBox)ll.getChildAt(i);
                        if(ch.isChecked())
                        {
                            String sl = (String)ch.getTag();
                            if(lc.length()>0)
                                lc+=',';
                            lc+=sl;
                        }
                    }
                    SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(ll.getContext());
                    p.edit().putString(st.PREF_KEY_LANGS, lc).commit();
                }
                return 0;
            }
        };
        Dlg.CustomDialog(this, v, getString(R.string.ok), getString(R.string.cancel), null, obs);
    }
    static class PrefRunSetKbd implements OnPreferenceClickListener
    {
        public PrefRunSetKbd(int action,JbKbdPreference a)
        {
            m_act = action;
            m_a = a;
        }
        int m_act;
        JbKbdPreference m_a;
        @Override
        public boolean onPreferenceClick(Preference preference)
        {
            switch(m_act)
            {
                case 1000:
                    m_a.startActivity(new Intent(m_a,AboutActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
                case st.SET_KEYS: 
                    st.runAct(KeySetActivity.class,m_a);
                    break;
                case st.SET_LANGUAGES_SELECTION: 
                    m_a.startActivity(new Intent(m_a,LangSetActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
                default:
                    m_a.runSetKbd(m_act);
                    break;
            }
            return true;
        }
    }
    
}
