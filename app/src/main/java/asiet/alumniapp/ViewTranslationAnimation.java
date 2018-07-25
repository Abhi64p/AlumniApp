package asiet.alumniapp;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ViewTranslationAnimation extends Animation
{
    private View view;
    private float oldValue, newValue;

    public ViewTranslationAnimation(View view)
    {
        oldValue = view.getY();
        this.view = view;
    }

    public void setNewValue(float newValue)
    {
        this.newValue = newValue;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation)
    {
        float val = oldValue + ((newValue - oldValue) * interpolatedTime);
        view.setY(val);
    }
}
