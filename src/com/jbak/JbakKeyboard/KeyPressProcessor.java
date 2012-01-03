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
/** Обработчик настроенных пользователем кнопок */
public class KeyPressProcessor
{
    static KeyPressProcessor inst;
/** Список настроенных пользователем клавиш*/    
    ArrayList<KeySet> m_arKeys = new ArrayList<KeySet>();
/** Текущее событие для короткого нажатия клавиши*/    
    KeySet m_ksShort = null;
/** Текущее событие для удержания клавиши*/    
    KeySet m_ksLong = null;
/** true - сработало длинное нажатие, false - не сработало (проеверяется в {@link #onKeyUp(KeyEvent, EditorInfo)}*/    
    boolean m_bLongProcessed = false;
/** Код клавиши, который возвращается в систему. Обработчики нажатий не обрабатывают клавишу с этим кодом*/    
    int mBlockedKey = 0;
/** root-консоль для отправки клавиш системе*/    
    KbdConsole m_console;
/** Вибратор для тактильной отдачи при удержании клавиш */    
    Vibrator m_vibro;
/** Конструктор. Пытается получить доступ к консоли root*/    
    public KeyPressProcessor()
    {
        inst = this;
//        m_console = KbdConsole.getConsole();
        m_vibro = (Vibrator)ServiceJbKbd.inst.getSystemService(Service.VIBRATOR_SERVICE);
        loadKeys();
    }
/** Деструктор */   
    void destroy()
    {
    	m_console = null;
        inst = null;
    }
/** Обработчик нажатия кнопки
*@param evt Событие кнопки 
*@param ei Информация о текущем окне 
*@return true - событие обработано, false - не обработано*/
    boolean onKeyDown(KeyEvent evt,EditorInfo ei)
    {
        if(evt.getKeyCode()==mBlockedKey)
        {
            return false;
        }
        int rep = evt.getRepeatCount(); 
        st.log("keyDown "+KeySet.getKeyName(evt.getKeyCode())+" flg:"+evt.getFlags());
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
/** Обработчик длинного нажатия кнопки
*@return true - событие обработано, false - не обработано*/
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
/** Обработчик отпускания кнопки
*@param evt Событие кнопки 
*@param ei Информация о текущем окне 
*@return true - событие обработано, false - не обработано*/
    boolean onKeyUp(KeyEvent evt,EditorInfo ei)
    {
        st.log("keyUp "+KeySet.getKeyName(evt.getKeyCode())+" flg:"+evt.getFlags());
        if(evt.getKeyCode()==mBlockedKey)
        {
            mBlockedKey = 0;
            return false;
        }
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
/** Ввод в систему нажатия и отпускания клавиши через root-консоль
*@param evt Событие, содержащее код кнопки, который нужно отправить 
*@return true - отправка удалась, false - не удалась (например, если нет прав root)*/
    boolean inputFromCmdLine(KeyEvent evt)
    {
        mBlockedKey = evt.getKeyCode();
        if(m_console==null||!m_console.runKeyCode(evt.getKeyCode()))
            return false;
        return true;
    }
/** Этот мегакосстыль разруливает ситуацию с пропаданием фокуса окна в некоторых случаях<br>
 * Создаём временное окно нулевого размера с фокусом ввода, вызываем и сразу же убираем. Пиздец, но работает 
 * И нихера не работает в CoolReader */ 
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
                    Thread.currentThread().join(100);
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
//          final int rk = m_RunKey;
//          m_RunKey = 0;
//          st.UniObserver obs = new st.UniObserver()
//          {
//              @Override
//              int OnObserver(Object param1, Object param2)
//              {
//                  ServiceJbKbd.inst.sendDownUpKeyEvents(rk);
//                  return 0;
//              }
//          };
//          st.SyncAsycOper ao = new st.SyncAsycOper(obs)
//          {
//              @Override
//              void makeOper(UniObserver obs)
//              {
//                  try
//                  {
//                      Thread.currentThread().join(50);
//                  } catch (Throwable e)
//                  {
//                      st.logEx(e);
//                  }
//              }
//          };
//          ao.startAsync();
            st.log("send evt "+KeySet.getKeyName(m_RunKey));
            ServiceJbKbd.inst.sendDownUpKeyEvents(m_RunKey);
            m_RunKey = 0;
        }
    }
    int m_RunKey = 0;
/** Отправляет нажатие клавиши системе */   
    void sendKeyEvent(KeyEvent evt)
    {
//        if (ic == null) return;
//        long eventTime = SystemClock.uptimeMillis();
//        ic.sendKeyEvent(new KeyEvent(eventTime, eventTime,
//                KeyEvent.ACTION_DOWN, evt.getKeyCode(), 0, 0, 0, 0,
//                KeyEvent.FLAG_FROM_SYSTEM|KeyEvent.FLAG_SOFT_KEYBOARD));
//        ic.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime,
//                KeyEvent.ACTION_UP, evt.getKeyCode(), 0, 0, 0, 0,
//                KeyEvent.FLAG_FROM_SYSTEM|KeyEvent.FLAG_SOFT_KEYBOARD));

//      ServiceJbKbd.inst.sendDownUpKeyEvents(evt.getKeyCode());
        //addNullView(evt);
        if(!inputFromCmdLine(evt))
            ServiceJbKbd.inst.sendDownUpKeyEvents(evt.getKeyCode());
    }
/** Возвращает true, если ei содержит сведения о поле редактирования*/  
    final boolean isEditor(EditorInfo ei)
    {
        return ei.inputType!=0;
    }
/** Возвращает клавишу, которая должна сработать для события evt 
*@param evt Клавиатурное событие, содержит код интересующей нас клавиши
*@param bLong true - длинное нажатие, false - короткое
*@param bEditor true - в фокусе реальное поле ввода
*@return Возвращает настроенную кнопку или null */
    KeySet getKey(KeyEvent evt, boolean bLong,boolean bEditor)
    {
        int code = evt.getKeyCode();
        char keychar = evt.getDisplayLabel(); 
        for(Iterator <KeySet> it = m_arKeys.iterator();it.hasNext();)
        {
            KeySet ks = it.next();
            if( ks.keycode!=code||
                ks.keychar!=keychar||
                !ks.checkEdit(bEditor)||
                bLong!=ks.isLong()
                )
                continue;
            return ks;
        }
        return null;
    }
/** Загружает настроенные пользователем клавиши из БД*/
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
}
