package com.jbak.JbakKeyboard;

import com.jbak.CustomGraphics.GradBack;

import android.graphics.Color;


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
 // Коды клавиатур  
    public static final int KBD_QWERTY_EN = 0;
    public static final int KBD_QWERTY_RU = 1;
    public static final int KBD_QWERTY_BE = 2;
    public static final int KBD_QWERTY_UA = 3;
    public static final int KBD_QWERTY_RU_HALF=4;
    public static final int KBD_QWERTY_EN_HALF=5;
    
 /** Массив ресурсов для клавиатуры */    
    public static Keybrd[] arKbd = 
    {
        new Keybrd(KBD_QWERTY_EN,       arLangs[LANG_EN],       R.xml.qwerty_en,        R.string.kbd_name_qwerty),
        new Keybrd(KBD_QWERTY_RU,       arLangs[LANG_RU],       R.xml.qwerty_ru,        R.string.kbd_name_qwerty),
        new Keybrd(KBD_QWERTY_BE,       arLangs[LANG_BE],       R.xml.qwerty_be,        R.string.kbd_name_qwerty),
        new Keybrd(KBD_QWERTY_UA,       arLangs[LANG_UK],       R.xml.qwerty_ua,        R.string.kbd_name_qwerty),
        new Keybrd(KBD_QWERTY_RU_HALF,  arLangs[LANG_RU],       R.xml.qwerty_ru_tablet, R.string.kbd_name_qwerty_tablet),
        new Keybrd(KBD_QWERTY_EN_HALF,  arLangs[LANG_EN],       R.xml.qwerty_en_tablet, R.string.kbd_name_qwerty_tablet),
    };
// Флаги дизайна (Design Flags)
/** Жирный шрифт */    
    public static final int DF_BOLD = 0x0001;
/** Большой отступ, {@link KeyDrw#BIG_GAP} */    
    public static final int DF_BIG_GAP = 0x0002;

    public static final int DEF_COLOR = 123;
    public static final int KBD_DESIGN_STANDARD = 0;
    public static final int KBD_DESIGN_IPHONE = 1;
    public static KbdDesign[] arDesign=
    {
        // Стандартный дизайн 
        new KbdDesign(R.string.kbd_design_standard, 
                      0, 
                      DEF_COLOR,
                      0,
                      0),
        // iPhone
        new KbdDesign(R.string.kbd_design_iphone, 
                      R.drawable.iphone_btn_keyboard_key, 
                      Color.BLACK,
                      R.drawable.iphone_background,
                      DF_BOLD|DF_BIG_GAP),
        // Украина
      new KbdDesign(R.string.kbd_design_ukraine, 
              		0, 
              		Color.YELLOW,
              		0,
              		DF_BOLD)
        	.setKeysBackground(new GradBack(0xff060a6c, 0xff1199af).setGradType(GradBack.GRADIENT_TYPE_SWEEP))
        	.setKbdBackground(new GradBack(Color.CYAN, Color.YELLOW).setGap(0).setCorners(0, 0))
            	,
        // Шоколад
        new KbdDesign(R.string.kbd_design_chokolate, 
                      0, 
                      0xffffffc0,
                      0,
                      DF_BOLD)
              .setKeysBackground(new GradBack(0xff75412b, 0xffc16643).setGradType(GradBack.GRADIENT_TYPE_SWEEP))
              .setKbdBackground(new GradBack(0xff400000, GradBack.DEFAULT_COLOR).setGap(0).setCorners(0, 0))
                  ,
      // RGB
      new KbdDesign(R.string.kbd_design_rgb, 
                    0, 
                    Color.RED,
                    0,
                    DF_BOLD)
            .setKeysBackground(new GradBack(0xff00ff00, 0xff008800).setGradType(GradBack.GRADIENT_TYPE_SWEEP))
            .setKbdBackground(new GradBack(0xff000088, 0xff0000ff).setGap(0).setCorners(0, 0))
                ,
        // Рожденный в СССР    	
      new KbdDesign(R.string.kbd_design_ussr, 
                0, 
                Color.YELLOW,
                0,
                0)
        .setKeysBackground(new GradBack(0xff800000, 0xffc00000).setGradType(GradBack.GRADIENT_TYPE_SWEEP))
        .setKbdBackground(new GradBack(0xffff0000,0xfff31c0d).setCorners(0, 0).setGap(0))
        ,
    };
//*****************************************************************    
    /** Класс для хранения оформлений клавиатур */
    public static class KbdDesign
    {
        public KbdDesign(int name,int drawable,int textColor,int backDrawable,int flags)
        {
            nameResId = name; 
            drawResId=drawable;
            this.textColor = textColor;
            this.backDrawableRes = backDrawable;
            this.flags = flags;
        }
        KbdDesign setKeysBackground(GradBack bg)
        {
        	m_keyBackground = bg;
        	return this;
        }
        KbdDesign setKbdBackground(GradBack bg)
        {
        	m_kbdBackground = bg;
        	return this;
        }
/** Id drawable-ресурса для рисования кнопок */     
        public int drawResId;
/** Id ресурса названия клавиатуры*/        
        public int nameResId;
/** Цвет текста */      
        public int textColor;
/** drawable-ресурс для рисования фона клавиатуры*/        
        public int backDrawableRes;
/** Флаги*/        
        public int flags=0;
        GradBack m_keyBackground=null;
        GradBack m_kbdBackground=null;
    }
    
//*****************************************************************    
/** Класс для хранения сведений о языке */    
    public static class Lang
    {
        public Lang(int lang,String name, int strId)
        {
            this.lang = lang;
            this.name = name;
            this.strId = strId;
        }
        /** Код языка, одна из констант LANG_ */
        public int lang;
        /** Символьный код языка ("ru" - для русского, "en" - для английского)*/
        public String name;
        /** Строка с названием языка из ресурсов */
        public int strId;
    }
//*****************************************************************    
/** Класс для хранения сведений о конкретной клавиатуре */    
    public static class Keybrd
    {
/** Конструктор
 * @param kbdCode Код клавиатуры, одна из констант KBD_
 * @param lang  Язык клавиатуры, один из элементов массива arLangs
 * @param resId XML-ресурс клавиатуры (из R.xml)
 * @param resName Строка из ресурсов с названием клавиатуры 
 */
        Keybrd(int kbdCode,Lang lang, int resId,int resName)
        {
            
            this.kbdCode = kbdCode;
            this.lang = lang;
            this.resId = resId;
            this.resName = resName;
        }
/** Язык клавиатуры, один из элементов массива arLangs*/        
        public Lang lang;
/** XML-ресурс клавиатуры (из R.xml) */     
        public int resId;
/** Код клавиатуры, одна из констант KBD_*/     
        public int kbdCode;
/** Строка из ресурсов с названием клавиатуры  */       
        public int resName;
    }
//-----------------------------------------------------------------------------    
    public static Keybrd defKbd()
    {
        return arKbd[0];
    }
//-----------------------------------------------------------------------------    
/** Возвращает язык по внутреннему коду, константы KBD_ */
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
/** Возвращает клавиатуру по коду клавиатуры из ресурсов */    
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
