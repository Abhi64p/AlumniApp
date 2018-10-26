package asiet.alumniapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class EntryActivity extends AppCompatActivity
{
    private EntryAnimation EA;
    private Thread AnimationThread;
    private EditText EmailET;
    private static final int SignUpRequestCode = 1;
    private static final int ProfileRequestCode = 2;
    private boolean PasswordShowed = false;
    private boolean EmailShowedAtStartup = false;
    private Button ContinueButton;
    private Button LoginButton;
    private EditText PasswordET;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        EA = findViewById(R.id.entryAnimation);
        EmailET = findViewById(R.id.EmailET);
        ContinueButton = findViewById(R.id.ContinueButton);
        LoginButton = findViewById(R.id.LoginButton);
        PasswordET = findViewById(R.id.PasswordET);

        Thread IntroAimThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(10);
                }
                catch(Exception ex){}
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        LoginSignUp();
                    }
                });
            }
        });
        if(!EA.isRunning)
            StartAnimation();
        IntroAimThread.start();
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

    private void LoginSignUp()
    {
        boolean LoggedIn = getSharedPreferences(CommonData.SP,MODE_PRIVATE).getBoolean("LoggedIn",false);
        if(!LoggedIn)
        {
            if(EA.isRunning)
                StopAnimation();
            ShowLoginMenu();
        }
        else
        {
            Thread TokenCheckThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    CheckToken();
                }
            });
            TokenCheckThread.start();
        }
    }

    private void CheckToken()
    {
        try
        {
            SharedPreferences SP = getSharedPreferences(CommonData.SP,MODE_PRIVATE);
            String Username = SP.getString("username",null);
            String Token = SP.getString("token",null);
            HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.CheckTokenAddress).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            BufferedWriter Writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),"UTF-8"));
            Writer.write("token=" + Token + "&username=" + Username);
            Writer.flush();
            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                StringBuilder stringBuilder = new StringBuilder();
                String Line;
                BufferedReader Reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
                while((Line = Reader.readLine()) != null)
                    stringBuilder.append(Line);
                if(stringBuilder.toString().equals("Correct"))
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(EA.isRunning)
                                StopAnimation();
                            startActivityForResult(new Intent(EntryActivity.this,ProfileActivity.class),ProfileRequestCode);
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
                            if(EA.isRunning)
                                StopAnimation();
                            ShowLoginMenu();
                            Toast.makeText(EntryActivity.this, "Please Login Again", Toast.LENGTH_LONG).show();
                        }
                    });
                    CommonData.Logout(this);
                }
            }
        }
        catch(Exception ex)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if(EA.isRunning)
                        StopAnimation();
                    findViewById(R.id.ConnectionErrTV).setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void ShowLoginMenu()
    {
        EmailShowedAtStartup = true;
        TextView AppNameTV = findViewById(R.id.AppNameTV);
        Button ContinueButton = findViewById(R.id.ContinueButton);

        float CurrentY = AppNameTV.getY();
        float NewY = CurrentY - (CurrentY/2);

        EA.setY(EA.getY() + NewY);

        ViewTranslationAnimation textViewTranslation = new ViewTranslationAnimation(AppNameTV);
        ViewAlphaAnimation alphaAnimEmailET = new ViewAlphaAnimation(EmailET);
        ViewAlphaAnimation alphaAnimContinueButton = new ViewAlphaAnimation(ContinueButton);

        textViewTranslation.setDuration(200);
        alphaAnimEmailET.setDuration(500);
        alphaAnimContinueButton.setDuration(500);

        textViewTranslation.setNewValue(NewY);
        alphaAnimEmailET.setNewValue(1);
        alphaAnimContinueButton.setNewValue(1);

        AppNameTV.startAnimation(textViewTranslation);
        EmailET.startAnimation(alphaAnimEmailET);
        ContinueButton.startAnimation(alphaAnimContinueButton);
    }

    public void ContinueButtonPressed(View view)
    {
        final String Email = EmailET.getText().toString();
        if(Email.isEmpty())
            EmailET.setError("Email cannot be empty!");
        else
        {
            view.setEnabled(false);
            EmailET.setEnabled(false);
            if (!EA.isRunning)
                StartAnimation();
            Thread EmailCheckThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(CommonData.CheckMailAddress).openConnection();
                        urlConnection.setDoInput(true);
                        urlConnection.setDoOutput(true);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                        writer.write("email=" + Email);
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
                            if(EA.isRunning)
                                StopAnimation();

                            if (LoginResponse.equals("AccountExist"))
                            {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        findViewById(R.id.ContinueButton).setEnabled(true);
                                        EmailET.setEnabled(true);
                                        ShowPasswordBox();
                                    }
                                });
                            }
                            else
                                startActivityForResult(new Intent(EntryActivity.this,SignUpActivity.class).putExtra("email",Email),SignUpRequestCode);
                        }
                    } catch (Exception ex)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Toast.makeText(EntryActivity.this, "Bad Internet Connection", Toast.LENGTH_SHORT).show();
                                if(EA.isRunning)
                                    StopAnimation();
                                findViewById(R.id.ContinueButton).setEnabled(true);
                                EmailET.setEnabled(true);
                            }
                        });

                    }
                }
            });
            EmailCheckThread.start();
        }
    }

    private void ShowPasswordBox()
    {
        PasswordShowed = true;

        ViewHorizTranslationAnimation EmailAnim = new ViewHorizTranslationAnimation(EmailET);
        ViewHorizTranslationAnimation ContinueAnim = new ViewHorizTranslationAnimation(ContinueButton);
        ViewHorizTranslationAnimation PasswordAnim = new ViewHorizTranslationAnimation(PasswordET);
        ViewHorizTranslationAnimation LoginAnim = new ViewHorizTranslationAnimation(LoginButton);

        LoginButton.setEnabled(true);
        LoginButton.setVisibility(View.VISIBLE);
        PasswordET.setEnabled(true);
        PasswordET.setVisibility(View.VISIBLE);
        ContinueButton.setEnabled(false);
        ContinueButton.setVisibility(View.GONE);
        EmailET.setEnabled(false);
        EmailET.setVisibility(View.GONE);
        PasswordET.setWidth(EmailET.getWidth());

        float ActualEtX = EmailET.getX();
        float ActualButX = ContinueButton.getX();

        EmailAnim.setNewValue(0);
        ContinueAnim.setNewValue(0);
        PasswordAnim.setNewValue(ActualEtX);
        PasswordET.setX(2*ActualEtX);
        LoginAnim.setNewValue(ActualButX);
        LoginButton.setX(2*ActualButX);

        EmailET.startAnimation(EmailAnim);
        ContinueButton.startAnimation(ContinueAnim);
        PasswordET.startAnimation(PasswordAnim);
        LoginButton.startAnimation(LoginAnim);

        PasswordET.requestFocus();
    }

    private void ShowEmailBox()
    {
        PasswordShowed = false;

        ViewHorizTranslationAnimation EmailAnim = new ViewHorizTranslationAnimation(EmailET);
        ViewHorizTranslationAnimation ContinueAnim = new ViewHorizTranslationAnimation(ContinueButton);
        ViewHorizTranslationAnimation PasswordAnim = new ViewHorizTranslationAnimation(PasswordET);
        ViewHorizTranslationAnimation LoginAnim = new ViewHorizTranslationAnimation(LoginButton);

        ContinueButton.setEnabled(true);
        ContinueButton.setVisibility(View.VISIBLE);
        LoginButton.setEnabled(false);
        LoginButton.setVisibility(View.GONE);
        PasswordET.setEnabled(false);
        PasswordET.setVisibility(View.GONE);
        EmailET.setEnabled(true);
        EmailET.setVisibility(View.VISIBLE);

        float ActualEtX = PasswordET.getX();
        float ActualButX = LoginButton.getX();

        EmailAnim.setNewValue(ActualEtX);
        ContinueAnim.setNewValue(ActualButX);
        PasswordAnim.setNewValue(2*ActualEtX);
        LoginAnim.setNewValue(2*ActualButX);

        EmailET.startAnimation(EmailAnim);
        ContinueButton.startAnimation(ContinueAnim);
        PasswordET.startAnimation(PasswordAnim);
        LoginButton.startAnimation(LoginAnim);
    }

    public void LoginButtonPressed(final View view)
    {

        final EditText PasswordET = findViewById(R.id.PasswordET);
        final String Password = PasswordET.getText().toString();
        final String Email = EmailET.getText().toString();

        if(Password.isEmpty())
        {
            PasswordET.requestFocus();
            PasswordET.setError("Enter password here!");
        }
        else
        {
            view.setEnabled(false);
            PasswordET.setEnabled(false);
            if (!EA.isRunning)
                StartAnimation();
            Thread PasswordCheckingThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(CommonData.CheckPasswordAddress).openConnection();
                        urlConnection.setDoInput(true);
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestMethod("POST");
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                        writer.write("email=" + Email + "&password=" + Password);
                        writer.flush();
                        if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK)
                        {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                            StringBuilder SB = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null)
                                SB.append(line);
                            reader.close();

                            final String[] LoginResponse = SB.toString().split(";");

                            if (LoginResponse[0].equals("Correct"))
                            {

                                JSONObject jsonObject = new JSONObject(LoginResponse[1]);
                                SharedPreferences.Editor editor = getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit();

                                editor.putBoolean("LoggedIn",true);
                                editor.putString("username",jsonObject.getString("username"));
                                editor.putString("name",jsonObject.getString("name"));
                                editor.putString("email",jsonObject.getString("email"));
                                editor.putString("password",jsonObject.getString("password"));
                                editor.putString("token",jsonObject.getString("token"));
                                editor.putBoolean("profile_completed",jsonObject.getInt("profile_completed")==1);
                                editor.putString("current_job",jsonObject.getString("current_job"));
                                editor.putString("area_of_expertise",jsonObject.getString("area_of_expertise"));
                                editor.putString("course_time_frame",jsonObject.getString("course_time_frame"));
                                editor.putString("university_roll_no",jsonObject.getString("university_roll_no"));
                                editor.putString("passout_year", jsonObject.getString("passout_year"));
                                editor.putString("department", jsonObject.getString("department"));

                                editor.apply();

                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        PasswordET.setText("");
                                        PasswordET.setEnabled(true);
                                        view.setEnabled(true);
                                        if (EA.isRunning)
                                            StopAnimation();
                                        startActivityForResult(new Intent(EntryActivity.this,ProfileActivity.class),ProfileRequestCode);
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
                                        if (EA.isRunning)
                                            StopAnimation();
                                        view.setEnabled(true);
                                        PasswordET.setEnabled(true);
                                        PasswordET.requestFocus();
                                        PasswordET.setError("You entered an incorrect password!");
                                    }
                                });
                            }
                        } else
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (EA.isRunning)
                                        StopAnimation();
                                    Toast.makeText(EntryActivity.this, "Bad Internet Connection. Try again", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } catch (final Exception ex)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (EA.isRunning)
                                    StopAnimation();
                                Toast.makeText(EntryActivity.this, "Bad Internet Connection. Try again\n" + ex.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
            PasswordCheckingThread.start();
        }
    }

    public void ConnectAgainPressed(View view)
    {
        view.setVisibility(View.GONE);
        if(!EA.isRunning)
            StartAnimation();
        Thread TokenCheckThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                CheckToken();
            }
        });
        TokenCheckThread.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        findViewById(R.id.ContinueButton).setEnabled(true);
        EmailET.setEnabled(true);
        if(requestCode == SignUpRequestCode)
        {
            if (resultCode == RESULT_OK)
            {
                String Status = data.getStringExtra("Status");
                if (Status.equals("Created"))
                {
                    startActivityForResult(new Intent(EntryActivity.this,ProfileActivity.class),ProfileRequestCode);
                }
                else if (Status.equals("ChangeEmail"))
                    EmailET.requestFocus();
                else
                    finish();
            }
        }
        else if(requestCode == ProfileRequestCode)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                String Status = data.getStringExtra("Status");
                if (Status.equals("Logout"))
                {
                    EmailET.setText("");
                    if(!EmailShowedAtStartup)
                        LoginSignUp();
                    else if(PasswordShowed)
                        ShowEmailBox();
                }
            }
            else
                finish();
        }
        else
            finish();
    }

    @Override
    public void onBackPressed()
    {
        if(PasswordShowed)
        {
            ShowEmailBox();
        }
        else
        {
            if(EA.isRunning)
                StopAnimation();
            finish();
        }
    }
}
