package com.jbak.JbakKeyboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class KbdConsole
{
	public static final String CMD_INPUT = "input";
	public static final String ARG_KEYCODE = "keyevent";
	public static KbdConsole getConsole()
	{
		KbdConsole kc = new KbdConsole();
		try
		{
			kc.m_proc = Runtime.getRuntime().exec("su");
		} catch (Throwable e)
		{
			st.log("no root");
			st.logEx(e);
			return null;
		}
		if(!kc.check())
			return null;
		return kc;
	}
	boolean check()
	{
		try
		{
			int av = m_proc.getErrorStream().available();
			if(av>0)
			{
				return false;
			}
			os = m_proc.getOutputStream();
			return true;
		} catch (Throwable e)
		{
			st.logEx(e);
		}
		return false;
	}
	boolean runKeyCode(int keycode)
	{
		String cmd = getKeyCmd(keycode)+"\n";
		try
		{
			os.write(cmd.getBytes());
			os.flush();
			return true;
		} catch (Throwable e)
		{
			st.logEx(e);
		}
		return false;
	}
	static final String getKeyCmd(int keycode)
	{
		return CMD_INPUT+" "+ARG_KEYCODE+" "+keycode;
	}
	OutputStream os;
	Process m_proc;
}
