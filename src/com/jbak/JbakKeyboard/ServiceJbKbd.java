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

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.Toast;

import com.jbak.JbakKeyboard.EditSetActivity.EditSet;
import com.jbak.JbakKeyboard.JbKbd.LatinKey;
import com.jbak.JbakKeyboard.Templates.CurInput;

/** Example of writing an input method for a soft keyboard. This code is focused
 * on simplicity over completeness, so it should in no way be considered to be a
 * complete soft keyboard implementation. Its purpose is to provide a basic
 * example for how you would get started writing an input method, to be fleshed
 * out as appropriate. */
public class ServiceJbKbd extends InputMethodService implements KeyboardView.OnKeyboardActionListener, OnSharedPreferenceChangeListener
{
    Words                      m_words;
    static final boolean       PROCESS_HARD_KEYS    = true;
    public boolean             m_bComplete          = false;
    public static final String PID                  = "a14ef033de91702";
    private CandidateView      mCandidateView;
    private CompletionInfo[]   mCompletions;
    private StringBuilder      mComposing           = new StringBuilder();
    private boolean            mPredictionOn;
    private boolean            mCompletionOn;
    private long               mMetaState;
    static ServiceJbKbd        inst;
    String                     m_SentenceEnds       = "";
    String                     m_SpaceSymbols       = "";
    boolean                    m_bForceShow         = false;
    int                        m_state              = 0;
    int                        m_PortraitEditType   = st.PREF_VAL_EDIT_TYPE_DEFAULT;
    int                        m_LandscapeEditType  = st.PREF_VAL_EDIT_TYPE_DEFAULT;
    /** Вибратор для тактильной отдачи при нажатии и удержании клавиш */
    VibroThread                m_vibro;
    /** Статус - предложения с большой буквы */
    public static final int    STATE_SENTENCE_UP    = 0x0000001;
    /** Статус - пробел после конца предложения */
    public static final int    STATE_SENTENCE_SPACE = 0x0000002;
    /** Статус - верхний регистр в пустом поле */
    public static final int    STATE_EMPTY_UP       = 0x0000004;
    EditSet                    m_es                 = new EditSet();
    @Override
    public void onCreate()
    {
        inst = this;
        CustomKbdDesign.loadCustomSkins();
        m_vibro = new VibroThread(this);
        m_words = new Words();
        startService(new Intent(this, ClipbrdService.class));
        // setTheme(R.style.fullscreen_input);
        super.onCreate();
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
        st.pref().unregisterOnSharedPreferenceChangeListener(this);
        inst = null;
        super.onDestroy();
    }

    boolean isLandscape()
    {
        return getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT;
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
        /* if(!m_bComplete) return null; */
        // return getLayoutInflater().inflate(R.layout.candidates, null);
        /* mCandidateView = new CandidateView(this);
         * mCandidateView.setService(this); return mCandidateView; */// return
                                                                     // null;
        return null;
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting)
    {
        m_SelStart = attribute.initialSelStart;
        m_SelEnd = attribute.initialSelEnd;
        st.log("onStartInputView " + m_SelStart + " " + m_SelEnd);
//        getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), STATE_EMPTY_UP)
        setCandidatesViewShown(false);
        if (JbKbdView.inst == null)
        {
            getLayoutInflater().inflate(R.layout.input, null);
            setInputView(st.kv());
            JbKbdView.inst.setOnKeyboardActionListener(this);
        }
        mComposing.setLength(0);
        updateCandidates();

        if (!restarting)
        {
            mMetaState = 0;
        }

        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;
        switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS)
        {
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_DATETIME:
            case EditorInfo.TYPE_CLASS_PHONE:
                st.setSymbolKeyboard(false);
            break;
            default:
                mPredictionOn = m_bComplete;
                mCompletionOn = m_bComplete;
                if ((attribute.inputType & EditorInfo.TYPE_MASK_VARIATION) == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS || (attribute.inputType & EditorInfo.TYPE_MASK_VARIATION) == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                    st.setTempEnglishQwerty();
                else
                    st.setQwertyKeyboard();
            break;
        }
        st.curKbd().setImeOptions(getResources(), attribute.imeOptions);
        makeEmptyUppercase();
        openWords();
        super.onStartInputView(attribute, restarting);
    }

    final void openWords()
    {
        // Lang l = st.langForId(st.curKbd().resId);
        // if(l!=null)
        // m_words.open(l.name);
        // else
        // m_words.close();
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting)
    {
        st.log("onStartInput " + attribute.packageName);
        if (attribute.initialSelStart < 0 && attribute.initialSelEnd < 0)
        {
            requestHideSelf(0);
        }
        super.onStartInput(attribute, restarting);
    }

    @Override
    public void onFinishInputView(boolean finishingInput)
    {
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
        mComposing.setLength(0);
        updateCandidates();
        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);

        if (JbKbdView.inst != null)
        {
            JbKbdView.inst.closing();
        }
    }

    int m_SelStart;
    int m_SelEnd;
    boolean makeEmptyUppercase()
    {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null || st.kv() == null)
            return false;
        boolean canMake = m_SelEnd == m_SelStart && m_SelStart == 0 && st.has(m_state, STATE_EMPTY_UP) && canAutoInput() && !st.kv().isUpperCase() && ic.getTextAfterCursor(1, 0).length() == 0;
        if (canMake)
            st.kv().handleShift();
        return canMake;
    }

    /** Изменение выделения в редакторе */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd)
    {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        st.log("onUpdateSelection " + m_SelStart + " " + m_SelEnd);
        m_SelStart = newSelStart;
        m_SelEnd = newSelEnd;
        if (m_SelStart == m_SelEnd)
        {
            makeEmptyUppercase();
        }
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd || newSelEnd != candidatesEnd))
        {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null)
            {
                ic.finishComposingText();
            }
        }
    }

    /** Предлагает юзеру набор автодополнений. В браузере - показывает какой-то
     * набор из закладок и посещенных ссылок */
    @Override
    public void onDisplayCompletions(CompletionInfo[] completions)
    {
        // if (mCompletionOn) {
        return;
//        mCompletions = completions;
//        if (completions == null)
//        {
//            setSuggestions(null, false, false);
//            return;
//        }
//
//        List<String> stringList = new ArrayList<String>();
//        for (int i = 0; i < (completions != null ? completions.length : 0); i++)
//        {
//            CompletionInfo ci = completions[i];
//            if (ci != null)
//                stringList.add(ci.getText().toString());
//        }
//        setSuggestions(stringList, true, true);
        // }
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

    AbstractInputMethodSessionImpl m_is;

    /** key down */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && handleBackPress())
            return true;
        return false;// super.onKeyDown(keyCode, event);
    }

    /** Ловим keyUp */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        return super.onKeyUp(keyCode, event);
    }

    /* @Override public boolean onKeyLongPress(int keyCode, KeyEvent event) {
     * if(m_kp!=null) return m_kp.onLongPress(); return
     * super.onKeyLongPress(keyCode, event); }; *//** Helper function to commit
     * any text being composed in to the editor. */
    private void commitTyped(InputConnection inputConnection)
    {
        if (mComposing.length() > 0)
        {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }

    /** Helper to determine if a given character code is alphabetic. */
    private boolean isAlphabet(int code)
    {
        if (Character.isLetter(code))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /** Helper to send a key down / key up pair to the current editor. */
    private void keyDownUp(int keyEventCode)
    {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    /** Helper to send a character to the editor as raw key events. */
    private void sendKey(int keyCode)
    {
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
        if (st.has(st.kv().m_state, JbKbdView.STATE_VIBRO_SHORT))
            m_vibro.vibro(false);
        // st.kv().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        if (st.has(st.kv().m_state, JbKbdView.STATE_SOUNDS))
        {
            int sound = SoundEffectConstants.CLICK;
            if (primaryCode == KeyEvent.KEYCODE_DPAD_LEFT)
                sound = SoundEffectConstants.NAVIGATION_LEFT;
            if (primaryCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                sound = SoundEffectConstants.NAVIGATION_RIGHT;
            if (primaryCode == KeyEvent.KEYCODE_DPAD_UP)
                sound = SoundEffectConstants.NAVIGATION_UP;
            if (primaryCode == KeyEvent.KEYCODE_DPAD_DOWN)
                sound = SoundEffectConstants.NAVIGATION_DOWN;
            st.kv().playSoundEffect(sound);
        }

        if (primaryCode < -200 && primaryCode > -300)
        {
            // Смайлики
            onText(st.curKbd().getKeyByCode(primaryCode).getMainText());
            st.setQwertyKeyboard();
        }
        else if (primaryCode < -300 && primaryCode > -400 || primaryCode >= KeyEvent.KEYCODE_DPAD_UP && primaryCode <= KeyEvent.KEYCODE_DPAD_RIGHT)
        {
            processTextEditKey(primaryCode);
        }
        else if (primaryCode == Keyboard.KEYCODE_DELETE)
        {
            handleBackspace();
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
        else if (primaryCode == JbKbdView.KEYCODE_OPTIONS)
        {
            onOptions();
        }
        else if (primaryCode == IKeyboard.KEYCODE_LANG_CHANGE)
        {
            st.kv().handleLangChange();
            openWords();
        }

        else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE && JbKbdView.inst != null)
        {
            JbKbd kb = st.curKbd();
            int rid = kb.resId;
            if (st.kbdForId(rid) == null)
            {
                st.setQwertyKeyboard();
            }
            else
            {
                st.setSymbolKeyboard(false);
            }
        }
        else if (isWordSeparator(primaryCode))
        {
            handleWordSeparator(primaryCode);
        }
        else
        {
            handleCharacter(primaryCode);
        }
    }
    public void onKey(int primaryCode, int[] keyCodes)
    {
        processKey(primaryCode);
    }
    final boolean canAutoInput()
    {
        EditorInfo ei = getCurrentInputEditorInfo();
        int var = ei.inputType & EditorInfo.TYPE_MASK_VARIATION;
        return ei != null && st.isQwertyKeyboard() && var != EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS && var != EditorInfo.TYPE_TEXT_VARIATION_URI && var != EditorInfo.TYPE_TEXT_VARIATION_PASSWORD && var != EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
    }
    private final void handleWordSeparator(int primaryCode)
    {
        // Handle separator
        if (mComposing.length() > 0)
        {
            commitTyped(getCurrentInputConnection());
        }
        sendKey(primaryCode);
        if (canAutoInput())
        {
            if (st.has(m_state, STATE_SENTENCE_SPACE) && m_SpaceSymbols.indexOf(primaryCode) > -1)
                sendKeyChar(' ');
            if (st.has(m_state, STATE_SENTENCE_UP) && !st.kv().isUpperCase() && m_SentenceEnds.indexOf(primaryCode) > -1)
                st.kv().handleShift();
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
        if (mComposing.length() > 0)
        {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
    }

    /** Update the list of available candidates from the current composing text.
     * This will need to be filled in by however you are determining candidates. */
    private void updateCandidates()
    {
        if (!mCompletionOn)
        {
            if (mComposing.length() > 0)
            {
                ArrayList<String> list = new ArrayList<String>();
                list.add(mComposing.toString());
                setSuggestions(list, true, true);
            }
            else
            {
                setSuggestions(null, false, false);
            }
        }
    }

    public void setSuggestions(List<String> suggestions, boolean completions, boolean typedWordValid)
    {
        if (suggestions != null && suggestions.size() > 0)
        {
            setCandidatesViewShown(true);
        }
        else if (isExtractViewShown())
        {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null)
        {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }

    private void handleBackspace()
    {
        final int length = mComposing.length();
        if (length > 1)
        {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates();
        }
        else if (length > 0)
        {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();
        }
        else
        {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
    }

    private void handleCharacter(int primaryCode)
    {
        InputConnection ic = getCurrentInputConnection();
        if (isInputViewShown())
        {
            if (JbKbdView.inst.isUpperCase())
            {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (isAlphabet(primaryCode) && mPredictionOn)
        {
            mComposing.append((char) primaryCode);
            ic.setComposingText(mComposing, 1);
            updateCandidates();
        }
        else
        {
            sendKeyChar((char) primaryCode);
            // onText(String.valueOf((char) primaryCode));
            // ic.commitText(, 1);
            // updateFullscreenMode();
        }
        if (st.has(JbKbdView.inst.m_state, JbKbdView.STATE_TEMP_SHIFT))
        {
            JbKbdView.inst.m_state = st.rem(JbKbdView.inst.m_state, JbKbdView.STATE_TEMP_SHIFT);
            JbKbdView.inst.setShifted(st.has(JbKbdView.inst.m_state, JbKbdView.STATE_CAPS_LOCK));
            JbKbdView.inst.invalidateAllKeys();
        }
    }

    private void handleClose()
    {
        commitTyped(getCurrentInputConnection());
        JbKbdView.inst.closing();
        forceHide();
    }

    public boolean isWordSeparator(int code)
    {
        return !Character.isLetterOrDigit(code);
    }

    public void SetWord(String word)
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
        // handleBackspace();
    }

    public void swipeDown()
    {
        // handleClose();
    }

    public void swipeUp()
    {
    }

    public void onPress(int primaryCode)
    {
        st.kv().onKeyPress(primaryCode);
    }

    public void onRelease(int primaryCode)
    {
    }
    public void onOptions()
    {
        ComMenu menu = new ComMenu();
        st.UniObserver onMenu = new st.UniObserver()
        {
            int OnObserver(Object param1, Object param2)
            {
                int id = ((Integer) param1).intValue();
                if(id==-10)
                {
                    reloadCurrentCustomSkin();
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
        if(st.kv().m_curDesign.path!=null)
            menu.add(R.string.mm_reload_skin, -10);
        
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
            int OnObserver(Object param1, Object param2)
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
        boolean bSel = isSelMode();
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
                boolean sel = isSelMode();
                if (sel)
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT));
                keyDownUp(code);
                if (sel)
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
            break;
            case -305: // Home
                handleHome(isSelMode());
            break;
            case -306: // End
                handleEnd(isSelMode());
            break;
            case -320: // Copy
                ic.performContextMenuAction(android.R.id.copy);
            break;
            case -321: // Paste
                ic.performContextMenuAction(android.R.id.paste);
            break;
            case -322: // Cut
                ic.performContextMenuAction(android.R.id.cut);
            break;
            case -323: // Select all
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
//        if(st.PREF_KEY_REPEAT_DELAY.equals(key)||key==null)
//        {
//            try{
//                m_repeat = Integer.decode(sharedPreferences.getString(st.PREF_KEY_REPEAT_DELAY, "0"));
//            }
//            catch (Throwable e) {
//                m_repeat = 0;
//            }
//        }
        if (st.PREF_KEY_SENTENCE_ENDS.equals(key) || key == null)
            m_SentenceEnds = sharedPreferences.getString(st.PREF_KEY_SENTENCE_ENDS, "?!.");
        if (st.PREF_KEY_ADD_SPACE_SYMBOLS.equals(key) || key == null)
            m_SpaceSymbols = sharedPreferences.getString(st.PREF_KEY_ADD_SPACE_SYMBOLS, ",?!.");
        m_LandscapeEditType = Integer.valueOf(sharedPreferences.getString(st.PREF_KEY_LANSCAPE_TYPE, "0"));
        m_PortraitEditType = Integer.valueOf(sharedPreferences.getString(st.PREF_KEY_PORTRAIT_TYPE, "0"));
        if (sharedPreferences.getBoolean(st.PREF_KEY_SENTENCE_UPPERCASE, false))
            m_state |= STATE_SENTENCE_UP;
        else
            m_state = st.rem(m_state, STATE_SENTENCE_UP);
        boolean bSpac = sharedPreferences.getBoolean(st.PREF_KEY_SENTENCE_SPACE, false);
        if (bSpac)
            m_state |= STATE_SENTENCE_SPACE;
        else
            m_state = st.rem(m_state, STATE_SENTENCE_SPACE);
        boolean bEmptyUp = sharedPreferences.getBoolean(st.PREF_KEY_EMPTY_UPPERCASE, false);
        if (bEmptyUp)
            m_state |= STATE_EMPTY_UP;
        else
            m_state = st.rem(m_state, STATE_EMPTY_UP);
    }

    void onChangeKeyboard()
    {
        makeEmptyUppercase();
    }

    EditText m_extraText = null;

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
                if (ve instanceof EditText)
                {
                    m_extraText = (EditText) ve;
                    m_es.setToEditor(m_extraText);
                }
            }
        }
        return v;
    }

    @Override
    public boolean onEvaluateFullscreenMode()
    {
        int set = isLandscape() ? m_LandscapeEditType : m_PortraitEditType;
        boolean b = super.onEvaluateFullscreenMode();
        if (set == st.PREF_VAL_EDIT_TYPE_FULLSCREEN)
            b = true;
        else if (set == st.PREF_VAL_EDIT_TYPE_NOT_FULLSCREEN)
            b = false;
        return b;
    }

    public void onLongPress(Key key)
    {
        try
        {
            if (st.has(st.kv().m_state, JbKbdView.STATE_VIBRO_LONG))
                m_vibro.vibro(false);
        }
        catch (Throwable e)
        {
        }
    }
    public void reloadCurrentCustomSkin()
    {
        int si = st.pref().getInt(st.PREF_KEY_KBD_SKIN, -1);
        String path = st.kv().m_curDesign.path;
        if(si<0||path==null)
            return;
        CustomKbdDesign cd = new CustomKbdDesign();
        if(!cd.load(path))
        {
            Toast.makeText(this, cd.getErrString(), 800);
            return;
        }
        st.arDesign[si] = cd.getDesign();
        st.kv().init();
        st.setQwertyKeyboard(true);
    }
}
