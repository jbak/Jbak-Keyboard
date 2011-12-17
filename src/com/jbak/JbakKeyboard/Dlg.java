package com.jbak.JbakKeyboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.ListAdapter;

import com.jbak.JbakKeyboard.st.UniObserver;

/** Класс предоставляет функции для вывода различных диалогов */
public class Dlg
{
    /** Обработчик нажатия кнопок в диалоге  */ 
    static class OnButtonListener implements DialogInterface.OnClickListener
    {
/** Конструктор. Получает обработчик нажатия 
 * @param call Обработчик, вызываемый при нажатии кнопки в диалоге. Первый параметр - код нажатой кнопки в виде Integer*/       
        public OnButtonListener(UniObserver call)
        {
            callback = call;
        }
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            if(callback!=null)
                callback.OnObserver(new Integer(which),callback.m_param2);
        }
        UniObserver callback;
    }
    /** Пользовательский диалог, содержащий заданное пользователем окно<br>
     * По окончании вызова - вызовет callback.OnObserver(Integer buttonCode, callback.m_param2)
     * @param c Контекст
     * @param customView Пользовательское окно 
     * @param but1 Текст кнопки BUTTON_POSITIVE или null, если кнопка не нужна
     * @param but2 Текст кнопки BUTTON_NEGATIVE или null, если кнопка не нужна 
     * @param but3 Текст кнопки BUTTON_NEUTRAL или null, если кнопка не нужна
     * @param callback Обработчик нажатия кнопок. Конструкция вызова - callback.OnObserver(Integer buttonCode, callback.m_param2)
     * @return Возвращает созданный диалог*/
        public static AlertDialog CustomDialog(Context c,View customView,String but1,String but2,String but3,UniObserver callback)
        {
            AlertDialog.Builder bd = new AlertDialog.Builder(c);
            AlertDialog dlg = bd.create();
            OnButtonListener cl = new OnButtonListener(callback);
            dlg.setView(customView);
            if(but1!=null) dlg.setButton(AlertDialog.BUTTON_POSITIVE, but1,cl);
            if(but2!=null) dlg.setButton(AlertDialog.BUTTON_NEGATIVE, but2,cl);
            if(but3!=null) dlg.setButton(AlertDialog.BUTTON_NEUTRAL, but3,cl);
            dlg.show();
            return dlg;
        }
////        
        public static AlertDialog CustomMenu(Context c,ListAdapter adapter,String title,UniObserver callback)
        {
            AlertDialog.Builder bd = new AlertDialog.Builder(c);
            final UniObserver obs = callback;
            bd.setAdapter(adapter, new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    obs.OnObserver(new Integer(which), obs);
                }
            });
            AlertDialog dlg = bd.create();
            OnButtonListener cl = new OnButtonListener(callback);
            if(title!=null)
                dlg.setTitle(title);
            dlg.show();
            return dlg;
            
        }
/** */      
        public static AlertDialog yesNoDialog(Context c,String query,UniObserver callback)
        {
            AlertDialog.Builder bd = new AlertDialog.Builder(c);
            OnButtonListener cl = new OnButtonListener(callback);
            bd.setPositiveButton(R.string.yes, cl);
            bd.setNegativeButton(R.string.no, cl);
            bd.setMessage(query);
            AlertDialog dlg = bd.create();
            dlg.show();
            return dlg;
        }
        public static abstract class RunOnYes
        {
            public RunOnYes(Context c,String query)
            {
                Dlg.yesNoDialog(c, query, mkObserver());
            }
            st.UniObserver mkObserver()
            {
                return new UniObserver()
                {
                    @Override
                    int OnObserver(Object param1, Object param2)
                    {
                        if(((Integer)param1).intValue()==AlertDialog.BUTTON_POSITIVE)
                            run();
                        return 0;
                    }
                };
            }
            public abstract void run();
        }
}
