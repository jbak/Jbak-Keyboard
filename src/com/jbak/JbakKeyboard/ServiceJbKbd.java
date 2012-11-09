/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.jbak.JbakKeyboard;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Vector;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.inputmethodservice.ExtractEditText;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import com.jbak.JbakKeyboard.EditSetActivity.EditSet;
import com.jbak.JbakKeyboard.IKeyboard.Keybrd;
import com.jbak.JbakKeyboard.JbKbd.LatinKey;
import com.jbak.JbakKeyboard.Templates.CurInput;
import com.jbak.ctrl.SameThreadTimer;
import com.jbak.words.IWords.WordEntry;
import com.jbak.words.Words;
import com.jbak.words.WordsService;

/** Основной сервис клавиатуры */
public class ServiceJbKbd extends InputMethodService implements KeyboardView.OnKeyboardActionListener, OnSharedPreferenceChangeListener
{
    AudioManager m_audio;
    String                     m_defaultWords          = ".,?!@/";
    static final boolean       PROCESS_HARD_KEYS       = true;
    boolean       PROCESS_VOLUME_KEYS       = true;
    /** Автодополнение отсутствует */
    public static final int    SUGGEST_NONE            = 0;
    /** Автодополнение из словаря */
    public static final int    SUGGEST_VOCAB           = 1;
    /** Автодополнение из программы, которой принадлежит текущий ввод */
    public static final int    SUGGEST_OWN             = 2;
/** Тип дополнений, одна из констант SUGGEST_ */    
    int                        m_suggestType           = SUGGEST_NONE;
    public boolean             m_bComplete             = true;
    public static final String PID                     = "a14ef033de91702";
    public boolean m_acAutocorrect = false;
/** Место, в котором показано окно автодополнения */    
    int m_acPlace = JbCandView.AC_PLACE_TITLE;
/** Текущий просмотр кандидатов */    
    JbCandView                 m_candView;
/** Просмотр кандидатов, прикрепленный к клавиатуре*/    
    JbCandView                 m_kbdCandView;
    WindowManager m_wm;
    static ServiceJbKbd        inst;
    /** Символы концов предложений */
    String                     m_SentenceEnds          = "";
    String                     m_SpaceSymbols          = "";
    boolean                    m_bForceShow            = false;
    int                        m_state                 = 0;
    int                        m_PortraitEditType      = st.PREF_VAL_EDIT_TYPE_DEFAULT;
    int                        m_LandscapeEditType     = st.PREF_VAL_EDIT_TYPE_DEFAULT;
    /** Разрешена автоматическая смена регистра */
    public static final int    STATE_AUTO_CASE         = 0x00000001;
    /** Статус - вставка пробела после конца предложения */
    public static final int    STATE_SENTENCE_SPACE    = 0x0000002;
    /** Статус - верхний регистр в пустом поле */
    public static final int    STATE_EMPTY_UP          = 0x0000004;
    /** Статус - верхний регистр только после пробела */
    public static final int    STATE_SPACE_SENTENCE_UP = 0x0000008;
    /** Статус - предложения с большой буквы после символов из строки
     * {@link #m_SentenceEnds} */
    public static final int    STATE_UP_AFTER_SYMBOLS  = 0x0000010;
    public static final int    STATE_GO_END            = 0x00001000;
    boolean                        m_bBackProcessed = false;
    Rect m_cursorRect;
    EditSet                    m_es                    = new EditSet();
    boolean                    m_bCanAutoInput         = false;
/** Обработка клавиш громкости. 0:нет, 1:+ влево, - вправо, 2: - влево, + вправо */
    int m_volumeKeys = 0;
    float m_soundVolume = 5;
    final static int MSG_SHOW_PANEL = 0x11;
    Handler                    m_autoCompleteHandler   = new Handler()
   {
       @Override
       public void handleMessage(android.os.Message msg)
       {
           if (msg.what == Words.MSG_GET_WORDS)
           {
               onWords((Vector<WordEntry>) msg.obj);
           }
           else if (msg.what == MSG_SHOW_PANEL)
           {
               if(isInputViewShown()&&ComMenu.inst==null)
                   m_candView.show(st.kv(), m_acPlace);
           }
       };
   };

    @Override
    public void onCreate()
    {
        super.onCreate();
        if(JbKbdPreference.inst!=null)
            JbKbdPreference.inst.onStartService();
        m_audio = (AudioManager)getSystemService(Service.AUDIO_SERVICE);
        inst = this;
        m_wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        WordsService.g_serviceHandler = m_autoCompleteHandler;
        WordsService.start(this);
        st.upgradeSettings(inst);
        startService(new Intent(this, ClipbrdService.class));
        m_candView = createNewCandView();
        SharedPreferences pref = st.pref();
        m_es.load(st.PREF_KEY_EDIT_SETTINGS);
        pref.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(pref, null);
    }
    /** Завершение работы сервиса */
    @Override
    public void onDestroy()
    {
//        JbKbdView.inst = null;
        removeCandView();
        if (ClipbrdService.inst != null)
            ClipbrdService.inst.stopSelf();
        st.pref().unregisterOnSharedPreferenceChangeListener(this);
        KeyboardPaints.inst = null;
        if (VibroThread.inst != null)
            VibroThread.inst.destroy();
        if (st.kv() != null)
        {
            st.kv().setOnKeyboardActionListener(null);
        }
        inst = null;
        super.onDestroy();
    }

    /** Стартует ввод */
    @Override
    public View onCreateInputView()
    {
        getLayoutInflater().inflate(R.layout.input, null);
        JbKbdView.inst.setOnKeyboardActionListener(this);
        st.setQwertyKeyboard();
        return JbKbdView.inst;
    }

    /** Должен вернуть просмотр кандидатов или null */
    @Override
    public View onCreateCandidatesView()
    {
//        m_kbdCandView = createNewCandView();
//        return m_kbdCandView;
        return null;
    }
    JbCandView createNewCandView()
    {
        return (JbCandView) getLayoutInflater().inflate(R.layout.candidates, null);
    }
    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting)
    {
        super.onStartInputView(attribute, restarting);
        if(!restarting)
        {
        m_SelStart = attribute.initialSelStart;
        m_SelEnd = attribute.initialSelEnd;
        }
        m_textAfterCursor = null;
        m_textBeforeCursor = null;
        if (JbKbdView.inst == null)
        {
            getLayoutInflater().inflate(R.layout.input, null);
            setInputView(st.kv());
            JbKbdView.inst.setOnKeyboardActionListener(this);
        }
        int var = attribute.inputType & EditorInfo.TYPE_MASK_VARIATION;
        switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS)
        {
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_DATETIME:
            case EditorInfo.TYPE_CLASS_PHONE:
                m_bCanAutoInput = false;
                st.setNumberKeyboard();
            break;
            default:
                m_bCanAutoInput = canAutoInput(attribute);
                if (m_bCanAutoInput)
                    changeCase(false);
                else
                    st.kv().setTempShift(false, false);
                if (var == EditorInfo.TYPE_TEXT_VARIATION_URI || var == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS || var == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD || var == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                    st.setTempEnglishQwerty();
                else
                    st.setQwertyKeyboard();
            break;
        }
        st.curKbd().setImeOptions(getResources(), attribute.imeOptions);
        startExtractingText(false);
    }
    int m_StatusBarHeight = 0;
/** Проверяет тип дополнений, запоминает в {@link #m_suggestType}*/    
    public final void checkSuggestType(EditorInfo ei)
    {
        m_suggestType = getSuggestType(ei, isFullscreenMode());
        if(m_suggestType==SUGGEST_VOCAB)
        {
            openWords();
            showCandView(true);
            getCandidates();
        }
        else if(m_suggestType==SUGGEST_OWN)
            showCandView(true);
        else
            showCandView(false);
    }
    final void showCandView(boolean bShow)
    {
        if(bShow)
        {
            if(ComMenu.inst!=null)
                return;
//            Rect r= new Rect();
//            Window win= getWindow().getWindow();
//            View dw = win.getDecorView();
//            dw.getWindowVisibleDisplayFrame(r);
//            int h = r.top;
//            boolean cantBeAtTop = Math.abs(h)<10;
            if(m_suggestType==SUGGEST_NONE||m_acPlace==JbCandView.AC_PLACE_NONE)
                return;
//            m_candView = createNewCandView();
//            boolean bAtTop = m_acPlace==JbCandView.AC_PLACE_KEYBOARD||cantBeAtTop;
            m_candView.show(st.kv(), m_acPlace);
//            m_autoCompleteHandler.sendMessageDelayed(m_autoCompleteHandler.obtainMessage(MSG_SHOW_PANEL), 100);
        }
        else
        {
            removeCandView();
        }
    }
    void removeCandView()
    {
        m_candView.remove();
    }
    public final String getCurQwertyLang()
    {
        JbKbd k = st.curKbd();
        if(k==null)
            return null;
        return st.isQwertyKeyboard(k.kbd)?k.kbd.lang.name:st.getCurLang();
    }
    final void openWords()
    {
        if (!m_bComplete||WordsService.inst==null)
            return;
        String lang = getCurQwertyLang();
        if (lang==null)
            return;
        WordsService.command(WordsService.CMD_OPEN_VOCAB, lang, inst);
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting)
    {
        if(m_candView!=null)
            showCandView(false);
        st.log("onStartInput " + attribute.packageName);
        if (attribute.initialSelStart < 0 && attribute.initialSelEnd < 0&&attribute.imeOptions==0)
        {
            requestHideSelf(0);
        }
        super.onStartInput(attribute, restarting);
    }

    @Override
    public void onFinishInputView(boolean finishingInput)
    {
        JbKbdView kv = st.kv();
        if (kv != null)
            kv.resetPressed();
        removeCandView();
        super.onFinishInputView(finishingInput);
    };

    @Override
    public void onBindInput()
    {
        st.log("onBindInput ");
        super.onBindInput();
    };

    /** Закрытие поля ввода */
    @Override
    public void onFinishInput()
    {
//        st.saveCurLang();
        super.onFinishInput();
        // Clear current composing text and candidates.
        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        removeCandView();
        if (JbKbdView.inst != null)
        {
            JbKbdView.inst.closing();
        }
    }

    int          m_SelStart;
    int          m_SelEnd;
    StringBuffer m_textBeforeCursor;
    CharSequence m_textAfterCursor;

    /** Изменение выделения в редакторе */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd)
    {

//        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        final ExtractEditText eet = m_extraText;
        if (eet != null && isFullscreenMode() && mExtractedText != null) {
            final int off = mExtractedText.startOffset;
            eet.startInternalChanges();
            newSelStart -= off;
            newSelEnd -= off;
            final int len = eet.getText().length();
            if (newSelStart < 0) newSelStart = 0;
            else if (newSelStart > len) newSelStart = len;
            if (newSelEnd < 0) newSelEnd = 0;
            else if (newSelEnd > len) newSelEnd = len;
            eet.setSelection(newSelStart, newSelEnd);
            eet.finishInternalChanges();
        }

  //      st.log("onUpdateSelection " + m_SelStart + " " + m_SelEnd);
        if (st.has(m_state, STATE_GO_END))
        {
            m_state = st.rem(m_state, STATE_GO_END);
            getCurrentInputConnection().setSelection(isSelMode()?m_SelEnd:newSelEnd, newSelEnd);
            return;
        }
        m_SelStart = newSelStart;
        m_SelEnd = newSelEnd;
        if (m_SelStart == m_SelEnd)
        {
            // Буферы не заполняются в том случае, если введена одна буква - сильно ускоряет на тормознутых редакторах при быстром вводе
            if(oldSelStart==oldSelEnd&&m_SelStart-oldSelStart==1)
            {
            }
            else
            {
                getTextBeforeCursor();
                if(m_bCanAutoInput&&m_suggestType==SUGGEST_VOCAB)
                    m_textAfterCursor = getCurrentInputConnection().getTextAfterCursor(40, 0);
                processCaseAndCandidates();
            }
        }
//        Log.d(PressArray.TAG, "sendKey "+ms);
    }
    void processCaseAndCandidates()
    {
        if (m_bCanAutoInput)
            changeCase(true);
        if(m_suggestType==SUGGEST_VOCAB)
            getCandidates();
    }
    void getCandidates()
    {
      try{  
          if(m_acPlace==JbCandView.AC_PLACE_NONE)
              return;
          if(m_textBeforeCursor==null)  
              getTextBeforeCursor();
          if(m_textAfterCursor==null)
              m_textAfterCursor = getCurrentInputConnection().getTextAfterCursor(40, 0);
          String wstart = Templates.getCurWordStart(m_textBeforeCursor, false);
          String wend = Templates.getCurWordEnd(m_textAfterCursor, false);
          if (wstart != null && wend != null)
          {
              String word = wstart + wend;
              if (word.length() < 1)
              {
                  WordsService.command(WordsService.CMD_CANCEL_VOCAB, null, inst);
                  onWords(null);
              }
              else
              {
                  WordsService.command(WordsService.CMD_GET_WORDS, word, inst);
              }
          }
          }
          catch(Throwable e)
          {
          }
    }
    /** Предлагает юзеру набор автодополнений. В браузере - показывает какой-то
     * набор из закладок и посещенных ссылок */
    @Override
    public void onDisplayCompletions(CompletionInfo[] completions)
    {
        if (m_candView == null)
            return;
        m_candView.setCompletions(completions);
    }

    /** Обаботка нажатия BACK */
    public boolean handleBackPress()
    {
        if (isInputViewShown())
        {
            if (ComMenu.inst != null)
            {
                ComMenu.inst.close();
                return true;
            }
            forceHide();
            return true;
        }
        return false;
    }
    @Override
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event)
    {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP)
        {
            processTextEditKey(KeyEvent.KEYCODE_DPAD_LEFT);
            return true;
        }
        if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            processTextEditKey(KeyEvent.KEYCODE_DPAD_RIGHT);
            return true;
        }
        return super.onKeyMultiple(keyCode, count, event);
    }
    /** key down */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (event.getRepeatCount() == 0 && handleBackPress())
            {
                m_bBackProcessed = true;
            }
            return m_bBackProcessed;
        }
        if(isInputViewShown()&&m_volumeKeys>0&&(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN||keyCode==KeyEvent.KEYCODE_VOLUME_UP))
        {
            processVolumeKey(keyCode, true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /** Ловим keyUp */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            boolean ret = m_bBackProcessed;
            m_bBackProcessed = false;
            return ret;
        }
        if((isInputViewShown()||m_volumeKeyTimer!=null)&&m_volumeKeys>0&&(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN||keyCode==KeyEvent.KEYCODE_VOLUME_UP))
        {
            processVolumeKey(keyCode, false);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /** Helper to send a key down / key up pair to the current editor. */
    private void keyDownUp(int keyEventCode)
    {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
    final void processChar(char ch)
    {
        if(m_textBeforeCursor==null)
            m_textBeforeCursor = new StringBuffer();
        m_textBeforeCursor.append(ch);
        if(m_textBeforeCursor.length()>40)
            m_textBeforeCursor.deleteCharAt(0);
        processCaseAndCandidates();
    }
    /** Helper to send a character to the editor as raw key events. */
    private void sendKey(int keyCode)
    {
        processChar((char)keyCode);
        switch (keyCode)
        {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
            break;
            default:
                if (keyCode >= '0' && keyCode <= '9')
                {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                }
                else
                {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
            break;
        }
    }

    public final void processKey(int primaryCode)
    {
        // st.kv().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        if (st.has(st.kv().m_state, JbKbdView.STATE_SOUNDS))
        {
            int sound = AudioManager.FX_KEY_CLICK;
            switch(primaryCode)
            {
                case KeyEvent.KEYCODE_DPAD_LEFT:sound = AudioManager.FX_FOCUS_NAVIGATION_LEFT; break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:sound = AudioManager.FX_FOCUS_NAVIGATION_RIGHT; break;
                case KeyEvent.KEYCODE_DPAD_UP:sound = AudioManager.FX_FOCUS_NAVIGATION_UP; break;
                case KeyEvent.KEYCODE_DPAD_DOWN:sound = AudioManager.FX_FOCUS_NAVIGATION_DOWN; break;
                case KeyEvent.KEYCODE_SPACE:sound = AudioManager.FX_KEYPRESS_SPACEBAR; break;
                case Keyboard.KEYCODE_DELETE:sound = AudioManager.FX_KEYPRESS_DELETE; break;
                case 10:sound = AudioManager.FX_KEYPRESS_RETURN; break;
            }
            try{
                m_audio.playSoundEffect(sound, m_soundVolume);
            }
            catch(Throwable e)
            {}
        }

        if (primaryCode < -200 && primaryCode > -300)
        {
            // Смайлики или текстовая метка
            LatinKey k = st.curKbd().getKeyByCode(primaryCode);
            if(k!=null)
            {
                onText(k.getMainText());
            }
        }
        else if (primaryCode < -300 && primaryCode > -400 || primaryCode >= KeyEvent.KEYCODE_DPAD_UP && primaryCode <= KeyEvent.KEYCODE_DPAD_RIGHT)
        {
            // Текстовая клавиатура
            processTextEditKey(primaryCode);
            
        }
        else if (primaryCode == Keyboard.KEYCODE_DELETE)
        {
            handleBackspace();
        }
        else if (primaryCode <= -500 && primaryCode >= -600)
        {
            st.kbdCommand(primaryCode);
        }
        else if (primaryCode == Keyboard.KEYCODE_SHIFT)
        {
            if (JbKbdView.inst != null)
                JbKbdView.inst.handleShift();
        }
        else if (primaryCode == Keyboard.KEYCODE_CANCEL)
        {
            handleClose();
            return;
        }
//        else if(primaryCode==10)
//        {
//            getCurrentInputConnection().performEditorAction(getCurrentInputEditorInfo().actionId);
//        }
        else if (primaryCode == st.CMD_LANG_CHANGE)
        {
            st.kv().handleLangChange();
//            checkSuggestType(getCurrentInputEditorInfo());
            if(m_suggestType==SUGGEST_VOCAB)
                getCandidates();
        }

        else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE && JbKbdView.inst != null)
        {
            if (st.isQwertyKeyboard(st.curKbd().kbd))
            {
                st.setSymbolKeyboard(false);
            }
            else
            {
                st.setQwertyKeyboard();
            }
        }
        else if (primaryCode == 0)
        {

        }
        else if (isWordSeparator(primaryCode))
        {
            handleWordSeparator(primaryCode);
        }
        else
        {
            handleCharacter(primaryCode);
        }
        checkGoQwerty(primaryCode);
    }
/** Проверяет, нужно ли выполнить переход к qwerty-клавиатуре по нажатию клавиши. Если нужно - выполняет переход    
*@param primaryCode код нажатой клавиши
*@return true - клавиатура закрыта, false - не закрыта */
    public final boolean checkGoQwerty(int primaryCode)
    {
        JbKbd kbd = st.curKbd();
        if(kbd==null||st.isQwertyKeyboard(kbd.kbd))
            return false;
        LatinKey k = kbd.getKeyByCode(primaryCode);
        if(k==null||st.has(k.flags, LatinKey.FLAG_NOT_GO_QWERTY))
            return false;
        if(kbd.kbd.lang.lang==st.LANG_SMIL||st.has(k.flags, LatinKey.FLAG_GO_QWERTY))
        {
            st.setQwertyKeyboard(true);
            return true;
        }
        return false;
    }
    public void onKey(int primaryCode, int[] keyCodes)
    {
        processKey(primaryCode);
    }

    /** Проверка, можно ли использовать автосмену регистра и вставку пробелов
     * @param ei Информация о редакторе
     * @return true - используется автоввод, false - не используется */
    final boolean canAutoInput(EditorInfo ei)
    {
        if (!st.has(m_state, STATE_AUTO_CASE))
            return false;
        try
        {
            int var = ei.inputType & EditorInfo.TYPE_MASK_VARIATION;
            int type = ei.inputType & EditorInfo.TYPE_MASK_CLASS;
            return type == EditorInfo.TYPE_CLASS_TEXT && ei != null && var != EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS && var != EditorInfo.TYPE_TEXT_VARIATION_URI && var != EditorInfo.TYPE_TEXT_VARIATION_PASSWORD && var != EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD && var != EditorInfo.TYPE_TEXT_VARIATION_FILTER;
        }
        catch (Throwable e)
        {
        }
        return false;
    }

    private final void handleWordSeparator(int primaryCode)
    {
        if(!(m_acAutocorrect&&primaryCode!='\''&&m_candView!=null&&m_candView.applyCorrection(primaryCode)))
            sendKey(primaryCode);
        if (m_bCanAutoInput)
        {
            if (st.has(m_state, STATE_SENTENCE_SPACE) && m_SpaceSymbols.indexOf(primaryCode) > -1)
                sendKeyChar(' ');
        }
    }

    public void onText(CharSequence text)
    {
        if (text == null)
            return;
        if (text.length() == 1)
        {
            int pc = (int) text.charAt(0);
            if (isWordSeparator(pc))
            {
                handleWordSeparator(pc);
                return;
            }
        }
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        ic.beginBatchEdit();
        ic.commitText(text, 0);
        ic.endBatchEdit();
    }

    private void handleBackspace()
    {
        keyDownUp(KeyEvent.KEYCODE_DEL);
        processCaseAndCandidates();
    }
    public void handleCharacter(int primaryCode)
    {
        if (isInputViewShown())
        {
            if (JbKbdView.inst.isUpperCase())
            {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        processChar((char) primaryCode);
        sendKeyChar((char) primaryCode);
        if (st.has(JbKbdView.inst.m_state, JbKbdView.STATE_TEMP_SHIFT))
        {
            JbKbdView.inst.m_state = st.rem(JbKbdView.inst.m_state, JbKbdView.STATE_TEMP_SHIFT);
            JbKbdView.inst.setShifted(st.has(JbKbdView.inst.m_state, JbKbdView.STATE_CAPS_LOCK));
            JbKbdView.inst.invalidateAllKeys();
        }
    }

    private void handleClose()
    {
        JbKbdView.inst.closing();
        forceHide();
    }

    public boolean isWordSeparator(int code)
    {
        return !Character.isLetterOrDigit(code);
    }
    public void setCompletionInfo(CompletionInfo ci)
    {
        getCurrentInputConnection().commitCompletion(ci);
    }
    public void setWord(String word)
    {
        CurInput ci = new CurInput();
        InputConnection ic = getCurrentInputConnection();
        ic.beginBatchEdit();
        if (ci.init(ic))
        {
            ci.replaceCurWord(ic, word);
        }
        ic.endBatchEdit();
    }

    public void swipeRight()
    {
    }

    public void swipeLeft()
    {
    }

    public void swipeDown()
    {
    }

    public void swipeUp()
    {
    }

    public void onPress(int primaryCode)
    {
    }

    public void onRelease(int primaryCode)
    {
    }

    public void onOptions()
    {
        ComMenu menu = new ComMenu();
        st.UniObserver onMenu = new st.UniObserver()
        {
            public int OnObserver(Object param1, Object param2)
            {
                int id = ((Integer) param1).intValue();
                if (id == -10)
                {
                    st.kv().reloadSkin();
                }
                else if (id == -11)
                {
                    CompiledKbdToXML();
                }
                else
                {
                    st.kbdCommand(id);
                }
                return 0;
            }
        };

        menu.add(R.string.mm_templates, st.CMD_TPL);
        menu.add(R.string.mm_multiclipboard, st.CMD_CLIPBOARD);
        menu.add(R.string.mm_settings, st.CMD_PREFERENCES);
        if (st.kv().m_curDesign.path != null)
            menu.add(R.string.mm_reload_skin, -10);
        if (Debug.isDebuggerConnected())
        {
            menu.add("compile kbd", st.CMD_COMPILE_KEYBOARDS);
            menu.add("compiled to xml", -11);
        }
        menu.show(onMenu);
    }

    void onVoiceRecognition(final ArrayList<String> ar)
    {
        if (ar == null)
        {
            forceShow();
            return;
        }
        ComMenu menu = new ComMenu();
        for (int i = 0; i < ar.size(); i++)
        {
            menu.add(ar.get(i), i);
        }
        st.UniObserver obs = new st.UniObserver()
        {
            @Override
            public int OnObserver(Object param1, Object param2)
            {
                int index = ((Integer) param1).intValue();
                onText(ar.get(index));
                return 0;
            }
        };
        menu.show(obs);
        if (!isInputViewShown())
        {
            forceShow();
        }
    }

    /** Форсированно показывает окно клавиатуры, запоминает статус в
     * {@link #m_bForceShow} */
    void forceShow()
    {
        showWindow(true);
        m_bForceShow = true;
    }

    /** Возвращает true, если находимся в режиме выделения текста, иначе
     * возвращает false */
    final boolean isSelMode()
    {
        LatinKey key = st.curKbd().getKeyByCode(-310);
        if (key != null && key.on)
        {
            return true;
        }
        return false;
    }

    /** Обработка клавиш с клавиатуры для текстовых операций */
    void processTextEditKey(int code)
    {
        InputConnection ic = getCurrentInputConnection();
        if (code == -310)
        {
            // LatinKey key = st.curKbd().getKeyByCode(-310);
            // if(key.on)
            // ic.performContextMenuAction(android.R.id.startSelectingText);
            // else
            // ic.performContextMenuAction(android.R.id.stopSelectingText);
            return;
        }
        switch (code)
        {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_UP: // Up
            case KeyEvent.KEYCODE_DPAD_DOWN: // Down
                if ((code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_UP) && m_SelStart == 0)
                    return;
                if (code == KeyEvent.KEYCODE_DPAD_RIGHT || code == KeyEvent.KEYCODE_DPAD_DOWN)
                {
                    CharSequence s = ic.getTextAfterCursor(1, 0);
                    if (s == null || s.length() == 0)
                        return;
                }
                boolean sel = isSelMode();
                if (sel)
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT));
                keyDownUp(code);
                if (sel)
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
            break;
            case st.TXT_ED_START:
                ic.setSelection(isSelMode()?m_SelEnd:0, 0);
                break;
            case st.TXT_ED_HOME: // Home
                handleHome(isSelMode());
            break;
            case st.TXT_ED_FINISH:
              m_state|=STATE_GO_END;
              ic.performContextMenuAction(android.R.id.selectAll);
            break;    
            case st.TXT_ED_END: // End
                handleEnd(isSelMode());
            break;
            case st.TXT_ED_COPY: // Copy
                ic.performContextMenuAction(android.R.id.copy);
            break;
            case st.TXT_ED_PASTE: // Paste
                ic.performContextMenuAction(android.R.id.paste);
            break;
            case st.TXT_ED_CUT: // Cut
                ic.performContextMenuAction(android.R.id.cut);
            break;
            case st.TXT_ED_SELECT_ALL: // Select all
                ic.performContextMenuAction(android.R.id.selectAll);
            break;
        }

    }

    void forceHide()
    {
        hideWindow();
        if (m_bForceShow)
        {
            m_bForceShow = false;
        }
        // requestHideSelf(0);
    }

    void handleHome(boolean bSel)
    {
        try
        {
            InputConnection ic = getCurrentInputConnection();
            String s = ic.getTextBeforeCursor(4000, 0).toString();
            int pos = Templates.chkPos(s.lastIndexOf('\n'), s.lastIndexOf('\r'), true, s.length());
            if (pos == -1)
                return;
            int cp = m_SelStart > m_SelEnd ? m_SelEnd : m_SelStart;
            cp = cp - (s.length() - pos);
            ic.setSelection(bSel ? m_SelStart : cp, cp);
        }
        catch (Throwable e)
        {
            st.logEx(e);
        }
    }

    @Override
    public void onAppPrivateCommand(String action, android.os.Bundle data)
    {
        super.onAppPrivateCommand(action, data);
    };

    @Override
    public boolean onExtractTextContextMenuItem(int id)
    {
        return super.onExtractTextContextMenuItem(id);
    };

    void handleEnd(boolean bSel)
    {
        try
        {
            InputConnection ic = getCurrentInputConnection();
            String s = ic.getTextAfterCursor(4000, 0).toString();
            int pos = Templates.chkPos(s.indexOf('\n'), s.indexOf('\r'), false, s.length());
            if (pos < 0)
                return;
            int cp = m_SelStart > m_SelEnd ? m_SelStart : m_SelEnd;
            cp = cp + pos;
            ic.setSelection(bSel ? m_SelStart : cp, cp);
        }
        catch (Throwable e)
        {
            st.logEx(e);
        }
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        VibroThread.getInstance(this).readSettings();
        if (key==null||st.PREF_KEY_AC_AUTOCORRECT.equals(key))
            m_acAutocorrect = sharedPreferences.getBoolean(st.PREF_KEY_AC_AUTOCORRECT, false);
        if (st.PREF_KEY_EDIT_SETTINGS.equals(key))
        {
            m_es.load(st.PREF_KEY_EDIT_SETTINGS);
            if (m_extraText != null)
            {
                try
                {
                    m_es.setToEditor(m_extraText);
                }
                catch (Throwable e)
                {
                }
            }
        }
        if (st.PREF_KEY_FONT_PANEL_AUTOCOMPLETE.equals(key)&&m_candView!=null)
            m_candView = createNewCandView();
        if (st.PREF_KEY_SENTENCE_ENDS.equals(key) || key == null)
            m_SentenceEnds = sharedPreferences.getString(st.PREF_KEY_SENTENCE_ENDS, "?!.");
        if (st.PREF_KEY_USE_VOLUME_KEYS.equals(key) || key == null)
            m_volumeKeys = Integer.valueOf(sharedPreferences.getString(st.PREF_KEY_USE_VOLUME_KEYS, st.ZERO_STRING));    
        if (st.PREF_KEY_AC_PLACE.equals(key) || key == null)
            m_acPlace= Integer.valueOf(sharedPreferences.getString(st.PREF_KEY_AC_PLACE, st.ONE_STRING));
        if (st.PREF_KEY_ADD_SPACE_SYMBOLS.equals(key) || key == null)
            m_SpaceSymbols = sharedPreferences.getString(st.PREF_KEY_ADD_SPACE_SYMBOLS, ",?!.");
        m_soundVolume  = Integer.valueOf(sharedPreferences.getString(st.PREF_KEY_SOUND_VOLUME, "5"));
        m_soundVolume/=10f;
        m_LandscapeEditType = Integer.valueOf(sharedPreferences.getString(st.PREF_KEY_LANSCAPE_TYPE, "0"));
        m_PortraitEditType = Integer.valueOf(sharedPreferences.getString(st.PREF_KEY_PORTRAIT_TYPE, "0"));
        if (sharedPreferences.getBoolean(st.PREF_KEY_AUTO_CASE, false))
            m_state |= STATE_AUTO_CASE;
        else
            m_state = st.rem(m_state, STATE_AUTO_CASE);
        if (sharedPreferences.getBoolean(st.PREF_KEY_UP_AFTER_SYMBOLS, true))
            m_state |= STATE_UP_AFTER_SYMBOLS;
        else
            m_state = st.rem(m_state, STATE_UP_AFTER_SYMBOLS);
        boolean bSpac = sharedPreferences.getBoolean(st.PREF_KEY_SENTENCE_SPACE, false);
        if (bSpac)
            m_state |= STATE_SENTENCE_SPACE;
        else
            m_state = st.rem(m_state, STATE_SENTENCE_SPACE);
        boolean bEmptyUp = sharedPreferences.getBoolean(st.PREF_KEY_EMPTY_UPPERCASE, true);
        if (bEmptyUp)
            m_state |= STATE_EMPTY_UP;
        else
            m_state = st.rem(m_state, STATE_EMPTY_UP);
        boolean bSpaceUp = sharedPreferences.getBoolean(st.PREF_KEY_UPERCASE_AFTER_SPACE, false);
        if (bSpaceUp)
            m_state |= STATE_SPACE_SENTENCE_UP;
        else
            m_state = st.rem(m_state, STATE_SPACE_SENTENCE_UP);
    }

    ExtractEditText m_extraText = null;

    @Override
    public View onCreateExtractTextView()
    {
        View v = super.onCreateExtractTextView();
        if (v instanceof ViewGroup)
        {
            ViewGroup vg = (ViewGroup) v;
            if (vg.getChildCount() > 0)
            {
                View ve = vg.getChildAt(0);
                if (ve instanceof ExtractEditText)
                {
                    m_extraText = (ExtractEditText) ve;
                    m_es.setToEditor(m_extraText);
                }
            }
        }
        return v;
    }

    @Override
    public boolean onEvaluateFullscreenMode()
    {
        int set = st.isLandscape(this) ? m_LandscapeEditType : m_PortraitEditType;
        boolean b = super.onEvaluateFullscreenMode();
        if (set == st.PREF_VAL_EDIT_TYPE_FULLSCREEN)
            b = true;
        else if (set == st.PREF_VAL_EDIT_TYPE_NOT_FULLSCREEN)
            b = false;
        return b;
    }

    void CompiledKbdToXML()
    {
        try
        {
            String path = st.getSettingsPath() + "keyboards/res/";
            new File(path).mkdirs();
            for (Keybrd kbd : st.arKbd)
            {
                if (kbd.path == null || kbd.path.startsWith("/"))
                    continue;
                File f = new File(path + kbd.path + ".xml");
                f.delete();
                f.createNewFile();
                CustomKeyboard.m_os = new DataOutputStream(new FileOutputStream(f));
                new CustomKeyboard(this, kbd);
                CustomKeyboard.m_os.flush();
                CustomKeyboard.m_os.close();
                CustomKeyboard.m_os = null;
            }
        }
        catch (Throwable e)
        {
            st.logEx(e);
        }
    }

    /** Определяет текущий регистр на основе позиции курсора и настроек в
     * {@link #m_state}
     * @return -1, для нижнего регистра, 1 - для верхнего, 0 - не делать никаких
     *         действий */
    final int getCase()
    {
        if (!m_bCanAutoInput || m_SelStart != m_SelEnd || !st.has(m_state, STATE_AUTO_CASE))
            return 0;
        try
        {
            if (st.has(st.kv().m_state, JbKbdView.STATE_CAPS_LOCK))
                return 0;
            if(m_textBeforeCursor==null)
                m_textBeforeCursor = new StringBuffer(getCurrentInputConnection().getTextBeforeCursor(40, 0)); 
            if (m_textBeforeCursor == null||m_textBeforeCursor.length() == 0 && st.has(m_state, STATE_EMPTY_UP))
                return 1;
            boolean bUpperCase = false;
            boolean bHasSpace = false;
            boolean bAfterSpace = st.has(m_state, STATE_SPACE_SENTENCE_UP);
            for (int i = m_textBeforeCursor.length() - 1; i >= 0; i--)
            {
                char ch = m_textBeforeCursor.charAt(i);
                if (Character.isWhitespace(ch))
                    bHasSpace = true;
                else if (m_SentenceEnds.indexOf((int) ch) > -1)
                {
                    bUpperCase = bHasSpace && bAfterSpace || !bAfterSpace;
                    break;
                }
                else
                    break;
            }
            return bUpperCase ? 1 : -1;
        }
        catch (Throwable e)
        {
            st.logEx(e);
        }
        return 0;

    }

    final void changeCase(boolean bInvalidate)
    {
        if (st.kv() == null)
            return;
        int c = getCase();
        JbKbdView kv = st.kv();
        if (kv == null)
            return;
        boolean bUpperCase = kv.isUpperCase();
        if (bUpperCase && c < 0)
            kv.setTempShift(false, bInvalidate);
        else if (!bUpperCase && c > 0)
            kv.setTempShift(true, bInvalidate);
    }

    void onWords(Vector<WordEntry>ar)
    {
        if(m_candView!=null)
            m_candView.setTexts(ar);
    }

    public final int getSuggestType(EditorInfo ei, boolean bFullscreen)
    {
        if (ei == null)
            return SUGGEST_NONE;
        int var = ei.inputType & EditorInfo.TYPE_MASK_VARIATION;
        int type = ei.inputType & EditorInfo.TYPE_MASK_CLASS;
        int flags = ei.inputType & EditorInfo.TYPE_MASK_FLAGS;
        if (type != EditorInfo.TYPE_CLASS_TEXT)
            return SUGGEST_NONE;
        if((flags&EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE)>0)
            return bFullscreen ? SUGGEST_OWN : SUGGEST_NONE;
        if (var == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS 
            || var == EditorInfo.TYPE_TEXT_VARIATION_URI 
            || var == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD 
            || var == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
            return SUGGEST_NONE;
        String lang = getCurQwertyLang();
        if(WordsService.inst==null||lang==null||!WordsService.inst.hasVocabForLang(lang))
            return SUGGEST_NONE;
        return SUGGEST_VOCAB;
    }
    public void saveUserWord(String word)
    {
        WordsService.command(WordsService.CMD_SAVE_WORD, word, inst);
        getCandidates();
    }
    @Override
    public void onComputeInsets(Insets outInsets)
    {
        // TODO Auto-generated method stub
        super.onComputeInsets(outInsets);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        try{
        super.onConfigurationChanged(newConfig);
        }
        catch(Throwable e)
        {
            
        }
    }
    public final boolean canGiveVocabWords()
    {
        return WordsService.inst!=null&&WordsService.inst.canGiveWords();
    }
    @Override
    public void setInputView(View view)
    {
        if(view instanceof JbKbdView)
        {
        }
        else
        {
            removeCandView();
        }
        super.setInputView(view);
    }
    @Override
    public void onUpdateCursor(Rect newCursor)
    {
        m_cursorRect = newCursor;
        if(isFullscreenMode()&&m_extraText!=null)
        {
//            int h = m_extraText.getHeight();
//            Path p = new Path();
//            RectF r = new RectF();
//            m_extraText.getLayout().getCursorPath(m_extraText.getSelectionEnd(), p, null);
//            p.computeBounds(r, true);
//            
//            m_extraText.getGlobalVisibleRect(newCursor);
//            m_cursorRect = new Rect((int)r.left,(int)r.top,(int)r.right,(int)r.bottom);
//            m_cursorRect.offset(newCursor.left,newCursor.top);
        }
        if(m_suggestType!=SUGGEST_NONE&&m_acPlace==JbCandView.AC_PLACE_CURSOR_POS)
            m_candView.show(st.kv(), m_acPlace);
        super.onUpdateCursor(newCursor);
    }
    @Override
    public void onUpdateExtractedText(int token, ExtractedText text)
    {
        if(token==mExtractedToken)
        {
            mExtractedText = text;
            m_extraText.setExtractedText(text);
        }
        super.onUpdateExtractedText(token, text);
    }
    ExtractedText mExtractedText;
    int mExtractedToken=1;
    void startExtractingText(boolean inputChanged) {
        final ExtractEditText eet = m_extraText;
        if (eet != null && getCurrentInputStarted()
                && isFullscreenMode()) {
            mExtractedToken++;
            ExtractedTextRequest req = new ExtractedTextRequest();
            req.token = mExtractedToken;
            req.flags = InputConnection.GET_TEXT_WITH_STYLES;
            req.hintMaxLines = 10;
            req.hintMaxChars = 10000;
            InputConnection ic = getCurrentInputConnection();
            mExtractedText = ic == null? null
                    : ic.getExtractedText(req, InputConnection.GET_EXTRACTED_TEXT_MONITOR);
            if (mExtractedText == null || ic == null) {
            }
            final EditorInfo ei = getCurrentInputEditorInfo();
            
            try {
                eet.startInternalChanges();
                onUpdateExtractingVisibility(ei);
                onUpdateExtractingViews(ei);
                int inputType = ei.inputType;
                if ((inputType&EditorInfo.TYPE_MASK_CLASS)
                        == EditorInfo.TYPE_CLASS_TEXT) {
                    inputType |= EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
//                    if ((inputType&EditorInfo.TYPE_TEXT_FLAG_IME_MULTI_LINE) != 0) {
//                    }
                }
                eet.setInputType(inputType);
                eet.setHint(ei.hintText);
                if (mExtractedText != null) {
                    eet.setEnabled(true);
                    eet.setExtractedText(mExtractedText);
                } else {
                    eet.setEnabled(false);
                    eet.setText("");
                }
            } finally {
                eet.finishInternalChanges();
            }
            
            if (inputChanged) {
                onExtractingInputChanged(ei);
            }
        }
    }
    final void onKeyboardChanged()
    {
        checkSuggestType(getCurrentInputEditorInfo());
    }
    public void onKeyboardWindowFocus(boolean bFocus)
    {
        showCandView(bFocus);
    }
    SameThreadTimer m_volumeKeyTimer;
    void processVolumeKey(int code,boolean down)
    {
        if(m_volumeKeyTimer!=null)
        {
            m_volumeKeyTimer.cancel();
            m_volumeKeyTimer = null;
        }
        if(down)
        {
            boolean left = m_volumeKeys==1&&code==KeyEvent.KEYCODE_VOLUME_UP||m_volumeKeys==2&&code==KeyEvent.KEYCODE_VOLUME_DOWN;
            final int key = left?KeyEvent.KEYCODE_DPAD_LEFT:KeyEvent.KEYCODE_DPAD_RIGHT;
            m_volumeKeyTimer = new SameThreadTimer(0,500)
            {
                @Override
                public void onTimer(SameThreadTimer timer)
                {
                    processKey(key);
                }
            };
            m_volumeKeyTimer.start();
        }
    }
    void getTextBeforeCursor()
    {
        CharSequence seq = getCurrentInputConnection().getTextBeforeCursor(40, 0);
        m_textBeforeCursor = new StringBuffer(seq==null?"":seq);
    }
}
