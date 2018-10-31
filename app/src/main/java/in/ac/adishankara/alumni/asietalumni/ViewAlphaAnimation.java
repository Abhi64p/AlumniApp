package in.ac.adishankara.alumni.asietalumni;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ViewAlphaAnimation extends Animation
{
    private View view;
    private float oldValue, newValue;

    public ViewAlphaAnimation(View view)
    {
        this.view = view;
        oldValue = view.getAlpha();
    }

    public void setNewValue(float newValue)
    {
        this.newValue = newValue;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation)
    {
        float val = oldValue + (newValue - oldValue)*interpolatedTime;
        view.setAlpha(val);
    }
}
