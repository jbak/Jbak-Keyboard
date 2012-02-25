package com.jbak.JbakKeyboard;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.jbak.ctrl.GlobDialog;

/** Универсальное меню. Используется как для выпадающего, так и для контекстного меню */
public class ComMenu
{
    /** Элемент-разделитель. Не нажимается, имеет оформление, отличное от основного списка*/
    public static final int ID_DELIMETER            =-2;
/** Окно меню */    
    View m_MainView;
    ArrayList<MenuEntry> m_arItems = new ArrayList<MenuEntry>();
    int m_state = 0;
    public static final int STAT_TEMPLATES = 0x000001;
    public static final int STAT_CLIPBOARD = 0x000002;
    static ComMenu inst; 
    protected static final int[] PRESSED_STATE_SET = {android.R.attr.state_pressed};
    protected static final int[] EMPTY_STATE_SET = {};
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
//    void setButtonKeyboardBackground(View btn)
//    {
//        if(st.kv().m_KeyBackDrw!=null)
//        {
//            btn.setBackgroundDrawable(st.kv().m_drwKeyBack);
////            btn.setOnTouchListener(m_btnListener);
//        }
//    }
/** Создаёт новую кнопку элемента меню */   
    View newView(MenuEntry ent)
    {
        Button btn = new Button(st.c());
        if(st.kv().isDefaultDesign())
        {
            btn.setBackgroundDrawable(st.kv().m_drwKeyBack.mutate());
        }
        else
        {
            btn.setBackgroundDrawable(st.kv().m_curDesign.m_keyBackground.clone().getStateDrawable());
        }
//        setButtonKeyboardBackground(btn);
        btn.setHeight(st.kv().m_KeyHeight);
        btn.setTextColor(st.paint().mainColor);
        if(st.has(m_state, STAT_TEMPLATES)||st.has(m_state, STAT_CLIPBOARD))
        {
            btn.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            btn.setLongClickable(true);
            btn.setOnLongClickListener(m_longListener);
        }
        btn.setMaxLines(1);
        btn.setEllipsize(TruncateAt.MARQUEE);
        btn.setTag(ent);
        btn.setText(ent.text);
        btn.setOnClickListener(m_listener);
        return btn;
    }
/** Обработчик нажатия кнопки меню */  
    st.UniObserver m_lvObserver = new st.UniObserver()
    {
        @Override
        public int OnObserver(Object param1, Object param2)
        {
            if(m_MenuObserver==null)return 0;
            m_MenuObserver.m_param1 = param1;
            m_MenuObserver.Observ();
            return 0;
        }
    };
    int m_longClicked=-1;
/** Обработчик длинного нажатия элемента меню */
    OnLongClickListener m_longListener = new OnLongClickListener()
    {
        @Override
        public boolean onLongClick(View v)
        {
            VibroThread vt = VibroThread.getInstance(st.c());
            vt.runVibro(vt.m_longVibro);
            if(!st.has(m_state, STAT_CLIPBOARD))
                close();
            MenuEntry me = (MenuEntry)v.getTag();
            m_longClicked = me.id;
            if(m_MenuObserver!=null)
            {
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
            try{
                st.kv().setKeyboard(st.curKbd());
                ServiceJbKbd.inst.setInputView(st.kv());
            }
            catch (Throwable e) {
            }
        }
    }
/** Обработчик короткого нажатия кнопок меню */    
    View.OnClickListener m_listener = new View.OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            VibroThread vt = VibroThread.getInstance(st.c());
            vt.runVibro(vt.m_shortVibro);
            close();
            switch (v.getId())
            {
                case R.id.but_new_template_folder: st.kbdCommand(st.CMD_TPL_NEW_FOLDER); return;
                case R.id.but_new_template: st.kbdCommand(st.CMD_TPL_EDITOR);return;
                case R.id.clear: 
                    GlobDialog gd = new GlobDialog(st.c());
                    gd.set(R.string.clipboard_clear, R.string.yes, R.string.no);
                    gd.setObserver(new st.UniObserver()
                    {
                        @Override
                        public int OnObserver(Object param1, Object param2)
                        {
                            if(((Integer)param1).intValue()==AlertDialog.BUTTON_POSITIVE)
                            {
                                st.stor().clearClipboard();
                            }
                            return 0;
                        }
                    });
                    gd.showAlert();
                    return;
                case R.id.close: return;
            }
            MenuEntry me = (MenuEntry)v.getTag();
            if(m_MenuObserver!=null)
            {
                m_MenuObserver.OnObserver(new Integer(me.id), new Boolean(false));
            }
        }
    };
    Adapt m_adapter;
    AdapterView.OnItemLongClickListener m_itemLongClickListener = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View clickedView, int pos, long id)
        {
            return true;
        }
        
    };
/** Показывает меню
 * @param observer Обработчик нажатия */    
    void show(st.UniObserver observer)
    {
        m_MenuObserver = observer;
        ListView lv = (ListView)m_MainView.findViewById(R.id.com_menu_container);
        m_adapter = new Adapt(st.c(), this);
        lv.setAdapter(m_adapter);
//        LinearLayout ll = (LinearLayout)m_MainView.findViewById(R.id.com_menu_container);
//        for(MenuEntry me:m_arItems)
//        {
//            ll.addView(newView(me));
//        }
        lv.setOnItemLongClickListener(m_itemLongClickListener);
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
            int id = v.getId();
            boolean bUse = false;
            if(id==R.id.clear)
               bUse = st.has(m_state, STAT_CLIPBOARD);
            else if(v.getId()==R.id.close)
                bUse = true;
            else
                bUse = st.has(m_state, STAT_TEMPLATES); 
            
            if(bUse)
                v.setOnClickListener(m_listener);
            else
                v.setVisibility(View.GONE);
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
        Cursor c = st.stor().getClipboardCursor();
        if(c==null)
            return false;
        final ComMenu menu = new ComMenu();
        menu.m_state = STAT_CLIPBOARD;
        int pos = 0;
        do
        {
            String s = c.getString(0);
            s = s.trim();
            if(s.length()>50)
                s = s.substring(0, 50)+"...";
            s.replace('\n', ' ');
            menu.add(s,pos);
            ++pos;
        }while(c.moveToPrevious());
        c.close();
        st.UniObserver obs = new st.UniObserver()
        {
            @Override
            public int OnObserver(Object param1, Object param2)
            {
                int id = ((Integer)param1).intValue();
                int pos = -1;
                for(int i=menu.m_arItems.size()-1;i>=0;i--)
                {
                    if(menu.m_arItems.get(i).id==id)
                    {
                        pos = i;
                        break;
                    }
                }
                if(pos<0)
                    return 0;
                if(((Boolean)param2).booleanValue())
                {
                    Intent in = new Intent(ServiceJbKbd.inst,TplEditorActivity.class)
                        .putExtra(TplEditorActivity.EXTRA_CLIPBOARD_ENTRY, pos)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ServiceJbKbd.inst.startActivity(in);
                    return 0;
                }
                Cursor c = st.stor().getClipboardCursor();
                if(c==null)
                    return 0;
                c.move(0-id);
                String cp = c.getString(0);
                ServiceJbKbd.inst.onText(cp);
                if(ClipbrdService.inst!=null)
                    ClipbrdService.inst.checkString(cp);
                return 0;
            }
        };
        menu.show(obs);
        return true;
    }
    static class Adapt extends ArrayAdapter<MenuEntry>
    {
        ComMenu m_menu; 
        public Adapt(Context context,ComMenu menu)
        {
            super(context,0);
            m_menu = menu;
        }
        @Override
        public int getCount() 
        {
            return m_menu.m_arItems.size();
        };
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            MenuEntry me = m_menu.m_arItems.get(position);
            if(convertView!=null)
            {
                Button b = (Button)convertView;
                b.setTag(me);
                b.setId(me.id);
                b.setText(me.text);
            }
            else
            {
                convertView = m_menu.newView(me);
            }
            return convertView;
        }
    }
    void removeLastLongClicked()
    {
        for(int i=m_arItems.size()-1;i>=0;i--)
        {
            MenuEntry me = m_arItems.get(i);
            if(me.id==m_longClicked)
            {
                m_arItems.remove(i);
                break;
            }
        }
        if(m_arItems.size()==0)
            close();
        else
            m_adapter.notifyDataSetChanged();
    }
}