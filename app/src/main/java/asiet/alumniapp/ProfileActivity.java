package asiet.alumniapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

public class ProfileActivity extends AppCompatActivity
{
    private ConstraintLayout LL;
    private int ProfileActivityResultCode = 0;
    private EntryAnimation EA;
    private Thread AnimationThread;
    private TextView PollLoadingTV;
    private View[] viewArray;
    public static String err = "";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.navigation_home:
                    HomePressed();
                    return true;
                case R.id.navigation_poll:
                    PollPressed();
                    return true;
                case R.id.navigation_jobs:
                    JobsPressed();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        LL = findViewById(R.id.ProfileLayout);
        HomePressed();

        if(!getSharedPreferences(CommonData.SP,MODE_PRIVATE).getBoolean("profile_completed",false))
           AddNotification("Profile Incomplete\nPlease complete your profile!");

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            LoadProfilePicture();
    }

    private void HomePressed()
    {
        LL.removeAllViewsInLayout();
        LL.addView(LayoutInflater.from(this).inflate(R.layout.profile_home,LL,false));
    }

    private void PollPressed()
    {
        LL.removeAllViewsInLayout();
        LL.addView(LayoutInflater.from(this).inflate(R.layout.profile_poll,LL,false));
        final LinearLayout CL = findViewById(R.id.PollLayout);
        EA = findViewById(R.id.PollAnim);
        PollLoadingTV = findViewById(R.id.LoadingPollTV);

        if(!EA.isRunning)
            StartAnimation();
        final Poll poll = new Poll(getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("username","..."),this);
        Thread BGThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                viewArray = poll.getPollViews();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(EA.isRunning)
                            StopAnimation();
                        if(viewArray == null)
                            PollLoadingTV.setText("Bad Internet Connection! Try Again." + err);
                        else
                        {
                            CL.removeAllViews();
                            View.OnClickListener onClickListener = new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    CustomButton customButton = (CustomButton)view;
                                    final String PollId = customButton.getPollId();
                                    final String Response = customButton.getText().toString();
                                    customButton.getView().findViewById(R.id.PollButton1).setEnabled(false);
                                    customButton.getView().findViewById(R.id.PollButton2).setEnabled(false);
                                    customButton.getView().findViewById(R.id.PollButton3).setEnabled(false);
                                    poll.UpdatePoll(Response,PollId);
                                    view.setBackgroundColor(Color.parseColor("#FF8D8D8D"));
                                }
                            };
                            for (View v : viewArray)
                            {
                                CL.addView(v);
                                v.findViewById(R.id.PollButton1).setOnClickListener(onClickListener);
                                v.findViewById(R.id.PollButton2).setOnClickListener(onClickListener);
                                v.findViewById(R.id.PollButton3).setOnClickListener(onClickListener);
                            }
                        }
                    }
                });
            }
        });
        BGThread.start();
    }

    private void JobsPressed()
    {
        LL.removeAllViewsInLayout();
        LL.addView(LayoutInflater.from(this).inflate(R.layout.profile_jobs,LL,false));
    }


    public void NotificationsPressed(View view)
    {
        startActivity(new Intent(this,NotificationActivity.class));
        findViewById(R.id.NotificationBadge).setVisibility(View.INVISIBLE);
    }

    public void ShareButtonPressed(View view)
    {
        startActivity(new Intent(this,ShareActivity.class));
    }

    public void FeedbackPressed(View view)
    {
        startActivity(new Intent(this,FeedbackActivity.class));
    }

    public void WhahtsNewPressed(View view)
    {
        startActivity(new Intent(this,WhatsnewActivity.class));
    }

    private void AddNotification(String Message)
    {
        NotificationActivity.AddNotification(Message);
        findViewById(R.id.NotificationBadge).setVisibility(View.VISIBLE);
    }

    private void LoadProfilePicture()
    {
        String ProPicPath = getExternalCacheDir().getAbsolutePath() + "/ProPic.webp";
        File Image = new File(ProPicPath);
        if(Image.exists())
        {
            ImageButton but = findViewById(R.id.ProfileImageButton);
            but.setImageBitmap(BitmapFactory.decodeFile(ProPicPath));
        }
    }

    public void ProfilePicturePressed(View view)
    {
        Intent AccountIntent = new Intent(this,AccountActivity.class);
        startActivityForResult(AccountIntent,ProfileActivityResultCode);
    }

    private void StartAnimation()
    {
        EA.isRunning = true;
        EA.setVisibility(View.VISIBLE);
        PollLoadingTV.setVisibility(View.VISIBLE);
        AnimationThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        Thread.sleep(150);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                EA.Change();
                            }
                        });
                    }
                    catch(Exception ex){ break; }
                }
            }
        });
        AnimationThread.start();
    }

    private void StopAnimation()
    {
        EA.isRunning = false;
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                EA.setVisibility(View.INVISIBLE);
            }
        });
        AnimationThread.interrupt();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        if(requestCode == ProfileActivityResultCode)
        {
            if (resultCode == RESULT_OK)
            {
                if (data.getStringExtra("Status").equals("Logout"))
                {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("Status", "Logout");
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
            else
            {
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    LoadProfilePicture();
            }
        }
    }
}
