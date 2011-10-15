package com.jbak.JbakKeyboard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;

import android.database.Cursor;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.jbak.JbakKeyboard.st.UniObserver;

public class KeyPressProcessor
{
	static KeyPressProcessor inst;
	public KeyPressProcessor()
	{
		m_v = new View(ServiceJbKbd.inst);
		inst = this;
		loadKeys();
	}
	void destroy()
	{
		inst = null;
	}
	boolean onKeyDown(KeyEvent evt,EditorInfo ei)
	{
		if(evt.getRepeatCount()>0)
			return false;
		if(st.has(evt.getFlags(),KeyEvent.FLAG_VIRTUAL_HARD_KEY))
			st.log("keyDown hard"+KeySet.getKeyName(evt.getKeyCode()));
		else	
			st.log("keyDown soft"+KeySet.getKeyName(evt.getKeyCode()));
		m_bLongProcessed = false;
		m_ksShort = getKey(evt, false, isEditor(ei));
		m_ksLong = getKey(evt, true, isEditor(ei));
		if(m_ksLong==null&&m_ksShort==null)
		{
			return false;
		}
		if(m_ksLong!=null)
		{
			evt.startTracking();
/*			m_timer = new Timer();
			m_timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					onLongPress();
				}
			}, 500);
*/		}
		return true;
	}
	void onLongPress()
	{
/*		if(m_ksLong==null)
			return;
		m_bLongProcessed = true;
		m_ksShort = null;
		m_ksLong.run();
		return;
*/	}
/** */
	boolean onKeyUp(KeyEvent evt,EditorInfo ei)
	{
		if(st.has(evt.getFlags(),KeyEvent.FLAG_VIRTUAL_HARD_KEY))
			st.log("keyUp hard"+KeySet.getKeyName(evt.getKeyCode()));
		else	
			st.log("keyUp soft"+KeySet.getKeyName(evt.getKeyCode()));
		if(m_timer!=null)
		{
			m_timer.cancel();
			m_timer = null;
		}
		if(m_ksLong!=null&&m_bLongProcessed)
		{
			m_bLongProcessed = false;
			m_ksShort = null;
			m_ksLong = null;
			return true;
		}
		if(m_ksShort!=null)
		{
			m_ksShort.run();
			m_ksShort = null;
			return true;
		}
		if(m_ksLong!=null)
		{
			// Ждали длинного нажатия, но не дождались, возвращаем короткое
			// Если нажата back - то проверяем наличие открытой софт-клавиатуры
			if(evt.getKeyCode()==KeyEvent.KEYCODE_BACK&&
					ServiceJbKbd.inst!=null&&
					ServiceJbKbd.inst.handleBackPress())
			{
				return true;
			}
			sendKeyEvent(evt.getKeyCode());
			return true;
		}
		return false;
	}
/** Отправляет нажатие клавиши системе */	
	void sendKeyEvent(final int keyEventCode)
	{
		st.SyncAsycOper op = new st.SyncAsycOper(null)
		{
			@Override
			void makeOper(UniObserver obs)
			{
		        InputConnection ic = ServiceJbKbd.inst.getCurrentInputConnection();
		        if (ic == null) return;
		        long eventTime = SystemClock.uptimeMillis();
		        ic.sendKeyEvent(new KeyEvent(eventTime, eventTime,
		                KeyEvent.ACTION_DOWN, keyEventCode, 0, 0, 0, 0,
		                KeyEvent.FLAG_SOFT_KEYBOARD));
		        ic.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime,
		                KeyEvent.ACTION_UP, keyEventCode, 0, 0, 0, 0,
		                KeyEvent.FLAG_SOFT_KEYBOARD));
			}
		};
		op.startAsync();
	}
/** Обработчик удержания клавиши */	
	boolean onKeyLong(KeyEvent evt,EditorInfo ei)
	{
		st.log("keyLong "+KeySet.getKeyName(evt.getKeyCode()));
		m_ksLong = getKey(evt, true, isEditor(ei));
		if(m_ksLong==null)
			return false;
		if(st.kv()!=null)
			st.kv().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		m_bLongProcessed = true;
		m_ksShort = null;
		m_ksLong.run();
		return true;
	}
/** Возвращает true, если ei содержит сведения о поле редактирования*/	
	final boolean isEditor(EditorInfo ei)
	{
		return ei.inputType!=0;
	}
	KeySet getKey(KeyEvent evt, boolean bLong,boolean bEditor)
	{
		int code = evt.getKeyCode();
		char keychar = evt.getDisplayLabel(); 
		for(Iterator <KeySet> it = m_arKeys.iterator();it.hasNext();)
		{
			KeySet ks = it.next();
			if(	ks.keycode!=code||
				ks.keychar!=keychar||
				!ks.checkEdit(bEditor)||
				bLong!=ks.isLong()
				)
				continue;
			return ks;
		}
		return null;
	}
	void loadKeys()
	{
		if(!m_arKeys.isEmpty())
			m_arKeys.clear();
		Cursor c = st.stor().getKeysCursor();
		if(c==null)
			return;
		do
		{
			KeySet ks = Stor.readKey(c);
			if(ks!=null)
				m_arKeys.add(ks);
		}while(c.moveToNext()); 
	}
	Timer m_timer;
	ArrayList<KeySet> m_arKeys = new ArrayList<KeySet>();
	KeySet m_ksShort = null;
	KeySet m_ksLong = null;
	boolean m_bLongProcessed = false;
	boolean m_bKeyEmulation = false;
	View m_v;
}
