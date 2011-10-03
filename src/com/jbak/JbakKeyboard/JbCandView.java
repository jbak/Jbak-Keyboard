package com.jbak.JbakKeyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class JbCandView extends LinearLayout
{
	public JbCandView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
//		m_spin = (Spinner)findViewById(android.R.id.candidatesArea);
/*		m_spin.setOnHierarchyChangeListener(new OnHierarchyChangeListener()
		{
			@Override
			public void onChildViewRemoved(View parent, View child)
			{
				
			}
			@Override
			public void onChildViewAdded(View parent, View child)
			{
			}
		});
*/	}
	Spinner m_spin;
}
