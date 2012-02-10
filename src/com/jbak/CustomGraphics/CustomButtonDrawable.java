package com.jbak.CustomGraphics;

import android.R;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

public class CustomButtonDrawable extends StateListDrawable
{
    GradBack m_grad;
    Drawable m_drw;
/** Объект, зависящий от текущего, получает уведомления при изменении размеров и статусов */    
    StateListDrawable m_dependedDrawable;
	public CustomButtonDrawable()
	{
		m_grad = new GradBack();
	}
	public CustomButtonDrawable(GradBack gb)
	{
		m_grad = gb;
        m_drw = m_grad.getDrawable();
        addState(new int[]{}, m_drw);
        addState(new int[]{R.attr.state_checkable}, m_drw);
        addState(new int[]{R.attr.state_checkable,R.attr.state_checked}, m_drw);
        addState(new int[]{R.attr.state_checkable,R.attr.state_pressed}, m_drw);
	}
	public CustomButtonDrawable setCorners(int radiusX,int radiusY)
	{
		m_grad.setCorners(radiusX, radiusY);
		return this;
	}
	@Override
	protected boolean onStateChange(int[] stateSet)
	{
        if(m_dependedDrawable!=null)
            m_dependedDrawable.setState(stateSet);
		if(m_grad!=null)
			m_grad.changeState(stateSet);
		super.onStateChange(stateSet);
		invalidateSelf();
		return false;
	}
	@Override
	public Drawable getCurrent()
	{
		return m_drw;
	}
	@Override
	protected void onBoundsChange(Rect bounds)
	{
	    if(m_dependedDrawable!=null)
	        m_dependedDrawable.setBounds(bounds);
	    super.onBoundsChange(bounds);
	}
	public void setDependentDrawable(StateListDrawable depDrawable)
    {
        m_dependedDrawable = depDrawable;
    }
	@Override 
	public boolean getPadding(Rect padding)
	{
	    int g = m_grad.m_gap+1;
	    padding.set(g, g, g, g);
	    return true;
	}
}
