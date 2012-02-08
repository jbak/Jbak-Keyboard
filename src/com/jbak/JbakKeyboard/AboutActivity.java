package com.jbak.JbakKeyboard;

import junit.runner.Version;
import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.jbak.CustomGraphics.ColorsGradientBack;
import com.jbak.CustomGraphics.ColorsGradientBack.RotatedColorsBackground;
import com.jbak.CustomGraphics.GradBack;

public class AboutActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        View v = getLayoutInflater().inflate(R.layout.about, null);
        v.setBackgroundDrawable(new ColorsGradientBack().setCorners(0, 0).setGap(0).getStateDrawable());
        try{
            String vers = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            String app = getString(R.string.about_version)+" "+vers+"\n\n"
                           +getString(R.string.about_market)+" https://market.android.com/details?id="+getPackageName()
                           +"\n\n"+getString(R.string.about_web); 
            ((TextView)v.findViewById(R.id.version)).setText(app);
        }
        catch (Throwable e) {}
        
        setContentView(v);
    }
}
