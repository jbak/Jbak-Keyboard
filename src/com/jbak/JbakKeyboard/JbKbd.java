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

import java.util.Iterator;
import java.util.List;

import com.google.ads.f;
import com.jbak.JbakKeyboard.IKeyboard.Keybrd;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

public class JbKbd extends Keyboard {

    private LatinKey mEnterKey;
    public Keybrd kbd;
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
            if(k.codes!=null&&k.codes.length>0&&k.codes[0]==code)
                return (LatinKey)k;
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
        
        public static final int FLAG_GO_QWERTY     = 0x000001;
        public static final int FLAG_NOT_GO_QWERTY = 0x000002;
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
        boolean hasLongPress()
        {
            return longCode!=0||getUpText()!=null||codes[0]==10||codes[0]==Keyboard.KEYCODE_SHIFT;
        }
    }    
}
