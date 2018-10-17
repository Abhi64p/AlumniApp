package asiet.alumniapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SignUpActivity extends AppCompatActivity
{
    private EditText SignUpPassword1ET, SignUpPassword2ET;
    private Thread AnimationThread;
    private EntryAnimation EA;
    private ScrollView SignUpScrollView;
    private String Email, Name, Password1, Phone, DOB, DeptSelected = "";
    private Button ContinueButton;
    private EditText SignUpPhoneET;
    private static EditText DOBTV;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        SignUpPassword1ET = findViewById(R.id.SignUpPassword1ET);
        SignUpPassword2ET = findViewById(R.id.SignUpPassword2ET);
        ImageButton UnMaskButton1, UnMaskButton2;
        UnMaskButton1 = findViewById(R.id.UnMaskButton1);
        UnMaskButton2 = findViewById(R.id.UnMaskButton2);
        UnMaskButton1.setOnTouchListener(MaskTouchListener);
        UnMaskButton2.setOnTouchListener(MaskTouchListener);
        EA = findViewById(R.id.SignUpEA);
        SignUpScrollView = findViewById(R.id.SignUpScrollView);
        TextView TV = findViewById(R.id.SignUpEmailTV);
        Email = this.getIntent().getStringExtra("email");
        TV.setText("E-Mail : " + Email);
        TV.setSelected(true);
        ContinueButton = findViewById(R.id.SignUpContinueButton);
        DOBTV = findViewById(R.id.DOBTV);
    }

    private View.OnTouchListener MaskTouchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent)
        {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            {
                if(view.getId() == R.id.UnMaskButton1)
                   SignUpPassword1ET.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                else if(view.getId() == R.id.UnMaskButton2)
                    SignUpPassword2ET.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else if(motionEvent.getAction() == MotionEvent.ACTION_UP)
            {
                if(view.getId() == R.id.UnMaskButton1)
                    SignUpPassword1ET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                else if(view.getId() == R.id.UnMaskButton2)
                    SignUpPassword2ET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            return  true;
        }
    };

    public void CreateAccountPressed(final View view)
    {
        EditText SignUpNameET = findViewById(R.id.SignUpNameET);
        SignUpPhoneET = findViewById(R.id.signUpPhoneET);
        EditText DeptET = findViewById(R.id.DeptET);

        Name = SignUpNameET.getText().toString();
        Phone = SignUpPhoneET.getText().toString();
        Password1 = SignUpPassword1ET.getText().toString();
        final String Password2 = SignUpPassword2ET.getText().toString();
        DOB = DOBTV.getText().toString();

        if(Name.isEmpty())
        {
            SignUpNameET.requestFocus();
            SignUpNameET.setError("Enter your name here!");
        }
        else if(Password1.isEmpty())
        {
            SignUpPassword1ET.requestFocus();
            SignUpPassword1ET.setError("Enter your password here!");
        }
        else if(Password1.length() <6)
        {
            SignUpPassword1ET.requestFocus();
            SignUpPassword1ET.setError("Password length must be 6 or more!");
        }
        else if(Password2.isEmpty())
        {
            SignUpPassword2ET.requestFocus();
            SignUpPassword2ET.setError("Re-enter your password here!");
        }
        else if(!Password1.equals(Password2))
        {
            SignUpPassword2ET.requestFocus();
            SignUpPassword2ET.setError("Passwords doesn't match!");
        }
        else if(Phone.isEmpty())
        {
            SignUpPhoneET.requestFocus();
            SignUpPhoneET.setError("Enter your phone number here!");
        }
        else if(DOB.isEmpty())
            DOBTV.setError("Enter date of birth here!");
        else if(DeptSelected.isEmpty())
            DeptET.setError("Enter department here!");
        else
        {
            Phone = "+91" + Phone;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("Email : " + Email + "\nPhone : " + Phone);
            builder.setTitle("Is this correct ?");
            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    StartAccountCreation();
                }
            });
            builder.setNegativeButton("Change Email", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("Status","ChangeEmail");
                    setResult(Activity.RESULT_OK,returnIntent);
                    SignUpActivity.this.finish();
                }
            });
            builder.setNeutralButton("Change Number", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    SignUpPhoneET.requestFocus();
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
        }
    }

    private void StartAccountCreation()
    {
        ContinueButton.setEnabled(false);
        if (!EA.isRunning)
            StartAnimation();
        Thread PhoneCheckThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(CommonData.CheckPhoneAddress).openConnection();
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                    writer.write("phone_number=" + Phone);
                    writer.flush();
                    if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK)
                    {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                        StringBuilder SB = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null)
                            SB.append(line);
                        reader.close();

                        final String LoginResponse = SB.toString();

                        if (LoginResponse.equals("PhoneNumberExist"))
                        {
                            if(EA.isRunning)
                                StopAnimation();
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    SignUpPhoneET.requestFocus();
                                    SignUpPhoneET.setError("This phone number is already registered!");
                                    ContinueButton.setEnabled(true);
                                }
                            });
                        }
                        else
                        {
                            urlConnection = (HttpsURLConnection) new URL(CommonData.InsertDetailsAddress).openConnection();
                            urlConnection.setDoOutput(true);
                            urlConnection.setRequestMethod("POST");
                            writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                            writer.write("email=" + Email+"&phone_number=" + Phone + "&name=" + Name + "&password=" + Password1 + "&phone_number=" + Phone + "&department=" + DeptSelected + "&year=" + DOB.split("/")[2]);
                            writer.flush();
                            if(EA.isRunning)
                                StopAnimation();
                            if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK)
                            {
                                BufferedReader Reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
                                StringBuilder stringBuilder = new StringBuilder();
                                String Line;
                                while((Line = Reader.readLine()) != null)
                                    stringBuilder.append(Line);
                                String ReturnData[] = stringBuilder.toString().split(":");
                                getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit()
                                        .putBoolean("LoggedIn",true)
                                        .putString("email",Email)
                                        .putString("password",Password1)
                                        .putString("name",Name)
                                        .putString("token",ReturnData[0])
                                        .putString("username",ReturnData[1])
                                        .apply();
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("Status","Created");
                                setResult(Activity.RESULT_OK,returnIntent);
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        new AlertDialog.Builder(SignUpActivity.this)
                                                .setTitle("Account Created")
                                                .setMessage("You will be logged into your account automatically")
                                                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i)
                                                    {
                                                        SignUpActivity.this.finish();
                                                    }
                                                })
                                                .setCancelable(false)
                                                .create().show();
                                    }
                                });
                            }
                            else
                            {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Toast.makeText(SignUpActivity.this, "Bad Internet Connection. Try again 1", Toast.LENGTH_LONG).show();
                                        if(EA.isRunning)
                                            StopAnimation();
                                        ContinueButton.setEnabled(true);
                                    }
                                });
                            }
                        }
                    }
                    else
                    {
                        if(EA.isRunning)
                            StopAnimation();
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Toast.makeText(SignUpActivity.this, "Bad Internet Connection. Try again 2", Toast.LENGTH_LONG).show();
                                if(EA.isRunning)
                                    StopAnimation();
                                ContinueButton.setEnabled(true);
                            }
                        });
                    }
                } catch (final Exception ex)
                {
                    if(EA.isRunning)
                        StopAnimation();
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(SignUpActivity.this, "Bad Internet Connection. Try again, " + ex.getMessage(), Toast.LENGTH_LONG).show();
                            if(EA.isRunning)
                                StopAnimation();
                            ContinueButton.setEnabled(true);
                        }
                    });

                }
            }
        });
        PhoneCheckThread.start();
    }

    public void DateOfBirthClicked(View view)
    {
        DialogFragment fragment = new DatePickerFragment();
        ((DatePickerFragment) fragment).setDate(DOBTV.getText().toString());
        fragment.show(getSupportFragmentManager(),"DatePicker");
    }

    public static void setDate(String Date)
    {
        DOBTV.setText(Date);
    }

    public void DeptETClicked(View view)
    {
        final String[] depts = { "Applied Electronics & Instrumentation", "Civil Engineering", "Computer Science & Engineering", "Electrical & Electronics Engineering", "Electronics & Communication Engineering", "Information Technology", "Mechanical Engineering"};
        final String[] deptsMapped = { "AIE","CE", "CSE", "EEE", "ECE", "IT", "ME"};

        ArrayAdapter<String> adp = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,depts);

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
                ((EditText)findViewById(R.id.DeptET)).setText(depts[index]);
                DeptSelected = deptsMapped[index];
            }
        });
        builder.create().show();
    }

    private void StartAnimation()
    {
        EA.isRunning = true;
        SignUpScrollView.setAlpha(0.5f);
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
                EA.setVisibility(View.GONE);
                SignUpScrollView.setAlpha(1);
            }
        });
        AnimationThread.interrupt();
    }

    @Override
    public void onBackPressed()
    {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
