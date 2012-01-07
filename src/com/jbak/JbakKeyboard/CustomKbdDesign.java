package com.jbak.JbakKeyboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Vector;

import android.os.Environment;

import com.jbak.CustomGraphics.GradBack;
import com.jbak.JbakKeyboard.IKeyboard.KbdDesign;

public class CustomKbdDesign
{
    public static final int P_KeyBackStartColor              = 0;
    public static final int P_KeyBackEndColor                = 1;
    public static final int P_KeyBackGradientType            = 2;
    public static final int P_KeyTextColor                   = 3;
    public static final int P_KeyTextBold                    = 4;
    public static final int P_KeyGapSize                     = 5;
    public static final int P_KeyStrokeStartColor            = 6;
    public static final int P_KeyStrokeEndColor              = 7;
    public static final int P_KeyboardBackgroundStartColor   = 8;
    public static final int P_KeyboardBackgroundEndColor     = 9;
    public static final int P_KeyboardBackgroundGradientType = 10;
    public static final int P_SpecKeyBackStartColor         =11;
    public static final int P_SpecKeyBackEndColor =12;
    public static final int P_SpecKeyStrokeStartColor = 13;
    public static final int P_SpecKeyStrokeEndColor = 14;
    public static final int P_SpecKeyTextColor = 15;
    int errorLine = 0;
    String arNames[] = new String[]{
          "KeyBackStartColor",
          "KeyBackEndColor",
          "KeyBackGradientType",
          "KeyTextColor",
          "KeyTextBold",
          "KeyGapSize",
          "KeyStrokeStartColor",
          "KeyStrokeEndColor",
          "KeyboardBackgroundStartColor",
          "KeyboardBackgroundEndColor",
          "KeyboardBackgroundGradientType",
          "SpecKeyBackStartColor",
          "SpecKeyBackEndColor",
          "SpecKeyStrokeStartColor",
          "SpecKeyStrokeEndColor",
          "SpecKeyTextColor"
    };
    String skinPath = "";
    Vector<IntEntry> arValues = new Vector<IntEntry>();
    boolean load(String path)
    {
        skinPath = path;
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(path));
        }catch (Throwable e) {
            st.logEx(e);
        }
        if(reader == null)
        {
            return false;
        }
        return load(reader);
    }
    boolean load(BufferedReader r)
    {
        int line = 1;
        try{
            String s;
            while((s = r.readLine())!=null)
            {
                int index = parseParam(s);
                if(index>-1)
                {
                    int dec = -1;
                    dec = processStringInt(m_val);
                    arValues.add(new IntEntry(index,dec));
                }
                ++line;
            }
        }
        catch (Throwable e) {
            errorLine = line;
            return false;
        }
        return true;
    }
    int getIntValue(int index,int defVal)
    {
        for(IntEntry ie:arValues)
        {
            if(ie.index==index)
                return ie.value;
        }
        return defVal;
    }
    KbdDesign getDesign()
    {
        KbdDesign ret = new KbdDesign(0, 0, st.DEF_COLOR, 0, 0);
        ret.path = skinPath;
        int startColor,endColor,gradType;
        startColor = getIntValue(P_KeyBackStartColor, st.DEF_COLOR);
        endColor = getIntValue(P_KeyBackEndColor, st.DEF_COLOR);
        gradType = getIntValue(P_KeyBackGradientType, GradBack.GRADIENT_TYPE_LINEAR);
        int gap = getIntValue(P_KeyGapSize, GradBack.DEFAULT_GAP);
        if(startColor!=st.DEF_COLOR)
        {
            ret.setKeysBackground(new GradBack(startColor, endColor)
                                       .setGradType(gradType)
                                       .setGap(gap));
        }
        startColor = getIntValue(P_KeyboardBackgroundStartColor, st.DEF_COLOR);
        endColor = getIntValue(P_KeyboardBackgroundEndColor, st.DEF_COLOR);
        gradType = getIntValue(P_KeyboardBackgroundGradientType, GradBack.GRADIENT_TYPE_LINEAR);
        if(startColor!=st.DEF_COLOR)
        {
            ret.setKbdBackground(new GradBack(startColor, endColor)
                                       .setGradType(gradType)
                                       .setGap(0)
                                       .setCorners(0, 0)
                                       );
        }
        startColor = getIntValue(P_KeyStrokeStartColor, st.DEF_COLOR);
        endColor = getIntValue(P_KeyStrokeEndColor, st.DEF_COLOR);
        if(startColor!=st.DEF_COLOR&&ret.m_keyBackground!=null)
        {
            ret.m_keyBackground.setStroke(
                    new GradBack(startColor,endColor)
                        .setGap(gap-1)
                            );
        }
        
        ret.textColor = getIntValue(P_KeyTextColor, st.DEF_COLOR);
        if(getIntValue(P_KeyTextBold, 0)==1)
            ret.flags|=st.DF_BOLD;
        startColor = getIntValue(P_SpecKeyBackStartColor, st.DEF_COLOR);
        endColor = getIntValue(P_SpecKeyBackEndColor, st.DEF_COLOR);
        if(startColor!=st.DEF_COLOR)
        {
            int textColor = getIntValue(P_SpecKeyTextColor, st.DEF_COLOR);
            GradBack gb = new GradBack(startColor, endColor).
            setGradType(ret.m_kbdBackground.m_gradType)
            .setGap(gap);
            startColor = getIntValue(P_SpecKeyStrokeStartColor, st.DEF_COLOR);
            endColor = getIntValue(P_SpecKeyStrokeEndColor, st.DEF_COLOR);
            if(startColor!=st.DEF_COLOR)
                gb.setStroke(new GradBack(startColor, endColor));
            ret.setFuncKeysDesign(new KbdDesign(0, 0, textColor, 0, 0).setKeysBackground(gb));
        }
        return ret;
    }
    int parseParam(String s)
    {
        int index = -1;
        if(s==null)
            return index;
        s = s.trim();
        if(s.length()==0)
            return index;
        int f = s.indexOf('=');
        if(f<0)return index;
        String name = s.substring(0,f).trim();
        index = findName(name);
        if(index>-1)
        {
            m_val = s.substring(f+1);
        }
        return index;
    }
    String m_val;
    int findName(String name)
    {
        int index = 0;
        for(String s:arNames)
        {
            if(s.compareTo(name)==0)
                return index;
            index++;
        }
        return -1;
    }
    public static class IntEntry{
        int index=-1;
        int value=-1;
        public IntEntry()
        {}
        public IntEntry(int i,int v)
        {index = i;value=v;}
    }
    int processStringInt(String s)
    {
        s = s.trim();
        if(s.indexOf('#')==0||s.startsWith("0x")) // 16-ричное значение
        {
            return parseInt(s.substring(1),16);
        }
        return Integer.valueOf(s);
    }
    String getErrString()
    {
        if(errorLine>0)
            return "Parse err: "+new File(skinPath).getName()+", line: "+errorLine+"\n";
        else
            return "Can't read: "+new File(skinPath).getName();

    }
    static String loadCustomSkins()
    {
        String err = "";
        try{
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                          +"/jbakKeyboard/skins";  
            File dir = new File(path);
            if(!dir.exists()||!dir.isDirectory())
                return err;
            File skins[] = dir.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String filename)
                {
                    int pos = filename.lastIndexOf('.');
                    if(pos<0)return false;
                    return filename.substring(pos+1).compareTo("skin")==0;
                }
            });
            Vector<KbdDesign> ar = new Vector<IKeyboard.KbdDesign>();
            for(File fs:skins)
            {
                CustomKbdDesign skin = new CustomKbdDesign();
                if(skin.load(fs.getAbsolutePath()))
                {
                    ar.add(skin.getDesign());
                }
                else
                {
                   err+=skin.getErrString();
                }
            }
            int pos = 0;
            for(KbdDesign kd:st.arDesign)
            {   
                if(kd.path!=null)
                {
                    break;
                }
                ++pos;
            }
            KbdDesign des[] = new KbdDesign[pos+ar.size()];
            System.arraycopy(st.arDesign, 0, des, 0, pos);
            for(KbdDesign kd:ar)
            {
                des[pos]=kd;
                pos++;
            }
            st.arDesign = des;
        }
        catch (Throwable e) {
            return "System error";
        }
        return err;
    }
    private static int parseInt(String string,int radix) {
        int result = 0;
        int degree = 1;
        for(int i=string.length()-1;i>=0;i--)
        {
            int digit = Character.digit(string.charAt(i), radix);
            if (digit == -1) {
                break;
            }
            result+=degree*digit;
            degree*=radix;
        }
        return result;
    }
}
