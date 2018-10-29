package asiet.alumniapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class NotificationRVAdapter extends RecyclerView.Adapter<NotificationRVAdapter.MyViewHolder>
{
    private ArrayList<CommonData.NotificationData> mDataset;
    private DisplayMetrics DM;
    private Context context;

    static class MyViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        MyViewHolder(View v)
        {
            super(v);
            mView = v;
        }
    }

    NotificationRVAdapter(ArrayList<CommonData.NotificationData> myDataset)
    {
        mDataset = myDataset;
    }

    @Override @NonNull
    public NotificationRVAdapter.MyViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType)
    {
        DM = parent.getResources().getDisplayMetrics();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(getDP(8),getDP(35),getDP(8),getDP(0));

        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_tv,parent,false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        TextView tv =  holder.mView.findViewById(R.id.NotificationContent);
        CommonData.NotificationData Data = mDataset.get(position);
        tv.setText(Data.NotificationText);
        tv.setHint(Data.NotificationId);
        tv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
               int NotificationId = Integer.parseInt(((TextView)view).getHint().toString());
                switch (NotificationId)
                {
                    case NotificationActivity.NotificationId.ProfileComplete :
                    {
                        context.startActivity(new Intent(context,EditAccountActivity.class));
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }

    private int getDP(int pixel)
    {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel,DM);
    }

    public void setContext(Context context)
    {
        this.context = context;
    }
}
