package in.ac.adishankara.alumni.asietalumni;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.msg91.sendotp.library.SendOtpVerification;
import com.msg91.sendotp.library.Verification;
import com.msg91.sendotp.library.VerificationListener;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PhoneVerificationActivity extends AppCompatActivity implements VerificationListener
{
    private Verification verification;
    private EntryAnimation EA;
    private Thread AnimationThread;
    private EditText PhoneVerificationET;
    private Button VerifyButton, ResendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
        EA = findViewById(R.id.PhoneVerificationEA);
        PhoneVerificationET = findViewById(R.id.PhoneVerificationET);
        VerifyButton = findViewById(R.id.PhoneVerificationButton);
        ResendButton = findViewById(R.id.PhoneVerificationResendButton);

        verification = SendOtpVerification.createSmsVerification(SendOtpVerification.config(
                getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("phone_number",null))
                .context(this)
                .autoVerification(false)
                .message("ASIET Alumni\nYour verification code is ##OTP##")
                .otplength("6")
                .build(),this);
        verification.initiate();
    }

    public void VerifyPressed(View view)
    {
        if(!EA.isRunning)
            StartAnimation();
        String VerificationCode = PhoneVerificationET.getText().toString();
        if(VerificationCode.isEmpty())
        {
            PhoneVerificationET.setError("Enter verification code here!");
            PhoneVerificationET.requestFocus();
        }
        else
            verification.verify(VerificationCode);
    }

    public void ResendPressed(View view)
    {
        verification.resend("text");
    }

    @Override
    public void onInitiated(String response) { }

    @Override
    public void onInitiationFailed(Exception exception)
    {
        if(EA.isRunning)
            StopAnimation();
        String Msg =  exception.getMessage().substring(12,36);
        if(Msg.equals("max_retry_count_exceeded"))
            Toast.makeText(this, "OTP sending limit exceeded! Try again after sometime.", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Can't initialize OTP verification. Try again after sometime.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onVerified(String response)
    {
        getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit()
                .putBoolean("phone_number_verified",true)
                .apply();
        Thread UpdateThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                PhoneVerified();
            }
        });
        UpdateThread.start();
    }

    @Override
    public void onVerificationFailed(Exception exception)
    {
        if(EA.isRunning)
            StopAnimation();
        Toast.makeText(PhoneVerificationActivity.this, "Verification failed! Please try again.", Toast.LENGTH_LONG).show();
    }

    private void PhoneVerified()
    {
        try
        {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.PhoneVerifiedAddress).openConnection();
            connection.setDoInput(false);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),"UTF-8"));
            bufferedWriter.write("username=" + getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("username",null));
            bufferedWriter.flush();
            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(EA.isRunning)
                            StopAnimation();
                        Toast.makeText(PhoneVerificationActivity.this, "Phone number verified successfully!", Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        PhoneVerificationActivity.this.finish();
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
                        Toast.makeText(PhoneVerificationActivity.this, "Bad Internet Connection! Try Again", Toast.LENGTH_LONG).show();
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
                    if(EA.isRunning)
                        StopAnimation();
                    Toast.makeText(PhoneVerificationActivity.this, "Bad Internet Connection! Try Again", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void StartAnimation()
    {
        EA.isRunning = true;
        EA.setVisibility(View.VISIBLE);
        PhoneVerificationET.setEnabled(false);
        VerifyButton.setEnabled(false);
        ResendButton.setEnabled(false);
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
                PhoneVerificationET.setEnabled(true);
                VerifyButton.setEnabled(true);
                ResendButton.setEnabled(true);
            }
        });
        AnimationThread.interrupt();
    }
}
