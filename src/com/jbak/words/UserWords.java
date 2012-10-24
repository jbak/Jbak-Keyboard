package com.jbak.words;

import java.io.File;
import java.util.Vector;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class UserWords
{
    public static final String C_WORD = "word";
    public static final String C_FREQ = "freq";
    public static String FILENAME = "user_vocab.cdb";
    public static final String CRT_TABLE = "CREATE TABLE %s ("+
                                           C_WORD+" text PRIMARY KEY,"+ 
                                           C_FREQ+" tinyint)";
    SQLiteStatement m_getWordsStat;
    public static Vector<String> getTables(SQLiteDatabase db)
    {
        Vector<String> ar = new Vector<String>();
        try{
            Cursor c = db.rawQuery("select name from sqlite_master where type = 'table'", null);
            if(c.moveToFirst())
            {
                do
                {
                    ar.add(c.getString(0));
                }while(c.moveToNext());
            }
            c.close();
        }
        catch (Throwable e) {
        }
        return ar;
    }
    SQLiteDatabase m_db;
    Vector<String> m_tables;
    String m_curTable;
    public boolean open(String path)
    {
        try
        {
            File f = new File(path);
            m_db = SQLiteDatabase.openOrCreateDatabase(f, null);
            m_tables = getTables(m_db);
            return true;
        }
        catch (Throwable e) {
        }
        return false;
    }
    public final boolean isTableExist(String name)
    {
        for(String t:m_tables)
        {
            if(t.equals(name))
                return true;
        }
        return false;
    }
    boolean addTable(String name)
    {
        try{
            String sql = String.format(CRT_TABLE, name);
            m_db.execSQL(sql);
            m_tables.add(name);
            return true;
        }
        catch (Throwable e) {
        }
        return false;
    }
    boolean addWord(String word)
    {
        if(isTableOpen())
            return addWord(word,m_curTable);
        return false;
    }
    boolean addWord(String word,String lang)
    {
        if(!isTableExist(lang)&&!addTable(lang))
            return false;
        
        ContentValues cv = new ContentValues(2);
        cv.put(C_WORD, word.toLowerCase());
        cv.put(C_FREQ, 1);
        long ri = m_db.insert(lang, null, cv);
        if(ri<0)
        {
            ri = m_db.update(lang, cv, C_WORD+"=?", new String[]{word});
        }
        return true;
    }
    public boolean setCurTable(String lang)
    {
        if(m_db==null)
            return false;
        if(m_tables==null)
            m_tables = getTables(m_db);
        if(!isTableExist(lang))
            addTable(lang);
        m_curTable = lang;
//        String sql = "SELECT * FROM "+lang+" WHERE "+C_WORD+" LIKE ?";
//        try
//        {
//            m_getWordsStat = m_db.compileStatement(sql);
//            return true;
//        }
//        catch (Throwable e) 
//        {
//            e.printStackTrace();
//        }
        return true;
    }
    public void close()
    {
        if(m_db!=null)
            m_db.close();
        m_curTable = null;
    }
    public boolean isTableOpen()
    {
        return m_curTable!=null;
    }
    String m_sel[] = new String[1];
    public WordsReader getWordsReader(String word)
    {
        if(m_db==null)
        {
            open(WordsService.getVocabDir()+FILENAME);
        }
        String sel = "%";
        if(word.length()>0)
            sel=word.charAt(0)+"%";
        m_sel[0]=sel;
        try{
            Cursor c = m_db.query(m_curTable, null, C_WORD+" LIKE ?", m_sel, null,null,null);
            return new WordsReader(c,word);
        }
        catch (Throwable e) {
        }
        return null;
    }
    public static class WordsReader extends IWords
    {
        public WordsReader(Cursor c,String word)
        {
            m_cursor = c;
            m_word = word;
        }
        final boolean next()
        {
            if(m_cursor==null)
            {
                m_bHasNext = false;
                return false;
            }
            m_bHasNext = m_cursor.isBeforeFirst()?m_cursor.moveToFirst():m_cursor.moveToNext();
            if(!m_bHasNext)
            {
                m_cursor.close();
                m_cursor = null;
                return false;
            }
            return true;    
        }
        public Cursor m_cursor;
        public String m_word;
        @Override
        public WordEntry getNextWordEntry(int minFreq, boolean bFull)
        {
            if(!next())
                return null;
            String word = m_cursor.getString(0);
            int freq = m_cursor.getInt(1);
            if(freq<minFreq&&!bFull)
                return null;
            int ct = TextTools.compare(m_word, word);
            if(ct==TextTools.COMPARE_TYPE_NONE)
                return null;
            WordEntry we = new WordEntry(word, freq*100, ct);
            we.flags+=WordEntry.FLAG_FROM_USER_VOCAB;
            return we;
        }
    }
}
