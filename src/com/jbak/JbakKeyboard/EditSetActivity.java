package com.jbak.JbakKeyboard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

public class EditSetActivity extends Activity
{
    public static EditSetActivity inst;
    public static final String EXTRA_PREF_KEY = "pref_key";
    public static final String EXTRA_DEFAULT_EDIT_SET = "def_edit_set";
    EditText m_edit;
    EditSet m_es = new EditSet();
    String m_prefKey;
    String m_defaultEditSet;
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
                        m_edit.setTextSize(TypedValue.COMPLEX_UNIT_PX,m_defaultFontSize);
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
        m_prefKey = getIntent().getStringExtra(EXTRA_PREF_KEY);
        if(m_prefKey==null)
            m_prefKey = st.PREF_KEY_EDIT_SETTINGS;
        View v = getLayoutInflater().inflate(R.layout.edit_settings, null);
        m_defaultEditSet = getIntent().getStringExtra(EXTRA_DEFAULT_EDIT_SET);
        if(!m_es.load(m_prefKey)&&m_defaultEditSet!=null)
            m_es.fromString(m_defaultEditSet);
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
        String s = m_es.toString();
        SharedPreferences p = st.pref();
        if(p.getString(m_prefKey, null)!=null||m_defaultEditSet!=null&&!s.equals(m_defaultEditSet))
            m_es.save(p,m_prefKey);
        inst = null;
        JbKbdView.inst = null;
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
            float df = et.getTextSize();
            et.setTypeface(typeface, style);
            et.setTextSize(TypedValue.COMPLEX_UNIT_PX,fontSize>0?fontSize:df);
        }
        TextPaint getTextPaint(boolean bOwnBoldAndItalic)
        {
            TextPaint tp = new TextPaint();
            tp.density = (float) 1.0;
            tp.setDither(true);
            tp.setAntiAlias(true);
            if(bOwnBoldAndItalic)
            {
                if(st.has(style, Typeface.BOLD))
                    tp.setFakeBoldText(true);
                if(st.has(style, Typeface.ITALIC))
                    tp.setTextSkewX((float) -0.25);
            }
            tp.setTypeface(Typeface.create(typeface, style));
            if(fontSize!=0)
                tp.setTextSize(fontSize);
            return tp;
        }
        TextPaint getTextPaint()
        {
            return getTextPaint(false);
        }
        boolean load(String prefKey)
        {
            return fromString(st.pref().getString(prefKey, ""));
        }
        boolean fromString(String s)
        {
            if(s==null||s.indexOf(';')<0)
                return false;
            String ar[] = s.split(";");
            if(ar.length<3)
                return false;
            try
            {
                typeface = intToTypeface(Integer.valueOf(ar[0]));
                style = Integer.valueOf(ar[1]);
                fontSize = Integer.valueOf(ar[2]);
            }
            catch (Throwable e)
            {
                return false;
            }
            return true;
        }
        public String toString()
        {
            return new StringBuffer().append(typefaceToInt(typeface)).append(';')
                                       .append(style).append(';')
                                       .append(fontSize).toString();
        }
        void save(SharedPreferences pref,String prefKey)
        {
            pref.edit().putString(prefKey,toString()) .commit();
        }
    }
}
