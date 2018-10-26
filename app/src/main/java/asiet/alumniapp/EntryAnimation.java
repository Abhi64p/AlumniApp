package asiet.alumniapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


public class EntryAnimation extends View
{
    private Paint Light, Dark;
    private int i;
    private boolean Direction;
    public boolean isRunning = false;

    public EntryAnimation(Context context)
    {
        super(context);
        Init();
    }

    public EntryAnimation(Context context, AttributeSet attributeSet)
    {
        super(context,attributeSet);
        Init();
    }

    private void Init()
    {
        Light = new Paint();
        Light.setColor(Color.parseColor("#c7c7c7"));
        Light.setAntiAlias(true);
        Light.setStyle(Paint.Style.FILL);

        Dark = new Paint();
        Dark.setAntiAlias(true);
        Dark.setColor(Color.parseColor("#8d8d8d"));
        Dark.setStyle(Paint.Style.FILL);

        i = 0;
        Direction = true;
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        for(int j = 0; j < 4; j++)
        {
            if(j == i % 4)
                canvas.drawCircle(getDP(10+j*20),getDP(20),getDP(7),Light);
            else
                canvas.drawCircle(getDP(10+j*20),getDP(20),getDP(7),Dark);
        }
    }

    private float getDP(int pixel)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, getResources().getDisplayMetrics());
    }

    public void Change()
    {
        if(Direction)
        {
            if(i == 3)
            {
                i = 2;
                Direction = false;
            }
            else
                i++;
        }
        else
        {
            if(i == 0)
            {
                i = 1;
                Direction = true;
            }
            else
                i--;
        }
        this.invalidate();
    }
}