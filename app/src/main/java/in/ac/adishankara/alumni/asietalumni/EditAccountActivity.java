package in.ac.adishankara.alumni.asietalumni;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
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
                {
                    editor.putBoolean("profile_completed", true);
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            NotificationActivity.RemoveNotification(NotificationActivity.NotificationId.ProfileComplete);

                        }
                    });
                }
                else
                {
                    editor.putBoolean("profile_completed", false);
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            NotificationActivity.AddNotification("Profile Incomplete\nPlease complete your profile!",NotificationActivity.NotificationId.ProfileComplete);
                        }
                    });
                }
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
        catch(final Exception ex)
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

    public void DeptETClicked(View view)
    {
        final String[] depts = { "Aeronautical Engineering", "Aerospace Engineering", "Applied Electronics & Instrumentation", "Civil Engineering", "Computer Science & Engineering",
                "Chemical Engineering", "Electrical & Electronics Engineering", "Electronics & Communication Engineering",
                "Information & Communication Technology", "Information Technology", "Mechanical Engineering"};

        ArrayAdapter<String> adp = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,depts);

        final Spinner spinner = new Spinner(this);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        spinner.setAdapter(adp);
        spinner.setPopupBackgroundResource(R.drawable.spinner_background);
        spinner.getBackground().setColorFilter(Color.BLUE,PorterDuff.Mode.SRC_ATOP);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Department");
        builder.setView(spinner);
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                int index = spinner.getSelectedItemPosition();
                ((EditText)findViewById(R.id.DepartmentET)).setText(depts[index]);
            }
        });
        builder.create().show();
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
