package asiet.alumniapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class CustomButton extends android.support.v7.widget.AppCompatButton
{
    private String PollId;
    private View view;

    public View getView()
    {
        return view;
    }

    public void setView(View view)
    {
        this.view = view;
    }

    public String getPollId()
    {
        return PollId;
    }

    public void setPollId(String pollId)
    {
        PollId = pollId;
    }

    public CustomButton(Context context)
    {
        super(context);
    }

    public CustomButton(Context context, AttributeSet attr)
    {
        super(context,attr);
    }
}
