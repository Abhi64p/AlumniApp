package asiet.alumniapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class VerifyPhoneActivity extends Activity
{
    Button VerifyButton, ResendButton;
    EditText VerificationCodeET;
    EntryAnimation EA;
    Thread AnimationThread;
    ScrollView SV;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        VerificationCodeET = findViewById(R.id.EmailVerificationCodeET);
        VerifyButton = findViewById(R.id.EmailVerifyButton);
        ResendButton = findViewById(R.id.ResendButton);
        EA = findViewById(R.id.VerifyAnim);
        SV = findViewById(R.id.VerifyScrollView);
        TextView VerifyPhoneNumberTV = findViewById(R.id.VerifyPhoneNumberTV);
        VerifyPhoneNumberTV.setText("Code send to " + getIntent().getStringExtra("Phone"));
    }

    public void VerifyButtonPressed(View view)
    {
        CognitoUser cognitoUser = CommonData.cognitoUser;
        String VerificationCode = VerificationCodeET.getText().toString();

        final GenericHandler confirmationCallback = new GenericHandler()
        {
            @Override
            public void onSuccess()
            {
                Thread UpDatePhoneVerifState = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(CommonData.VerifiedPhoneAddress).openConnection();
                            urlConnection.setDoOutput(true);
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                            writer.write("email=" + getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("email",null));
                            writer.flush();
                            if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK)
                            {
                                getSharedPreferences(CommonData.SP, MODE_PRIVATE).edit().putBoolean("PhoneNumberVerified",true).apply();
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if(EA.isRunning)
                                            StopAnimation();
                                        setResult(Activity.RESULT_OK);
                                        finish();
                                    }
                                });
                            } else
                            {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Toast.makeText(VerifyPhoneActivity.this, "Bad Internet Connection. Try again 1", Toast.LENGTH_LONG).show();
                                        if (EA.isRunning)
                                            StopAnimation();
                                    }
                                });
                            }
                        } catch (Exception ex)
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(VerifyPhoneActivity.this, "Bad Internet Connection. Try again 2", Toast.LENGTH_LONG).show();
                                    if (EA.isRunning)
                                        StopAnimation();
                                }
                            });
                        }
                    }
                });
                UpDatePhoneVerifState.start();
            }

            @Override
            public void onFailure(Exception exception)
            {
                if(EA.isRunning)
                    StopAnimation();
                Toast.makeText(VerifyPhoneActivity.this, "Verification failed. Try again!", Toast.LENGTH_LONG).show();
            }
        };

        if(VerificationCode.isEmpty())
        {
            VerificationCodeET.requestFocus();
            VerificationCodeET.setError("Enter verification code here!");
        }
        else
        {
            if(!EA.isRunning)
                StartAnimation();
            cognitoUser.confirmSignUpInBackground(VerificationCode,false,confirmationCallback);
        }
    }

    @Override
    public void onBackPressed()
    {
        if(EA.isRunning)
            StopAnimation();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public void ResendVerifyButtonPressed(View view)
    {
        CommonData.cognitoUser.resendConfirmationCodeInBackground(verificationHandler);
    }

    VerificationHandler verificationHandler = new VerificationHandler()
    {
        @Override
        public void onSuccess(CognitoUserCodeDeliveryDetails verificationCodeDeliveryMedium)
        {
            Toast.makeText(VerifyPhoneActivity.this, "Verfication code resended successfully", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(Exception exception)
        {
            Toast.makeText(VerifyPhoneActivity.this, "Bad internet connection. Try again!", Toast.LENGTH_SHORT).show();
        }
    };

    private void StartAnimation()
    {
        EA.isRunning = true;
        EA.setVisibility(View.VISIBLE);
        VerificationCodeET.setEnabled(false);
        VerifyButton.setEnabled(false);
        ResendButton.setEnabled(false);
        SV.setAlpha(0.5f);
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
                VerificationCodeET.setEnabled(true);
                VerifyButton.setEnabled(true);
                ResendButton.setEnabled(true);
                SV.setAlpha(1);
            }
        });
        AnimationThread.interrupt();
    }
}
