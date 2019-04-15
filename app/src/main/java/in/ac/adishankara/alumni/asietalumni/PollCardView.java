package in.ac.adishankara.alumni.asietalumni;

import android.content.Context;
import android.util.AttributeSet;

import androidx.cardview.widget.CardView;

public class PollCardView extends CardView
{
    private int PollId;
    private String Response = "";
    private String OtherText = null;

    public String getOtherText()
    {
        return OtherText;
    }

    public void setOtherText(String otherText)
    {
        OtherText = otherText;
    }

    public PollCardView(Context context)
    {
        super(context);
    }

    public PollCardView(Context context, AttributeSet attr)
    {
        super(context,attr);
    }

    public int getPollId()
    {
        return PollId;
    }

    public void setPollId(int pollId)
    {
        PollId = pollId;
    }

    public String getResponse()
    {
        return Response;
    }

    public void setResponse(String response)
    {
        Response = response;
    }
}
