package com.jbak.JbakKeyboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.jbak.JbakKeyboard.IKeyboard.Keybrd;
import com.jbak.JbakKeyboard.IKeyboard.Lang;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.util.DisplayMetrics;
import android.util.Xml;

public class CustomKeyboard extends JbKbd
{
    public static final String A_ANDROID = "android:";
    public static final String TAG_KEYBOARD = "Keyboard";
    public static final String VAL_PERCENT = "%p";
    public static final String VAL_PIXELS = "px";
    public static final String TAG_ROW = "Row";
    public static final String TAG_KEY = "Key";
    public static final String DRW_PREFIX = "@drawable/sym_keyboard_";
    public static final String A_keyWidth = "keyWidth";
    public static final String A_keyHeight = "keyHeight";
    public static final String A_verticalGap = "verticalGap";
    public static final String A_horizontalGap = "horizontalGap";
    
    public static final String A_codes="codes";     
    public static final String A_iconPreview="iconPreview"; 
    public static final String A_isModifier="isModifier";   
    public static final String A_isRepeatable="isRepeatable";   
    public static final String A_isSticky="isSticky";       
    public static final String A_keyEdgeFlags="keyEdgeFlags";   
    public static final String A_keyIcon="keyIcon";     
    public static final String A_keyLabel="keyLabel";       
    public static final String A_keyOutputText="keyOutputText"; 
    public static final String A_popupCharacters="popupCharacters"; 
    public static final String A_popupKeyboard="popupKeyboard";   
    
    int m_displayWidth;
    int m_displayHeight;
    int m_x = 0;
    int m_y = 0;
    Row m_row = null;
    Context m_context;
    boolean m_bBrokenLoad = false;
    public CustomKeyboard(Context context,Keybrd kbd)
    {
        super(context, kbd);
        m_context = context;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        m_displayWidth = dm.widthPixels;
        m_displayHeight = dm.heightPixels;

        try{
            XmlPullParser xp = Xml.newPullParser();
            FileInputStream fs = new FileInputStream(kbd.path);
            xp.setInput(fs, null);
            makeKeyboard(xp,context);
        }
        catch (Throwable e) 
        {
            m_bBrokenLoad = true;
            st.logEx(e);
        }
    }
    void makeKeyboard(XmlPullParser parser, Context context)
    {
        try
        {
            int eventType = parser.getEventType();
            boolean done = false;
            List <Key> keys = getKeys();
            while (eventType != XmlPullParser.END_DOCUMENT && !done)
            {
                String name = null;
                switch (eventType)
                {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if(name.equals(TAG_KEYBOARD))
                            parseKeyboard(parser);
                        else if(name.equals(TAG_ROW))
                            parseRow(parser);
                        else if(name.equals(TAG_KEY))
                            parseKey(parser, keys);
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if(name.equals(TAG_ROW))
                        {
                            m_y+=m_row.defaultHeight+m_row.verticalGap;
                        }
                        break;
                }
                eventType = parser.next();
            }
        }
        catch (XmlPullParserException e) 
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try
        {
            String totalHeight = "mTotalHeight";
            String totalWidth = "mTotalWidth";
            String modKeys = "mModifierKeys";
            Field f;
            f = Keyboard.class.getDeclaredField(totalWidth);
            f.setAccessible(true);
            f.set(this, m_displayWidth);
            f = Keyboard.class.getDeclaredField(totalHeight);
            f.setAccessible(true);
            f.set(this, m_y-getVerticalGap());
            f = Keyboard.class.getDeclaredField(modKeys);
            f.setAccessible(true);
            List<Key> arMod = (List<Key>)f.get(this);
                
        }
        catch (Throwable e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    final boolean parseKeyboard(XmlPullParser p)
    {
        int cnt = p.getAttributeCount();
        for(int i=0;i<cnt;i++)
        {
            String name = attName(p, i);
            if(name.equals(A_keyWidth))
                setKeyWidth(getSize(p.getAttributeValue(i),m_displayWidth,m_displayWidth/10));
            else if(name.equals(A_keyHeight))
                setKeyWidth(getSize(p.getAttributeValue(i),m_displayHeight,50));
            else if(name.equals(A_verticalGap))
                setVerticalGap(getSize(p.getAttributeValue(i),m_displayWidth,0));
            else if(name.equals(A_horizontalGap))
                setVerticalGap(getSize(p.getAttributeValue(i),m_displayHeight,0));
        }
        return true;
    }
    final boolean parseRow(XmlPullParser p)
    {
        m_x = 0;
        m_row = new Row(this);
        m_row.defaultWidth = getKeyWidth();
        m_row.defaultHorizontalGap = 0;
        m_row.verticalGap = 0;
        m_row.defaultHeight = getKeyHeight();
        return true;
    }
    final boolean parseKey(XmlPullParser p, List<Key> keys)
    {
        int cnt = p.getAttributeCount();
        LatinKey k = new LatinKey(m_row);
        k.width = getKeyWidth();
        k.height = getKeyHeight();
        k.gap = getHorizontalGap();
        k.x = m_x;
        k.y = m_y;
        k.pressed = false;
        k.on = false;
        for(int i=0;i<cnt;i++)
        {
            String name = attName(p, i);
            if(name.equals(A_keyWidth))
                k.width = getSize(p.getAttributeValue(i), m_displayWidth, getKeyWidth());
            if(name.equals(A_keyHeight))
                k.height = getSize(p.getAttributeValue(i), m_displayHeight, getKeyWidth());
            if(name.equals(A_codes))
                k.codes = parseCodes(p.getAttributeValue(i));
            if(name.equals(A_keyLabel))
                k.label = processLabel(p.getAttributeValue(i));
            if(name.equals(A_horizontalGap))
                k.gap = getSize(p.getAttributeValue(i), m_displayWidth, getHorizontalGap());
            if(name.equals(A_keyIcon))
                k.icon = getDrawable(p.getAttributeValue(i));
            if(name.equals(A_isSticky))
                k.sticky = getBoolean(p.getAttributeValue(i));
            if(name.equals(A_isRepeatable))
                k.repeatable = getBoolean(p.getAttributeValue(i));
            if(name.equals(A_popupCharacters))
            {
                k.popupResId = R.xml.kbd_empty;
                k.popupCharacters =p.getAttributeValue(i);
            } 
        }
        k.x+=k.gap;
        k.init(m_row);
        if(k.codes!=null&&k.codes.length>0&&k.codes[0]==KEYCODE_SHIFT)
            setShiftKey(keys.size(), k);
        keys.add(k);
        m_x+=k.gap+k.width;
        return true;
    }
    public String processLabel(String label)
    {
        for(int i=0;i<label.length();i++)
        {
            if(label.charAt(i)=='\\'&&i<label.length()-1)
            {
                char c = label.charAt(i+1);
                switch (c)
                {
                    case 'n':
                        c = '\n';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    default:
                    break;
                }
                label = label.substring(0,i)+c+label.substring(i+2);
            }
        }
        return label;
    }
    private int[] parseCodes(String attributeValue)
    {
        attributeValue.trim();
        String a[] = attributeValue.split(",");
        int cd[] = new int[a.length];
        for(int i=0;i<a.length;i++)
            cd[i]=Integer.decode(a[i]);
        return cd;
    }
    final String attName(XmlPullParser p,int index)
    {
        String name = p.getAttributeName(index);
        if(name.startsWith(A_ANDROID))
            return name.substring(A_ANDROID.length());
        return name;
    }
    int getSize(String att,float percentBase,int defValue)
    {
        float ret = defValue;
        if(att.endsWith(VAL_PERCENT))
        {
            String v = att.substring(0,att.length()-VAL_PERCENT.length());
            ret = (float) (Float.valueOf(v)*percentBase/100.0);
        }
        else if(att.endsWith(VAL_PIXELS))
        {
            String v = att.substring(0,att.length()-VAL_PIXELS.length());
            ret = Float.valueOf(v);
        }
        return (int)ret;
    }
    final boolean getBoolean(String att)
    {
        if(att.compareToIgnoreCase("true")==0)return true;
        return false;
    }
    final Drawable getDrawable(String att)
    {
        if(att.startsWith(DRW_PREFIX))
            att = att.substring(DRW_PREFIX.length());
        if(att.equals("delete")) return m_context.getResources().getDrawable(R.drawable.sym_keyboard_delete);
        if(att.equals("done")) return m_context.getResources().getDrawable(R.drawable.sym_keyboard_done);
        if(att.equals("return")) return m_context.getResources().getDrawable(R.drawable.sym_keyboard_return);
        if(att.equals("shift")) return m_context.getResources().getDrawable(R.drawable.sym_keyboard_shift);
        if(att.equals("space")) return m_context.getResources().getDrawable(R.drawable.sym_keyboard_space);    
        if(att.equals("search")) return m_context.getResources().getDrawable(R.drawable.sym_keyboard_search);    
        return null;
    }
    void setShiftKey(int index,Key key)
    {
        try{
            Field f;
            f = Keyboard.class.getDeclaredField("mShiftKey");
            f.setAccessible(true);
            f.set(this, key);
            f = Keyboard.class.getDeclaredField("mShiftKeyIndex");
            f.setAccessible(true);
            f.set(this, index);
        }
        catch (Throwable e) {
        }
    }
    public static String loadCustomKeyboards()
    {
        try{
            String path = st.getSettingsPath()+"keyboards";
            Vector<Keybrd> arKb = new Vector<Keybrd>();
            File f = new File(path);
            if(!f.exists())
            {
                f.mkdirs();
                return null;
            }
            File keyboards[] = f.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String filename)
                {
                    int pos = filename.lastIndexOf('.');
                    if(pos<0)return false;
                    return filename.substring(pos+1).compareTo("xml")==0;
                }
            });
            if(keyboards==null||keyboards.length==0)
                return null;
            for(File kf:keyboards)
            {
                Keybrd k = processCustomKeyboardFile(kf);
                if(k!=null)
                    arKb.add(k);
            }
            if(arKb.size()==0)
                return null;
            int pos = 0;
            for(Keybrd k:st.arKbd)
            {
                if(k.kbdCode==st.KBD_CUSTOM)
                    break;
                ++pos;
            }
            Keybrd ak []=new Keybrd[pos+arKb.size()];
            System.arraycopy(st.arKbd, 0, ak, 0, pos);
            for(Keybrd k:arKb)
            {
                ak[pos]=k;
                ++pos;
            }
            st.arKbd = ak;
        }
        catch(Throwable e)
        {
            
        }
        return null;
    }
    public static Keybrd processCustomKeyboardFile(File kf)
    {
        if(!kf.exists()||!kf.canRead())
            return null;
        String name = kf.getName();
        int f = name.indexOf('_');
        if(f<0)
            return null;
        name = name.substring(0,f);
        Lang lng = null;
        for(Lang l:st.arLangs)
        {
            if(l.name.equals(name))
            {
                lng = l;
                break;
            }
        }
        if(lng==null)
            lng = st.addCustomLang(name);
        Keybrd kb = new Keybrd(st.KBD_CUSTOM, lng, R.xml.kbd_empty, 0);
        kb.path = kf.getAbsolutePath();
        return kb;
    }
}
