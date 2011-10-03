package com.jbak.JbakKeyboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

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
}
