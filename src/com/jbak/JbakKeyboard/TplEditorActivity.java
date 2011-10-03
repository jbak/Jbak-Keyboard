package com.jbak.JbakKeyboard;

import android.app.Activity;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TplEditorActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if(Templates.inst==null)
			finish();
		View v = getLayoutInflater().inflate(R.layout.tpl_editor, null);
		v.findViewById(R.id.tpl_save).setOnClickListener(m_clkListener);
		v.findViewById(R.id.close).setOnClickListener(m_clkListener);
		View bSpec =v.findViewById(R.id.tpl_spec_options); 
		bSpec.setOnClickListener(m_clkListener);
		m_edName = (EditText)v.findViewById(R.id.tpl_name);
		m_edText = (EditText)v.findViewById(R.id.tpl_text);
		if(Templates.inst.isEditFolder())
		{
			setTitle(R.string.tpl_new_folder);
			m_edName.setHint(R.string.tpl_folder_name);
			m_edText.getLayoutParams().width=0;
			bSpec.getLayoutParams().width=0;
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
		super.onCreate(savedInstanceState);
		setContentView(v);
		m_edName.requestFocusFromTouch();
	}
	@Override
	protected void onDestroy()
	{
		if(Templates.inst!=null)
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
	void onSpecOptions()
	{
		
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
			}
		}
	};
	EditText m_edName;
	EditText m_edText;
}
