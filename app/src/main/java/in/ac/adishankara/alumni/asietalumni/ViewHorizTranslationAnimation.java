package in.ac.adishankara.alumni.asietalumni;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ViewHorizTranslationAnimation extends Animation
{
    private View view;
    private float oldValue, newValue;
    private float oldAlpha, newAlpha;

    public ViewHorizTranslationAnimation(View view)
    {
        this.view = view;
        oldValue = view.getX();
        oldAlpha = view.getAlpha();
        if(oldAlpha == 1)
            newAlpha = 0;
        else
            newAlpha = 1;
        this.setDuration(300);
    }

    public void setNewValue(float newValue)
    {
        this.newValue = newValue;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation)
    {
        float val = oldValue + ((newValue - oldValue) * interpolatedTime);
        float alpha = oldAlpha + ((newAlpha - oldAlpha) * interpolatedTime);
        view.setX(val);
        view.setAlpha(alpha);
    }
}
