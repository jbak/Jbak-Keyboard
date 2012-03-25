package com.jbak.words;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Vector;

import android.util.Log;

import com.jbak.JbakKeyboard.st;

public class WordsIndex
{
    static class IndexEntry
    {
        public IndexEntry()
        {
            first = 0;
            second = 0;
            filepos = 0;
        }
        public IndexEntry(IndexEntry e)
        {
            first = e.first;
            second = e.second;
            filepos = e.filepos;
            endpos = e.endpos;
        }
        public IndexEntry(char f,char s,int fp)
        {
            first = f;
            second = s;
            filepos = fp;
        }
        static final int sz = 8;
/** Первый символ*/        
        char first;
/** Второй символ */        
        char second;
/** Начальная позиция в файле */        
        int  filepos;
/** Конечная позиция в файле */        
        int endpos;
    }

    int                m_startBytes = 0;
    byte               m_delimSize  = 0;
    IndexEntry m_curEnt = new IndexEntry();
    ByteBuffer m_index;
    final boolean testFile(String path)
    {
        try
        {
            int tl = 100;
            FileInputStream in = new FileInputStream(path);
            byte buf[] = new byte[tl];
            in.read(buf);
            if (buf.length < tl)
                return false;
            if (buf[0]==0xef&&buf[1]==0xbb&&buf[2]==0xbf)
                m_startBytes = 3;
            if (buf[0]==-17&&buf[1]==-69&&buf[2]==-65)
                m_startBytes = 3;
            for (int i = m_startBytes; i < tl; i++)
            {
                if (buf[i] == '\r')
                {
                    if (buf[i + 1] == '\n')
                        m_delimSize = 2;
                    else
                        m_delimSize = 1;
                    break;
                }
                else if (buf[i] == '\n')
                    m_delimSize = 0;
            }
            return true;
        }
        catch (Throwable e)
        {
        }
        return false;
    }
    final boolean processLine(CharBuffer cb)
    {
        if(cb.limit()<2)
            return false;
        char c1 = Character.toLowerCase(cb.get());
        char c2 = Character.toLowerCase(cb.get());
        if(c1<'A')
            return false;
        if(c1!=m_curEnt.first)
        {
            m_curEnt.first = c1;
            m_curEnt.second=c2<'A'?0:c2;
            return true;
        }
        else if(c1==m_curEnt.first&&c2>='A'&&c2!=m_curEnt.second)
        {
            m_curEnt.second = c2;
            return true;
        }
        return false;
    }
    final boolean processLine(String line)
    {
        if(line.length()<2)
            return false;
        char c1 = line.charAt(0);
        char c2 = line.charAt(1);
        if(c1<'A')
            return false;
        if(c1!=m_curEnt.first)
        {
            m_curEnt.first = c1;
            m_curEnt.second=c2<'A'?0:c2;
            return true;
        }
        else if(c1==m_curEnt.first&&c2>='A'&&c2!=m_curEnt.second)
        {
            m_curEnt.second = c2;
            return true;
        }
        return false;
    }
    boolean makeIndexFromVocab(String path)
    {
        if(!testFile(path))
            return false;
        try
        {
            long time = System.currentTimeMillis();
            LineFileReader fr = new LineFileReader();
            fr.open(path, "r");
            fr.seek(m_startBytes);
            Vector<IndexEntry> entries = new Vector<IndexEntry>();
            while (fr.nextLine())
            {
                CharBuffer cb =fr.getCharBuffer(); 
                if(processLine(cb))
                {
                    m_curEnt.filepos = (int) fr.getLineFilePos();
                    entries.add(new IndexEntry(m_curEnt));
                }
            }
            m_index = getBytes(entries, new File(path).length());
            time = System.currentTimeMillis()-time;
            Log.w("JbakKeyboard", "Index takes: "+time+" milliseconds");
            return true;
        }
        catch (Throwable e)
        {
            st.logEx(e);
        }
        return false;
    }
    boolean getIndexes(IndexEntry e)
    {
        int pos = 9;
        int len = m_index.capacity();
        while(pos<len)
        {
            m_index.position(pos);
            char ch = m_index.getChar();
            char ch2 = m_index.getChar();
            if(ch==e.first&&ch2==e.second)
            {
                setSizes(e);
                return true;
            }
// По идее, если буква больше - то выходим, но тут всё наламывает буква ё            
//            else if(ch>e.first||ch==e.first&&ch2>e.second)
//                return false;
            pos = m_index.position()+4;
        }
        return false;
    }
    final void setSizes(IndexEntry e)
    {
        e.filepos = m_index.getInt();
        int pos = m_index.position();
        if(m_index.capacity()-pos<IndexEntry.sz)
            e.endpos = (int)getFileSize();
        else
        {
            m_index.position(pos+4);
            e.endpos = m_index.getInt();
        }
    }
    ByteBuffer getBytes(Vector<IndexEntry> ent,long filesize)
    {
        int sz = IndexEntry.sz*ent.size()+8+1;
        ByteBuffer ret = ByteBuffer.allocate(sz);
        ret.putLong(filesize);
        ret.put(m_delimSize);
        for(IndexEntry e:ent)
        {
            ret.putChar(e.first);
            ret.putChar(e.second);
            ret.putInt(e.filepos);
        }
        return ret;
    }
    final byte getDelimSize()
    {
        return m_index.get(8);
    }
    final long getFileSize()
    {
        return m_index.getLong(0);
    }
    final boolean load(String filePath)
    {
        try{
            FileInputStream is = new FileInputStream(filePath);
            byte buf[] = new byte[(int) new File(filePath).length()];
            is.read(buf);
            m_index = ByteBuffer.wrap(buf);
            return true;
        }
        catch (Throwable e) {
        }
        return false;
    }
    final boolean save(String filePath)
    {
        try{
            FileOutputStream fs = new FileOutputStream(filePath);
            fs.write(m_index.array());
            fs.close();
            return true;
        }
        catch(Throwable e)
        {
        }
        return false;
    }
}
