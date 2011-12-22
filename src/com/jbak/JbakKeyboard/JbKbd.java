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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.preference.PreferenceManager;
import android.view.inputmethod.EditorInfo;

public class JbKbd extends Keyboard {

    private LatinKey mEnterKey;
    public JbKbd(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
        init();
        resId = xmlLayoutResId;
    }
    public JbKbd(Context context, int layoutTemplateResId, 
            CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
        init();
        resId = layoutTemplateResId;
    }
    void init()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(st.c());
        if(!sp.contains(st.PREF_KEY_DEF_HEIGHT))
        {
            sp.edit().putInt(st.PREF_KEY_DEF_HEIGHT, getHeightKey()).commit();
        }
    }
    public int getHeightKey()
    {
        return super.getKeyHeight();
    }
    void setHeightKey(int height)
    {
        super.setKeyHeight(height);
    }
    int resId;
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
/** Собственный класс клавиш. Отнаследован от системного. <br>
 * При создании клавиши, если метка содержит разделитель \n - рисуется собственная картинка через {@link KeyDrw} 
 */
    static class LatinKey extends Keyboard.Key {
        
        public LatinKey(Resources res, Keyboard.Row parent, int x, int y, XmlResourceParser parser) {
            super(res, parent, x, y, parser);
            if(JbKbdView.inst.m_KeyHeight>0)
            {
                parent.defaultHeight = JbKbdView.inst.m_KeyHeight; 
                height = JbKbdView.inst.m_KeyHeight;
            }
            m_kd = new KeyDrw(this);
            icon = m_kd.getDrawable();
            label = null;
            iconPreview = icon;
        }
        public final String getMainText()
        {
            return m_kd.txtMain;
        }
        public final String getUpText()
        {
            return m_kd.txtSmall;
        }
        KeyDrw m_kd;
    }    
}
