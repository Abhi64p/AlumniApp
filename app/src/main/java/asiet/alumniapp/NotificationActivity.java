package asiet.alumniapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity
{
    private static ArrayList<String> NotificationList;
    private RecyclerView.Adapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        if(NotificationList == null)
            NotificationList = new ArrayList<>();
        RecyclerView RV = findViewById(R.id.NotificationRV);
        adapter = new NotificationRVAdapter(NotificationList);
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

    public static void AddNotification(String Text)
    {
        if(NotificationList == null)
            NotificationList = new ArrayList<>();
        NotificationList.add(0,Text);
    }

    public static void ClearList()
    {
        if(NotificationList != null)
            NotificationList.clear();
    }
}
