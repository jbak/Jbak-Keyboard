package com.jbak.JbakKeyboard;

import java.io.IOException;
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
import android.widget.Toast;
/** Класс содержит полезные статические переменные */
public class st extends IKeyboard implements IKbdSettings
{
//--------------------------------------------------------------------------
    /** Универсальный обсервер. Содержит 2 параметра m_param1 и m_param2, которые вызываются и меняются в зависимости от контекста*/
    public static abstract class UniObserver
    {
    /** Конструктор с двумя параметрами */
        public UniObserver(Object param1,Object param2)
        {
            m_param1 = param1;
            m_param2 = param2;
        }
    /** Пустой конструктор. Оба параметра - null*/
        public UniObserver()
        {
        }
    /** Вызов функции {@link #OnObserver(Object, Object)} с текущими параметрами*/
        public int Observ(){return OnObserver(m_param1, m_param2);}
    /** Основная функция обработчика */ 
        abstract int OnObserver(Object param1,Object param2);
    /** Пользовательский параметр 1 */  
        Object m_param1;
    /** Пользовательский параметр 2 */  
        Object m_param2;
    }
    
    /** Эквивалент вызова (val&flag)>0*/        
    public static final boolean has(int val,int flag)
    {
        return (val&flag)>0;
    }
/** Убирает бит flag из значения val, если бит выставлен*/      
    public static final int rem(int val,int flag)
    {
        if(has(val,flag))
            return val^flag;
        return val;
    }
    public static final int min(int val1,int val2)
    {
    	if(val1<val2)return val1;
    	return val2;
    }
    public static final int max(int val1,int val2)
    {
    	if(val1>val2)return val1;
    	return val2;
    }
    public static final void logEx(Throwable e)
    {
        if(e.getMessage()!=null)
            Log.e(TAG, e.getMessage());
        Log.e(TAG, Log.getStackTraceString(e));
    }
/** Возвращает клавиатуру для языка с именем langName */    
    public static Keybrd kbdForName(String langName)
    {
        int index = pref().getInt(st.PREF_KEY_LANG_KBD+langName, -1);
        if(index>=0)
            return arKbd[index];
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
/** Сохраняет текущий ресурс qwerty-клавиатуры, если редактирование происходит в qwerty */    
    public static void saveCurLang()
    {
        JbKbd kb = curKbd();
        if(kb==null)
            return;
        Keybrd l = kbdForId(kb.resId);
        if(l==null)
            return;
        pref().edit().putString(st.PREF_KEY_LAST_LANG, l.lang.name).commit();
    }
/** Возвращает текущий ресурс для qwerty-клавиатуры */    
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
        return kbdForName(pref.getString(PREF_KEY_LAST_LANG, defKbd().lang.name)).resId;
    }
/** Возвращает текущую клавиатуру или null*/    
    public static JbKbd curKbd()
    {
        if(JbKbdView.inst==null) return null;
        return (JbKbd)JbKbdView.inst.getCurKeyboard();
    }
/** Возвращает активный контекст. Если запущено {@link SetKbdActivity} - то возвращает его, иначе - {@link ServiceJbKbd}*/    
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
    /** Класс для запуска пользовательского кода синхронно или асинхронно
     * Создаётся без параметров. По окончании выполнения запускается обработчик */
    public static abstract class SyncAsycOper extends AsyncTask<Void,Void,Void>
    {
    /** Конструктор
     * @param obs Обработчик, который запустится по выполнении */
        public SyncAsycOper(UniObserver obs)
        {
            m_obs = obs;
        }
    /** Синхронно стартует операцию {@link #makeOper(UniObserver)}*/
        void startSync()
        {
            makeOper(m_obs);
        }
    /** Асинхронно стартует операцию {@link #makeOper(UniObserver)}*/
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
    /** Выполняемая операция  */
        abstract void makeOper(UniObserver obs);
    /** Обработчик операции */  
        UniObserver m_obs;
    }
/** Установка клавиатуры редактирования текста */
    public static void setTextEditKeyboard()
    {
        JbKbdView.inst.setKeyboard(new JbKbd(st.c(),R.xml.edittext));
    }
/** Установка клавиатуры смайликов */
    public static void setSmilesKeyboard()
    {
        JbKbdView.inst.setKeyboard(new JbKbd(st.c(),R.xml.smileys));
    }
/** Установка символьной клавиатуры 
*@param bShift true - для установки symbol_shift, false - для symbol */
    public static void setSymbolKeyboard(boolean bShift)
    {
        JbKbdView.inst.setKeyboard(new JbKbd(st.c(),bShift?R.xml.symbols_shift:R.xml.symbols));
    }
/** Установка qwerty-клавиатуры с учётом последнего использования */    
    public static void setQwertyKeyboard()
    {
        setQwertyKeyboard(false);
    }
/** Установка qwerty-клавиатуры с учётом последнего использования */    
    public static void setQwertyKeyboard(boolean bForce)
    {
        JbKbd kb = curKbd();
        if(kb!=null&&!bForce&&getCurQwertyRes()==kb.resId)
            return;
        JbKbdView.inst.setKeyboard(new JbKbd(st.c(),getCurQwertyRes()));
        saveCurLang();
    }
    public static String getCurLang()
    {
        return pref().getString(PREF_KEY_LAST_LANG, defKbd().lang.name);
    }
/** Возвращает текущий запущеный {@link JbKbdView}*/    
    public static JbKbdView kv()
    {
        return JbKbdView.inst;
    }
/** Возвращает строку по умолчанию для переключения языков <br>
 * По умолчанию - язык текущей локали+английский, если нет языка текущей локали - то только английский
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
/** Возвращает массив языков для переключения */    
    public static String[] getLangsArray(Context c)
    {
        String langs = pref().getString(st.PREF_KEY_LANGS, st.getDefaultLangString());
        return langs.split(",");
    }
/** Возвращает позицию строки search в массиве ar, или -1, если не найдено*/    
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
/** Возвращает коннект к БД или создаёт новый */    
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
/** Заменяет спецсимволы в строке fn на _ , возвращает модифицированную строку */   
    public static final String normalizeFileName(String fn)
    {
        return fn.replaceAll("[\\\"\\/:?!\\\'\\\\]", "_");
    }
/** Выполняет клавиатурную команду с кодом cmd*/    
    static boolean kbdCommand(int action)
    {
        switch(action)
        {
          case 100:
            try {
                Process proc = Runtime.getRuntime().exec("fbtool -d /sdcard/jbkbd.bmp");
                int av = proc.getInputStream().available();
                int ave = proc.getErrorStream().available();
                String in=null,err = null;
                if(av>0)
                {
                  byte b[] = new byte[av];
                  proc.getInputStream().read(b);
                  in = new String(b);
                }
                if(ave>0)
                {
                  byte b[] = new byte[ave];
                  proc.getErrorStream().read(b);
                  err = new String(b);
                }
                Toast.makeText(c(), err==null?in:err, 1500).show();
            } catch (Throwable e) {
                st.logEx(e);
            }
              break;
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
/** Возвращает команду по текстовой метке */    
    static int getCmdByLabel(String label)
    {
        if(!label.startsWith(DRW_PREFIX))
            return 0;
        String l = label.substring(DRW_PREFIX.length());
        if(l.equals("vr"))
            return CMD_VOICE_RECOGNIZER;
        return 0;
    }
/** Возвращает id иконки по команде*/    
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
}
