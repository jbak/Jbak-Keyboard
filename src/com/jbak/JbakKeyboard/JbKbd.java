/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.jbak.JbakKeyboard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import com.jbak.JbakKeyboard.IKeyboard.Keybrd;

public class JbKbd extends Keyboard {
    private LatinKey mEnterKey;
    public Keybrd kbd;
    ArrayList<Replacement> arReplacement = new ArrayList<Replacement>();
    ArrayList<Replacement> tempReplace = new ArrayList<Replacement>();
    public JbKbd(Context context, Keybrd kbd) {
        super(context, kbd.resId);
        this.kbd = kbd;
    }
    @Override
    public int getKeyHeight() 
    {
        if(JbKbdView.inst.m_KeyHeight>0)
        {
            return JbKbdView.inst.m_KeyHeight; 
        }
        return super.getKeyHeight();
    };
    public int getHeightKey()
    {
        return super.getKeyHeight();
    }
    void setHeightKey(int height)
    {
        super.setKeyHeight(height);
    }
    public boolean hasKey(Key k)
    {
        for(Key key:getKeys())
        {
            if(key==k)
                return true;
        }
        return false;
    }
    public final boolean hasReplacements()
    {
        return arReplacement.size()>0;
    }
    public final ArrayList<Replacement> getReplacements(String prefix)
    {
        tempReplace.clear();
        for(Replacement r:arReplacement)
        {
            if(r.isEqual(prefix))
                tempReplace.add(r);
        }
        return tempReplace;
    }
    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, 
            XmlResourceParser parser) {
        LatinKey key = new LatinKey(res, parent, x, y, parser);
        if (key.codes[0] == 10) {
            mEnterKey = key;
        }
        return key;
    }
/** Выставляет на клавишу Enter строку из ресурсов для текущего типа 
*@param res Ресурсы программы
*@param options Тип редактирования, {@link EditorInfo#imeOptions}
 */
    void setImeOptions(Resources res, int options) {
        if (mEnterKey == null) {
            return;
        }
        
        switch (options&(EditorInfo.IME_MASK_ACTION|EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_GO:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_go_key);
                break;
            case EditorInfo.IME_ACTION_NEXT:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_next_key);
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                mEnterKey.icon = res.getDrawable(
                        R.drawable.sym_keyboard_search);
                mEnterKey.label = null;
                break;
            case EditorInfo.IME_ACTION_SEND:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_send_key);
                break;
            default:
                mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_return);
                mEnterKey.label = null;
                break;
        }
        mEnterKey.m_kd = new KeyDrw(mEnterKey);
        mEnterKey.icon = mEnterKey.m_kd.getDrawable();
        mEnterKey.label = null;
    }
    public final boolean resetPressed()
    {
        boolean ret = false;
        for(Key k:getKeys())
        {
            if(k!=null)
            {
                if(!ret)
                    ret = k.pressed;
                k.pressed = false;
                ((LatinKey)k).processed = false;
            }
        }
        return ret;
    }
    LatinKey getKeyByCode(int code)
    {
        List<Key> ar = getKeys();
        for(Iterator<Key>it = ar.iterator();it.hasNext();)
        {
            Key k = it.next();
            if(k.codes!=null)
            {
                for(int c:k.codes)
                {
                    if(c==code)
                        return (LatinKey)k;
                }
            }
        }
        return null;
    }
    public final int getKeyIndex(Key key)
    {
        int pos = 0;
        for(Key k:getKeys())
        {
            if(k==key)
                return pos;
            ++pos;
        }
        return -1;
    }

/** Собственный класс клавиш. Отнаследован от системного. <br>
 * При создании клавиши, если метка содержит разделитель \n - рисуется собственная картинка через {@link KeyDrw} 
 */
    static class LatinKey extends Keyboard.Key {
        
    	/** По нажатию клавиши происходит переход на qwerty-клавиатуру */
        public static final int FLAG_GO_QWERTY     = 0x000001;
    	/** По нажатию клавиши запрещен переход на qwerty-клавиатуру */
        public static final int FLAG_NOT_GO_QWERTY = 0x000002;
    	/** Нажатие клавиши открывает клавиатуру, прописанную в mainText*/
        public static final int FLAG_USER_KEYBOARD = 0x000004;
    	/** Нажатие клавиши выполняет шаблон, прописанный в mainText*/
        public static final int FLAG_USER_TEMPLATE = 0x000008;
    	/** Удержание клавиши открывает клавиатуру, прописанную в longText */
        public static final int FLAG_USER_KEYBOARD_LONG = 0x000010;
    	/** Удержание клавиши выполняет шаблон, прописанный в longText */
        public static final int FLAG_USER_TEMPLATE_LONG = 0x000020;
        KeyDrw m_kd;
        int longCode = 0;
        int specKey = -1;
        public int flags = 0;
        boolean smallLabel = false;
        boolean noColorIcon = false;
        boolean trueRepeat = false;
/** Клавиша обработана по longPress или repeat */        
        boolean processed = false;
        String mainText;
        String longText;
        public LatinKey(Resources res, Keyboard.Row parent, int x, int y, XmlResourceParser parser) {
            super(res, parent, x, y, parser);
            init(parent);
        }
        public LatinKey(Row row)
        {
            super(row);
        }
        void init(Row parent)
        {
//            if(JbKbdView.inst.m_KeyHeight>0)
//            {
//                parent.defaultHeight = JbKbdView.inst.m_KeyHeight; 
//                height = JbKbdView.inst.m_KeyHeight;
//            }
            trueRepeat = repeatable;
            repeatable = false;
            m_kd = new KeyDrw(this);
            m_kd.m_bNoColorIcon = noColorIcon;
            m_kd.setSmallLabel(smallLabel);
            if((codes==null||codes.length>0&&codes[0]==0)&&m_kd.txtMain!=null)
            {
                if(m_kd.txtMain.length()==1&&mainText==null)
                    codes = new int[]{(int)m_kd.txtMain.charAt(0)};
                else
                    codes = new int[]{st.KeySymbol--};
            }
            if(longCode==0&&getUpText()!=null)
            {
                longCode = st.getCmdByLabel(getUpText());
            }
            m_kd.setFuncKey(isFuncKey());
            icon = m_kd.getDrawable();
            label = null;
            iconPreview = icon;
        }
        public final void setGoQwerty(boolean go)
        {
            if(go)flags|=FLAG_GO_QWERTY;
            else flags|=FLAG_NOT_GO_QWERTY;
        }
        public final boolean isGoQwerty()
        {
            return st.has(flags, FLAG_GO_QWERTY);
        }
        public final String getMainText()
        {
            if(mainText!=null)
                return mainText.toString();
            return m_kd.txtMain;
        }
        public final String getUpText()
        {
            if(longText!=null)
                return longText.toString();
            return m_kd.txtSmall;
        }
        boolean isFuncKey()
        {
            if(specKey==1)
                return true;
            else if(specKey==0)
                return false;
            if(codes==null)return false;
            int c = codes[0];
            return c<0||c==10;
        }
        public boolean runSpecialInstructions(boolean longpress)
        {
        	if(!processTemplate(longpress)&&!processUserKeyboard(longpress))
        		return false;
        	return true;
        }
        final boolean processTemplate(boolean longPress)
        {
        	int flag = longPress?FLAG_USER_TEMPLATE_LONG:FLAG_USER_TEMPLATE;
        	if(!st.has(flags, flag))
        		return false;
        	String t = longPress?longText:mainText;
        	Templates.inst.processTemplate(t);
        	return true;
        }
        final boolean processUserKeyboard(boolean longPress)
        {
        	int flag = longPress?FLAG_USER_KEYBOARD_LONG:FLAG_USER_KEYBOARD;
        	if(!st.has(flags, flag))
        		return false;
        	String t = longPress?longText:mainText;
        	if(TextUtils.isEmpty(t))
        		return false;
        	Keybrd kb = null;
        	try{
	        	if(t.startsWith("$$"))
	        	{
	        		kb = new Keybrd(st.getKeybrdForLangName(t.substring(2)));
	        		kb.lang = st.getLangByName(IKeyboard.LANG_SYM_KBD);
	        	}
	        	else if(t.startsWith("$"))
	        	{
	        		kb = new Keybrd(t.substring(1), R.string.kbd_name_qwerty);
	        	}
	        	else
	        	{
	        		if(!t.startsWith("/"))
	        			t = st.getSettingsPath()+CustomKeyboard.KEYBOARD_FOLDER+"/"+t;
	        		kb = new Keybrd(IKeyboard.KBD_SYM, st.getLangByName(IKeyboard.LANG_SYM_KBD), R.xml.kbd_empty, R.string.kbd_name_sym_edit);
	        		kb.path = t;
	        	}
	        	st.kv().setKeyboard(st.loadKeyboard(kb));	
	        	return true;
        	}
        	catch(Throwable e)
        	{
        		e.printStackTrace();
        	}
    		return false;
        }
        final boolean hasLongPress()
        {
            return longCode!=0||getUpText()!=null||codes[0]==10||codes[0]==Keyboard.KEYCODE_SHIFT||st.has(flags, FLAG_USER_KEYBOARD_LONG)||st.has(flags, FLAG_USER_TEMPLATE_LONG);
        }
        @Override
        public void onPressed()
        {
            if(m_kd!=null)
                m_kd.m_bPressed = true;
            super.onPressed();
        }
        @Override
        public void onReleased(boolean inside)
        {
            if(m_kd!=null)
                m_kd.m_bPressed = false;
            super.onReleased(inside);
        }
    }
    public static class Replacement
    {
        public String from;
        public String to;
        public Replacement(String from,String to)
        {
            this.from = from;
            this.to = to;
        }
        boolean isEqual(String prefix)
        {
            if(prefix.length()<from.length())
                return false;
            if(prefix.substring(prefix.length()-from.length()).equals(from))
                return true;
            return false;
        }
    }
}
