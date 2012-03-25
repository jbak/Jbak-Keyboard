package com.jbak.words;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.jbak.JbakKeyboard.st;
import com.jbak.JbakKeyboard.st.UniObserver;

public class WordsService extends Service
{
    public static Handler g_serviceHandler;
    public static final String EXTRA_CMD="cmd";
    public static final String EXTRA_STR1 = "str1";
    public static final String EXTRA_STR2 = "str2";
/** Команда открытия словаря. В EXTRA_STR1 должно быть название языка */    
    public static final int CMD_OPEN_VOCAB = 1;
/** Команда получения слова. В EXTRA_STR1 должно быть исходное слово. Результат возвращается в {@link #g_serviceHandler}*/    
    public static final int CMD_GET_WORDS = 2;
/** Команда сохранения слова в пользовательском словаре. В EXTRA_STR1 должно быть слово */    
    public static final int CMD_SAVE_WORD = 3;
    Words m_words;
    UserWords m_userWords;
    String m_curWord;
    public static void start(Context c)
    {
        c.startService(new Intent(c,WordsService.class));
    }
    public static void command(int cmd, String param,Context c)
    {
        Intent in = new Intent(c,WordsService.class)
            .putExtra(EXTRA_CMD, cmd)
            .putExtra(EXTRA_STR1, param);
        c.startService(in);
    }
    @Override
    public void onCreate()
    {
        m_words = new Words();
        m_userWords = new UserWords();
        m_userWords.open(st.getSettingsPath()+Words.DEF_PATH+UserWords.FILENAME);
    }
    void asyncOper(final String param)
    {
        st.SyncAsycOper op = new st.SyncAsycOper(null)
        {
            @Override
            public void makeOper(UniObserver obs)
            {
                while(m_newWord!=null)
                {
                    m_words.cancelSync(false);
                    m_bRun = true;
                    String s = m_newWord;
                    m_newWord = null;
                    m_words.getWordsSync(s, g_serviceHandler);
                    m_bRun = false;
                }
            }
        };
        op.startAsync();
    }
    String m_newWord = null;
    boolean m_bRun = false;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        int cmd = intent.getIntExtra(EXTRA_CMD, 0);
        final String param = intent.getStringExtra(EXTRA_STR1);
        if(cmd==CMD_OPEN_VOCAB)
        {
            m_words.open(param);
            m_userWords.setCurTable(param);
        }
        else if(cmd==CMD_GET_WORDS)
        {
            m_newWord = param;
            if(m_bRun)
            {
                m_words.cancelSync(true);
            }
            else 
                asyncOper(param);
        }
        else if(cmd==CMD_SAVE_WORD)
        {
            m_userWords.addWord(param);
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
