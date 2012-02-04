package com.jbak.JbakKeyboard;

import java.io.File;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

public class TplEditorActivity extends Activity
{
    public static final String EXTRA_CLIPBOARD_ENTRY = "e_clp";
    Long m_clipbrdDate=null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if(Templates.inst==null&&!getIntent().hasExtra(EXTRA_CLIPBOARD_ENTRY))
            finish();
        View v = getLayoutInflater().inflate(R.layout.tpl_editor, null);
        v.findViewById(R.id.tpl_save).setOnClickListener(m_clkListener);
        v.findViewById(R.id.close).setOnClickListener(m_clkListener);
        v.findViewById(R.id.delete).setOnClickListener(m_clkListener);
        View bSpec =v.findViewById(R.id.tpl_spec_options); 
        bSpec.setOnClickListener(m_clkListener);
        m_edName = (EditText)v.findViewById(R.id.tpl_name);
        m_edText = (EditText)v.findViewById(R.id.tpl_text);
        int pos = -1;
        pos = getIntent().getIntExtra(EXTRA_CLIPBOARD_ENTRY, -1);
        if(pos>-1)
        {
            setTitle(getString(R.string.mm_multiclipboard));
            Cursor c = st.stor().getClipboardCursor();
            v.findViewById(R.id.tpl_save).setVisibility(View.GONE);
            v.findViewById(R.id.tpl_spec_options).setVisibility(View.GONE);
            m_edName.setVisibility(View.GONE);
            m_edText.setFocusableInTouchMode(false);
            if(c!=null)
            {
                c.move(0-pos);
                String cp = c.getString(0);
                m_clipbrdDate = new Long(c.getLong(2));
                m_edText.setText(cp);
                c.close();
            }
        }
        else
        {
            File f =Templates.inst.m_editFile; 
            if(Templates.inst.isEditFolder())
            {
                setTitle(R.string.tpl_new_folder);
                m_edName.setHint(R.string.tpl_folder_name);
                m_edText.getLayoutParams().width=0;
                bSpec.getLayoutParams().width=0;
            }
            if(f!=null)
            {
                m_edName.setText(f.getName());
                if(!f.isDirectory())
                {
                    String txt = Templates.getFileString(f);
                    if(txt!=null)
                        m_edText.setText(txt);
                }
                v.findViewById(R.id.delete).getLayoutParams().width = -2;
            }
            m_edName.setOnFocusChangeListener(new OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if(v==m_edName&&hasFocus)
                    {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            });
        }
        super.onCreate(savedInstanceState);
        setContentView(v);
        m_edName.requestFocusFromTouch();
    }
    @Override
    protected void onDestroy()
    {
        if(Templates.inst!=null&&m_clipbrdDate==null)
            Templates.inst.onCloseEditor();
        super.onDestroy();
    }
    void onSave()
    {
        if(Templates.inst==null)
            finish();
        
        String n = m_edName.getEditableText().toString();
        if(n.length()==0)
        {
            Toast.makeText(this, getString(R.string.tpl_fields_empty), 500).show();
            return;
        }
        finish();
        n = st.normalizeFileName(n);
        if(Templates.inst.isEditFolder())
        {
            Templates.inst.saveFolder(n);
        }
        else
        {
            String t = m_edText.getEditableText().toString();
            if(n.length()==0|t.length()==0)
            {
                Toast.makeText(this, getString(R.string.tpl_fields_empty), 500).show();
                return;
            }
            Templates.inst.saveTemplate(n,t);
        }
        Templates.inst.onCloseEditor();
    }
    void delete()
    {
        if(m_clipbrdDate!=null)
        {
            st.stor().removeClipboardByDate(m_clipbrdDate.longValue(), 0);
            if(ComMenu.inst!=null)
                ComMenu.inst.removeLastLongClicked();
            finish();
            return;
        }
        String query = getString(R.string.tpl_delete,Templates.inst.m_editFile.getName());
        new Dlg.RunOnYes(this,query){
            @Override
            public void run()
            {
                Templates.inst.onDelete();
                finish();
            }
        };
    }
    @Override
    public void onBackPressed() 
    {
        finish();
    };
    void onSpecOptions()
    {
        int rlist = R.layout.tpl_instr_list;
        final ArrayAdapter<String> ar = new ArrayAdapter<String>(this, 
                                                    rlist,
                                                    getResources().getStringArray(R.array.tpl_spec_instructions)
                                                    );
        Dlg.CustomMenu(this, ar, null, new st.UniObserver()
        {
            @Override
            public int OnObserver(Object param1, Object param2)
            {
                int which = ((Integer)param1).intValue();
                if(which>=0)
                {
                    String txt = ar.getItem(which);
                    int f = txt.indexOf(' ');
                    if(f>0)
                        txt = txt.substring(0,f);
                    int s = m_edText.getSelectionStart();
                    int e = m_edText.getSelectionEnd();
                    m_edText.getText().replace(s<e?s:e,e>s?e:s, txt);
                }
                return 0;
            }
        });
        
    }
    View.OnClickListener m_clkListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch(v.getId())
            {
                case  R.id.tpl_save: onSave(); break;
                case  R.id.tpl_spec_options: onSpecOptions(); break;
                case  R.id.close: finish(); Templates.inst.onCloseEditor();break;
                case R.id.delete:delete();break;
            }
        }
    };
    EditText m_edName;
    EditText m_edText;
}
