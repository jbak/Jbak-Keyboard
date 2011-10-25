package com.jbak.JbakKeyboard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.jbak.JbakKeyboard.st.UniObserver;

public class KeyPressProcessor
{
	static KeyPressProcessor inst;
	public KeyPressProcessor()
	{
		inst = this;
//		m_console = KbdConsole.getConsole();
		m_vibro = (Vibrator)ServiceJbKbd.inst.getSystemService(Service.VIBRATOR_SERVICE);
		loadKeys();
	}
	void destroy()
	{
		inst = null;
	}
	boolean onKeyDown(KeyEvent evt,EditorInfo ei)
	{
		int rep = evt.getRepeatCount(); 
		st.log("keyDown "+KeySet.getKeyName(evt.getKeyCode())+" "+rep);
		if(rep==0)
		{
			m_bLongProcessed = false;
			m_ksShort = getKey(evt, false, isEditor(ei));
			m_ksLong = getKey(evt, true, isEditor(ei));
			if(m_ksShort==null&&m_ksLong==null)
			{
				return false;
			}
			if(m_ksLong!=null)
			{
				//evt.startTracking();
			}
			return true;
		}
		else if(rep==1)
		{
			return onLongPress();
		}
		else if(rep>1&&m_ksLong!=null)
			return true;
		return false;
	}
	boolean onLongPress()
	{
		if(m_ksLong!=null)
		{
			try{
			m_vibro.vibrate(25);
			m_ksLong.run();
			}catch (Throwable e) {
				st.logEx(e);
			}
			m_bLongProcessed = true;
			st.log("m_bLongProcessed = true");
		}
		return m_ksLong!=null;
	}
	/** */
	boolean onKeyUp(KeyEvent evt,EditorInfo ei)
	{
		st.log("keyUp "+KeySet.getKeyName(evt.getKeyCode()));
		if(m_ksShort!=null)
		{
			m_ksShort.run();
			m_ksShort = null;
			st.log("true");
			return true;
		}
		if(m_ksLong!=null)
		{
			if(m_bLongProcessed)
			{
				return true;
			}
			else
			{
				// Ждали длинного нажатия, но не дождались, возвращаем короткое
				// Если нажата back - то проверяем наличие открытой софт-клавиатуры
				if(evt.getKeyCode()==KeyEvent.KEYCODE_BACK&&
						ServiceJbKbd.inst!=null&&
						ServiceJbKbd.inst.handleBackPress())
				{
					return true;
				}
				sendKeyEvent(evt);
				return true;
			}
		}
		return false;
	}
	boolean inputFromCmdLine(KeyEvent evt)
	{
		if(m_console==null||!m_console.runKeyCode(evt.getKeyCode()))
			return false;
		mBlockedKey = evt.getKeyCode();
		return true;
	}
/** Этот мегакосстыль разруливает ситуацию с пропаданием фокуса окна в некоторых случаях<br>
 * Создаём временное окно нулевого размера с фокусом ввода, вызываем и сразу же убираем. Пиздец, но работает */	
	void addNullView(final KeyEvent ke)
	{
		final WindowManager wm = (WindowManager)ServiceJbKbd.inst.getSystemService(Service.WINDOW_SERVICE);
		WindowManager.LayoutParams lp = 
			new WindowManager.LayoutParams(0, 0, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, 0, PixelFormat.TRANSLUCENT);
		final View v = new View(ServiceJbKbd.inst);
		wm.addView(v,lp);
		st.UniObserver obs = new st.UniObserver()
		{
			@Override
			int OnObserver(Object param1, Object param2)
			{
				m_RunKey = ke.getKeyCode();
				wm.removeView(v);
				return 0;
			}
		};
		st.SyncAsycOper ao = new st.SyncAsycOper(obs)
		{
			@Override
			void makeOper(UniObserver obs)
			{
				try
				{
					Thread.currentThread().join(30);
				} catch (Throwable e)
				{
					st.logEx(e);
				}
			}
		};
		ao.startAsync();
	}
	void onStartInput()
	{
		if(m_RunKey!=0)
		{
			ServiceJbKbd.inst.sendDownUpKeyEvents(m_RunKey);
			m_RunKey = 0;
		}
	}
	int m_RunKey = 0;
/** Отправляет нажатие клавиши системе */	
	void sendKeyEvent(KeyEvent evt)
	{
		addNullView(evt);
/*		final int keyEventCode = evt.getKeyCode();
		final int keyEventScanCode = evt.getScanCode();
		st.SyncAsycOper op = new st.SyncAsycOper(null)
		{
			@Override
			void makeOper(UniObserver obs)
			{
		        InputConnection ic = ServiceJbKbd.inst.getCurrentInputConnection();
		        if (ic == null) return;
		        long eventTime = SystemClock.uptimeMillis();
		        boolean bSendDown = ic.sendKeyEvent(new KeyEvent(eventTime, eventTime,
		                KeyEvent.ACTION_DOWN, keyEventCode, 0, 0, 0, keyEventScanCode,
		                KeyEvent.FLAG_VIRTUAL_HARD_KEY|KeyEvent.FLAG_KEEP_TOUCH_MODE));
		        boolean bSendUp = ic.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis()+1, eventTime,
		                KeyEvent.ACTION_UP, keyEventCode, 0, 0, 0, keyEventScanCode,
		                KeyEvent.FLAG_VIRTUAL_HARD_KEY|KeyEvent.FLAG_KEEP_TOUCH_MODE));
		        if(!bSendDown)
		        {
		        	st.log("no down");
		        }
		        if(!bSendUp)
		        {
		        	st.log("no down");
		        }
			}
		};
		op.startAsync();
*/	}
	
/** Обработчик удержания клавиши */	
/*	
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
		m_ksLong = null;
		return true;
	}
*/	
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
	int mBlockedKey = 0;
	KbdConsole m_console;
	Vibrator m_vibro;
}
