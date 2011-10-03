package com.jbak.JbakKeyboard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jbak.JbakKeyboard.st.SyncAsycOper;
import com.jbak.JbakKeyboard.st.UniObserver;


public class AppsList
{
    public AppsList(Activity act)
    {
    	m_act = act;
        m_adapter = new BaseAdapter()
		{
			@Override
			public View getView(int position, View v, ViewGroup parent)
			{
				if(v==null)
				{
					v = m_act.getLayoutInflater().inflate(R.layout.app_list_entry, null);
				}
				MenuEntry me = m_arItems.get(position);
				((ImageView)v.findViewById(R.id.app_icon)).setImageDrawable(me.icon);
				((TextView)v.findViewById(R.id.app_title)).setText(me.text);
				v.setTag(me);
				return v;
			}
			@Override
			public long getItemId(int position)
			{
				return 0;
			}
			@Override
			public Object getItem(int position)
			{
				return null;
			}
			@Override
			public int getCount()
			{
				return m_arItems.size();
			}
		};
        m_getApps.startAsync();
    }
    SyncAsycOper m_getApps = new st.SyncAsycOper(null)
	{
		@Override
		void makeOper(UniObserver obs)
		{
	        PackageManager pm = st.c().getPackageManager();
	        Intent in = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
	        List<ResolveInfo> AppList = pm.queryIntentActivities(in, 0);
	        Collections.sort(AppList, new ResolveInfo.DisplayNameComparator(pm));
	        Iterator<ResolveInfo> iter = AppList.iterator();
	        while(iter.hasNext())
	        {
	            ResolveInfo info = iter.next();
	            try{
	                ComponentName cm = new ComponentName(
	                        info.activityInfo.applicationInfo.packageName,
	                        info.activityInfo.name);
	                String lab = info.loadLabel(pm).toString();
		            MenuEntry me = new MenuEntry(lab);
	                me.icon = info.activityInfo.loadIcon(pm);
	                me.cn = cm;
	                m_arItems.add(me);
	            }
	            catch (Throwable e) {
	                st.logEx(e);
	            }
	        }

		}
	};
    static MenuEntry getMenuEntryForComponent(ComponentName cn,PackageManager pm)
    {
    	try
		{
			ActivityInfo ai = pm.getActivityInfo(cn, 0);
			MenuEntry me = new MenuEntry(ai.loadLabel(pm).toString());
			me.icon = ai.loadIcon(pm);
			me.cn = cn;
			return me;
		} catch (Throwable e)
		{
			st.logEx(e);
		}
		return null;
    }
    void show(st.UniObserver observer)
    {
    	final st.UniObserver obs = observer;
        m_MainView = (ListView)m_act.getLayoutInflater().inflate(R.layout.app_list, null);
    	m_MainView.setAdapter(m_adapter);
    	final AlertDialog ad = Dlg.CustomDialog(st.c(), m_MainView, null, null, null, null);
    	m_MainView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				ad.dismiss();
				if(obs!=null)
					obs.OnObserver(arg1.getTag(), obs.m_param2);
			}
		});
		
    }
    
/** Класс, хранящий информацию об элементе меню */  
    public static class MenuEntry
    {
/**
 * Конструктор      
 * @param t Текст элемента
 * @param i id элемента. Может быть одной из констант ID_ . Если  = ID_DELIMETER - выводит ненажимаемый элемент с другим оформлением
 */
        public MenuEntry(String t)
        {
            text = t;
        }
/** Текст элемента */       
        String text;
/** Компонент программы  */
        ComponentName cn;
/** Иконка программы*/
        Drawable icon;
    }
    /** Окно меню */    
    ListView m_MainView;
/** Массив элементов */ 
    BaseAdapter m_adapter; 
    ArrayList<MenuEntry> m_arItems = new ArrayList<AppsList.MenuEntry>();
    Activity m_act;
}
