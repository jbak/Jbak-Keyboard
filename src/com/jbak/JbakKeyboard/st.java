package com.jbak.JbakKeyboard;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;
import java.util.Vector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.jbak.CustomGraphics.GradBack;
/** Класс содержит полезные статические переменные */
public class st extends IKeyboard implements IKbdSettings
{
    public static final boolean DEBUG = false;
/** Код, который используется, если основной текст клавиши из нескольких букв*/    
    public static int KeySymbol = -201;
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
    public static Drawable getBack()
    {
        return new GradBack(0xff000088, 0xff008800).setCorners(0, 0).setGap(0).setDrawPressedBackground(false).getStateDrawable();
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
        if(DEBUG)
        {
        if(e.getMessage()!=null)
            Log.e(TAG, e.getMessage());
        Log.e(TAG, Log.getStackTraceString(e));
        }
    }
/** Возвращает клавиатуру для языка с именем langName */    
    public static Keybrd kbdForLangName(String langName)
    {
        String pname = isLandscape(c())?st.PREF_KEY_LANG_KBD_LANDSCAPE:st.PREF_KEY_LANG_KBD_PORTRAIT;
        pname+=langName;
        String kbdName = pref().getString(pname, "");
        Keybrd ret = null;
        for(int i=0;i<3;i++)
        {
            for(Keybrd k:arKbd)
            {
                if(k.isLang(langName))
                {
                    // если не настроено - вернем первую в списке клаву с тем же языком
                    if(kbdName.length()==0||kbdName.equals(k.path))
                            return k;
                }
            }
            // Теперь попробуем перезагрузить клавиатуры...
            if(i==0)
                CustomKeyboard.loadCustomKeyboards(false);
            else if(i==1)
                kbdName = "";
        }
        Context c = st.c();
        if(c!=null)
            Toast.makeText(c, "Lang not found:"+langName, 700).show();
        return arKbd[0];
    }
    public static float screenDens(Context c)
    {
        return c.getResources().getDisplayMetrics().density;
    }
    static void log(String txt)
    {
        if(DEBUG)
            Log.w(TAG, txt);
    }
/** Сохраняет текущий ресурс qwerty-клавиатуры, если редактирование происходит в qwerty */    
    public static void saveCurLang()
    {
        JbKbd kb = curKbd();
        if(kb==null||kb.kbd==null)
            return;
        pref().edit().putString(st.PREF_KEY_LAST_LANG, kb.kbd.lang.name).commit();
    }
/** Возвращает текущий ресурс для qwerty-клавиатуры */    
    public static Keybrd getCurQwertyKeybrd()
    {
        SharedPreferences p =pref();
        if(p==null||!p.contains(PREF_KEY_LAST_LANG))
        {
            String lang = Locale.getDefault().getLanguage();
            Keybrd l = kbdForLangName(lang);
            if(l!=null)
                return l;
            return defKbd();
        }
        return kbdForLangName(p.getString(PREF_KEY_LAST_LANG, defKbd().lang.name));
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
        if(SetKbdActivity.inst!=null)
            return SetKbdActivity.inst;
        if(ServiceJbKbd.inst!=null)
            return ServiceJbKbd.inst;
        if(LangSetActivity.inst!=null)
            return LangSetActivity.inst;
        if(EditSetActivity.inst!=null)
            return EditSetActivity.inst;
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
    public static boolean isQwertyKeyboard(Keybrd k)
    {
        return !k.lang.isVirtualLang();
    }
    static Vector <Keybrd> getKeybrdArrayByLang(String lang)
    {
        Vector<Keybrd> ret = new Vector<IKeyboard.Keybrd>();
        for(Keybrd k:st.arKbd)
        {
            if(k.lang.name.equals(lang))
                ret.add(k);
        }
        return ret;
    }

/** Установка клавиатуры редактирования текста */
    public static void setTextEditKeyboard()
    {
        JbKbdView.inst.setKeyboard(loadKeyboard(kbdForLangName(LANG_EDITTEXT)));
    }
/** Установка клавиатуры смайликов */
    public static void setSmilesKeyboard()
    {
        JbKbdView.inst.setKeyboard(loadKeyboard(kbdForLangName(LANG_SMILE)));
    }
/** Установка символьной клавиатуры 
*@param bShift true - для установки symbol_shift, false - для symbol */
    public static void setSymbolKeyboard(boolean bShift)
    {
        Keybrd k = kbdForLangName(bShift?LANG_SYM_KBD1:LANG_SYM_KBD);
        JbKbdView.inst.setKeyboard(loadKeyboard(k));
    }
/** Установка qwerty-клавиатуры с учётом последнего использования */    
    public static void setQwertyKeyboard()
    {
        setQwertyKeyboard(false);
    }
/** Установка qwerty-клавиатуры с учётом последнего использования */    
    public static void setQwertyKeyboard(boolean bForce)
    {
//        CustomKeyboard ck = new CustomKeyboard(JbKbdView.inst.getContext(), "/mnt/sdcard/jbakKeyboard/keyboards/qwerty_ru.xml");
//        JbKbdView.inst.setKeyboard(ck);
        JbKbd kb = curKbd();
        Keybrd cur = getCurQwertyKeybrd();
//        if(kb!=null&&!bForce) // Проверить, одинаковы ли клавиатуры
//            return;
        JbKbd newKbd = null;
        JbKbdView.inst.setKeyboard(loadKeyboard(cur));
        saveCurLang();
    }
    static JbKbd loadKeyboard(Keybrd k)
    {
        KeySymbol = -201;
        JbKbd kb;
        CustomKeyboard jk =  new CustomKeyboard(st.c(), k);
        if(!jk.m_bBrokenLoad)
        {
            return jk;
        }
        for(Keybrd ck:arKbd)
        {
            if(ck.lang.name.equals(k.lang.name))
                return loadKeyboard(ck);
        }
        return loadKeyboard(arKbd[0]);
    }
/** Временно устанавливает английскую клавиатуру без запоминания языка */    
    public static void setTempEnglishQwerty()
    {
        JbKbd kb = curKbd();
        Keybrd k = kbdForLangName(arLangs[LANG_EN].name);
//        if(kb!=null&&kb.resId==k.resId)
//            return;
        JbKbdView.inst.setKeyboard(loadKeyboard(k));
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
        String lang = Locale.getDefault().getLanguage();
        String defKbdLang = defKbd().lang.name;
        if(kbdForLangName(lang)!=null&&!lang.equals(defKbdLang))
        {
            return lang+','+defKbdLang;
        }
        return defKbdLang;
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
                        .setComponent(new ComponentName(c,cls))
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
            case CMD_COMPILE_KEYBOARDS:
                    CustomKeyboard.loadCustomKeyboards(true);
                break;
            case CMD_MAIN_MENU: 
                if(st.kv().isUserInput())
                {
                    ServiceJbKbd.inst.onOptions();
                }break;
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
        {
            if(label.equals("tab"))
                return 9;
            if(label.equals("opt"))
                return CMD_MAIN_MENU;
            return 0;
        }
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
        return pref(c());
    }
    static final SharedPreferences pref(Context c)
    {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }
    static final KeyboardPaints paint()
    {
        if(KeyboardPaints.inst==null)
            return new KeyboardPaints();
        return KeyboardPaints.inst;
    }
    public static int parseInt(String string,int radix) {
        int result = 0;
        int degree = 1;
        for(int i=string.length()-1;i>=0;i--)
        {
            int digit = Character.digit(string.charAt(i), radix);
            if (digit == -1) {
                continue;
            }
            result+=degree*digit;
            degree*=radix;
        }
        return result;
    }
    static final boolean isLandscape(Context c)
    {
        return c.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT;
    }
    public static KbdDesign getSkinByPath(String path)
    {
        if(path.startsWith("/"))
        {
            for(int i=0;i<2;i++)
            {
                for(KbdDesign d:st.arDesign)
                {
                    if(path.equals(d.path))
                        return d;
                }
                // Учтем возможность отключения карты и сделаем перезагрузку скинов
                CustomKbdDesign.loadCustomSkins();
            }
            path = ZERO_STRING;
        }
        try{
            return arDesign[Integer.decode(path)];
        }
        catch (Throwable e) {
        }
        return arDesign[0];
    }
    public static String getSkinPath(KbdDesign kd)
    {
        if(kd.path!=null)
            return kd.path;
        int pos = 0;
        for(KbdDesign k:arDesign)
        {
            if(k==kd)
                return ""+pos;
            ++pos;
        }
        return ZERO_STRING;
    }
    public static void upgradeSettings(Context c)
    {
        SharedPreferences pref = st.pref(c);
        Editor ped = pref.edit();
        if(pref.contains(st.PREF_KEY_KBD_SKIN))
        {
            CustomKbdDesign.loadCustomSkins();
            int id = pref.getInt(st.PREF_KEY_KBD_SKIN, 0);
            if(st.arDesign.length>id&&id>=0)
            {
                ped.putString(st.PREF_KEY_KBD_SKIN_PATH, st.getSkinPath(st.arDesign[id]));
            }
            ped.remove(st.PREF_KEY_KBD_SKIN);
        }
        int ph = pref.getInt(st.PREF_KEY_HEIGHT_PORTRAIT,getDefaultKeyHeight(c));
        if(ph%2>0)
        {
            ped.putInt(st.PREF_KEY_HEIGHT_PORTRAIT,ph-1);
        }
        ph = pref.getInt(st.PREF_KEY_HEIGHT_LANDSCAPE,getDefaultKeyHeight(c));
        if(ph%2>0)
        {
            ped.putInt(st.PREF_KEY_HEIGHT_LANDSCAPE,ph-1);
        }
        if(pref.contains(st.PREF_KEY_VIBRO_SHORT_KEY))
        {
            boolean bVibroShort = pref.getBoolean(st.PREF_KEY_VIBRO_SHORT_KEY, false);
            String vt = bVibroShort?st.ONE_STRING:st.ZERO_STRING;
            ped.putString(st.PREF_KEY_VIBRO_SHORT_TYPE,vt);
            ped.remove(st.PREF_KEY_VIBRO_SHORT_KEY);
        }
        if(!pref.contains(st.PREF_KEY_UP_AFTER_SYMBOLS))
        {
            ped.putBoolean(st.PREF_KEY_UP_AFTER_SYMBOLS, pref.getBoolean(PREF_KEY_AUTO_CASE, false));
        }
        ped.commit();
    }
    public static int getDefaultKeyHeight(Context c)
    {
        return (int) (c.getResources().getDimension(R.dimen.def_key_height));
    }
    public static File[] getFilesByExt(File dir,final String ext)
    {
        return dir.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String filename)
            {
                int pos = filename.lastIndexOf('.');
                if(pos<0)return false;
                return filename.substring(pos+1).compareTo(ext)==0;
            }
        });
    }
}
