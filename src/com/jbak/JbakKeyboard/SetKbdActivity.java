package com.jbak.JbakKeyboard;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard.Key;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
/** Класс для настроек различных значений клавиатуры, требующих просмотра qwerty-слоя */
public class SetKbdActivity extends Activity
{
	/** Текущий экземпляр класса */
	static SetKbdActivity inst;
	int m_curAction;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		inst = this;
		m_curAction = getIntent().getIntExtra(st.SET_INTENT_ACTION, st.SET_KEY_HEIGHT_PORTRAIT);
		if(m_curAction==st.SET_KEY_HEIGHT_LANDSCAPE)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			setTitle(R.string.set_key_height_landscape);
		}
		else if(m_curAction==st.SET_KEY_HEIGHT_PORTRAIT)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setTitle(R.string.set_key_height_portrait);
		}
		int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		super.onCreate(savedInstanceState);
		getWindow().addFlags(flags);
		View v = getLayoutInflater().inflate(R.layout.set_sizes, null);
		SeekBar sb = (SeekBar)v.findViewById(R.id.key_height);
		st.setQwertyKeyboard(true);
		
		sb.setProgress(st.kv().getCurKeyboard().getHeightKey());
		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				if(fromUser)
					changeKeyHeight(progress);
			}
		});
		setContentView(v);
	}
	void changeKeyHeight(int height)
	{
		String pname = m_curAction==st.SET_KEY_HEIGHT_PORTRAIT?st.PREF_KEY_HEIGHT_PORTRAIT:st.PREF_KEY_HEIGHT_LANDSCAPE;
		PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(pname, height).commit();
		JbKbdView.inst.m_KeyHeight = height;
		st.setQwertyKeyboard(true);
	}
	@Override
	protected void onDestroy()
	{
		JbKbdView.inst = null;
		inst = null;
		super.onDestroy();
	}

}
