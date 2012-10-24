package com.jbak.CustomGraphics;

import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;

public class BitmapCachedGradBack extends GradBack
{
    int m_cacheSize = 20;
    static Vector<Vector<BmpCacheEntry>> caches = new Vector<Vector<BmpCacheEntry>>();
    Vector<BmpCacheEntry> m_cache = new Vector<BitmapCachedGradBack.BmpCacheEntry>();
    BmpCacheEntry m_curEntry;
    public BitmapCachedGradBack(int startColor, int endColor)
    {
        super(startColor, endColor);
    }
    @Override
    protected void onResize(float width, float height) 
    {
        int w = (int)width,h = (int)height;
        m_curEntry = searchEntry((int)width, (int)height);
        if(m_curEntry!=null)
        {
            if(m_curEntry.isValid())
                return;
            m_cache.remove(m_curEntry);
        }
        super.onResize((int)width, (int)height);
        if(m_cache.size()==0)
            caches.add(m_cache);
        m_curEntry = new BmpCacheEntry();
        m_curEntry.w = w;
        m_curEntry.h = h;
        m_curEntry.bmpNormal = Bitmap.createBitmap(w, h,Config.ARGB_8888);
        boolean press = m_bPressed;
        boolean ch = m_bCheckable;
        m_bCheckable = false;
        m_bPressed = false;
        super.draw(new Canvas(m_curEntry.bmpNormal), null);
        m_bPressed = true;
        m_curEntry.bmpPress = Bitmap.createBitmap(w, h,Config.ARGB_8888);
        super.draw(new Canvas(m_curEntry.bmpPress), null);
        if(m_cache.size()==m_cacheSize)
            m_cache.remove(0);
        m_cache.add(m_curEntry);
        m_bPressed = press;
        m_bCheckable = ch;
    }
    @Override
    public void draw(Canvas canvas, Paint paint) 
    {
        if(!m_curEntry.isValid())
        {
            onResize(m_curEntry.w, m_curEntry.h);
        }
        Bitmap bmp = m_bPressed?m_curEntry.bmpPress:m_curEntry.bmpNormal;
        canvas.drawBitmap(bmp,0,0,null);
        if(m_bCheckable||m_bChecked)
            onDrawCheckMark(canvas,m_bChecked, m_rect);
    };
    public BmpCacheEntry searchEntry(int width,int height)
    {
        for(BmpCacheEntry ce:m_cache)
        {
            if(ce.w==width&&ce.h==height)
                return ce;
        }
        return null;
    }
    public BitmapCachedGradBack setCacheSize(int size)
    {
        m_cacheSize = size;
        return this;
    }
    public void recycle()
    {
        for(BmpCacheEntry ce:m_cache)
        {
            ce.recycle();
        }
        m_cache.clear();
    }
    public static class BmpCacheEntry
    {
        int w;
        int h;
        Bitmap bmpNormal;
        Bitmap bmpPress;
        void recycle()
        {
            if(bmpNormal!=null&&!bmpNormal.isRecycled())
                bmpNormal.recycle();
            if(bmpPress!=null&&!bmpPress.isRecycled())
                bmpPress.recycle();
        }
        boolean isValid()
        {
            return bmpNormal!=null&&!bmpNormal.isRecycled()&&bmpPress!=null&&!bmpPress.isRecycled();
        }
    }
    public static void clearAllCache()
    {
        for(Vector<BmpCacheEntry> cache:caches)
        {
            for(BmpCacheEntry be:cache)
            {
                be.recycle();
            }
            cache.clear();
        }
        caches.clear();
    }
}
