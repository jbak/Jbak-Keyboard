package com.jbak.JbakKeyboard;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class AdPreference extends Preference
{
    public AdPreference(Context context, AttributeSet attrs, int defStyle) {super    (context, attrs, defStyle);}
    public AdPreference(Context context, AttributeSet attrs) {super(context, attrs);}
    public AdPreference(Context context) {super(context);}

    @Override
    protected View onCreateView(ViewGroup parent) {
        // this will create the linear layout defined in ads_layout.xml
        View view = super.onCreateView(parent);

        // the context is a PreferenceActivity
        Activity activity = (Activity)getContext();

        // Create the adView
        AdView adView = new AdView(activity, AdSize.BANNER, ServiceJbKbd.PID);

        ((LinearLayout)view).addView(adView);

        // Initiate a generic request to load it with an ad
        AdRequest request = new AdRequest();
        adView.loadAd(request);     

        return view;    
    }
}
