package com.jbak.JbakKeyboard;

import java.util.Vector;

import android.app.Service;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jbak.words.IWords.WordEntry;
import com.jbak.words.TextTools;
import com.jbak.words.WordsService;

public class JbCandView extends RelativeLayout
{
    public static final int AC_PLACE_NONE = 0;
    public static final int AC_PLACE_KEYBOARD = 1;
    public static final int AC_PLACE_TITLE = 2;
    public static final int AC_PLACE_CURSOR_POS = 3;
    int m_place = AC_PLACE_NONE;
    String m_texts[] = DEF_WORDS;
    LayoutInflater m_inflater;
    TextView m_addVocab;
    ImageView m_rightView;
    LinearLayout.LayoutParams m_lp;
    boolean m_bCanCorrect = false;
    boolean m_bBlockClickOnce = false;
    CompletionInfo m_completions[];
    public static final String[] DEF_WORDS = new String[]
    {
        ",",
        ".",
        "!",
        "?",
        ":",
        ";",
        "@",
    };
    public int getPlace()
    {
        return m_place;
    }
    public void setPlace(int place)
    {
        this.m_place = place;
    }
    public JbCandView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        m_height = context.getResources().getDimensionPixelSize(R.dimen.cand_height);
        wm = (WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE);
        m_lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        m_inflater = (LayoutInflater)context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        setTexts(null);
    }
    int m_height;
    WindowManager wm;
    LinearLayout m_ll;
    @Override
    protected void onFinishInflate() 
    {
        m_ll = (LinearLayout)findViewById(R.id.completions);
        m_addVocab = (TextView)findViewById(R.id.cand_left);
        m_addVocab.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ServiceJbKbd.inst.saveUserWord(((TextView)v).getText().toString());
            }
        });
        m_rightView = (ImageView)findViewById(R.id.cand_right);
        m_rightView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(m_bBlockClickOnce)
                {
                    m_bBlockClickOnce = true;
                    return;
                }
                if(m_popupWnd!=null)
                {
                    hideFullView();
                }
                else
                {
                    showFullView();
                }
            }
        });
    }
    public void setCompletions(CompletionInfo[] completions)
    {
        m_bCanCorrect = false;
        m_addVocab.setVisibility(GONE);
        if (completions == null)
        {
            setTexts(null);
            return;
        }
        m_completions = completions;
        String texts[] = new String[completions.length];
        int pos = 0;
        for (CompletionInfo ci : completions)
        {
            texts[pos] = ci.getText().toString();
            pos++;
        }
        setTexts(texts,completions);
    }
    public void setTexts(Vector<WordEntry> ar)
    {
        m_completions = null;
        m_bCanCorrect = false;
        if(ar==null||ar.size()==0)
        {
            if(m_addVocab!=null)
                m_addVocab.setVisibility(GONE);
            setTexts(null, null);
            return;
        }
        WordEntry wd = ar.get(0);
        int sz = ar.size();
        if(wd.compareType==TextTools.COMPARE_TYPE_NONE)
        {
            m_addVocab.setText(wd.word);
            m_addVocab.setVisibility(VISIBLE);
            --sz;
        }
        else
        {
            m_addVocab.setVisibility(GONE);
        }
        String words[] = new String[sz];
        m_bCanCorrect = sz>0;
        int pos = 0;
        for(WordEntry we:ar)
        {
            if(we.compareType==TextTools.COMPARE_TYPE_NONE)
                continue;
            words[pos]=we.word;
            pos++;
        }
        setTexts(words,null);
    }
    public void setTexts(String words[],CompletionInfo[]completions)
    {
        hideFullView();
        m_texts = words==null?DEF_WORDS:words;
        if(m_ll==null) return;
        int pos = 0;
        int cc = m_ll.getChildCount();
        for(String s:m_texts)
        {
            if(s==null)break;
            TextView tv = null;
            if(pos<cc)
            {
                tv = (TextView)m_ll.getChildAt(pos);
                tv.setVisibility(View.VISIBLE);
            }
            else
            {
                tv = (TextView)m_inflater.inflate(R.layout.candidate_item, null);
                tv.setOnClickListener(m_textClickListener);
                m_ll.addView(tv,m_lp);
            }
            tv.setText(s);
            tv.setTag(completions!=null?completions[pos]:null);
            pos++;
        }
        while(pos<cc)
        {
            ((TextView)m_ll.getChildAt(pos)).setVisibility(View.GONE);
            pos++;
        }
        m_ll.measure(0, 0);
        int w = m_ll.getMeasuredWidth();
        if(w<getWidth()-m_addVocab.getWidth())
        {
            m_rightView.setVisibility(GONE);
        }
        else
        {
            m_rightView.setVisibility(VISIBLE);
        }
    }
    View.OnClickListener m_textClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(ServiceJbKbd.inst!=null)
            {
                CompletionInfo ci = (CompletionInfo)v.getTag();
                if(ci==null)
                    ServiceJbKbd.inst.setWord(((TextView)v).getText().toString());
                else
                    ServiceJbKbd.inst.setCompletionInfo(ci);
            }
        }
    };
    PopupWindow m_popupWnd = null;
    void hideFullView()
    {
        if(m_popupWnd!=null)
        {
            m_rightView.setImageResource(R.drawable.cand_arrow_icon);
            m_popupWnd.dismiss();
            m_popupWnd = null;
        }
    }
    void showFullView()
    {
        m_rightView.setImageResource(R.drawable.cand_arrow_up_icon);
        final View v = m_inflater.inflate(R.layout.candidates_full, null);
        final LinearLayout ll = (LinearLayout)v.findViewById(R.id.cand_view);
        int width = getWidth();
        addFullViewPart(ll, width, 0);
        int h = st.kv().getHeight();
        if(m_place==AC_PLACE_KEYBOARD)
            h-=m_height;
        m_popupWnd = new PopupWindow(v, getWidth(), h);
        m_popupWnd.setBackgroundDrawable(new BitmapDrawable());
        m_popupWnd.setTouchable(true);
        m_popupWnd.setOutsideTouchable(true);
        m_popupWnd.setTouchInterceptor(new OnTouchListener()
        {
            
            @Override
            public boolean onTouch(View view, MotionEvent event)
            {
                int act = event.getAction();
                boolean bHide = false;
                if(act==MotionEvent.ACTION_OUTSIDE)
                {
//                    Rect r = new Rect(),rf = new Rect();
//                    m_rightView.getGlobalVisibleRect(r);
//                    m_rightView.getWindowVisibleDisplayFrame(rf);
//                    int x = (int)event.getRawX(),y = (int)event.getRawY();
//                    y-=rf.top;
//                    if(r.contains(x,y))
//                        m_bBlockClickOnce = true;
                    if(m_rightView.dispatchTouchEvent(event))
                        return false;
                    bHide = true;
                }
                else if(act==MotionEvent.ACTION_DOWN)
                {
                    int r = ll.getRight();
                    int b = ll.getBottom();
                    float x = event.getX();
                    float y = event.getY();
                    if(x>r||y>b)
                        bHide = true;
                }
                if(bHide)
                    hideFullView();
                return bHide;
            }
        });
        int yoff = 0-st.kv().getCurKeyboard().getHeight();
        if(m_place==AC_PLACE_KEYBOARD)
            yoff+=m_height;
        m_popupWnd.showAsDropDown(st.kv(), 0, yoff);
//        m_popupWnd.showAtLocation(st.kv(), Gravity.LEFT|Gravity.TOP, 0, m_height);
//        ServiceJbKbd.inst.setInputView(v);
//        m_bShownFull = true;
    }
    
    void addFullViewPart(LinearLayout parent,int width,int pos)
    {
        LinearLayout ll = new LinearLayout(getContext());
        
        parent.addView(ll);
        int w = 0;
        while (pos<m_texts.length)
        {
            String txt = m_texts[pos];
            if(txt==null)
                return;
            TextView tv = (TextView)m_inflater.inflate(R.layout.candidate_item, null);
            tv.setText(txt);
            tv.measure(0, 0);
            w+=tv.getMeasuredWidth();
            if(w>width)
            {
                addFullViewPart(parent, width, pos);
                return;
            }
            if(m_completions!=null&&m_completions.length>pos)
                tv.setTag(m_completions[pos]);
            tv.setOnClickListener(m_textClickListener);
            ll.addView(tv,m_lp);
            pos++;
        }
    }
    public void remove()
    {
        hideFullView();
        try{
            if(m_place==AC_PLACE_KEYBOARD&&st.kv()!=null)
            {
                CustomKeyboard kbd = (CustomKeyboard)st.kv().getCurKeyboard();
                kbd.setTopSpace(0);
            }
            m_place = AC_PLACE_NONE;
            wm.removeViewImmediate(this);
        }
        catch (Throwable e) {
        }
    }
    int getFixedHeight()
    {
        return m_height;
    }
    int getYCursor()
    {
        if(ServiceJbKbd.inst!=null&&ServiceJbKbd.inst.m_cursorRect!=null)
        {
            
            int ret = ServiceJbKbd.inst.m_cursorRect.top-m_height;
//            if(ServiceJbKbd.inst.isFullscreenMode())
//                ret-=ServiceJbKbd.inst.m_extraText.getHeight();
            return ret;
        }
        return 0;
    }
    public void show(JbKbdView kv,int place)
    {
        if(place==AC_PLACE_CURSOR_POS&&place==m_place&&getYCursor()==m_yPos)
            return;
        if(m_place!=AC_PLACE_NONE)
            remove();
        if(!ServiceJbKbd.inst.isInputViewShown())
            return;
        CustomKeyboard kbd = (CustomKeyboard)kv.getCurKeyboard();
        m_place = place;
        kbd.setTopSpace(place==AC_PLACE_KEYBOARD?m_height:0);
        
        int ypos = place==AC_PLACE_KEYBOARD?getContext().getResources().getDisplayMetrics().heightPixels-kbd.getHeight():0;
        if(place==AC_PLACE_CURSOR_POS)
        {
            ypos = getYCursor();
        }
        showInView(ypos,place==AC_PLACE_TITLE);
    }
    int m_yPos = -10000;
    public void showInView(int yPos,boolean bSystemAlert)
    {
        m_yPos = yPos;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    |WindowManager.LayoutParams.FLAG_FULLSCREEN
                    |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    |WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    ;
        lp.gravity = Gravity.LEFT|Gravity.TOP;
        IBinder tok = st.kv().getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        if(tok!=null&&!bSystemAlert)
        {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
            lp.token = tok;
        }
        lp.x = 0;
        lp.y = yPos;
        wm.addView(this, lp);
    }
    public boolean applyCorrection(int code)
    {
        if(!m_bCanCorrect||m_ll.getChildCount()<1||WordsService.isSelectNow())
            return false;
        TextView tv = (TextView)m_ll.getChildAt(0);
        String text = tv.getText().toString()+(char)code;
        ServiceJbKbd.inst.setWord(text);
        return true;
    }
}
