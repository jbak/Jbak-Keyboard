package com.jbak.words;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jbak.JbakKeyboard.st;

public class VocabFile
{
    public static final String DEF_EXT = ".dic";
    public final String        REGEXP  = "([A-z]{2})_v(\\d+).*?\\" + DEF_EXT;
    Pattern                    m_pattern;
    Matcher                    m_matcher;
    int                        m_version;
    String                     m_filePath;

    public VocabFile()
    {
        m_pattern = Pattern.compile(REGEXP, Pattern.CASE_INSENSITIVE);
    }

    public boolean match(String filename)
    {
        try
        {
            m_matcher = m_pattern.matcher(filename);
            boolean bRet = m_matcher.find();
            return bRet;
        }
        catch (Exception e)
        {
        }
        return false;
    }

    public String getLang()
    {
        return m_matcher.group(1);
    }

    public int getVersion()
    {
        try
        {
            return Integer.decode(m_matcher.group(2));
        }
        catch (Exception e)
        {
        }
        return 0;
    }
    public String processDir(String lang, File[] files)
    {
        m_filePath = null;
        m_version = 0;
        if (files == null)
            return m_filePath;
        try
        {
            for (File f : files)
            {
                if (match(f.getName()))
                {
                    String l = getLang();
                    if (l != null && l.equals(lang))
                    {
                        int v = getVersion();
                        if (v > m_version)
                        {
                            m_filePath = f.getAbsolutePath();
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return m_filePath;
    }
    public String processDir(String path, String lang)
    {
        return processDir(lang, st.getFilesByExt(new File(path), DEF_EXT));
    }
}
