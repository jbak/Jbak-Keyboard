package com.jbak.JbakKeyboard;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SpinnerAdapter;

public class EditSetActivity extends Activity
{
    public static EditSetActivity inst;
    EditText m_edit;
    EditSet m_es = new EditSet();
    float m_defaultFontSize;
    OnCheckedChangeListener m_onCheckChange = new OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            switch (buttonView.getId())
            {
                case R.id.es_font_bold:
                    if(isChecked)
                        m_es.style|=Typeface.BOLD;
                    else
                        m_es.style = st.rem(m_es.style, Typeface.BOLD);
                break;
                
                case R.id.es_font_italic:
                    if(isChecked)
                        m_es.style|=Typeface.ITALIC;
                    else
                        m_es.style = st.rem(m_es.style, Typeface.ITALIC);
                break;
            }
            m_es.setToEditor(m_edit);
        }
    };
    OnItemSelectedListener m_OnSpinnerChange = new OnItemSelectedListener()
    {

        @Override
        public void onItemSelected(AdapterView<?> view, View selView, int pos, long id)
        {
            switch(view.getId())
            {
                case R.id.es_fonts:
                {
                    m_es.typeface = EditSet.intToTypeface(pos);
                }
                break;
                case R.id.es_font_size:
                {
                    if(pos==0)
                    {
                        m_es.fontSize = 0;
                        m_edit.setTextSize(m_defaultFontSize);
                        return;
                    }
                    else
                    {
                        m_es.fontSize = pos-1+10;
                    }
                }
            }
            m_es.setToEditor(m_edit);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0)
        {
        }
        
    };
    @Override
    public void onCreate(android.os.Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        inst = this;
        View v = getLayoutInflater().inflate(R.layout.edit_settings, null);
        m_es.load();
        m_edit = (EditText) v.findViewById(R.id.es_edit);
        m_defaultFontSize = m_edit.getTextSize();
        m_es.setToEditor(m_edit);
        CheckBox cb = (CheckBox)v.findViewById(R.id.es_font_bold);
        cb.setOnCheckedChangeListener(m_onCheckChange);
        cb.setChecked(st.has(m_es.style,Typeface.BOLD));
        cb = (CheckBox)v.findViewById(R.id.es_font_italic);
        cb.setOnCheckedChangeListener(m_onCheckChange);
        cb.setChecked(st.has(m_es.style,Typeface.ITALIC));
        Spinner s = (Spinner)v.findViewById(R.id.es_fonts); 
        s.setOnItemSelectedListener(m_OnSpinnerChange);
        s.setSelection(EditSet.typefaceToInt(m_es.typeface));
        s=(Spinner)v.findViewById(R.id.es_font_size);
        s.setOnItemSelectedListener(m_OnSpinnerChange);
        ArrayAdapter<String> adapt = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapt.add(getString(R.string.es_def_font));
        for(int i=10;i<=48;i++)
        {
            adapt.add(""+i);
        }
        s.setAdapter(adapt);
        if(m_es.fontSize==0)
            s.setSelection(0);
        else
        {
            int sel = m_es.fontSize-10+1;
            if(sel>=s.getCount())
                sel = s.getCount()-1;
            s.setSelection(sel);
        }
        setContentView(v);
    };
    @Override
    protected void onDestroy() 
    {
        m_es.save();
        inst = null;
        super.onDestroy();
    };
    public static class EditSet
    {
        Typeface typeface = Typeface.DEFAULT;
        int style = 0;
        int fontSize = 0;
        public EditSet()
        {}
        final boolean isDefault()
        {
            return typeface==Typeface.DEFAULT&&fontSize==0&&style==0;
        }
        static final int typefaceToInt(Typeface tf)
        {
            if(tf==Typeface.SERIF)
                return 1;
            if(tf==Typeface.MONOSPACE)
                return 2;
            return 0;
        }
        static final Typeface intToTypeface(int tf)
        {
            if(tf==1)
                return Typeface.SERIF;
            if(tf==2)
                return Typeface.MONOSPACE;
            return Typeface.DEFAULT;
        }
        void setToEditor(EditText et)
        {
            et.setTypeface(typeface, style);
            if(fontSize!=0)
                et.setTextSize(fontSize);
        }
        void load()
        {
            fromString(st.pref().getString(st.PREF_KEY_EDIT_SETTINGS, ""));
        }
        void fromString(String s)
        {
            if(s==null||s.indexOf(';')<0)
                return;
            String ar[] = s.split(";");
            if(ar.length<3)
                return;
            try
            {
                typeface = intToTypeface(Integer.valueOf(ar[0]));
                style = Integer.valueOf(ar[1]);
                fontSize = Integer.valueOf(ar[2]);
            }
            catch (Throwable e)
            {}
        }
        public String toString()
        {
            return new StringBuffer().append(typefaceToInt(typeface)).append(';')
                                       .append(style).append(';')
                                       .append(fontSize).toString();
        }
        void save()
        {
            st.pref().edit().putString(st.PREF_KEY_EDIT_SETTINGS,toString()) .commit();
        }
    }
}
