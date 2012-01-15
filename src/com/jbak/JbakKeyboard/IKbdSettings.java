package com.jbak.JbakKeyboard;
/** Константы для настроек клавиатуры */
public interface IKbdSettings
{
	//-------------------------------------------------------------------    
	/// Ключи для SharedPreferences
	//-------------------------------------------------------------------    

/** Ключ, boolean, хранящий значение "включить/отключить просмотр клавиш" */    
    public static final String PREF_KEY_PREVIEW = "ch_preview";
/** Ключ, int ,хранящий код последней используемой клавиатуры */    
    public static final String PREF_KEY_LAST_LANG = "lastLng";
/** Ключ, int, хранящий высоту клавиш в портретном режиме */    
    public static final String PREF_KEY_HEIGHT_PORTRAIT = "kh";
/** Ключ, int, хранящий высоту клавиш в ландшафтном режиме */    
    public static final String PREF_KEY_HEIGHT_LANDSCAPE = "khl";
/** Ключ, int, хранящий высоту клавиш по умолчанию, на всякий случай */    
    public static final String PREF_KEY_DEF_HEIGHT = "dh";
/** Ключ, String, хранящий порядок переключения языков */    
    public static final String PREF_KEY_LANGS = "langs";
/** Ключ, boolean, хранящий настройку вибро при коротком нажатии */    
    public static final String PREF_KEY_VIBRO_SHORT_KEY = "vs";
/** Ключ, boolean, хранящий настройку вибро при коротком нажатии */    
    public static final String PREF_KEY_VIBRO_LONG_KEY = "vl";
/** Ключ, boolean, хранящий настройку проигрывания звуков */    
    public static final String PREF_KEY_SOUND = "sound";
/** Ключ, int, хранящий клавиатуру для выбраного языка в портрете
 *  Полный ключ выглядит как PREF_KEY_LANG_KBD+"en".
 *  Хранит индекс клавиатуры в массиве, который возвращает функция st.getKeybrdArrayByLang() */    
    public static final String PREF_KEY_LANG_KBD_PORTRAIT = "langkbd_portrait_";
/** Ключ, int, хранящий клавиатуру для выбраного языка в ландшафте
 *  Полный ключ выглядит как PREF_KEY_LANG_KBD+"en".
 *  Хранит индекс клавиатуры в массиве, который возвращает функция st.getKeybrdArrayByLang() */    
    public static final String PREF_KEY_LANG_KBD_LANDSCAPE = "langkbd_landscape_";
/** Ключ, int, хранящий индекс текущего скина*/    
    public static final String PREF_KEY_KBD_SKIN = "kbd_skin";
/** Ключ, boolean - предложения с большой буквы */    
    public static final String PREF_KEY_SENTENCE_UPPERCASE = "up_sentence";
/** Ключ, String, список символов для перехода в верхний регистр */    
    public static final String PREF_KEY_SENTENCE_ENDS = "sentence_ends";
/** Ключ, boolean - добавление пробела после конца предложения */    
    public static final String PREF_KEY_SENTENCE_SPACE = "space_sentence";
/** Ключ, boolean - переход в верхний регистр в пустом поле */    
    public static final String PREF_KEY_EMPTY_UPPERCASE = "up_empty";
    /** Ключ, String - набор символов, после которых вставляется пробел*/    
    public static final String PREF_KEY_ADD_SPACE_SYMBOLS = "space_symbols";
    /** Ключ, String, тип ландшафтного редактора.  Одна из констант PREF_VAL_EDIT_TYPE_ , в видк строки */
    public static final String PREF_KEY_LANSCAPE_TYPE = "landscape_type";
    /** Ключ, String, тип портретного редактора.  Одна из констант PREF_VAL_EDIT_TYPE_ , в видк строки */
    public static final String PREF_KEY_PORTRAIT_TYPE = "portrait_type";
    /** Ключ, String, настройки редактора ExtractedText*/
    public static final String PREF_KEY_EDIT_SETTINGS = "edit_set";
    /** Ключ, String, настройка основного шрифта */
    public static final String PREF_KEY_MAIN_FONT = "pMainFont";
    /** Ключ, String, настройка шрифта дополнительных символов */
    public static final String PREF_KEY_SECOND_FONT = "pSecondFont";
    /** Ключ, String, настройка шрифта меток */
    public static final String PREF_KEY_LABEL_FONT = "pLabelFont";
    /** Ключ, String, варианты клавиши Shift, строка со значениями:0 - 3-позиц., 1 - normal/shift, 2 - normal/capslock */
    public static final String PREF_KEY_SHIFT_STATE = "shift_state";
    /** Ключ, String, задержка нажатий клавиш в ms (хранится в виде строки, по умолчанию - 0)*/
    public static final String PREF_KEY_REPEAT_DELAY = "key_repeat";
    

    public static final int PREF_VAL_EDIT_TYPE_DEFAULT = 0;
    public static final int PREF_VAL_EDIT_TYPE_FULLSCREEN = 1;
    public static final int PREF_VAL_EDIT_TYPE_NOT_FULLSCREEN = 2;
	//-------------------------------------------------------------------    
	/// Константы для запуска настроечных активностей
	//-------------------------------------------------------------------    
    
/** Значение для запуска {@link SetKbdActivity} - настройка высоты клавиш в портретном режиме */    
    public static final int SET_KEY_HEIGHT_PORTRAIT = 1;
/** Значение для запуска {@link SetKbdActivity} - настройка высоты клавиш в ландшафтном режиме */    
    public static final int SET_KEY_HEIGHT_LANDSCAPE =2;
/** Вызывает настройку переключения языков */    
    public static final int SET_LANGUAGES_SELECTION =3;
/** Вызывает настройку клавиш*/    
    public static final int SET_KEYS =4;
/** Вызывает настройку вида клавиатуры (обычный, для планешета...)*/    
    public static final int SET_SELECT_KEYBOARD = 5;
    /** Вызывает настройку внешнего вида клавиатуры (стандартный, айфон..)*/    
    public static final int SET_SELECT_SKIN= 6;
    
	//-------------------------------------------------------------------    
	/// Список команд 
	//-------------------------------------------------------------------    

/** Внутреняя команда - открывает главное меню*/   
    public static final int CMD_MAIN_MENU = -500;
 /** Внутреняя команда - голосовой ввод */   
    public static final int CMD_VOICE_RECOGNIZER = -501;
/** Внутреняя команда - показ шаблонов на клавиатуре */ 
    public static final int CMD_TPL = -502;
/** Внутреняя команда - запуск настроек */  
    public static final int CMD_PREFERENCES = -503;
/** Внутреняя команда - запуск мультибуфера обмена */   
    public static final int CMD_CLIPBOARD = -504;
/** Внутреняя команда - создание папки шаблонов */  
    public static final int CMD_TPL_NEW_FOLDER = -505;
    /** Внутреняя команда - запуск редактора шаблонов */    
    public static final int CMD_TPL_EDITOR = -506;

  //-------------------------------------------------------------------    
  /// Прочие строковые значения
  //-------------------------------------------------------------------    
    
/** Значение для запуска {@link SetKbdActivity}. С этим ключом передаётся параметр типа int<br>
 *  Параметр int - одно из значений SET_*/    
    public static final String SET_INTENT_ACTION = "sa";
 /**  Параметр String - название языка, для которого производится выбор клавиатуры */    
    public static final String SET_INTENT_LANG_NAME = "sl";
    
/** Строковый префикс для кнопки, где метка для длинного нажатия является иконкой */
    public static final String DRW_PREFIX = "d_"; 
/** Тэг для записи в logcat*/
    public static final String TAG = "JBK";

}
