package com.jbak.CustomGraphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;

/** Класс для рисования прямоугольников со скругленными углами, залитых градиентом или цветом */
public class GradBack extends RectShape
{
	public static final int DEFAULT_CORNER_X = 4;
	public static final int DEFAULT_CORNER_Y = 4;
	public static final int DEFAULT_GAP = 2;
	public static final int DEFAULT_COLOR = 12345678;
	public static final int GRADIENT_TYPE_LINEAR = 0;
	public static final int GRADIENT_TYPE_SWEEP = 1;
	RectF m_rect = new RectF();
	Paint m_ptFill;
	Paint m_ptFillPressed;
	Paint m_ptFillCheckable;
	Paint m_ptFillChecked;
/** Цвет начала градиента */	
	int m_clrStart;
/** Цвет конца градиента */	
	int m_clrEnd;
/** Отступ краёв фона от прямоугольника, на котором фон отрисовывается */	
	int m_gap = DEFAULT_GAP;
/** Текущее состояние фона */	
	int m_states[];
/** Радиус скругления прямоугольника в пикселях по X*/	
	int m_cornerX = DEFAULT_CORNER_X;
/** Радиус скругления прямоугольника в пикселях по Y*/	
	int m_cornerY = DEFAULT_CORNER_Y;
	boolean m_bDrawPressedBack = true;
/** Тип градиента, одна из констант GRADIENT_TYPE */	
	int m_gradType = GRADIENT_TYPE_LINEAR;
    int shadowColor = Color.RED;
/** Фон обводки. Если не null - рисуется под основным фоном, и в этом случае будет рисоваться его тень*/    
    GradBack m_stroke=null;
/** Толщина обводки*/    
    int m_strokeSize=1;
    int abrisColor = DEFAULT_COLOR;
/** Пустой конструктор*/	
	public GradBack()
	{
		m_ptFillPressed = newColorPaint(0x44ffffff);
		m_ptFillChecked = newColorPaint(Color.GREEN);
		m_ptFillCheckable = newColorPaint(Color.DKGRAY);
	}
/** Конструктор, задающий цвета градииента
 * @param startColor Начальный цвет градиента
 * @param endColor Конечный цвет градиента */
	public GradBack (int startColor,int endColor)
	{
		this();
		set(startColor,endColor);
	}
/** Установка начального и конечного цветов градиента  
 * @param startColor Начальный цвет градиента
 * @param endColor Конечный цвет градиента. Если равен {@link #DEFAULT_COLOR} 
 * 		  - то фон заполняется цветом startColor*/
	public GradBack set(int startColor,int endColor)
	{
		m_clrStart = startColor;
		m_clrEnd = endColor;
		return this;
	}
/** Установка отступа краёв фона от прямоугольника, на котором фон отрисовывается
 * @param gap Значение отступа в пикселях
 * @return Возвращает текущий объект */	
	public GradBack setGap(int gap)
	{
		m_gap = gap;
		return this;
	}
/** Установка типа градиента 
*@param gradType Тип градиента, одна из констант GRADIENT_TYPE
*@return Возвращает текущий объект */
	public GradBack setGradType(int gradType)
	{
		this.m_gradType = gradType;
		return this;
	}
	public GradBack setDrawPressedBackground(boolean bDrawPressed)
	{
	    m_bDrawPressedBack = bDrawPressed;
	    return this;
	}
/** Устанавливает радиус скругления углов 
*@param cx Радиус скругления по оси X
*@param cy Радиус скругления по оси Y
*@return Возвращает текущий объект */
	public GradBack setCorners(int cx,int cy)
	{
		m_cornerX = cx;
		m_cornerY = cy;
		return this;
	}
/** Возвращает новый объект для отрисовки с предустановленными параметрами */
	protected Paint newPaint()
	{
		Paint ret = new Paint();
		ret.setDither(true);
		ret.setAntiAlias(true);
		return ret;
	}
/** Возвращает новый объект для заливки цветом */
	protected Paint newColorPaint(int color)
	{
		Paint ret = newPaint();
		ret.setColor(color);
        ret.setStyle(Style.FILL);
		return ret;
	}
/** Возвращает объект {@link Drawable}, который содержит текущий объект*/	
	public Drawable getDrawable()
	{
		return new ShapeDrawable(this);
	}
/** Возвращает объект {@link CustomButtonDrawable}, который содержит текущий объект*/   
	public CustomButtonDrawable getStateDrawable()
	{
		return new CustomButtonDrawable(this);
	}
	protected Paint makeBackground(float width, float height)
	{
        if(m_clrEnd==DEFAULT_COLOR)
        {
            return newColorPaint(m_clrStart);
        }
        Paint pt = newPaint();
        if(m_gradType==GRADIENT_TYPE_SWEEP)
        {
            pt.setShader(new SweepGradient(width, height, m_clrStart, m_clrEnd));
        }
        else
        {
            pt.setShader(new LinearGradient(0, 0, 0, height, m_clrStart, m_clrEnd, TileMode.CLAMP));
        }
        pt.setStyle(Style.FILL);
        if(shadowColor!=DEFAULT_COLOR&&m_stroke==null)
            pt.setShadowLayer(2, 2, 2, shadowColor);
        return pt;
	}
/** Действия по установке размеров текущего объекта */	
	@Override
	protected void onResize(float width, float height) 
	{
        m_ptFill = makeBackground(width, height);
        if(m_stroke!=null)
            m_stroke.onResize(width, height);
		super.onResize(width, height);
        m_rect.set(m_gap, m_gap, width-m_gap, height-m_gap);
	};
/** Отрисовка */	
	@Override
	public void draw(Canvas canvas, Paint paint)
	{
	    if(m_stroke!=null)
	    {
	        m_stroke.draw(canvas, paint);
	    }
		canvas.drawRoundRect(m_rect, m_cornerX, m_cornerY, m_ptFill);
//		if(m_bDrawPressedBack&&hasState(android.R.attr.state_pressed))
//			canvas.drawRoundRect(m_rect, m_cornerX, m_cornerY, m_ptFillPressed);
		
		boolean bChecked = hasState(android.R.attr.state_checked);
		boolean bCheckable = hasState(android.R.attr.state_checkable);
		if(bCheckable||bChecked)
		    onDrawCheckMark(canvas,bChecked, m_rect);
	}
/** Функция отрисовки метки для состояний checked и checkable.
 * Вызывается только при наличии этих состояний
*@param canvas Canvas для отрисовки 
*@param bCheck true - помечено, false - нет 
*@param rect Прямоугольник, на котором производится отрисовка */
	public void onDrawCheckMark(Canvas canvas,boolean bCheck,RectF rect)
	{
        RectF rr = new RectF(m_rect.left+4, m_rect.top+4, m_rect.left+12, m_rect.top+12);
        canvas.drawArc(rr, 0, 360, false, bCheck?m_ptFillChecked:m_ptFillCheckable);
	}
/** Обработчик изменения состояния */	
	public void changeState(int []states)
	{
	    boolean bPress = hasState(android.R.attr.state_pressed);
		m_states = new int[states.length];
		System.arraycopy(states, 0, m_states, 0, states.length);
		if(m_ptFill!=null)
		{
		    if(hasState(android.R.attr.state_pressed))
		        m_ptFill.setColorFilter(new PorterDuffColorFilter(0xff888888, PorterDuff.Mode.MULTIPLY));
		    else if(bPress)
		        m_ptFill.setColorFilter(null);
		}
		    
	}
    public GradBack setShadowColor(int shadow)
    {
        shadowColor = shadow;
        return this;
    }
    public GradBack setAbrisColor(int color)
    {
        abrisColor = color;
        return this;
    }
/** Устанавливает обводку stroke, в виде еще одного объекта {@link GradBack}
 * Необходимо правильно задать отступ (gap) для нового объекта, с учетом того, что сверху будет нарисован текущий объект
*@param stroke Объект для рисования обводки 
*@return Текущий объект
 */
    public GradBack setStroke(GradBack stroke)
    {
        m_stroke = stroke;
        return this;
    }
/** Проверяет наличие статуса s в массиве текущих статусов 
*@param s Проверяемый статус, одна из констант <b>android.R.attr.state_</b>
*@return true - статус есть, false - нет */
	public boolean hasState(int s)
	{
	    if(m_states==null)
	        return false;
		for(int stat:m_states)
		{
			if(stat==s)
				return true;
		}
		return false;
	}
}
