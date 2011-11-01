package com.jbak.JbakKeyboard;


public class IKeyboard
{
  //--------------------------------------------------------------------------
    public static final int LANG_EN = 0;
    public static final int LANG_RU = 1;
    public static final int LANG_UK = 2;
    public static final int LANG_BE = 3;
//--------------------------------------------------------------------------
    public static Lang[] arLangs = 
    {
    	new Lang(LANG_EN,"en", R.string.lang_en),
    	new Lang(LANG_RU,"ru", R.string.lang_ru),
    	new Lang(LANG_UK,"uk", R.string.lang_ua),
    	new Lang(LANG_BE,"be", R.string.lang_be),
    };
//--------------------------------------------------------------------------
 // ���� ��������� 	
    public static final int KBD_QWERTY_EN = 0;
    public static final int KBD_QWERTY_RU = 1;
    public static final int KBD_QWERTY_BE = 2;
    public static final int KBD_QWERTY_UA = 3;
    public static final int KBD_QWERTY_RU_HALF=4;
    public static final int KBD_QWERTY_EN_HALF=5;
    
 /** ������ �������� ��� ���������� */    
    public static Keybrd[] arKbd = 
    {
    	new Keybrd(KBD_QWERTY_EN,		arLangs[LANG_EN],		R.xml.qwerty_en,		R.string.kbd_name_qwerty),
    	new Keybrd(KBD_QWERTY_RU,		arLangs[LANG_RU],		R.xml.qwerty_ru,		R.string.kbd_name_qwerty),
    	new Keybrd(KBD_QWERTY_BE,		arLangs[LANG_BE],		R.xml.qwerty_be,		R.string.kbd_name_qwerty),
    	new Keybrd(KBD_QWERTY_UA,		arLangs[LANG_UK],		R.xml.qwerty_ua,		R.string.kbd_name_qwerty),
    	new Keybrd(KBD_QWERTY_RU_HALF,	arLangs[LANG_RU],		R.xml.qwerty_ru_tablet,	R.string.kbd_name_qwerty_tablet),
    	new Keybrd(KBD_QWERTY_EN_HALF,	arLangs[LANG_EN],		R.xml.qwerty_en_tablet,	R.string.kbd_name_qwerty_tablet),
    };
//*****************************************************************    
/** ����� ��� �������� �������� � ����� */    
    public static class Lang
    {
    	public Lang(int lang,String name, int strId)
    	{
    		this.lang = lang;
    		this.name = name;
    		this.strId = strId;
    	}
    	/** ��� �����, ���� �� �������� LANG_ */
    	public int lang;
    	/** ���������� ��� ����� ("ru" - ��� ��������, "en" - ��� �����������)*/
    	public String name;
    	/** ������ � ��������� ����� �� �������� */
    	public int strId;
    }
//*****************************************************************    
/** ����� ��� �������� �������� � ���������� ���������� */    
    public static class Keybrd
    {
/** �����������
 * @param kbdCode ��� ����������, ���� �� �������� KBD_
 * @param lang 	���� ����������, ���� �� ��������� ������� arLangs
 * @param resId XML-������ ���������� (�� R.xml)
 * @param resName ������ �� �������� � ��������� ���������� 
 */
    	Keybrd(int kbdCode,Lang lang, int resId,int resName)
    	{
    		
    		this.kbdCode = kbdCode;
    		this.lang = lang;
    		this.resId = resId;
    		this.resName = resName;
    	}
/** ���� ����������, ���� �� ��������� ������� arLangs*/    	
    	public Lang lang;
/** XML-������ ���������� (�� R.xml) */    	
    	public int resId;
/** ��� ����������, ���� �� �������� KBD_*/    	
    	public int kbdCode;
/** ������ �� �������� � ��������� ����������  */    	
    	public int resName;
    }
//-----------------------------------------------------------------------------    
    public static Keybrd defKbd()
    {
    	return arKbd[0];
    }
//-----------------------------------------------------------------------------    
/** ���������� ���� �� ����������� ����, ��������� KBD_ */
    public static Keybrd kbdForCode(int kbd)
    {
    	for(int i=0;i<arKbd.length;i++)
    	{
    		Keybrd l = arKbd[i];
    		if(kbd==l.kbdCode)
    			return l;
    	}
    	return null;
    }
//-----------------------------------------------------------------------------    
/** ���������� ���������� �� ���� ���������� �� �������� */    
    public static Keybrd kbdForId(int rid)
    {
    	for(int i=0;i<arKbd.length;i++)
    	{
    		Keybrd l = arKbd[i];
    		if(rid==l.resId)
    			return l;
    	}
    	return null;
    }
    public static final int KEYCODE_LANG_CHANGE = -20;
}
