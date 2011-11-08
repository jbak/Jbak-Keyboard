package com.jbak.JbakKeyboard;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jbak.JbakKeyboard.AppsList.MenuEntry;
/** Класс настроек аппаратных клавиш*/
public class KeySetActivity extends Activity
{
	static KeySetActivity inst;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		inst = this;
		m_MainView = getLayoutInflater().inflate(R.layout.key_set_main, null);
		m_ListView = (ListView)m_MainView.findViewById(R.id.ks_key_list);
		m_MainView.findViewById(R.id.ks_add_key).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				m_key = new KeySet();
				runKeySet();
			}
		});
		Cursor c = st.stor().getKeysCursor();
		if(c!=null)
		{
			m_ListView.setAdapter(new KeysAdapter(this, c));
		}
		onListChanged();
		m_ListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				m_key = (KeySet)arg1.getTag();
				runKeySet();
			}
		});
		setView(new BackEntry(m_MainView, new st.UniObserver()
		{
			@Override
			int OnObserver(Object param1, Object param2)
			{
				finish();
				return 0;
			}
		}));
		super.onCreate(savedInstanceState);
		m_appList = new AppsList(this);
	}
	void onListChanged()
	{
		boolean bEmpty=m_ListView.getAdapter()==null||m_ListView.getAdapter().getCount()==0;
		View v = m_MainView.findViewById(R.id.empty_text);
		ViewGroup.LayoutParams lp = v.getLayoutParams();
		if(bEmpty)
		{
			lp.height =ViewGroup.LayoutParams.WRAP_CONTENT; 
		}
		else
		{
			lp.height =0;
		}
		v.setLayoutParams(lp);
		
	}
	@Override
	public void onBackPressed()
	{
		int index = m_BackStack.size()-1;
		if(index==0)
		{
			finish();
			return;
		}	
		BackEntry be = m_BackStack.get(index);
		m_BackStack.remove(index);
		be.onBack();
		--index;
		m_BackStack.get(index).setView(this);
	};
	@Override
	protected void onDestroy()
	{
		inst = null;
		super.onDestroy();
	}
/** Устанавливает новое окно, которое участвует в стеке нажатий BACK*/	
	void setView(BackEntry be)
	{
		m_BackStack.add(be);
		be.setView(this);
	}
/** Сохраняет текущую клавишу */	
	void saveKey()
	{
		switch (m_key.action)
		{
			case KeySet.ACT_TEXT:
					EditText ed = (EditText)m_ksAW.findViewById(R.id.input_text);
					m_key.setText(ed.getEditableText().toString());
				break;
		}
		st.stor().saveKey(m_key);
		Cursor c = st.stor().getKeysCursor();
		if(c!=null)
		{
			m_ListView.setAdapter(new KeysAdapter(this, c));
		}
		if(KeyPressProcessor.inst!=null)
			KeyPressProcessor.inst.loadKeys();
		onListChanged();
	}
/** Запускает настройку клавиши <br>
*	Если в текущей клавише m_key keycode>0 - то выводим на экран редактирование клавиши
*   Иначе будет показан экран с предложением нажать клавишу */  
	void runKeySet()
	{
		View v = getLayoutInflater().inflate(R.layout.key_set, null);
		m_ksKey = (TextView)v.findViewById(R.id.key_label);
		m_ksKeySet = (ScrollView)v.findViewById(R.id.ks_key_set);
		m_ksAW = (LinearLayout)v.findViewById(R.id.ks_aw);
		m_ksActionLabel = (TextView)v.findViewById(R.id.ks_aw_title);
		if(m_key.keycode==0)
			m_state|=STAT_WAIT_KEY;
		else
			editKey();
		st.UniObserver obs = new st.UniObserver()
		{
			@Override
			int OnObserver(Object param1, Object param2)
			{
				saveKey();
				return 0;
			}
		};
		m_spinAction =(Spinner)v.findViewById(R.id.ks_action);
		m_spinKeyPressType =(Spinner)v.findViewById(R.id.ks_press_type);
		m_spinType =(Spinner)v.findViewById(R.id.ks_type);
		m_spinAction.setOnItemSelectedListener(m_spinnerListener);
		m_spinKeyPressType.setOnItemSelectedListener(m_spinnerListener);
		m_spinType.setOnItemSelectedListener(m_spinnerListener);
		if(m_key.keycode!=0)
		{
			m_spinKeyPressType.setSelection(st.has(m_key.flags, KeySet.FLAG_LONG_PRESS)?1:0);
			if(st.has(m_key.flags, KeySet.FLAG_TEXT_FIELDS))
				m_spinType.setSelection(1);
			else if(st.has(m_key.flags, KeySet.FLAG_NOT_IN_TEXT_FIELDS))
				m_spinType.setSelection(2);
			else
				m_spinType.setSelection(0);
			m_spinAction.setSelection(m_key.action);
		}
		setView(new BackEntry(v, obs));
	}
/** Возвращает */	
	String getKeyString(int code,char disp)
	{
		String s = null;
		if(disp>0)
		{
			s = String.valueOf(disp);
		}
		else 
		{
			s = KeySet.getKeyName(code);
		}
		if(s!=null)
		{
			s = getString(R.string.ks_char_label,s);
		}
		else
		{
			s=getString(R.string.ks_non_char_label,code);
		}	
		return s;
	}
/** Вызывается при редактировании клавиши, если m_key заполнена*/	
	void editKey()
	{
		m_ksKey.setText(getKeyString(m_key.keycode, m_key.keychar));	
		ViewGroup.LayoutParams lp = m_ksKeySet.getLayoutParams();
		lp.height =ViewGroup.LayoutParams.WRAP_CONTENT; 
		m_ksKeySet.setLayoutParams(lp);
	}
/** Обработчик нажатия клавиши аппаратной клавиатуры */	
	boolean onHardwareKey(KeyEvent ke)
	{
		if(!isWaitKey())
			return false;
		int act = ke.getAction();
		if(act==KeyEvent.ACTION_UP)
		{
			m_state = st.rem(act, STAT_WAIT_KEY);
		}
		if(act==KeyEvent.ACTION_DOWN)
		{
			int code = ke.getKeyCode();
			char disp = ke.getDisplayLabel();
			m_key.keycode = code;
			m_key.keychar = disp;
			editKey();
		}
		return true;
	}
/** Возвращает true, если мы находимся в режиме ожидания нажатия аппаратной клавиши */	
	public final boolean isWaitKey()
	{
		return st.has(m_state, STAT_WAIT_KEY);
	}
/** Класс стека окон. Если окно закрывается клавишей BACK - запускается {@link #onBack()}
 * */	
	static class BackEntry
	{
		public BackEntry(View v,st.UniObserver close)
		{
			view = v;
			onClose = close;
		}
		void setView(Activity act)
		{
			act.setContentView(view);
			act.onContentChanged();
			InputMethodManager imm = (InputMethodManager)act.getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromInputMethod(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
			boolean bAct = imm.isActive(view);
			if(bAct)
			{
				boolean bAcc = imm.isAcceptingText();
			}
		}
		void onBack()
		{
			if(onClose!=null)
			{
				onClose.Observ();
			}
		}
/** Окно стека */		
		View view;
/** Действие для выполнения при закрытии окна  */		
		st.UniObserver onClose;
	}
/** Устанавливает программу для запуска в  текущем окне настроек*/	
	void setApp(ComponentName cn)
	{
		if(cn==null)
			return;
		MenuEntry me=AppsList.getMenuEntryForComponent(cn, getPackageManager());
		if(me==null)
			return;
		((ImageView)m_ksAW.findViewById(R.id.app_icon)).setImageDrawable(me.icon);
		((TextView)m_ksAW.findViewById(R.id.app_title)).setText(me.text);
	}
	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
	};
/** Устанавливает выбор действия на слой */	
	void setAction(int action)
	{
		m_ksAW.removeAllViews();
		
		m_key.action = action;
		if(action==KeySet.ACT_RUN_APP)
		{
			m_ksAW.setVisibility(View.VISIBLE);
			View v  = getLayoutInflater().inflate(R.layout.app_list_entry, m_ksAW);
			v.setClickable(true);
			m_ksActionLabel.setText(R.string.ks_app_for_launch);
			setApp(m_key.getApp());
			v.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					m_appList.show(new st.UniObserver()
					{
						
						@Override
						int OnObserver(Object param1, Object param2)
						{
							ComponentName cn = ((AppsList.MenuEntry)param1).cn;
							m_key.setApp(cn);
							setApp(cn);
							return 0;
						}
					});	
				}
			});
		}
		else if(action==KeySet.ACT_TEXT)
		{
			m_ksAW.setVisibility(View.VISIBLE);
			getLayoutInflater().inflate(R.layout.aw_input_text, m_ksAW);
			m_ksActionLabel.setText(R.string.ks_text_for_input);
			String s = m_key.getText();
			if(s!=null)
				((EditText)m_ksAW.findViewById(R.id.input_text)).setText(s);
			
		}
		else
		{
			m_ksAW.setVisibility(View.INVISIBLE);
		}
		m_ksAW.forceLayout();
	}
/** Адаптер для вывода списка кнопок */	
	static class KeysAdapter extends CursorAdapter
	{
		public KeysAdapter(Context context, Cursor c)
		{
			super(context, c);
			m_a = (KeySetActivity)context;
		}
		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			KeySet ks = st.stor().readKey(cursor);
			String key = m_a.getKeyString(ks.keycode, ks.keychar);
			if(st.has(ks.flags, KeySet.FLAG_LONG_PRESS))
				key+=" - "+m_a.getString(R.string.ks_press_type_long);
			key+=" - ";
			if(ks.action==KeySet.ACT_RUN_APP)
				key+=m_a.getString(R.string.ks_action_run_app);
			if(ks.action==KeySet.ACT_TEXT)
				key+=m_a.getString(R.string.ks_action_input_text);
			view.setTag(ks);
			((TextView)view).setText(key);
		}
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent)
		{
			return m_a.getLayoutInflater().inflate(R.layout.com_menu_entry, null);
		}
		KeySetActivity m_a;
	}
/** ***************************************************************/
/** Обработчик изменений комбобоксов */	
	AdapterView.OnItemSelectedListener m_spinnerListener= new AdapterView.OnItemSelectedListener()
	{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position,long arg3)
		{
			int id  = parent.getId();
			switch(id)
			{
				case R.id.ks_press_type:
				{
					if(position==0)
						m_key.flags = st.rem(m_key.flags, KeySet.FLAG_LONG_PRESS);
					else
						m_key.flags|=KeySet.FLAG_LONG_PRESS;		
						
				}break;
				case R.id.ks_type:
				{
					m_key.flags = st.rem(m_key.flags, KeySet.FLAG_TEXT_FIELDS);
					m_key.flags = st.rem(m_key.flags, KeySet.FLAG_NOT_IN_TEXT_FIELDS);
					if(position==1)
						m_key.flags|=KeySet.FLAG_TEXT_FIELDS;
					else if(position==2)
						m_key.flags|=KeySet.FLAG_NOT_IN_TEXT_FIELDS;	
				}break;					
				case R.id.ks_action:
				{
					setAction(position);					
				}break;
			}
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0){}
	};
	
/** Стек окон*/	
	ArrayList<BackEntry> m_BackStack = new ArrayList<KeySetActivity.BackEntry>();
/** Флаги статусов*/	
	int m_state=0;
/** Флаг статуса - ждём нажатия клавиши от аппаратной клавиатуры */	
	public static final int STAT_WAIT_KEY = 0x000001;
/** Метка клавиши. После нажатия аппаратной кнопки тут отображается название*/	
	TextView m_ksKey;
/** Метка выбираемой настройки для действия */	
	TextView m_ksActionLabel;
/** Слой, на котором отображаются настройки кнопки. Если кнопка еще не выбрана - скрыт*/	
	ScrollView m_ksKeySet;
/** Слой, на котором отображается дополнительный выбор опций для выбранного действия*/	
	LinearLayout m_ksAW;
/** Текущая клавиша */	
	KeySet m_key = new KeySet();
	AppsList m_appList;
	ListView m_ListView;
	Spinner m_spinAction;
	Spinner m_spinKeyPressType;
	Spinner m_spinType;
	View m_MainView;
}
