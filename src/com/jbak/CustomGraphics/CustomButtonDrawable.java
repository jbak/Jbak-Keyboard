package com.jbak.CustomGraphics;

import android.R;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

public class CustomButtonDrawable extends StateListDrawable
{
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
	GradBack m_grad;
	Drawable m_drw;
	@Override
	protected boolean onStateChange(int[] stateSet)
	{
		if(m_grad!=null)
			m_grad.changeState(stateSet);
		return super.onStateChange(stateSet);
	}
	@Override
	public Drawable getCurrent()
	{
		return m_drw;
	}
}
