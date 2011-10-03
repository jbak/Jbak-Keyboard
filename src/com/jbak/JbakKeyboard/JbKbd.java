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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.inputmethodservice.Keyboard;
import android.preference.PreferenceManager;
import android.view.inputmethod.EditorInfo;

public class JbKbd extends Keyboard {

    private Key mEnterKey;
    
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
        Key key = new LatinKey(res, parent, x, y, parser);
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
                mEnterKey.icon = res.getDrawable(
                        R.drawable.sym_keyboard_return);
                mEnterKey.label = null;
                break;
        }
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
/** Собственная картинка для клавиш. Генерируется на основе {@link Key#label}*/    
    static class KeyDrw extends RectShape
    {
    	public static final String DRW_PREFIX = "d_"; 
/** Конструктор
 * @param lab Текст метки, {@link Key#label}
 * @param w Ширина клавиши, {@link Key#width}
 * @param h Высота клавиши. Всегда одинаковая, поскольку не используются клавиши разной высоты
 */
    	public KeyDrw(CharSequence lab,int w,int h,boolean bPreview)
		{
    		m_bPreview = bPreview;
        	int f = lab.toString().indexOf('\n');
        	if(f>-1)
        	{
        		txtSmall = lab.subSequence(0, f).toString();
        		bmp = st.getBitmapByCmd(st.getCmdByLabel(txtSmall));
        		txtMain = lab.subSequence(f+1, lab.length()).toString();
        	}
        	else
        	{
        		txtMain = lab.toString();
        		txtSmall = null;
        	}
        	rb = new Rect(0,0,w,h);
		}
    	Rect rb;
		@Override
		public void draw(Canvas canvas, Paint paint)
		{
//			Rect rb = canvas.getClipBounds();
			
			Paint p1 = JbKbdView.inst.m_tpMainKey;
			if(m_bPreview)
			{
				p1 = JbKbdView.inst.m_tpPreview;
			}
			else
			{
				canvas.translate(0-rb.width()/2, 0-rb.height()/2);
				if(txtMain.length()>1)
				{
					p1 = st.kv().m_tpLabel;
				}
			}
			Paint p2 = JbKbdView.inst.m_tpSmallKey;
			if(txtMain.length()==1)
			{
				if(JbKbdView.inst.isUpperCase())
					txtMain = txtMain.toUpperCase();
				else
					txtMain = txtMain.toLowerCase();
			}
// txtMain всегда рисуем по центру canvas;
// txtSmall, если позволяет остаток от верха txtMain до верха canvas - рисуем выше.
// Если не позволяет - то правее
			float mw = p1.measureText(txtMain);
			float y = rb.height()/2+(0-p1.ascent())/2;
			float x = rb.width()/2-mw/2;
			canvas.drawText(txtMain, x, y, p1);
			if(txtSmall!=null&&!m_bPreview)
			{
				if(bmp!=null)
				{
					x = rb.width()/2-bmp.getWidth()/2;
					canvas.drawBitmap(bmp, x, 4, null);
					return;
				}
				float asc = 0-p2.ascent()+4;
				float sw = p2.measureText(txtSmall);
				if(y>=p2.getTextSize())
				{
					x = rb.width()/2-sw/2;
					canvas.drawText(txtSmall, x, asc, p2);
				}
				else
				{
					x+=mw+1;
					canvas.drawText(txtSmall, x, asc, p2);
				}
			}
		}
		Bitmap bmp;
		String txtMain;
		String txtSmall;
		boolean m_bPreview = false;
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
            if(icon==null)
            {
            	icon = createLabel(width, parent.defaultHeight);
            	if(icon!=null)
            	{
            		iconPreview = createPreviewLabel();
            		label = null;
            	}
            }
            
        }
        Drawable createPreviewLabel()
        {
        	int w = JbKbdView.inst.m_PreviewHeight;
        	KeyDrw d = new KeyDrw(label,w,w,true);
        	ShapeDrawable drw = new ShapeDrawable(d);
        	drw.setBounds(0, 0, w, w);
        	return drw;
        }
        Drawable createLabel(int width,int height)
        {
        	int f = label.toString().indexOf('\n');
        	if(f<0)
        	{
        		return null;
        	}
        	m_kd = new KeyDrw(label,width,height,false);
        	ShapeDrawable drw = new ShapeDrawable(m_kd);
        	drw.setBounds(0, 0, width, height);
        	return drw;
        }
    	KeyDrw m_kd;
    	
    }    
        /**
         * Overriding this method so that we can reduce the target area for the key that
         * closes the keyboard. 
         */
/*        @Override
        public boolean isInside(int x, int y) {
            return super.isInside(x, codes[0] == KEYCODE_CANCEL ? y - 10 : y);
        }
*/
}
