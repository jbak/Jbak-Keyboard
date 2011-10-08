package com.jbak.JbakKeyboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import android.os.Environment;
import android.view.inputmethod.InputConnection;

public class Templates
{
	static Templates inst;
	static void destroy()
	{
		if(inst!=null)
			inst = null;
	}
/** �����������. �������������� rootDir */	
	Templates()
	{
		inst = this;
		File f = Environment.getExternalStorageDirectory();
		if(f==null||!f.exists()||!f.canWrite())
		{
			f = Environment.getDataDirectory();
		}
		if(f==null||!f.exists()||!f.canWrite())
		{
			return;
		}
		String rd = f.getAbsolutePath()+File.separatorChar+TEMPLATE_PATH;
		m_rootDir = new File(rd);
		if(!m_rootDir.exists())
		{
			if(!m_rootDir.mkdirs())
				m_rootDir = null;
		}
		m_curDir = m_rootDir;
	}
	void setEditFolder(boolean bSet)
	{
		if(bSet)
		{
			m_state|=STAT_EDIT_FOLDER;
		}
		else
		{
			m_state = st.rem(m_state, STAT_EDIT_FOLDER);
		}
	}
	boolean openFolder(File f)
	{
		m_curDir = f.getAbsoluteFile();
		makeCommonMenu();
		return true;
	}
	boolean isEditFolder()
	{
		return st.has(m_state, STAT_EDIT_FOLDER);
	}
/** ���� ������� �������������� ������� */
	void onCloseEditor()
	{
		setEditFolder(false);
		setEditTpl(null);
		ServiceJbKbd.inst.showWindow(true);
		Templates.inst.makeCommonMenu();
	}
/** ������ ����� � ������ name */	
	void saveFolder(String name)
	{
		String fpath = m_curDir.getAbsolutePath()+File.separator+name;
		File f = new File(fpath);
		f.mkdirs();
	}
/** ��������� ������ � ��������� name � ������� text */	
	void saveTemplate(String name,String text)
	{
		String fpath = m_curDir.getAbsolutePath()+File.separator+name;
		try{
			File f = new File(fpath);
			FileOutputStream os = new FileOutputStream(f);
			os.write(new byte[]{(byte)0xef,(byte)0xbb,(byte)0xbf});
			os.write(text.getBytes());
			os.close();
		}
		catch(Throwable e)
		{
			st.logEx(e);
		}
	}
/** ������������� ���� ������� ��� �������������� � ��������� �������. null - ��� ������ �������*/	
	void setEditTpl(File f)
	{
		m_editFile = f;
	}
/** ����� ��� ��������� ���� ������, ������������ � ���������� */	
	static class FilesComp implements Comparator<File>
	{
		@Override
		public int compare(File object1, File object2)
		{
			boolean bDir1 = object1.isDirectory(), bDir2 = object2.isDirectory(); 
			if(bDir1&&!bDir2)
				return -1;
			else if(!bDir1&&bDir2)
				return 1;
			return object1.getName().compareToIgnoreCase(object2.getName());
		}
	}
	
/** ���������� ������ ��������������� ������ �� ������� ����� �������� */	
	ArrayList<File> getSortedFiles()
	{
		ArrayList<File> ar = new ArrayList<File>();
		try{
			File af[] = m_curDir.listFiles();
			for(int i=0;i<af.length;i++)
			{
				ar.add(af[i]);
			}
			Collections.sort(ar, new FilesComp());
			return ar;
		}
		catch (Throwable e) {
			st.logEx(e);
		}
		return null;
	}
/** ���������� ������ �� ����� f ��� null, ���� ��������� ������<br>
*@param f ���� ��� ������, ����� ������ ���� � ��������� UTF-8
*@return ���������� ����� ��� null
 */
	static String getFileString(File f)
	{
		try{
			FileInputStream fi = new FileInputStream(f);
			byte buf[] = new byte[(int)f.length()];
			fi.read(buf);
			int start = 0;
			if(buf.length>3&&buf[0]==0xef&&buf[1]==0xbb&&buf[2]==0xbf)
			{
				start = 3;
			}
			fi.close();
			return new String(buf, start, buf.length-start);
		}
		catch(Throwable e)
		{
			st.logEx(e);
		}
		return null;
	}
/** ����������� ����������� ���������� � ������ ������� s, ���������� ���������������� ������*/	
	void processTemplate(String s)
	{
		if(s==null)
			return;
		int del = 0;
		int pos = 0;
		int len = s.length();
		String buf[] = null;
		while(true)
		{
			int f = s.indexOf(TPL_SPEC_CHAR, pos);
			if(f<0||f==len-1)
				break;
			if(s.charAt(f+1)==TPL_SPEC_CHAR)
			{
				pos = f+2;
				continue;
			}
			boolean bFound = false;
			for(int i=0;i<Instructions.length;i++)
			{
				String ss = Instructions[i];
				if(s.indexOf(ss, f+1)==f+1)
				{
					bFound = true;
					if(buf==null)
						buf = getInputBuffers();
					String repl = buf[IB_SEL];
					switch(i)
					{
						case 0:  break;
						case 1: 
							if(repl.length()==0)
							{
								if(del==0)
									del=IB_WORD;
								repl = buf[IB_WORD]; 
							}break;
						case 2: if(repl.length()==0)
							{
								del = IB_LINE;
								repl = buf[IB_LINE]; break;
							}break;
					}
					pos = i+repl.length();
					s = s.substring(0,i)+repl+s.substring(i+ss.length()+1);
					break;
				}
				if(!bFound)
					pos++;
			}
		}
		if(del==IB_WORD)
			deleteCurrentWord();
		else if(del==IB_LINE)
			deleteCurrentLine();
		ServiceJbKbd.inst.onText(s);
	}
/** ������������ ������ �� �������� ������� */	
	void processTemplateClick(int index, ArrayList<File> ar)
	{
		if(index<0)
		{
			openFolder(m_curDir.getParentFile());
			return;
		}
		if(index>ar.size())
			return;
		File f = ar.get(index);
		if(f.isDirectory())
		{
			openFolder(f);
		}
		else
		{
			processTemplate(getFileString(f));
		}
	}
/** �������� ������� ��� ������ �������� � CommonMenu*/
	void makeCommonMenu()
	{
		if(m_curDir==null)
			return;
		ComMenu menu = new ComMenu();
		menu.m_state|=ComMenu.STAT_TEMPLATES;
		ArrayList<File> ar = getSortedFiles();
		if(ar==null)
			return;
		Iterator<File> it = ar.iterator();
		int pos = 0;
		if(!m_curDir.getAbsolutePath().equals(m_rootDir.getAbsolutePath()))
		{
			menu.add("[..]",-1);
		}
		while(it.hasNext())
		{
			File f = it.next();
			if(f.isDirectory())
			{
				menu.add("["+f.getName()+"]",pos);
			}
			else
			{
				menu.add(f.getName(),pos);
			}
			pos++;
		}
		st.UniObserver obs = new st.UniObserver(null,ar)
		{
			@SuppressWarnings("unchecked")
			@Override
			int OnObserver(Object param1, Object param2)
			{
				processTemplateClick(((Integer)param1).intValue(),(ArrayList<File>)param2);
				return 0;
			}
		};
		menu.show(obs);
	}
/** ������� ��� ������ ����� ������ � ������, ��������� �� �������.
*@param f1 ������� ������� \r . ��� bLast=true ����������� ����� lastIndexOf, ��� bLast =false - ����� indexOf 
*@param f2 ������� ������� \n. ��� ��������, ������
*@param bLast true - ����� ������� ����� �� �������, false - ���� �� �������
*@param len ������� ����� ������. ���� len<4000 � �� ������ �������� ������ �� ������� - ��� bLast = true ������ 0, ��� bLast=false - len
*@return ���������� ������� ����� ��� ������ ������ ��� -1, ���� �� ������� (����� >=4000 �������� � � �� ��� �� ������ ��������)*/
	static int chkPos(int f1,int f2,boolean bLast,int len)
	{
		int s = 0;
		if(f1>-1&&f2==-1)
			s = bLast?f1+1:f1;
		else if(f2>-1&&f1==-1)
			s = bLast?f2+1:f2;
		else if(f1==-1&&f2==-1)
		{
			if(len<4000)
			{
				if(bLast)
					s = 0;
				else
					s=len;
			}
			else 
				s = -1;
		}
		else 
		{
			if(bLast)
				s = f1>f2?f1:f2;
			else
				s = f1>f2?f2:f1;
		}
		return s;
	}
/** ������� ��� ��������� ������� �� ���������
 * @param positions
 *@return ���������� ������ ������� - ���������� ����� ({@link #IB_SEL}, ������ {@link #IB_LINE}, ����� {@link #IB_WORD} */
	static String[] getInputBuffers()
	{
		try{
			if(ServiceJbKbd.inst==null)
				return null;
			if(ServiceJbKbd.inst.m_SelStart<0||ServiceJbKbd.inst.m_SelEnd<0)
				return null;
			String ret[] = new String[3];
			int ss = ServiceJbKbd.inst.m_SelStart,  se = ServiceJbKbd.inst.m_SelEnd;
			// ss - �������� ������� �������, ����� ���� ������, ��� ss
			InputConnection ic = ServiceJbKbd.inst.getCurrentInputConnection();
			ic.beginBatchEdit();
			String sel="",word="",line="";
			int cnt = se-ss;
			
			int cp = se;
			if(se>ss)
				cp=ss;
			if(cnt<0)
				cnt = 0-cnt;
			if(cnt>0)
			{
				ic.setSelection(cp, cp);
				sel = ic.getTextAfterCursor(cnt, 0).toString();
			}
			cp = se;
			if(cnt>0)
				ic.setSelection(cp, cp);
			CharSequence sec1 = ic.getTextBeforeCursor(4000, 0);
			CharSequence sec2 = ic.getTextAfterCursor(4000, 0);
			String bef = sec1.toString();
			String aft = sec2.toString();
			int s = chkPos(bef.lastIndexOf('\n'), bef.lastIndexOf('\r'), true, bef.length());
			int e = chkPos(aft.indexOf('\n'), aft.indexOf('\r'), false, bef.length());
			if(s!=-1&&e!=-1)
			{
				String s1 = bef.substring(s);
				String s2 =aft.substring(0,e); 
				line=s1+s2;
			}
			word = getCurWordStart(sec1)+getCurWordStart(sec2);
			if(cnt>0)
				ic.setSelection(ss, se);
			ic.endBatchEdit();
			ret[IB_SEL] = sel;
			ret[IB_LINE] = line;
			ret[IB_WORD] = word;
			return ret;
		}
		catch(Throwable e)
		{
			st.logEx(e);
		}
		return null;
	}
/** ���������� ����� ������ ����� ����� �� �������
*@param seq �����, ������ �������� {@link InputConnection#getTextBeforeCursor(int, int)}. ����� ���� null
*@return ����� ������ ����� ��� �������� **/
	static String getCurWordStart(CharSequence seq)
	{
		if(seq==null)
		{
			seq = ServiceJbKbd.inst.getCurrentInputConnection().getTextBeforeCursor(40, 0);
		}
		for(int i=seq.length()-1;i>=0;i--)
		{
			if(!Character.isLetterOrDigit(seq.charAt(i)))
			{
				return seq.subSequence(i+1, seq.length()).toString();
			}
		}
		return "";
	}
	static boolean deleteCurrentLine()
	{
		InputConnection ic = ServiceJbKbd.inst.getCurrentInputConnection(); 
		String bef = ic.getTextBeforeCursor(4000, 0).toString();
		String aft = ic.getTextAfterCursor(4000, 0).toString();
		int s = chkPos(bef.lastIndexOf('\n'), bef.lastIndexOf('\r'), true, bef.length());
		int e = chkPos(aft.indexOf('\n'), aft.indexOf('\r'), false, bef.length());
		if(s==-1||e!=-1)
		{
			return false;
		}
		ic.deleteSurroundingText(bef.length()-s, e);
		return true;
	}
	static void deleteCurrentWord()
	{
		String s1 = getCurWordStart(null);
		String s2 = getCurWordEnd(null);
		ServiceJbKbd.inst.getCurrentInputConnection().deleteSurroundingText(s1.length(), s2.length());
	}
/** ���������� ����� ����� ����� ���� �� �������
*@param seq �����, ������ �������� {@link InputConnection#getTextBeforeCursor(int, int)}. ����� ���� null
*@return ����� ����� ����� ��� �������� **/
	static String getCurWordEnd(CharSequence seq)
	{
		if(seq==null)
		{
			seq=ServiceJbKbd.inst.getCurrentInputConnection().getTextAfterCursor(40, 0);
		}
		int len = seq.length();
		for(int i=0;i<seq.length();i++)
		{
			if(!Character.isLetterOrDigit(seq.charAt(i)))
			{
				return seq.subSequence(0, i).toString();
			}
		}
		return "";
	}
/** ������� ����� */	
	File m_cd;
	public static final String TEMPLATE_PATH = "JbakKeyboard/templates";
	File m_rootDir;
	File m_curDir;
	File m_editFile;
	int m_state=0;
/** ��������� - �������������� ����� */	
	public static final int STAT_EDIT_FOLDER = 0x00001;
	public static final int IB_SEL = 0;
	public static final int IB_WORD = 1;
	public static final int IB_LINE = 2;
	public static final char TPL_SPEC_CHAR = '$';
	public static final String[] Instructions = {"select","selword","selline"};
	
}
