package com.jbak.words;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import com.jbak.JbakKeyboard.st;
import com.jbak.words.WordsIndex.IndexEntry;

public class Words
{
    public String m_name;
    public int m_wordsLimit = 10;
    public static final String DEF_PATH              = "vocab/";
    public static final String DEF_EXT               = ".dic";
    public static final String INDEX_EXT               = ".index";
    WordsIndex m_index;
    LineFileReader m_file;
    public boolean open(String name)
    {
        if(name.equals(m_name)&&canGiveWords())
            return true;
        String path = st.getSettingsPath()+DEF_PATH+name+DEF_EXT;
        m_name = name;
        String indexPath = path+INDEX_EXT;
        m_index = new WordsIndex();
        if(!new File(indexPath).exists())
        {
            if(!m_index.makeIndexFromVocab(path))
            {
                m_index = null;
                return false;
            }
            m_index.save(indexPath);
        }
        else
        {
            if(!m_index.load(indexPath))
                m_index = null;
        }
        try{
            m_file = new LineFileReader();
            m_file.open(path, "r");
        }
        catch (Throwable e) {
            m_file = null;
            return false;
        }
        return canGiveWords();
    }

    public void close()
    {
        m_index = null;
    }

    public final boolean canGiveWords()
    {
        return m_index!=null;
    }
    IndexEntry m_ie = new IndexEntry();
    public String[] getWords(String word)
    {
        if(word==null||word.length()<2||!canGiveWords())
            return null;
        m_ie.first = word.charAt(0);
        m_ie.second = word.charAt(1);
        if(!m_index.getIndexes(m_ie))
            return null;
        return readWords(word);
    }
    Comparator<WordEntry> m_wordsComparator = new Comparator<Words.WordEntry>()
    {
        @Override
        public int compare(WordEntry object1, WordEntry object2)
        {
            if(object1.freq<object2.freq)return 1;
            if(object1.freq>object2.freq)return -1;
            return 0;
        }
        
    };
    String m_ret[] = new String[m_wordsLimit];
    private String[] readWords(String word)
    {
        try{
            String lc = word.toLowerCase();
            int pos = m_ie.filepos;
            m_file.seek(pos);
            Vector<WordEntry> ar = new Vector<Words.WordEntry>(m_wordsLimit); 
            int minF = 0;
            boolean bFull = false;
            while(m_file.nextLine()&&m_file.getLineFilePos()<m_ie.endpos)
            {
                CharBuffer line = m_file.getCharBuffer();
                if(!compare(lc, line))
                    continue;
                WordEntry we = processLine(line,minF);
                if(we.freq==1578)
                {
                    int dbg1 = 2;
                    int dbg2 = dbg1;
                }
                if(we==null||we.freq<=minF&&bFull)
                    continue;
                if(!bFull)
                {
                    ar.add(we);
                    if(minF==0)
                        minF = we.freq;
                    else
                        minF = Math.min(minF, we.freq);
                    bFull = ar.size()==m_wordsLimit;
                }
                else if(we.freq>minF)
                {
                    boolean bSet = false;
                    for(WordEntry w:ar)
                    {
                        if(w.freq==minF&&!bSet)
                        {
                            w.freq = we.freq;
                            w.word= we.word;
                            bSet = true;
                        }
                        else
                            minF = Math.min(w.freq, we.freq);
                    }
                }
            }
            Collections.sort(ar, m_wordsComparator);
            for(int i=0;i<m_wordsLimit;i++)
            {
                if(i<ar.size())
                    m_ret[i]=ar.get(i).word;
                else
                    m_ret[i]=null;
            }
            return m_ret;
        }
        catch (Throwable e) 
        {
            e.printStackTrace();
        }
        return null;
    }
    WordEntry processLine(CharBuffer line,int minFreq)
    {
        int windex = charbufIndexOf(line, ' ', true);
        if(windex<0)
            return null;
        int index = charbufIndexOf(line, ' ', false);
        CharSequence b = line.subSequence(index,line.limit());
        int f = Integer.parseInt(b.toString());
        if(f<minFreq)
            return null;
        WordEntry we = new WordEntry();
        we.word = line.subSequence(0, windex-1).toString();
        we.freq = f;
        return we;
    }
    static class WordEntry
    {
        String word;
        int freq;
    }
    final int charbufIndexOf(CharBuffer cb,int chr,boolean bFirst)
    {
        int pos = -1;
        int p = cb.position();
        int lim = cb.length();
        while(cb.position()<lim)
        {
            if(cb.get()==chr)
            {
                pos = cb.position();
                if(bFirst)
                    break;
            }
        }
        cb.position(p);
        return pos;
    }
    final boolean compare(String str,CharBuffer cb)
    {
        boolean bEq = true;
        int p = cb.position();
        int lim = cb.length();
        int pos = 0;
        while(cb.position()<lim&&pos<str.length())
        {
            if(Character.toLowerCase(cb.get())!=str.charAt(pos++))
            {
                bEq = false;
                break;
            }
        }
        cb.position(p);
        return bEq;
    }
}
