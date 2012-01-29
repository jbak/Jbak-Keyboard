package com.jbak.JbakKeyboard;
import android.inputmethodservice.Keyboard.Key;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.ads.v;
import com.jbak.JbakKeyboard.JbKbd.LatinKey;


public class KeyboardGesture extends GestureDetector
{
    public KeyboardGesture(JbKbdView view)
    {
        super(view.getContext(), new KvListener().setKeyboardView(view));
    }
    static class KvListener extends SimpleOnGestureListener
    {
        JbKbdView m_kv;
        public KvListener setKeyboardView(JbKbdView kv)
        {
            m_kv = kv;
            return this;
        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            float downX = e1.getX();
            float downY = e1.getY();
            float dx = e2.getX()-e1.getX();
            float dy = e2.getY()-e1.getY();
            if(Math.abs(velocityX)>1000&&(velocityY==0||Math.abs(velocityX/velocityY)>=4))
            {
                float mdx = Math.abs(dx);
                int type = velocityX>0?GestureInfo.RIGHT:GestureInfo.LEFT;
                if(60<mdx&&mdx<150)
                    m_kv.gesture(new GestureInfo(getKey((int)downX, (int)downY),type));
                else if(mdx>=150)
                    m_kv.gesture(new GestureInfo(null,type));
            }
            if(Math.abs(velocityY)>1000&&(velocityX==0||Math.abs(velocityY/velocityX)>=4))
            {
                float mdy = Math.abs(dy);
                int type = velocityY>0?GestureInfo.DOWN:GestureInfo.UP;
                if(60<mdy&&mdy<150)
                    m_kv.gesture(new GestureInfo(getKey((int)downX, (int)downY),type));
                else if(mdy>=150)
                    m_kv.gesture(new GestureInfo(null,type));
            }
            Log.w(st.TAG, "dx="+dx+"; dy="+dy+";vX="+velocityX+";vY="+velocityY+"|downX="+downX+"; downY="+downY);
            return true;
//            return super.onFling(e1, e2, velocityX, velocityY);
        }
        final LatinKey getKey(int x,int y)
        {
            for(Key k:m_kv.getCurKeyboard().getKeys())
            {
                if(k.isInside(x, y))
                    return (LatinKey)k;
            }
            return null;
        }
    };
    public static class GestureInfo
    {
        public static final int LEFT    = 1;
        public static final int RIGHT   = 2;
        public static final int UP      = 3;
        public static final int DOWN    = 4;
        LatinKey downKey;
        int dir;
        public GestureInfo(LatinKey k,int dir)
        {
            downKey = k;
            this.dir = dir;
        }
        
    }
}
