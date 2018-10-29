package asiet.alumniapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity
{
    private static ArrayList<CommonData.NotificationData> NotificationList;
    private static RecyclerView.Adapter adapter = null;
    static class NotificationId
    {
        static final int ProfileComplete = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        if(NotificationList == null)
            NotificationList = new ArrayList<>();
        RecyclerView RV = findViewById(R.id.NotificationRV);
        adapter = new NotificationRVAdapter(NotificationList);
        ((NotificationRVAdapter) adapter).setContext(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        RV.setLayoutManager(layoutManager);
        RV.setAdapter(adapter);

        if(NotificationList.size() == 0)
        {
            findViewById(R.id.NoNotificationTV).setVisibility(View.VISIBLE);
            findViewById(R.id.NotificationDismissTV).setVisibility(View.GONE);
        }
        else
        {
            findViewById(R.id.NoNotificationTV).setVisibility(View.GONE);
            findViewById(R.id.NotificationDismissTV).setVisibility(View.VISIBLE);
        }

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT)
        {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1)
            {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i)
            {
                int Position = viewHolder.getAdapterPosition();
                NotificationList.remove(Position);
                adapter.notifyItemRemoved(Position);
                if(NotificationList.size() == 0)
                {
                    findViewById(R.id.NoNotificationTV).setVisibility(View.VISIBLE);
                    findViewById(R.id.NotificationDismissTV).setVisibility(View.GONE);
                }
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(RV);

    }

    static void AddNotification(String Text, int NotificationId)
    {
        if (NotificationList == null)
            NotificationList = new ArrayList<>();
        boolean Found = false;
        ProfileActivity.ShowBadge = true;
        for (int i = 0; i < NotificationList.size(); i++)
        {
            if (NotificationId == Integer.parseInt(NotificationList.get(i).NotificationId))
                Found = true;
        }
        if (!Found)
            NotificationList.add(0, new CommonData.NotificationData(String.valueOf(NotificationId), Text));
        ProfileActivity.ShowBadge = true;
    }

    public static void ClearList()
    {
        if(NotificationList != null)
            NotificationList.clear();
    }

    static void RemoveNotification(int NotificationId)
    {
        if (NotificationList != null)
            for (int i = 0; i < NotificationList.size(); i++)
            {
                if (NotificationId == Integer.parseInt(NotificationList.get(i).NotificationId))
                {
                    NotificationList.remove(i);
                    if (adapter != null)
                        adapter.notifyItemRemoved(i);
                }
            }
    }
}
