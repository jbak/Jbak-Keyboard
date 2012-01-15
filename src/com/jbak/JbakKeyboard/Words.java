package com.jbak.JbakKeyboard;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class Words
{
    boolean open(String name)
    {
        String path = st.getSettingsPath()+DEF_EXT;
        try{
            if(m_db!=null)
            {
                m_db.close();
                m_db = null;
            }
            m_db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
            m_cCompletions = (SQLiteCursor)m_db.query(TABLE_WORDS, 
                    new String[]{C_WORD}, 
                    C_WORD+" like ?",
                    new String[]{"%"},
                    null, 
                    null, 
                    null,
                    "20");
            return m_db!=null;
        }
        catch(Throwable e)
        {
            st.logEx(e);
        }
        return false;
    }
    void close()
    {
        if(m_cCompletions!=null)
        {
            m_cCompletions.close();
        }
        if(m_db!=null)
            m_db.close();
    }
    final boolean canGiveWords()
    {
        return m_db!=null;
    }
    ArrayList<String> getWords(String word)
    {
        ArrayList<String> ret = new ArrayList<String>();
        try{
            Cursor c = m_cCompletions; 
            m_cArrayCompletions[0]=word+S_LETTERS;
            m_cCompletions.setSelectionArguments(m_cArrayCompletions);
            m_cCompletions.requery();
            if(c.moveToFirst())
            {
                do
                {
                    ret.add(c.getString(0));
                    if(ret.size()>20)
                        break;
                }while(c.moveToNext());
            }
        }
        catch (Throwable e) {
            st.logEx(e);
        }
        return ret;
    }
    public static final String DEF_PATH = "vocab/";
    public static final String DEF_EXT = ".cdb";
    
    public static final String TABLE_WORDS = "tWords";
    public static final String C_WORD = "word";

    public static final String S_LETTERS = "%";
    public static final String ST_SEL_LEN = ".cdb";
    SQLiteStatement m_stAllWords;
    SQLiteCursor m_cCompletions;
    String m_cArrayCompletions[] = new String[]{S_LETTERS};
    SQLiteDatabase m_db;
}
