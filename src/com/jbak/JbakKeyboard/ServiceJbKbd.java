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


import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jbak.JbakKeyboard.JbKbd.LatinKey;
import com.jbak.JbakKeyboard.Templates.CurInput;
import com.jbak.JbakKeyboard.st.Lang;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class ServiceJbKbd extends InputMethodService 
        implements KeyboardView.OnKeyboardActionListener {
    static final boolean DEBUG = false;
    
    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on 
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    Words m_words;
    static final boolean PROCESS_HARD_KEYS = true;
    public boolean m_bComplete = false;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;
    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private long mMetaState;
    static ServiceJbKbd inst;
    private String mWordSeparators;
    KeyPressProcessor m_kp;
    boolean m_bForceShow = false;
    int m_state = 0;
    public static final int STATE_SEL = 0x0000001;
/**
 * Main initialization of the input method component.  Be sure to call
 * to super class.
 */
    @Override public void onCreate() {
    	m_words = new Words();
		startService(new Intent(this,ClipbrdService.class));
    	inst = this;
    	m_kp = new KeyPressProcessor();
        super.onCreate();
        mWordSeparators = getResources().getString(R.string.word_separators);
    }
/** Завершение работы сервиса */    
    @Override
	public void onDestroy()
	{
    	inst = null;
    	JbKbdView.inst = null;
		super.onDestroy();
	}
/** Инициализация интерфейса */
    @Override public void onInitializeInterface() {
    }
/** Стартует ввод */    
    @Override public View onCreateInputView() {
        getLayoutInflater().inflate(R.layout.input, null);
        JbKbdView.inst.setOnKeyboardActionListener(this);
        st.setQwertyKeyboard();
        return JbKbdView.inst;
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override public View onCreateCandidatesView() {
/*    	if(!m_bComplete)
    		return null;
*/    	
    	//return getLayoutInflater().inflate(R.layout.candidates, null);
/*        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
       return mCandidateView;
*///    	return null;
    	return null;
    }
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
    	m_SelStart = attribute.initialSelStart;
    	m_SelEnd = attribute.initialSelEnd;
        setCandidatesViewShown(true);
        if(JbKbdView.inst==null)
        {
        	getLayoutInflater().inflate(R.layout.input, null);
        	setInputView(st.kv());
            JbKbdView.inst.setOnKeyboardActionListener(this);
        }
        mComposing.setLength(0);
        updateCandidates();
        
        if (!restarting) {
            mMetaState = 0;
        }
        
        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;
        switch (attribute.inputType&EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_DATETIME:
            case EditorInfo.TYPE_CLASS_PHONE:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
            	st.setSymbolKeyboard(false);
                break;
            default:
            	
            	mPredictionOn = m_bComplete;
            	mCompletionOn=m_bComplete;
            	st.setQwertyKeyboard();
            	break;
        }
        st.curKbd().setImeOptions(getResources(), attribute.imeOptions);
        openWords();
        super.onStartInputView(attribute, restarting);
    }
    final void openWords()
    {
        Lang l = st.langForId(st.curKbd().resId);
        if(l!=null)
        	m_words.open(l.name);
        else
        	m_words.close();
    }
    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
    	super.onStartInput(attribute, restarting);
    }
    @Override public void onFinishInputView(boolean finishingInput) 
    {
    	super.onFinishInputView(finishingInput);
    };
/** Закрытие поля ввода */
    @Override public void onFinishInput() {
        st.saveCurLang();
        super.onFinishInput();
        // Clear current composing text and candidates.
        mComposing.setLength(0);
        updateCandidates();
        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);
        
        if (JbKbdView.inst != null) {
        	JbKbdView.inst.closing();
        }
    }
    int m_SelStart;
    int m_SelEnd;
    int m_CursorPos;
/** Изменение выделения в редакторе */    
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        m_SelStart = newSelStart;
        m_SelEnd = newSelEnd;
        if(m_SelStart==m_SelEnd)
        {
/*        	String word = Templates.getCurWordStart(null)+Templates.getCurWordEnd(null);
        	if(word.length()>0)
        	{
        		ArrayList<String> ar = m_words.getWords(word);
        		setSuggestions(ar, true, false);
        	}
        	else
        	{
        		setSuggestions(new ArrayList<String>(), true, false);
        	}
*/        }
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }
/** Предлагает юзеру набор автодополнений. В браузере - показывает какой-то набор из закладок и посещенных ссылок*/
    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
//        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }
            
            List<String> stringList = new ArrayList<String>();
            for (int i=0; i<(completions != null ? completions.length : 0); i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
  //      }
    }
/** Транслирует кнопку в InputConnection */    
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }
        
        boolean dead = false;

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }
        
        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() -1 );
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length()-1);
            }
        }
        
        onKey(c, null);
        
        return true;
    }
    public boolean handleBackPress()
    {
    	if(isInputViewShown())
    	{
        	if(ComMenu.inst!=null)
        	{
        		ComMenu.inst.close();
        		return true;
        	}
        	forceHide();
            return true;
    	}
    	return false;
    }
    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(KeySetActivity.inst!=null&&KeySetActivity.inst.onHardwareKey(event))
    		return true;
    	if(m_kp!=null&&m_kp.onKeyDown(event, getCurrentInputEditorInfo()))
    		return true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            {
                if (event.getRepeatCount() == 0 && st.kv() != null) {
                	if(handleBackPress())
                		return true;
                }
                break;
            }
            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;
                
            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;
                
            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState()&KeyEvent.META_ALT_ON) != 0) {
                        // A silly example: in our input method, Alt+Space
                        // is a shortcut for 'android' in lower case.
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            // First, tell the editor that it is no longer in the
                            // shift state, since we are consuming this.
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // And we consume this event.
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
	public boolean onKeyLongPress (int keyCode, KeyEvent event)
    {
    	if(m_kp!=null)
    		return m_kp.onKeyLong(event, getCurrentInputEditorInfo());
    	return false;
    }
    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override 
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if(KeySetActivity.inst!=null&&KeySetActivity.inst.onHardwareKey(event))
    		return true;
    	if(m_kp!=null&&m_kp.onKeyUp(event, getCurrentInputEditorInfo()))
    		return true;
        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }
    
    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
    
    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

    // Implementation of KeyboardViewListener
    public void onKey(int primaryCode, int[] keyCodes) {
		if(st.has(st.kv().m_state, JbKbdView.STATE_VIBRO_LONG))
			st.kv().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        if (isWordSeparator(primaryCode)) 
        {
            // Handle separator
            if (mComposing.length() > 0) {
                commitTyped(getCurrentInputConnection());
            }
            sendKey(primaryCode);
        } 
        else if (primaryCode<-200&&primaryCode>-300) 
        {
        	// Смайлики
        	onText(st.curKbd().getKeyByCode(primaryCode).label);
        	st.setQwertyKeyboard();
        } 
        else if (primaryCode<-300&&primaryCode>-400)
        {
        	processTextEditKey(primaryCode);
        }
        else if (primaryCode == Keyboard.KEYCODE_DELETE) 
        {
            handleBackspace();
        } 
        else if (primaryCode == Keyboard.KEYCODE_SHIFT) 
        {
        	if(JbKbdView.inst!=null)
        		JbKbdView.inst.handleShift();
        }
        else if (primaryCode == Keyboard.KEYCODE_CANCEL) 
        {
            handleClose();
            return;
        } 
        else if (primaryCode == JbKbdView.KEYCODE_OPTIONS) 
        {
        	onOptions();
        } 
        else if (primaryCode == -20) {
        	onLangChange();
        } 
        
        else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE
                && JbKbdView.inst != null) {
        	JbKbd kb = st.curKbd();
        	int rid = kb.resId;
            if (st.langForId(rid)==null) {
                st.setQwertyKeyboard();
            } else {
                st.setSymbolKeyboard(false);
            }
        } else {
            handleCharacter(primaryCode, keyCodes);
        }
    }

    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(mComposing.toString());
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
    }
    
    public void setSuggestions(List<String> suggestions, boolean completions,
            boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }
    
    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates();
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
    }

    
    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (JbKbdView.inst.isUpperCase()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (isAlphabet(primaryCode) && mPredictionOn) {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates();
        } else {
            getCurrentInputConnection().commitText(
                    String.valueOf((char) primaryCode), 1);
        }
        if(st.has(JbKbdView.inst.m_state, JbKbdView.STATE_TEMP_SHIFT))
        {
        	JbKbdView.inst.m_state = st.rem(JbKbdView.inst.m_state, JbKbdView.STATE_TEMP_SHIFT);
        	JbKbdView.inst.setShifted(st.has(JbKbdView.inst.m_state, JbKbdView.STATE_CAPS_LOCK));
        	JbKbdView.inst.invalidateAllKeys();
        }
    }

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        JbKbdView.inst.closing();
        forceHide();
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }
    
    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char)code));
    }

    
    public void SetWord(String word)
    {
    	CurInput ci = new CurInput();
    	InputConnection ic = getCurrentInputConnection();
    	ic.beginBatchEdit();
    	if(ci.init(ic))
    	{
    		ci.replaceCurWord(ic, word);
    	}
    	ic.endBatchEdit();
    }
    public void swipeRight() {
    }
    
    public void swipeLeft() {
        //handleBackspace();
    }

    public void swipeDown() {
        //handleClose();
    }

    public void swipeUp() {
    }
    
    public void onPress(int primaryCode) {
    }
    
    public void onRelease(int primaryCode) {
    }
    public void onLangChange()
    {
    	String ls[]=st.getLangsArray(this);
    	Lang l = st.langForId(st.curKbd().resId);
    	int f = st.searchStr(l.name, ls);
    	String newLang = st.defLang().name;
    	if(f==ls.length-1)
    	{
    		newLang = ls[0];
    	}
    	else if(f<ls.length-1)
    	{
    		newLang = ls[f+1];
    	}
    	l = st.langForName(newLang);
    	if(l==null)
    		l = st.defLang();
    	st.kv().setKeyboard(new JbKbd(this, l.resId));
    	st.saveCurLang();
    	openWords();
    }
    public void onOptions()
    {
    	ComMenu menu = new ComMenu();
    	st.UniObserver onMenu = new st.UniObserver()
		{
			int OnObserver(Object param1, Object param2)
			{
				st.kbdCommand(((Integer)param1).intValue());
				return 0;
			}
		};
    	menu.add(R.string.mm_templates, st.CMD_TPL);
    	menu.add(R.string.mm_multiclipboard, st.CMD_CLIPBOARD);
    	menu.add(R.string.mm_settings, st.CMD_PREFERENCES);
    	menu.add(R.string.mm_close, 0);
    	menu.show(onMenu);
    }
    void onVoiceRecognition(final ArrayList<String> ar)
    {
    	if(ar==null)
    	{
			forceShow();
    		return;
    	}
    	ComMenu menu = new ComMenu();
    	for(int i=0;i<ar.size();i++)
    	{
    		menu.add(ar.get(i), i);
    	}
    	st.UniObserver obs = new st.UniObserver()
		{
			@Override
			int OnObserver(Object param1, Object param2)
			{
				int index = ((Integer)param1).intValue();
				onText(ar.get(index));
				return 0;
			}
		};
    	menu.show(obs);
		if(!isInputViewShown())
		{
			forceShow();
		}
    }
    void forceShow()
    {
    	showWindow(true);
    	m_bForceShow = true;
    }
    void processTextEditKey(int code)
    {
    	InputConnection ic = getCurrentInputConnection();
    	if(code==-310)
    	{
        	LatinKey key = st.curKbd().getKeyByCode(-310);
        	if(key.on)
        		ic.performContextMenuAction(android.R.id.startSelectingText);
        	else
        		ic.performContextMenuAction(android.R.id.stopSelectingText);
    		return;
    	}
    	boolean bSel = false;
    	LatinKey key = st.curKbd().getKeyByCode(-310);
    	if(key!=null&&key.on)
    	{
    		bSel = true;
    	}
    	switch(code)
    	{
    		case -301: //Up
    			sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP); 
    			break;
    		case -302: //Left 
    			sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT); 
    			break;
    		case -303: // Right
    			sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT); 
    			break;
    		case -304: // Down
    			sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN); 
    			break;
    		case -305: // Home
    			handleHome(bSel);
    			break;
    		case -306: // End
    			handleEnd(bSel);
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
    	if(m_bForceShow)
    	{
    		m_bForceShow = false;
    		hideWindow();
    	}
    	requestHideSelf(0);
    }
    void handleHome(boolean bSel)
    {
    	try{
	    	InputConnection ic = getCurrentInputConnection();
			String s = ic.getTextBeforeCursor(4000,0).toString();
			int pos = Templates.chkPos(s.lastIndexOf('\n'), s.lastIndexOf('\r'), true, s.length());
			if(pos==-1)
				return;
			int cp = m_SelStart>m_SelEnd?m_SelEnd:m_SelStart;
			cp = cp-(s.length()-pos);
			ic.setSelection(bSel?m_SelStart:cp, cp);
    	}
    	catch (Throwable e) {
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
    	return false;
    };
    void handleEnd(boolean bSel)
    {
    	try{
	    	InputConnection ic = getCurrentInputConnection();
			String s = ic.getTextAfterCursor(4000,0).toString();
			int pos = Templates.chkPos(s.indexOf('\n'), s.indexOf('\r'), false, s.length());
			if(pos<0)
				return;
			int cp = m_SelStart>m_SelEnd?m_SelStart:m_SelEnd;
			cp = cp+pos;
			ic.setSelection(bSel?m_SelStart:cp, cp);
    	}
    	catch (Throwable e) {
    		st.logEx(e);
		}
    }
}
