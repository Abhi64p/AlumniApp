package asiet.alumniapp;

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
    private ArrayList<String> mDataset;
    private DisplayMetrics DM;

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        public View mView;

        public MyViewHolder(View v)
        {
            super(v);
            mView = v;
        }
    }

    public NotificationRVAdapter(ArrayList<String> myDataset)
    {
        mDataset = myDataset;
    }

    @Override
    public NotificationRVAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        DM = parent.getResources().getDisplayMetrics();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(getDP(8),getDP(35),getDP(8),getDP(0));

        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_tv,parent,false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
        TextView tv =  holder.mView.findViewById(R.id.NotificationContent);
        tv.setText(mDataset.get(position));
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
}
