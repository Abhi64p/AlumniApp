package asiet.alumniapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler;
import com.amazonaws.regions.Regions;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class EmailPhoneVerificationActivity extends Activity
{
    private Button VerifyButton, ResendButton;
    private EditText VerificationCodeET;
    private EntryAnimation EA;
    private Thread AnimationThread;
    private ScrollView SV;
    private TextView TV;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_phone_verification);
        VerificationCodeET = findViewById(R.id.EmailVerificationCodeET);
        VerifyButton = findViewById(R.id.EmailVerifyButton);
        ResendButton = findViewById(R.id.EmailResendButton);
        EA = findViewById(R.id.EmailVerifyAnim);
        SV = findViewById(R.id.EmailVerifyScrollView);
        TV = findViewById(R.id.EmailVerifMessageTV);

        CognitoUserPool userPool = new CognitoUserPool(getApplicationContext(), CommonData.UserPoolId, CommonData.ClientId, CommonData.ClientSecret, Regions.AP_SOUTH_1);
        CommonData.cognitoUser  = userPool.getUser(getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("email",null));

        if(getIntent().getStringExtra("Verify").equals("Phone"))
            CommonData.cognitoUser.resendConfirmationCodeInBackground(verificationHandler);
        else
            CommonData.cognitoUser.getSessionInBackground(authenticationHandler);
    }

    private AuthenticationHandler authenticationHandler = new AuthenticationHandler()
    {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice)
        {
            CommonData.cognitoUser.getAttributeVerificationCodeInBackground("email",verificationHandler);
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId)
        {
            AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId,getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("password",null),null);
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
            authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation continuation) { }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) { }

        @Override
        public void onFailure(Exception exception)
        {
            Toast.makeText(EmailPhoneVerificationActivity.this, "Bad Internet Connection. try again!", Toast.LENGTH_SHORT).show();
        }
    };

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
                            HttpsURLConnection urlConnection;
                            if(getIntent().getStringExtra("Verify").equals("Phone"))
                                urlConnection= (HttpsURLConnection) new URL(CommonData.VerifiedPhoneAddress).openConnection();
                            else
                                urlConnection= (HttpsURLConnection) new URL(CommonData.VerifiedEmailAddress).openConnection();
                            urlConnection.setDoOutput(true);
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                            writer.write("email=" + getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("email",null));
                            writer.flush();
                            if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK)
                            {
                                if(getIntent().getStringExtra("Verify").equals("Phone"))
                                    getSharedPreferences(CommonData.SP, MODE_PRIVATE).edit().putBoolean("PhoneNumberVerified",true).apply();
                                else
                                    getSharedPreferences(CommonData.SP, MODE_PRIVATE).edit().putBoolean("EmailVerified",true).apply();
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
                                        Toast.makeText(EmailPhoneVerificationActivity.this, "Bad internet connection. Try again!", Toast.LENGTH_LONG).show();
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
                                    Toast.makeText(EmailPhoneVerificationActivity.this, "Bad internet connection. Try again!", Toast.LENGTH_LONG).show();
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
                Toast.makeText(EmailPhoneVerificationActivity.this, "Bad internet connection. Try again!", Toast.LENGTH_LONG).show();
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

            if(getIntent().getStringExtra("Verify").equals("Phone"))
                cognitoUser.confirmSignUpInBackground(VerificationCode,false,confirmationCallback);
            else
                cognitoUser.verifyAttributeInBackground("email",VerificationCode,confirmationCallback);
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
        Toast.makeText(this, "Resending Verification Code", Toast.LENGTH_SHORT).show();
        if(getIntent().getStringExtra("Verify").equals("Phone"))
            CommonData.cognitoUser.resendConfirmationCodeInBackground(verificationHandler);
        else
            CommonData.cognitoUser.getSessionInBackground(authenticationHandler);
    }

    VerificationHandler verificationHandler = new VerificationHandler()
    {
        @Override
        public void onSuccess(CognitoUserCodeDeliveryDetails verificationCodeDeliveryMedium)
        {
            TV.setText("Verification code is send to " + verificationCodeDeliveryMedium.getDestination() + " by " + verificationCodeDeliveryMedium.getDeliveryMedium());
        }

        @Override
        public void onFailure(Exception exception)
        {
            Toast.makeText(EmailPhoneVerificationActivity.this, "Wrong verification code or bad internet connection. Try again!, " + exception.getMessage(), Toast.LENGTH_LONG).show();
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