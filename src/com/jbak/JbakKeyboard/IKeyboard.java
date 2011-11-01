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
 //  оды клавиатур 	
    public static final int KBD_QWERTY_EN = 0;
    public static final int KBD_QWERTY_RU = 1;
    public static final int KBD_QWERTY_BE = 2;
    public static final int KBD_QWERTY_UA = 3;
    public static final int KBD_QWERTY_RU_HALF=4;
    public static final int KBD_QWERTY_EN_HALF=5;
    
 /** ћассив ресурсов дл€ клавиатуры */    
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
/**  ласс дл€ хранени€ сведений о €зыке */    
    public static class Lang
    {
    	public Lang(int lang,String name, int strId)
    	{
    		this.lang = lang;
    		this.name = name;
    		this.strId = strId;
    	}
    	/**  од €зыка, одна из констант LANG_ */
    	public int lang;
    	/** —имвольный код €зыка ("ru" - дл€ русского, "en" - дл€ английского)*/
    	public String name;
    	/** —трока с названием €зыка из ресурсов */
    	public int strId;
    }
//*****************************************************************    
/**  ласс дл€ хранени€ сведений о конкретной клавиатуре */    
    public static class Keybrd
    {
/**  онструктор
 * @param kbdCode  од клавиатуры, одна из констант KBD_
 * @param lang 	язык клавиатуры, один из элементов массива arLangs
 * @param resId XML-ресурс клавиатуры (из R.xml)
 * @param resName —трока из ресурсов с названием клавиатуры 
 */
    	Keybrd(int kbdCode,Lang lang, int resId,int resName)
    	{
    		
    		this.kbdCode = kbdCode;
    		this.lang = lang;
    		this.resId = resId;
    		this.resName = resName;
    	}
/** язык клавиатуры, один из элементов массива arLangs*/    	
    	public Lang lang;
/** XML-ресурс клавиатуры (из R.xml) */    	
    	public int resId;
/**  од клавиатуры, одна из констант KBD_*/    	
    	public int kbdCode;
/** —трока из ресурсов с названием клавиатуры  */    	
    	public int resName;
    }
//-----------------------------------------------------------------------------    
    public static Keybrd defKbd()
    {
    	return arKbd[0];
    }
//-----------------------------------------------------------------------------    
/** ¬озвращает €зык по внутреннему коду, константы KBD_ */
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
/** ¬озвращает клавиатуру по коду клавиатуры из ресурсов */    
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
