package com.jbak.JbakKeyboard;

import java.net.ContentHandler;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;

public class Stor extends SQLiteOpenHelper
{
	static Stor inst;
    public static final int DATABASE_VERSION = 2;
	// Таблицы    
/** Таблица буфера обммена*/	
    public static final String TABLE_CLIPBOARD = "tClipbrd";
    public static final String TABLE_KEYS = "tKeys";
// Столбцы таблицы буфера обмена     
    public static final String C_TEXT 	= "txt";
    public static final String C_LENGTH = "len";
    public static final String C_DATE = "dat";

// Столбцы    
    public static final String C_ID = "_id";
    public static final String C_KEYCODE = "kc";
    public static final String C_CHAR = "chr";
    public static final String C_FLAGS = "flg";
    public static final String C_ACTION = "act";
    public static final String C_BINARY = "bin";

    public static final int CLIPBOARD_LIMIT = 20;
    public static final String DB_FILENAME="kbstor";
    private static final String KEYS_TABLE_CREATE = 
	    "CREATE TABLE IF NOT EXISTS " + TABLE_KEYS + " (" +
	    C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	    C_KEYCODE + " INTEGER, " +
	    C_CHAR + " INTEGER, " +
	    C_FLAGS + " INTEGER, " +
	    C_ACTION + " INTEGER, " +
	    C_TEXT + " TEXT, " +
	    C_BINARY + " BLOB);";
    private static final String CLIPBOARD_TABLE_CREATE = 
        "CREATE TABLE IF NOT EXISTS " + TABLE_CLIPBOARD + " (" +
        C_TEXT + " TEXT, " +
        C_LENGTH + " INTEGER, " +
        C_DATE + " INTEGER);";

    Stor(Context context) 
    {
    	super(context, DB_FILENAME, null, DATABASE_VERSION);
        inst = this;
        m_db = getWritableDatabase();
        try{m_db.execSQL(CLIPBOARD_TABLE_CREATE);}catch (Exception e){st.logEx(e);}
        try{m_db.execSQL(KEYS_TABLE_CREATE);}catch (Exception e){st.logEx(e);}
    }
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2){}
	@Override
	public void onCreate(SQLiteDatabase db){}
	void saveClipboardString(String s)
	{
		long date = System.currentTimeMillis();
		try{
			SQLiteStatement stat = m_db.compileStatement("INSERT INTO "+TABLE_CLIPBOARD+" VALUES(?,?,?)");
			m_db.beginTransaction();
			stat.bindString(1, s);
			stat.bindLong(2, s.length());
			stat.bindLong(3, date);
			stat.executeInsert();
            m_db.setTransactionSuccessful();
            m_db.endTransaction();
		}
		catch (Throwable e) {
			st.logEx(e);
		}
	}
/** Запускает запрос sql на выполнение, в случае ошибки возвращает false и пишет в лог */	
	boolean runSql(String sql)
	{
		try{
			m_db.execSQL(sql);
		}
		catch(Throwable e)
		{
			st.logEx(e);
			return false;
		}
		return true;
	}
/** */
	static KeySet readKey(Cursor c)
	{
		try
		{
			KeySet ks = new KeySet();
			ks.keycode = c.getInt(1);
			ks.keychar = (char) c.getInt(2);
			ks.flags = 	 c.getInt(3);
			ks.action =  c.getInt(4);
			ks.sext = c.getString(5);
			ks.extra = getIntentFromBytes(c.getBlob(6));
			return ks;
		}
		catch (Throwable e) {
			st.logEx(e);
		}
		return null;
	}
/** */
	Cursor getKeysCursor()
	{
		try
		{
			Cursor cursor = m_db.query(TABLE_KEYS, null, null, null, null, null, null);
			if(cursor.moveToFirst())
				return cursor;
			cursor.close();
		}
		catch (Throwable e) {
			st.logEx(e);
		}
		return null;
	}
	Cursor getClipboardCursor()
	{
		try
		{
			Cursor cursor = m_db.query(TABLE_CLIPBOARD, null, null, null, null, null, null);
			if(cursor.moveToLast())
				return cursor;
			cursor.close();
		}
		catch (Throwable e) {
			st.logEx(e);
		}
		return null;
	}
/** Удаляет вхождения буфера обмена по датам. Если date2==0 - удаляет только по date*/	
	void removeClipboardByDate(long date,long date2)
	{
		String sql = "DELETE FROM "+TABLE_CLIPBOARD+" WHERE "+C_DATE+"="+date;
		if(date2>0)
		{
			sql+=" OR "+C_DATE+"="+date2;
		}
		runSql(sql);
	}
/** Сохраняет в БД настройку клавиши ks*/	
	void saveKey(KeySet ks)
	{
		try{
			runSql("DELETE FROM "+TABLE_KEYS+
					" WHERE "+C_KEYCODE+"="+ks.keycode+
							" AND "+C_CHAR+"="+((int)ks.keychar)+
							" AND "+C_FLAGS+"="+ks.flags);
			if(ks.action==KeySet.ACT_DEFAULT)
				return;
			m_db.beginTransaction();
			ContentValues val = new ContentValues();
			val.put(C_KEYCODE, ks.keycode);
			val.put(C_CHAR, (int)ks.keychar);
			val.put(C_FLAGS, ks.flags);
			val.put(C_ACTION, ks.action);
			val.put(C_TEXT, ks.sext);
			val.put(C_BINARY, getBytesFromIntent(ks.extra));
			m_db.insert(TABLE_KEYS, null, val);
            m_db.setTransactionSuccessful();
            m_db.endTransaction();
		}
		catch (Throwable e) {
			st.logEx(e);
		}
	}
/** Удаляет строки, совпадающие с txt, проверяет */	
	boolean checkClipboardString(String txt )
	{
		long date = 0;
		long date2=0;
		try
		{
			Cursor cursor = m_db.query(TABLE_CLIPBOARD, null, null, null, null, null, null);
			if(cursor==null)
				return false;
			if(!cursor.moveToLast())
			{
				cursor.close();
				saveClipboardString(txt);
				return true;
			}
			int count = 1;
			do
			{
				long len = cursor.getLong(1); // C_LENGTH
				if(len==txt.length())
				{
					String s = cursor.getString(0);
					if(txt.equals(s))
					{
						date = cursor.getLong(2);// Нашли одинаковую строку, удаляем
						--count;
					}
					if(count==CLIPBOARD_LIMIT-1)
					{
						date2 = cursor.getLong(2);
					}
				}
				++count;
			}while(cursor.moveToPrevious());
			cursor.close();
			if(date>0)
				removeClipboardByDate(date, date2);
			saveClipboardString(txt);
		}
		catch (Throwable e) {
			st.logEx(e);
		}
		return true;
	}
    static byte[] getBytesFromIntent(Intent in)
    {
        if(in==null)
            return null;
        Parcel pars = Parcel.obtain();
        in.writeToParcel(pars, 0);
        return pars.marshall();
    }
    static Intent getIntentFromBytes(byte[] ar)
    {
        if(ar==null)
            return null;
        Parcel parc = Parcel.obtain();
        Intent in = new Intent();
        parc.unmarshall(ar, 0, ar.length);
        parc.setDataPosition(0);
        in.readFromParcel(parc);
        return in;
    }
	SQLiteDatabase m_db;
}
