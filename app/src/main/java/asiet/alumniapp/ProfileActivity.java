package asiet.alumniapp;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ProfileActivity extends AppCompatActivity
{
    private ConstraintLayout LL;
    private final int AccountActivityResultCode = 0;
    private final int NotificationActivityrequestCode = 1;
    private final int PhoneVerificationActivityRequestCode = 2;
    private EntryAnimation EA;
    private Thread AnimationThread;
    private TextView PollLoadingTV;
    private View[] viewArray;
    static boolean ShowBadge = false;

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

        if(!getSharedPreferences(CommonData.SP,MODE_PRIVATE).getBoolean("phone_number_verified",false))
            startActivityForResult(new Intent(this, PhoneVerificationActivity.class), PhoneVerificationActivityRequestCode);

        if(!getSharedPreferences(CommonData.SP,MODE_PRIVATE).getBoolean("profile_completed",false))
           AddNotification("Profile Incomplete\nPlease complete your profile!",NotificationActivity.NotificationId.ProfileComplete);

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
        LL.addView(LayoutInflater.from(this).inflate(R.layout.profile_poll, LL, false));
        final LinearLayout CL = findViewById(R.id.PollLayout);
        EA = findViewById(R.id.PollAnim);
        PollLoadingTV = findViewById(R.id.LoadingPollTV);

        if (!EA.isRunning)
            StartAnimation();
        final Poll poll = new Poll(getSharedPreferences(CommonData.SP, MODE_PRIVATE).getString("username", "..."), this);
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
                        if (EA.isRunning)
                            StopAnimation();
                        if (viewArray == null)
                            PollLoadingTV.setText("Bad Internet Connection! Try Again.");
                        else
                        {
                            CL.removeAllViews();
                            View.OnClickListener responseClickListener = new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    PollCardView pollCardView = (PollCardView) view.getParent().getParent();
                                    pollCardView.setResponse(((Button) view).getText().toString());
                                    TextView responseTV = pollCardView.findViewById(R.id.PollResponseTV);
                                    responseTV.setText("Your response is '" + pollCardView.getResponse() + "'");
                                    responseTV.setVisibility(View.VISIBLE);
                                }
                            };
                            View.OnClickListener submitClickListener = new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    PollCardView pollCardView = (PollCardView) view.getParent().getParent();
                                    if (!pollCardView.getResponse().isEmpty())
                                    {
                                        poll.UpdatePoll(pollCardView.getResponse(), String.valueOf(pollCardView.getPollId()));
                                        view.setEnabled(false);
                                        view.setVisibility(View.GONE);
                                        View parentView = (View) view.getParent();
                                        Button b1, b2, b3, b4;
                                        b1 = parentView.findViewById(R.id.PollButton1);
                                        b2 = parentView.findViewById(R.id.PollButton2);
                                        b3 = parentView.findViewById(R.id.PollButton3);
                                        b4 = parentView.findViewById(R.id.PollButton4);
                                        b1.setVisibility(View.GONE);
                                        b2.setVisibility(View.GONE);
                                        b3.setVisibility(View.GONE);
                                        b4.setVisibility(View.GONE);
                                        b1.setEnabled(false);
                                        b2.setEnabled(false);
                                        b3.setEnabled(false);
                                        b4.setEnabled(false);
                                    } else
                                    {
                                        TextView responseTV = pollCardView.findViewById(R.id.PollResponseTV);
                                        responseTV.setText("Please select a response!");
                                        responseTV.setVisibility(View.VISIBLE);
                                    }
                                }
                            };
                            View.OnClickListener otherClickListener = new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    final PollCardView pollCardView = (PollCardView) view.getParent().getParent();
                                    final EditText ET = new EditText(ProfileActivity.this);
                                    ET.setText(pollCardView.getResponse());
                                    new AlertDialog.Builder(ProfileActivity.this)
                                            .setTitle(pollCardView.getOtherText())
                                            .setView(ET)
                                            .setPositiveButton("Continue", new DialogInterface.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i)
                                                {
                                                    pollCardView.setResponse(ET.getText().toString());
                                                    TextView responseTV = pollCardView.findViewById(R.id.PollResponseTV);
                                                    responseTV.setText("Your response is '" + pollCardView.getResponse() + "'");
                                                    responseTV.setVisibility(View.VISIBLE);
                                                }
                                            })
                                            .create().show();
                                }
                            };
                            for (View v : viewArray)
                            {
                                CL.addView(v);
                                v.findViewById(R.id.PollButton1).setOnClickListener(responseClickListener);
                                v.findViewById(R.id.PollButton2).setOnClickListener(responseClickListener);
                                v.findViewById(R.id.PollButton3).setOnClickListener(responseClickListener);
                                v.findViewById(R.id.PollButton4).setOnClickListener(otherClickListener);
                                v.findViewById(R.id.PollSubmitButton).setOnClickListener(submitClickListener);
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

    public void RefreshPollPressed(View view)
    {
        PollPressed();
    }

    public void NotificationsPressed(View view)
    {
        startActivityForResult(new Intent(this,NotificationActivity.class),NotificationActivityrequestCode);
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

    private void AddNotification(String Message, int Identifier)
    {
        NotificationActivity.AddNotification(Message, Identifier);
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
        startActivityForResult(AccountIntent,AccountActivityResultCode);
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
        if(requestCode == AccountActivityResultCode)
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
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                LoadProfilePicture();
        }
        else if(requestCode == NotificationActivityrequestCode)
        {
            ShowBadge = false;
        }
        else if(requestCode == PhoneVerificationActivityRequestCode)
        {
            if(resultCode != RESULT_OK)
            {
                new AlertDialog.Builder(this)
                        .setTitle("Verification Needed")
                        .setMessage("Phone number must be verified to use our service!")
                        .setPositiveButton("Verify Now", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                startActivityForResult(new Intent(ProfileActivity.this,PhoneVerificationActivity.class),PhoneVerificationActivityRequestCode);
                            }
                        })
                        .setNegativeButton("Logout", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                CommonData.Logout(ProfileActivity.this);
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("Status","Logout");
                                setResult(Activity.RESULT_OK,returnIntent);
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create().show();
            }
        }

        if(ShowBadge)
            findViewById(R.id.NotificationBadge).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.NotificationBadge).setVisibility(View.INVISIBLE);
    }
}
