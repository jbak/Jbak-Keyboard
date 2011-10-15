package com.jbak.JbakKeyboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.ListAdapter;

import com.jbak.JbakKeyboard.st.UniObserver;

/** ����� ������������� ������� ��� ������ ��������� �������� */
public class Dlg
{
	/** ���������� ������� ������ � �������  */ 
    static class OnButtonListener implements DialogInterface.OnClickListener
    {
/** �����������. �������� ���������� ������� 
 * @param call ����������, ���������� ��� ������� ������ � �������. ������ �������� - ��� ������� ������ � ���� Integer*/       
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
	/** ���������������� ������, ���������� �������� ������������� ����<br>
	 * �� ��������� ������ - ������� callback.OnObserver(Integer buttonCode, callback.m_param2)
	 * @param c ��������
	 * @param customView ���������������� ���� 
	 * @param but1 ����� ������ BUTTON_POSITIVE ��� null, ���� ������ �� �����
	 * @param but2 ����� ������ BUTTON_NEGATIVE ��� null, ���� ������ �� ����� 
	 * @param but3 ����� ������ BUTTON_NEUTRAL ��� null, ���� ������ �� �����
	 * @param callback ���������� ������� ������. ����������� ������ - callback.OnObserver(Integer buttonCode, callback.m_param2)
	 * @return ���������� ��������� ������*/
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
