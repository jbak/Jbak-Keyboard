package com.jbak.JbakKeyboard;

import java.util.Locale;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
/** ����� �������� �������� ����������� ���������� */
public class st extends IKeyboard
{
//--------------------------------------------------------------------------
    /** ������������� ��������. �������� 2 ��������� m_param1 � m_param2, ������� ���������� � �������� � ����������� �� ���������*/
    public static abstract class UniObserver
    {
    /** ����������� � ����� ����������� */
        public UniObserver(Object param1,Object param2)
        {
            m_param1 = param1;
            m_param2 = param2;
        }
    /** ������ �����������. ��� ��������� - null*/
        public UniObserver()
        {
        }
    /** ����� ������� {@link #OnObserver(Object, Object)} � �������� �����������*/
        public int Observ(){return OnObserver(m_param1, m_param2);}
    /** �������� ������� ����������� */ 
        abstract int OnObserver(Object param1,Object param2);
    /** ���������������� �������� 1 */  
        Object m_param1;
    /** ���������������� �������� 2 */  
        Object m_param2;
    }
    
	/** ���������� ������ (val&flag)>0*/        
    public static final boolean has(int val,int flag)
    {
        return (val&flag)>0;
    }
/** ������� ��� flag �� �������� val, ���� ��� ���������*/      
    public static final int rem(int val,int flag)
    {
        if(has(val,flag))
            return val^flag;
        return val;
    }
    public static final void logEx(Throwable e)
    {
        if(e.getMessage()!=null)
            Log.e(TAG, e.getMessage());
        Log.e(TAG, Log.getStackTraceString(e));
    }
/** ���������� ���� ��� ����� � ������ langName, ������� �� {@link Locale#getLanguage()} */    
    public static Keybrd kbdForName(String langName)
    {
    	for(int i=0;i<arKbd.length;i++)
    	{
    		Keybrd l = arKbd[i];
    		if(langName.equals(l.lang.name))
    			return l;
    	}
    	return null;
    }
    static void log(String txt)
    {
    	Log.w(TAG, txt);
    }
/** ��������� ������� ������ qwerty-����������, ���� �������������� ���������� � qwerty */    
    public static void saveCurLang()
    {
    	JbKbd kb = curKbd();
    	if(kb==null)
    		return;
    	Keybrd l = langForId(kb.resId);
    	if(l==null)
    		return;
        SharedPreferences pref =PreferenceManager.getDefaultSharedPreferences(JbKbdView.inst.getContext());
        pref.edit()
        	.putInt(st.PREF_KEY_LAST_LANG, l.lang.lang)
        	.commit();
    }
/** ���������� ������� ������ ��� qwerty-���������� */    
    public static int getCurQwertyRes()
    {
        SharedPreferences pref =pref();
        if(pref==null||!pref.contains(PREF_KEY_LAST_LANG))
        {
        	String lang = Locale.getDefault().getLanguage();
        	Keybrd l = kbdForName(lang);
        	if(l!=null)
        		return l.resId;
        	return defKbd().resId;
        }
        int lang = pref.getInt(PREF_KEY_LAST_LANG, defKbd().lang.lang);
        String ln = arLangs[lang].name;
        int idLang = pref.getInt(PREF_KEY_LANG_KBD+ln, -1);
        if(idLang>=0)return arKbd[idLang].resId;
        for(Keybrd k:arKbd)
        {
        	if(k.lang.lang==lang)
        		return k.resId;
        }
        return defKbd().resId;
    }
/** ���������� ������� ���������� ��� null*/    
    public static JbKbd curKbd()
    {
    	if(JbKbdView.inst==null) return null;
    	return (JbKbd)JbKbdView.inst.getCurKeyboard();
    }
/** ���������� �������� ��������. ���� �������� {@link SetKbdActivity} - �� ���������� ���, ����� - {@link ServiceJbKbd}*/    
    public static Context c()
    {
    	if(KeySetActivity.inst!=null)
    		return KeySetActivity.inst;
    	if(SetKbdActivity.inst!=null)
    		return SetKbdActivity.inst;
    	if(ServiceJbKbd.inst!=null)
    		return ServiceJbKbd.inst;
    	return ClipbrdService.inst;
    }
  //********************************************************************
    /** ����� ��� ������� ����������������� ���� ��������� ��� ����������
     * �������� ��� ����������. �� ��������� ���������� ����������� ���������� */
    public static abstract class SyncAsycOper extends AsyncTask<Void,Void,Void>
    {
    /** �����������
     * @param obs ����������, ������� ���������� �� ���������� */
        public SyncAsycOper(UniObserver obs)
        {
            m_obs = obs;
        }
    /** ��������� �������� �������� {@link #makeOper(UniObserver)}*/
        void startSync()
        {
            makeOper(m_obs);
        }
    /** ���������� �������� �������� {@link #makeOper(UniObserver)}*/
        void startAsync()
        {
            execute();
        }
    /** @hide */
        @Override
        protected void onProgressUpdate(Void... values)
        {
        	if(m_obs!=null)
        		m_obs.Observ();
        }
    /** @hide */
        @Override
        protected Void doInBackground(Void... arg0)
        {
            try{
                makeOper(m_obs);
                publishProgress();
            }
            catch (Exception e) {
            }
            return null;
        }
    /** ����������� ��������  */
        abstract void makeOper(UniObserver obs);
    /** ���������� �������� */  
        UniObserver m_obs;
    }
/** ��������� ���������� �������������� ������ */
    public static void setTextEditKeyboard()
    {
    	JbKbdView.inst.setKeyboard(new JbKbd(st.c(),R.xml.edittext));
    }
/** ��������� ���������� ��������� */
    public static void setSmilesKeyboard()
    {
    	JbKbdView.inst.setKeyboard(new JbKbd(st.c(),R.xml.smileys));
    }
/** ��������� ���������� ���������� 
*@param bShift true - ��� ��������� symbol_shift, false - ��� symbol */
    public static void setSymbolKeyboard(boolean bShift)
    {
    	JbKbdView.inst.setKeyboard(new JbKbd(st.c(),bShift?R.xml.symbols_shift:R.xml.symbols));
    }
/** ��������� qwerty-���������� � ������ ���������� ������������� */    
    public static void setQwertyKeyboard()
    {
    	setQwertyKeyboard(false);
    }
/** ��������� qwerty-���������� � ������ ���������� ������������� */    
    public static void setQwertyKeyboard(boolean bForce)
    {
    	JbKbd kb = curKbd();
    	if(kb!=null&&!bForce&&getCurQwertyRes()==kb.resId)
    		return;
    	JbKbdView.inst.setKeyboard(new JbKbd(st.c(),getCurQwertyRes()));
    }
/** ���������� ������� ��������� {@link JbKbdView}*/    
    public static JbKbdView kv()
    {
    	return JbKbdView.inst;
    }
/** ���������� ������ �� ��������� ��� ������������ ������ <br>
 * �� ��������� - ���� ������� ������+����������, ���� ��� ����� ������� ������ - �� ������ ����������
 */
    public static String getDefaultLangString()
    {
    	String ret = "";
    	String lang = Locale.getDefault().getLanguage();
    	if(kbdForName(lang)!=null)
    	{
    		ret+=lang+',';
    	}
    	ret+=defKbd().lang.name;
    	return ret;
    }
/** ���������� ������ ������ ��� ������������ */    
    public static String[] getLangsArray(Context c)
    {
    	SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(c);
    	String langs = p.getString(st.PREF_KEY_LANGS, st.getDefaultLangString());
    	return langs.split(",");
    }
/** ���������� ������� ������ search � ������� ar, ��� -1, ���� �� �������*/    
    public static int searchStr(String search,String ar[])
    {
    	if(ar==null||search==null)
    		return -1;
    	for(int i=0;i<ar.length;i++)
    	{
    		if(ar[i].equals(search))
    			return i;
    	}
    	return -1;
    }
/** ���������� ������� � �� ��� ������ ����� */    
    static Stor stor()
    {
    	if(Stor.inst!=null)
    		return Stor.inst;
    	if(st.c()==null)
    		return null;
    	return new Stor(st.c());
    }
/** */ 
    static boolean runAct(Class<?>cls)
    {
    	return runAct(cls,c());
    }
    static boolean runAct(Class<?>cls,Context c)
    {
		try{
			
			c.startActivity(
					new Intent(Intent.ACTION_VIEW)
						.setComponent(new ComponentName(st.c(),cls))
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP)
			);
		}
		catch(Throwable e)
		{
			st.logEx(e);
			return false;
		}
		return true;
    }
/** �������� ����������� � ������ fn �� _ , ���������� ���������������� ������ */   
    public static final String normalizeFileName(String fn)
    {
        return fn.replaceAll("[\\\"\\/:?!\\\'\\\\]", "_");
    }
/** ��������� ������������ ������� � ����� cmd*/    
    static boolean kbdCommand(int action)
    {
    	switch(action)
    	{
	    	case CMD_VOICE_RECOGNIZER: new VRTest().startVoice(); return true;//return runAct(VRActivity.class);
	    	case CMD_TPL_EDITOR: return runAct(TplEditorActivity.class);
	    	case CMD_TPL_NEW_FOLDER: 
	    		if(Templates.inst==null)
	    			return false;
	    		Templates.inst.setEditFolder(true);
	    		return runAct(TplEditorActivity.class);
	    	case CMD_TPL: new Templates().makeCommonMenu(); return true;
	    	case CMD_PREFERENCES: return runAct(JbKbdPreference.class);
	    	case CMD_CLIPBOARD: return ComMenu.showClipboard();
    	}
    	return false;
    }
/** ���������� ������� �� ��������� ����� */    
    static int getCmdByLabel(String label)
    {
    	if(!label.startsWith(DRW_PREFIX))
    		return 0;
    	String l = label.substring(DRW_PREFIX.length());
    	if(l.equals("vr"))
    		return CMD_VOICE_RECOGNIZER;
    	return 0;
    }
/** ���������� id ������ �� �������*/    
    static Bitmap getBitmapByCmd(int cmd)
    {
    	int bid = 0;
    	switch (cmd)
		{
			case CMD_VOICE_RECOGNIZER: bid = R.drawable.vr_small_white;
		}
    	if(bid!=0)
    		return BitmapFactory.decodeResource(st.c().getResources(), bid);
    	return null;
    }
    static final SharedPreferences pref()
    {
    	return PreferenceManager.getDefaultSharedPreferences(c());
    }
/** ����, boolean, �������� �������� "��������/��������� �������� ������" */    
    public static final String PREF_KEY_PREVIEW = "ch_preview";
/** ����, int ,�������� ��� ��������� ������������ ���������� */    
    public static final String PREF_KEY_LAST_LANG = "lastLang";
/** ����, int, �������� ������ ������ � ���������� ������ */    
    public static final String PREF_KEY_HEIGHT_PORTRAIT = "kh";
/** ����, int, �������� ������ ������ � ����������� ������ */    
    public static final String PREF_KEY_HEIGHT_LANDSCAPE = "khl";
/** ����, int, �������� ������ ������ �� ���������, �� ������ ������ */    
    public static final String PREF_KEY_DEF_HEIGHT = "dh";
/** ����, String, �������� ������� ������������ ������ */    
    public static final String PREF_KEY_LANGS = "langs";
/** ����, boolean, �������� ��������� ����� ��� �������� ������� */    
    public static final String PREF_KEY_VIBRO_SHORT_KEY = "vs";
/** ����, boolean, �������� ��������� ����� ��� �������� ������� */    
    public static final String PREF_KEY_VIBRO_LONG_KEY = "vl";
/** ����, int, �������� ������ ���������� ��� ��������� �����. ������ ���� �������� ��� PREF_KEY_LANG_KBD+"en"*/    
    public static final String PREF_KEY_LANG_KBD = "lkbd_";

 /** �������� ��� ������� {@link SetKbdActivity}. � ���� ������ ��������� �������� ���� int<br>
 *  �������� int - ���� �� �������� SET_*/    
    public static final String SET_INTENT_ACTION = "sa";
 /**  �������� String - �������� �����, ��� �������� ������������ ����� ���������� */    
    public static final String SET_INTENT_LANG_NAME = "sl";
    
/** �������� ��� ������� {@link SetKbdActivity} - ��������� ������ ������ � ���������� ������ */    
    public static final int SET_KEY_HEIGHT_PORTRAIT = 1;
/** �������� ��� ������� {@link SetKbdActivity} - ��������� ������ ������ � ����������� ������ */    
    public static final int SET_KEY_HEIGHT_LANDSCAPE =2;
/** �������� ��������� ������������ ������ */    
    public static final int SET_LANGUAGES_SELECTION =3;
/** �������� ��������� ������*/    
    public static final int SET_KEYS =4;
    public static final int SET_SELECT_KEYBOARD = 5;
    
/** ��������� ������� ��� ������, ��� ����� ��� �������� ������� �������� ������� */
	public static final String DRW_PREFIX = "d_"; 
/** ��������� ������� - ��������� ���� */	
	public static final int CMD_VOICE_RECOGNIZER = -1;
/** ��������� ������� - ������ ��������� �������� */	
	public static final int CMD_TPL_EDITOR = -2;
/** ��������� ������� - ����� �������� �� ���������� */	
	public static final int CMD_TPL = -3;
/** ��������� ������� - ������ �������� */	
	public static final int CMD_PREFERENCES = -4;
/** ��������� ������� - ������ ������������ ������ */	
	public static final int CMD_CLIPBOARD = -5;
/** ��������� ������� - �������� ����� �������� */	
	public static final int CMD_TPL_NEW_FOLDER = -6;
	public static final String TAG = "JBK";
}
