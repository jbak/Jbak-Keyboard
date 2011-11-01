package com.jbak.JbakKeyboard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.shapes.RectShape;
import android.inputmethodservice.Keyboard.Key;
import android.text.TextPaint;

/** Собственная картинка для клавиш. Генерируется на основе {@link Key#label}*/    
class KeyDrw extends RectShape
{
	public static DrawMetrics dm = null; 
	public static final String DRW_PREFIX = "d_"; 
	Bitmap bmp;
	String txtMain;
	String txtSmall;
	boolean m_bPreview = false;
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
    	dm.processKey(this);
	}
	Rect rb;
	public static final int GAP = 5;
	public static final int DELIM = 2;
	@Override
	public void draw(Canvas canvas, Paint paint)
	{
//		Rect rb = canvas.getClipBounds();
		
		Paint p1 = JbKbdView.inst.m_tpMainKey;
		if(m_bPreview)
		{
//Рисуем просмотр				
			p1 = JbKbdView.inst.m_tpPreview;
			float mw = p1.measureText(txtMain);
			float y = rb.height()/2+(0-p1.ascent())/2;
			float x = rb.width()/2-mw/2;
			canvas.drawText(txtMain, x, y, p1);
			return;
		}
//Если текст длинее 1 символа - считаем меткой и рисуем с помощью JbKbdView.m_tpLabel 
		if(txtMain.length()>1)
		{
			p1 = st.kv().m_tpLabel;
		}
//canvas сдвинут к середине, вернём его в позицию 0,0			
		canvas.translate(0-rb.width()/2, 0-rb.height()/2);
		Paint p2 = JbKbdView.inst.m_tpSmallKey;
		if(txtMain.equals("123"))
		{
			int d1 = 0;
			int d2 = d1;
		}
		if(txtMain.length()==1)
		{
			if(JbKbdView.inst.isUpperCase())
				txtMain = txtMain.toUpperCase();
			else
				txtMain = txtMain.toLowerCase();
		}
		Rect b = new Rect();
		p1.getTextBounds(txtMain, 0, txtMain.length(), b);
		int a1 = (int) (0-p1.ascent());
		int a2 = (int) (0-p2.ascent());
		int d1 = (int) p1.descent();
		int d2 = (int) p2.descent();
		int w1,h1=d1+a1,w2,h2;
		if(bmp!=null)
		{
			h2 = bmp.getHeight();
			w2 = bmp.getWidth();
		}
		else
		{
			h2 = a2+d2;
			w2 = (int) p2.measureText(txtSmall);
		}
		w1 = (int) p1.measureText(txtMain);
		int fh = GAP*2+h1+h2+DELIM; // Полная высота
		if(fh>rb.height())
		{
			// Всё изображение не уместится
			int x2 = rb.width()-GAP-w2;
			if(bmp!=null)
			{
				canvas.drawBitmap(bmp, x2, GAP, p2);
			}
			else
			{
				canvas.drawText(txtSmall, x2, GAP+a2, p2);
			}
			int x = rb.width()/2-w1/2;
			while(x+w1>x2-DELIM&&x>GAP)
				--x;
			canvas.drawText(txtMain, x, rb.height()-GAP-d1, p1);
		}
		else
		{
			// Всё изображение умещается
			if(bmp!=null)
			{
				canvas.drawBitmap(bmp, rb.width()/2-w2/2, GAP, p2);
			}
			else
			{
				canvas.drawText(txtSmall, rb.width()/2-w2/2, GAP+a2, p2);
			}
			canvas.drawText(txtMain, rb.width()/2-w1/2, rb.height()-GAP-d1, p1);
			
		}
		
	}
/** Через этот класс пропускаются все клавиши при их создании.
 * По окончании создания необходимо вызвать функцию finish(), в которой решим, как именно рисовать клавиши */
	static class DrawMetrics
	{
/** Отступы от краёв кнопки*/		
		int gap = 5;
		int maxLabelWidth = 0;
		int maxLabelHeight = 0;
		int maxTextWidth = 0;
		int maxTextHeight = 0;
		int maxTextAscent = 0;
		int maxTextDescent = 0;
		int maxLabelAscent = 0;
		int maxLabelDescent = 0;
		
		Rect bounds = new Rect();
/** При создании все клавиши пропускаются через эту функцию, определяющую макс. ширину и высоту элементов*/		
		void processKey(KeyDrw kd)
		{
			int lw = 0,lh = 0,w=0,h=0;
			if(keyHeight==0)
				keyHeight = kd.rb.height();
			if(kd.bmp!=null)
			{
				lw = kd.bmp.getWidth();
				lh = kd.bmp.getHeight();
			}
			else
			{
				st.kv().m_tpSmallKey.getTextBounds(kd.txtSmall, 0, kd.txtSmall.length(), bounds);
				lw = bounds.width();
				lh = bounds.height();
				if(0-bounds.top>maxLabelAscent)maxLabelAscent = 0-bounds.top;
				if(0-bounds.bottom>maxLabelDescent)maxLabelDescent = 0-bounds.bottom;
			}
			if(kd.txtMain.length()>1)
			{
				st.kv().m_tpLabel.getTextBounds(kd.txtMain, 0, kd.txtMain.length(), bounds);	
			}
			else
			{
				st.kv().m_tpMainKey.getTextBounds(kd.txtMain.toLowerCase(), 0, kd.txtMain.length(), bounds);	
			}
			w = bounds.width();
			h = bounds.height();
			if(0-bounds.top>maxTextAscent)maxTextAscent = 0-bounds.top;
			if(0-bounds.bottom>maxTextDescent)maxTextDescent = 0-bounds.bottom;
			if(maxLabelWidth<lw) maxLabelWidth = lw;				
			if(maxLabelHeight<lh) maxLabelHeight = lh;
			if(maxTextWidth<w) maxTextWidth = w;				
			if(maxTextHeight<h)maxTextHeight = h;
		}
/** false - основной и верхний тексты рисуются друг над другом. true - основной текст прибиваем к левому краю,  */		
		boolean bLeftAlign = false;
		int mainBaseLine = 0;
		int labelBaseLine = 0;
		int minSpace = 2;
		int keyHeight = 0;
/** Подбиваем бабки. Знаем макс. ширину и высоту всех элементов, выставляем флаги рисования */		
		void finish()
		{
			int th = gap*2+minSpace+maxLabelHeight+maxTextHeight; // высота всего изображения
			int dh = keyHeight-th; // Разница между высотами кнопки и изображения
			labelBaseLine = gap+maxLabelAscent;
			if(dh<=0)
			{
				// Изображение в высоту не помещается
				bLeftAlign = true;
				// Основной текст рисуется внизу
				mainBaseLine = keyHeight-gap;
//				mainBaseLine = (int) (keyHeight-gap-st.kv().m_tpMainKey.descent());
			}
			else
			{
				// Изображение помещается в высоту
				bLeftAlign = false;
				// Пробуем разместить основной текст по центру
				mainBaseLine = keyHeight-gap-maxTextAscent;//gap+maxLabelHeight+minSpace+maxTextAscent;//(int) (keyHeight/2+maxTextAscent/2);
			}
		}
	}
}
