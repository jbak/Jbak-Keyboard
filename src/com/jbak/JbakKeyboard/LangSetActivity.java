package com.jbak.JbakKeyboard;

import java.util.ArrayList;
import java.util.Vector;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jbak.JbakKeyboard.IKeyboard.Keybrd;
import com.jbak.JbakKeyboard.IKeyboard.Lang;

public class LangSetActivity extends ListActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		LangAdapter adapt = new LangAdapter(this, R.layout.lang_list_item);
		for(Lang l:st.arLangs)
			adapt.add(l);
		setListAdapter(adapt);
		super.onCreate(savedInstanceState);
	}
	@Override
	protected void onDestroy()
	{
		String langs = ((LangAdapter)getListAdapter()).getLangString();
		st.pref().edit().putString(st.PREF_KEY_LANGS, langs).commit();
		super.onDestroy();
	}
	View.OnClickListener m_butListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Lang l = (Lang)v.getTag();
	    	try{
		    	Intent in = new Intent(Intent.ACTION_VIEW)
		    	.setComponent(new ComponentName(v.getContext(), SetKbdActivity.class))
		    	.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		    	.putExtra(st.SET_INTENT_ACTION, st.SET_SELECT_KEYBOARD)
		    	.putExtra(st.SET_INTENT_LANG_NAME, l.name);
		    	startActivity(in);
	    	}
	    	catch(Throwable e)
	    	{
	    		st.logEx(e);
	    	}

			
		}
	};
	class LangAdapter extends ArrayAdapter<IKeyboard.Lang>
	{
		String getLangString()
		{
			String ret="";
			for(String s:m_arLangs)
			{
				if(ret.length()>0)
					ret+=",";
				ret+=s;
			}
			return ret;
		}
		OnCheckedChangeListener m_chkListener = new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				Lang l = (Lang)buttonView.getTag();
				if(isChecked)
					m_arLangs.add(l.name);
				else
				{
					int f = -1;
					for(int i=0;i<m_arLangs.size();i++)
					{
						if(m_arLangs.get(i).equals(l.name))
						{
							f = i;
							break;
						}
					}
					if(f>-1)
						m_arLangs.remove(f);
				}
			}
		};
		public LangAdapter(Context context, int textViewResourceId)
		{
			super(context, textViewResourceId);
			rId = textViewResourceId;
			m_arLangs = new Vector<String>();
			for(String l:st.getLangsArray(context))
				m_arLangs.add(l);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if(convertView==null)
				convertView = getLayoutInflater().inflate(rId, null);
			Lang l = st.arLangs[position];
			CheckBox cb = (CheckBox)convertView.findViewById(R.id.checkbox);
			cb.setText(l.strId);
			cb.setTag(l);
			for(String s:m_arLangs)
			{
				if(s.equals(l.name))
				{
					cb.setChecked(true);
				}
			}
			cb.setOnCheckedChangeListener(m_chkListener);
			ImageButton b = (ImageButton)convertView.findViewById(R.id.button);
			int cnt = 0;
			for(Keybrd k:st.arKbd)
			{
				if(l.strId==k.lang.strId)
					cnt++;
			}
			if(cnt>1)
			{
				//b.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
				b.setOnClickListener(m_butListener);
				b.setVisibility(View.VISIBLE);
			}
			else
			{
				b.setVisibility(View.INVISIBLE);
				//b.getLayoutParams().width = 0;
			}
			b.setTag(l);
			return convertView;
		}
		int rId;
		Vector<String> m_arLangs;
	}
}
