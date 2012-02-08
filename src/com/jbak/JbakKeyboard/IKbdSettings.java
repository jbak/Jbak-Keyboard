package com.jbak.JbakKeyboard;

import android.view.KeyEvent;

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
/** @deprecated Ключ, int, хранящий высоту клавиш в портретном режиме */    
    public static final String PREF_KEY_HEIGHT_PORTRAIT = "kh";
    /** Ключ, float, хранящий высоту клавиш в портретном режиме */    
    public static final String PREF_KEY_HEIGHT_PORTRAIT_PERC = "kh_p";
/** @deprecated Ключ, int, хранящий высоту клавиш в ландшафтном режиме */    
    public static final String PREF_KEY_HEIGHT_LANDSCAPE = "khl";
    public static final String PREF_KEY_HEIGHT_LANDSCAPE_PERC = "kh_l";
/** Ключ, int, хранящий высоту клавиш по умолчанию, на всякий случай */    
    public static final String PREF_KEY_DEF_HEIGHT = "dh";
/** Ключ, String, хранящий порядок переключения языков */    
    public static final String PREF_KEY_LANGS = "langs";
/** @deprecated Ключ, boolean, хранящий настройку вибро при коротком нажатии 
 *  Заменено на PREF_KEY_VIBRO_SHORT_TYPE */
    public static final String PREF_KEY_VIBRO_SHORT_KEY = "vs";
/** Ключ, String, тип вибро при коротком нажатии. "0" - нет, "1" - при отпускании, "2" - при нажатии*/    
    public static final String PREF_KEY_VIBRO_SHORT_TYPE = "vibro_short";
/** Ключ, boolean, хранящий настройку вибро при коротком нажатии */    
    public static final String PREF_KEY_VIBRO_LONG_KEY = "vl";
/** Ключ, boolean, хранящий настройку проигрывания звуков */    
    public static final String PREF_KEY_SOUND = "sound";
/** Ключ, String, хранящий путь к клавиатуре для выбраного языка в портрете
 *  Полный ключ выглядит как PREF_KEY_LANG_KBD+"en".*/    
    public static final String PREF_KEY_LANG_KBD_PORTRAIT = "kp_path_";
/** Ключ, String, хранящий путь к клавиатуре для выбраного языка в ландшафте
 *  Полный ключ выглядит как PREF_KEY_LANG_KBD+"en".*/
    public static final String PREF_KEY_LANG_KBD_LANDSCAPE = "kl_path_";
/**@deprecated
 *  Ключ, int, хранящий индекс текущего скина
 *  Уже не юзается. Но его надо выпилить со старых версий */    
    public static final String PREF_KEY_KBD_SKIN = "kbd_skin";
/** Ключ, String. Для сторонних скинов - путь к скину, для встроенных - индекс в виде строки?*/    
    public static final String PREF_KEY_KBD_SKIN_PATH = "kbd_skin_path";
/** Ключ, boolean - глобальный ключ, включающий/отключающий автоматическую смену регистра */    
    public static final String PREF_KEY_AUTO_CASE = "up_sentence";
/** Ключ, boolean - включает/отключает смену регистра после набора символов */    
    public static final String PREF_KEY_UP_AFTER_SYMBOLS = "up_after_symbols";
/** Ключ, String, список символов для перехода в верхний регистр */    
    public static final String PREF_KEY_SENTENCE_ENDS = "sentence_ends";
/** Ключ, boolean - смена регистра только после символа из PREF_KEY_SENTENCE_ENDS и следующего за ним пробела*/    
    public static final String PREF_KEY_UPERCASE_AFTER_SPACE = "space_after_sentence";
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
    /** Ключ, String, количество записей в буфере обмена (преобразуется в int)*/
    public static final String PREF_KEY_CLIPBRD_SIZE = "clipboard_size";
    /** Ключ, String, интервал вибро для короткого нажатия*/
    public static final String PREF_KEY_VIBRO_SHORT_DURATION = "vibro_short_duration";
    /** Ключ, String, интервал вибро для удержания*/
    public static final String PREF_KEY_VIBRO_LONG_DURATION = "vibro_long_duration";
    /** Ключ, int, интервал длинного нажатия в ms*/
    public static final String PREF_KEY_LONG_PRESS_INTERVAL = "int_long_press";
    /** Ключ, int, интервал первого повтора в ms*/
    public static final String PREF_KEY_REPEAT_FIRST_INTERVAL = "int_first_repeat";
    /** Ключ, int, интервал следующих повторов в ms*/
    public static final String PREF_KEY_REPEAT_NEXT_INTERVAL = "int_next_repeat";
    
    /** Ключ, none, ключ для пункта сохранения настроек*/
    public static final String PREF_KEY_SAVE = "save";
    /** Ключ, none, ключ для пункта загрузки настроек*/
    public static final String PREF_KEY_LOAD = "load";

    /** Ключ, boolean, true - включает использование жестов */
    public static final String PREF_KEY_USE_GESTURES = "use_gestures";
    /** Ключ, String, команда для жеста влево (код команды)*/
    public static final String PREF_KEY_GESTURE_LEFT = "g_left";
    /** Ключ, String, команда для жеста вправо (код команды)*/
    public static final String PREF_KEY_GESTURE_RIGHT = "g_right";
    /** Ключ, String, команда для жеста вверх (код команды)*/
    public static final String PREF_KEY_GESTURE_UP = "g_up";
    /** Ключ, String, команда для жеста вниз (код команды)*/
    public static final String PREF_KEY_GESTURE_DOWN = "g_down";
    
    
    public static final String EXT_XML = "xml";
    public static final String SETTINGS_BACKUP_FILE= "settings_backup"+'.'+EXT_XML;
    
    
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
/** Внутреняя команда - переключение языка */    
    public static final int CMD_LANG_CHANGE = -20;
/** Внутреняя команда - компиляция клавиатур */    
    public static final int CMD_COMPILE_KEYBOARDS = -10000;
    
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
    public static final String ZERO_STRING = "0";
    public static final String ONE_STRING = "1";

    public static class KbdGesture
    {
        public KbdGesture(int name,int cod)
        {
            nameId = name;
            code = cod;
        }
        int nameId;
        int code;
    }
    public static KbdGesture arGestures[] = new KbdGesture[]{
        new  KbdGesture(R.string.cmd_none, 0),
        new  KbdGesture(R.string.cmd_lang_change, CMD_LANG_CHANGE),
        new  KbdGesture(R.string.cmd_left, KeyEvent.KEYCODE_DPAD_LEFT),
        new  KbdGesture(R.string.cmd_right, KeyEvent.KEYCODE_DPAD_RIGHT),
        new  KbdGesture(R.string.cmd_up, KeyEvent.KEYCODE_DPAD_UP),
        new  KbdGesture(R.string.cmd_down, KeyEvent.KEYCODE_DPAD_DOWN),
        new  KbdGesture(R.string.mm_multiclipboard, CMD_CLIPBOARD),
        new  KbdGesture(R.string.mm_templates, CMD_TPL),
        new  KbdGesture(R.string.mm_settings, CMD_PREFERENCES),
    };
}
