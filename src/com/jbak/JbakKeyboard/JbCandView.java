package com.jbak.JbakKeyboard;

import java.util.Vector;

import com.jbak.words.IWords.WordEntry;
import com.jbak.words.TextTools;

import android.app.Service;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class JbCandView extends RelativeLayout
{
    String m_texts[] = DEF_WORDS;
    LayoutInflater m_inflater;
    TextView m_addVocab;
    ImageView m_rightView;
    LinearLayout.LayoutParams m_lp;
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
    public JbCandView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        m_lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        m_inflater = (LayoutInflater)context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        setTexts(null);
    }
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
                if(m_popupWnd!=null)
                    hideFullView();
                else
                    showFullView();
            }
        });
    }
    public void setCompletions(CompletionInfo[] completions)
    {
        m_addVocab.setVisibility(GONE);
        if (completions == null)
        {
            setTexts(null);
            return;
        }
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
        if(ar==null||ar.size()==0)
        {
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
            if(completions!=null)
                tv.setTag(completions[pos]);
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
            m_popupWnd.dismiss();
            m_popupWnd = null;
        }
    }
    void showFullView()
    {
        View v = m_inflater.inflate(R.layout.candidates_full, null);
        LinearLayout ll = (LinearLayout)v.findViewById(R.id.cand_view);
        int width = getWidth();
        addFullViewPart(ll, width, 0);
        m_popupWnd = new PopupWindow(v, m_ll.getWidth(), st.kv().getHeight());
        m_popupWnd.showAtLocation(st.kv(), Gravity.LEFT|Gravity.TOP, 0, m_ll.getHeight());
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
            tv.setOnClickListener(m_textClickListener);
            ll.addView(tv,m_lp);
            pos++;
        }
    }
    public void setAsTitle()
    {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    |WindowManager.LayoutParams.FLAG_FULLSCREEN
                    |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    |WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    ;
        lp.gravity = Gravity.LEFT|Gravity.TOP;
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        lp.x = 0;
        lp.y = 0;
        WindowManager wm = (WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE);
        wm.addView(this, lp);
    }
}
