package asiet.alumniapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ProfileCompletionActivity extends AppCompatActivity
{

    private String CurrentJob, Expertise, CourseTimeFrame, UniversityRollNo;
    private Button UpdateButton;
    private ScrollView SV;
    private EntryAnimation EA;
    private Thread AnimationThread;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_completion);
        UpdateButton = findViewById(R.id.UpdateProfileContinueButton);
        SV = findViewById(R.id.UpdateProfileSV);
        EA = findViewById(R.id.UpdateProfileAnim);
    }

    public void UpdateProfilePressed(View view)
    {
        EditText CurrentJobET = findViewById(R.id.CurrentJobET);
        EditText ExpertiseET = findViewById(R.id.ExpertiseET);
        EditText CoureTimeFrameET = findViewById(R.id.CourseTimeFrameET);
        EditText UniversityRollNoET = findViewById(R.id.UniversityRollNoET);

        CurrentJob = CurrentJobET.getText().toString();
        Expertise = ExpertiseET.getText().toString();
        CourseTimeFrame = CoureTimeFrameET.getText().toString();
        UniversityRollNo = UniversityRollNoET.getText().toString();

        if(CurrentJob.isEmpty())
        {
            CurrentJobET.requestFocus();
            CurrentJobET.setError("Enter current job here!");
        }
        else if(Expertise.isEmpty())
        {
            ExpertiseET.requestFocus();
            ExpertiseET.setError("Enter area of expertise here!");
        }
        else if(CourseTimeFrame.isEmpty())
        {
            CoureTimeFrameET.requestFocus();
            CoureTimeFrameET.setError("Enter course time frame here!");
        }
        else if(UniversityRollNo.isEmpty())
        {
            UniversityRollNoET.requestFocus();
            UniversityRollNoET.setError("Enter university roll number here!");
        }
        else
        {
            Thread ProfileUpdationThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    UploadNewDetails();
                }
            });
            SV.setAlpha(0.5f);
            if(!EA.isRunning)
                StartAnimation();
            ProfileUpdationThread.start();
        }
    }

    private void UploadNewDetails()
    {
        try
        {
            String Username = getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("username","...");
            HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.UpdateProfileAddress).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),"UTF-8"));
            bufferedWriter.write("username=" + Username + "&CurrentJob=" + CurrentJob + "&Expertise=" + Expertise + "&CourseTimeFrame=" + CourseTimeFrame + "&UniversityRollNo=" + UniversityRollNo);
            bufferedWriter.flush();
            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit()
                        .putBoolean("profile_completed",true)
                        .apply();
                Intent returnIntent = new Intent();
                setResult(RESULT_OK,returnIntent);
                finish();
            }
            else
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        SV.setAlpha(1);
                        if(EA.isRunning)
                                StopAnimation();
                        Toast.makeText(ProfileCompletionActivity.this, "Bad Internet Connection!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        catch(Exception ex)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    SV.setAlpha(1);
                    if(EA.isRunning)
                        StopAnimation();
                    Toast.makeText(ProfileCompletionActivity.this, "Bad Internet Connection!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void StartAnimation()
    {
        EA.isRunning = true;
        EA.setVisibility(View.VISIBLE);
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
}
