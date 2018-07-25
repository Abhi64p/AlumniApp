package asiet.alumniapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ProfileActivity extends Activity
{
    private EntryAnimation EA;
    Thread AnimationThread;
    String Email, Password;
    static final int EmailPhoneVerifyRequest = 1;
    private boolean EmailVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        EA = findViewById(R.id.ProfileAnim);
        CheckVerification();
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

    private void CheckVerification()
    {
        TextView TV = findViewById(R.id.ProfileMessage);
        TV.setText("Please Wait");
        SharedPreferences SP = getSharedPreferences(CommonData.SP,MODE_PRIVATE);
        boolean PhoneNumberVerified = SP.getBoolean("PhoneNumberVerified",false);
        EmailVerified = SP.getBoolean("EmailVerified",false);
        Email = SP.getString("email",null);
        Password = SP.getString("password",null);

        if(!PhoneNumberVerified)
            VerifyPhone();
        else if(!EmailVerified)
            VerifyEmail();
        TV.setText("Welcome");
    }

    private void VerifyEmail()
    {
        if(!EA.isRunning)
            StartAnimation();
        Thread CheckThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(CommonData.IsEmailVerified).openConnection();
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
                        if (EA.isRunning)
                            StopAnimation();

                        if (LoginResponse.equals("Verified"))
                        {
                            SharedPreferences.Editor editor = getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit();
                            editor.putBoolean("EmailVerified",true);
                            editor.apply();
                        }
                        else
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                                    builder.setTitle("Pending Verification");
                                    builder.setCancelable(false);
                                    builder.setMessage("You haven't verified your email. Please verify your email!");
                                    builder.setPositiveButton("Verify", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {
                                            startActivityForResult(new Intent(ProfileActivity.this,EmailPhoneVerificationActivity.class).putExtra("Verify","Email"),EmailPhoneVerifyRequest);
                                        }
                                    });
                                    builder.create().show();
                                }
                            });
                        }
                    }
                } catch (final Exception ex)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(ProfileActivity.this, "Bad Internet Connection. Login to verify", Toast.LENGTH_SHORT).show();
                            Logout();
                        }
                    });
                }
            }
        });
        CheckThread.start();
    }

    private void VerifyPhone()
    {
        if(!EA.isRunning)
            StartAnimation();
        Thread CheckThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(CommonData.IsPhoneVerified).openConnection();
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
                        if (EA.isRunning)
                            StopAnimation();

                        if (LoginResponse.equals("Verified"))
                        {
                            SharedPreferences.Editor editor = getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit();
                            editor.putBoolean("PhoneNumberVerified",true);
                            editor.apply();
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if(!EmailVerified)
                                        VerifyEmail();
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
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                                    builder.setTitle("Pending Verification");
                                    builder.setCancelable(false);
                                    builder.setMessage("You haven't verified your phone number. Please verify your phone number!");
                                    builder.setPositiveButton("Verify", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {
                                            startActivityForResult(new Intent(ProfileActivity.this,EmailPhoneVerificationActivity.class).putExtra("Verify","Phone"),EmailPhoneVerifyRequest);
                                        }
                                    });
                                    builder.create().show();
                                }
                            });
                        }
                    }
                } catch (Exception ex)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(ProfileActivity.this, "Bad Internet Connection. Login to verify", Toast.LENGTH_SHORT).show();
                            Logout();
                        }
                    });
                }
            }
        });
        CheckThread.start();
    }

    private void Logout()
    {
        SharedPreferences.Editor editor = getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit();
        editor.putBoolean("PhoneNumberVerified",false);
        editor.putBoolean("EmailVerified",false);
        editor.putString("email","...");
        editor.putString("password","...");
        editor.putBoolean("LoggedIn",false);
        editor.apply();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("Status","Logout");
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    public void LogoutButtonPressed(View view)
    {
        Logout();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == EmailPhoneVerifyRequest)
        {
            if (resultCode == RESULT_OK)
            {
                if (!EmailVerified)
                    VerifyEmail();
            }
            else
                Logout();
        }
    }
}
