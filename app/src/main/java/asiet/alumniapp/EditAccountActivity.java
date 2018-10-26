package asiet.alumniapp;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class EditAccountActivity extends AppCompatActivity
{

    private String CurrentJob, Expertise, CourseTimeFrame, UniversityRollNo, PassoutYear, Department;
    private Button UpdateButton;
    private ScrollView SV;
    private EntryAnimation EA;
    private Thread AnimationThread;
    private Boolean Complete;
    private EditText CurrentJobET;
    private EditText ExpertiseET;
    private EditText CourseTimeFrameET;
    private EditText UniversityRollNoET;
    private EditText PassoutYearET;
    private EditText DepartmentET;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);
        UpdateButton = findViewById(R.id.UpdateProfileContinueButton);
        SV = findViewById(R.id.UpdateProfileSV);
        EA = findViewById(R.id.UpdateProfileAnim);
        CurrentJobET = findViewById(R.id.CurrentJobET);
        ExpertiseET = findViewById(R.id.ExpertiseET);
        CourseTimeFrameET = findViewById(R.id.CourseTimeFrameET);
        UniversityRollNoET = findViewById(R.id.UniversityRollNoET);
        PassoutYearET = findViewById(R.id.PassoutYearET);
        DepartmentET = findViewById(R.id.DepartmentET);

        LoadData();
    }

    private void LoadData()
    {
        SharedPreferences SP = getSharedPreferences(CommonData.SP,MODE_PRIVATE);
        CurrentJobET.setText(SP.getString("current_job",""));
        ExpertiseET.setText(SP.getString("area_of_expertise",""));
        CourseTimeFrameET.setText(SP.getString("course_time_frame",""));
        UniversityRollNoET.setText(SP.getString("university_roll_no",""));
        PassoutYearET.setText(SP.getString("passout_year",""));
        DepartmentET.setText(SP.getString("department",""));
    }

    public void UpdateProfilePressed(View view)
    {
        CurrentJob = CurrentJobET.getText().toString();
        Expertise = ExpertiseET.getText().toString();
        CourseTimeFrame = CourseTimeFrameET.getText().toString();
        UniversityRollNo = UniversityRollNoET.getText().toString();
        PassoutYear = PassoutYearET.getText().toString();
        Department = DepartmentET.getText().toString();

        Complete = true;
        int Update = 0;

        if(CurrentJob.isEmpty())
        {
            Complete=false;
            Update++;
        }
        if(Expertise.isEmpty())
        {
            Complete=false;
            Update++;
        }
        if(CourseTimeFrame.isEmpty())
        {
            Complete=false;
            Update++;
        }
        if(UniversityRollNo.isEmpty())
        {
            Complete=false;
            Update++;
        }
        if(PassoutYear.isEmpty())
        {
            Complete=false;
            Update++;
        }
        if(Department.isEmpty())
        {
            Complete=false;
            Update++;
        }

        if(Update != 6)
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
            UpdateButton.setEnabled(false);
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
            int Completed = 0;
            if(Complete)
                Completed = 1;
            bufferedWriter.write("username=" + Username + "&CurrentJob=" + CurrentJob + "&Expertise=" + Expertise + "&CourseTimeFrame=" + CourseTimeFrame + "&UniversityRollNo=" + UniversityRollNo +"&Completed=" + Completed + "&passout_year=" + PassoutYear + "&department=" + Department);
            bufferedWriter.flush();
            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                SharedPreferences.Editor editor = getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit();
                if(Complete)
                    editor.putBoolean("profile_completed",true);
                else
                    editor.putBoolean("profile_completed",false);
                editor.putString("current_job",CurrentJob);
                editor.putString("area_of_expertise",Expertise);
                editor.putString("course_time_frame",CourseTimeFrame);
                editor.putString("university_roll_no",UniversityRollNo);
                editor.putString("passout_year", PassoutYear);
                editor.putString("department", Department);
                editor.apply();

                setResult(RESULT_OK);
                this.finish();
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
                        Toast.makeText(EditAccountActivity.this, "Bad Internet Connection!", Toast.LENGTH_SHORT).show();
                        UpdateButton.setEnabled(true);
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
                    Toast.makeText(EditAccountActivity.this, "Bad Internet Connection!", Toast.LENGTH_SHORT).show();
                    UpdateButton.setEnabled(true);
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
