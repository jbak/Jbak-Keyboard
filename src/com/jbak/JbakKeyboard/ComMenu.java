package com.jbak.JbakKeyboard;

import java.util.ArrayList;
import java.util.Iterator;

import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.StateListDrawable;
import android.location.Address;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/** Универсальное меню. Используется как для выпадающего, так и для контекстного меню */
public class ComMenu
{
    static ComMenu inst; 
/** Класс, хранящий информацию об элементе меню */  
    public static class MenuEntry
    {
/**
 * Конструктор      
 * @param t Текст элемента
 * @param i id элемента. Может быть одной из констант ID_ . Если  = ID_DELIMETER - выводит ненажимаемый элемент с другим оформлением
 */
        public MenuEntry(String t,int i)
        {
            text = t;
            id = i;
        }
/** Текст элемента */       
        String text;
/** id элемента */      
        int id;
    }
/** Конструктор 
 * @param act Контекст */   
    ComMenu()
    {
        inst = this;
        m_MainView = ServiceJbKbd.inst.getLayoutInflater().inflate(R.layout.com_menu, null);
    }
/** Устанавливает фон ненажатой кнопки, соответствующий текущему оформлению клавиатуры*/    
    void setButtonKeyboardBackground(View btn)
    {
        if(st.kv().m_KeyBackDrw!=null)
        {
            btn.setBackgroundDrawable(st.kv().m_drwKeyBack);
            btn.setOnTouchListener(m_btnListener);
        }
    }
/** Создаёт новую кнопку элемента меню */   
    View newView(MenuEntry ent)
    {
        Button btn = new Button(st.c());
        setButtonKeyboardBackground(btn);
        btn.setTextColor(st.kv().m_tpMainKey.getColor());
        if(st.has(m_state, STAT_TEMPLATES))
        {
            btn.setLongClickable(true);
            btn.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            btn.setLongClickable(true);
            btn.setOnLongClickListener(m_longListener);
        }
        btn.setDuplicateParentStateEnabled(false);
        btn.setTag(ent);
        btn.setText(ent.text);
        btn.setOnClickListener(m_listener);
        return btn;
    }
/** Обработчик нажатия кнопки меню */  
    st.UniObserver m_lvObserver = new st.UniObserver()
    {
        @Override
        int OnObserver(Object param1, Object param2)
        {
            if(m_MenuObserver==null)return 0;
            m_MenuObserver.m_param1 = param1;
            m_MenuObserver.Observ();
            return 0;
        }
    };
/** Обработчик длинного нажатия кнопки меню */
    OnLongClickListener m_longListener = new OnLongClickListener()
    {
        @Override
        public boolean onLongClick(View v)
        {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            close();
            if(m_MenuObserver!=null)
            {
                MenuEntry me = (MenuEntry)v.getTag();
                m_MenuObserver.OnObserver(new Integer(me.id),new Boolean(true));
            }
            return true;
        }
    };
/** Обработчик событий сенсора. Нужен для отрисовки кнопок меню */    
    OnTouchListener m_btnListener = new OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            int act =event.getAction(); 
            if(act==MotionEvent.ACTION_DOWN)
            {
                v.setBackgroundDrawable(st.kv().m_drwKeyPress);
            }
            if(act==MotionEvent.ACTION_UP||act==MotionEvent.ACTION_CANCEL)
            {
                v.setBackgroundDrawable(st.kv().m_drwKeyBack);
            }
            return false;
        }
    };
/** Сторонний обработчик, который был передан в функции {@link #show(com.jbak.JbakTaskMan.st.UniObserver)}*/    
    st.UniObserver m_MenuObserver;
/** Добавляет в меню элемент с текстом text и идентификатором id */ 
    void add(String text,int id)
    {
        m_arItems.add(new MenuEntry(text, id));
    }
/** Добавляет в меню элемент со id строки tid, которая берётся из ресурсов и идентификатором id */  
    void add(int tid,int id)
    {
        add(st.c().getString(tid),id);
    }
    void close()
    {
        inst = null;
        if(ServiceJbKbd.inst!=null)
        {
            st.setQwertyKeyboard();
            ServiceJbKbd.inst.setInputView(st.kv());
        }
    }
/** Обработчик короткого нажатия кнопок меню */    
    View.OnClickListener m_listener = new View.OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            close();
            switch (v.getId())
            {
                case R.id.but_new_template_folder: st.kbdCommand(st.CMD_TPL_NEW_FOLDER); return;
                case R.id.but_new_template: st.kbdCommand(st.CMD_TPL_EDITOR);return;
                case R.id.close: return;
            }
            MenuEntry me = (MenuEntry)v.getTag();
            if(m_MenuObserver!=null)
            {
                m_MenuObserver.OnObserver(new Integer(me.id), new Boolean(false));
            }
        }
    };
/** Показывает меню
 * @param observer Обработчик нажатия */    
    void show(st.UniObserver observer)
    {
        m_MenuObserver = observer;
        LinearLayout ll = (LinearLayout)m_MainView.findViewById(R.id.com_menu_container);
        Iterator<MenuEntry> it = m_arItems.iterator();
        while(it.hasNext())
        {
            ll.addView(newView(it.next()));
        }
        m_MainView.setBackgroundDrawable(st.kv().getBackground());
        View bClose = m_MainView.findViewById(R.id.close);
        if(bClose!=null)
        {
            bClose.setOnClickListener(m_listener);
        }
        LinearLayout bl = (LinearLayout)m_MainView.findViewById(R.id.com_menu_buttons);
        int cnt = bl.getChildCount();
        for(int i=cnt-1;i>=0;i--)
        {
            View v = bl.getChildAt(i);
            if(st.has(m_state, STAT_TEMPLATES))
            {
                v.setOnClickListener(m_listener);
            }
            else 
            {
                if(v.getId()==R.id.close)
                {
                    v.setOnClickListener(m_listener);
                }
                else
                {
                    bl.removeViewAt(i);
                }
            }
        }
        ServiceJbKbd.inst.setInputView(m_MainView);
        ViewGroup.LayoutParams lp = m_MainView.getLayoutParams();
        lp.width= st.kv().getWidth();
        lp.height = st.kv().getHeight();
        m_MainView.setLayoutParams(lp);
    }
/** Функция создаёт меню для мультибуфера обмена */    
    static boolean showClipboard()
    {
        ComMenu menu = new ComMenu();
        Cursor c = st.stor().getClipboardCursor();
        if(c==null)
            return false;
        int pos = 0;
        do
        {
            String s = c.getString(0);
            if(s.length()>50)
                menu.add(s.substring(0, 50),pos);
            else
                menu.add(s,pos);
            ++pos;
        }while(c.moveToPrevious());
        c.close();
        st.UniObserver obs = new st.UniObserver()
        {
            @Override
            int OnObserver(Object param1, Object param2)
            {
                int pos = ((Integer)param1).intValue();
                Cursor c = st.stor().getClipboardCursor();
                if(c==null)
                    return 0;
                c.move(0-pos);
                String cp = c.getString(0);
                ServiceJbKbd.inst.onText(cp);
                return 0;
            }
        };
        menu.show(obs);
        return true;
    }
/** Элемент-разделитель. Не нажимается, имеет оформление, отличное от основного списка*/
    public static final int ID_DELIMETER            =-2;
/** Окно меню */    
    View m_MainView;
    ArrayList<MenuEntry> m_arItems = new ArrayList<ComMenu.MenuEntry>();
    int m_state = 0;
    public static final int STAT_TEMPLATES = 0x000001;
}