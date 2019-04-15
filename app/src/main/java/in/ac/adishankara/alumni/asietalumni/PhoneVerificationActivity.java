package in.ac.adishankara.alumni.asietalumni;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.msg91.sendotp.library.SendOtpVerification;
import com.msg91.sendotp.library.Verification;
import com.msg91.sendotp.library.VerificationListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
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
        Toast.makeText(this, "Verification code resended!", Toast.LENGTH_SHORT).show();
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

    public void ChangeNumberPressed(View view)
    {

        View ABView = LayoutInflater.from(this).inflate(R.layout.change_phone_layout,null,false);
        final EditText CountryCode = ABView.findViewById(R.id.VerificationCountryCode);
        final EditText PhoneNumber = ABView.findViewById(R.id.VerificationPhoneNumber);

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(ABView)
                .create();

        ((Button)ABView.findViewById(R.id.VerificationUpdateButton)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final String Code = CountryCode.getText().toString();
                final String PhoneNo = PhoneNumber.getText().toString();
                if (Code.isEmpty())
                {
                    CountryCode.setError("Enter country code here!");
                    CountryCode.requestFocus();
                } else if (PhoneNo.isEmpty())
                {
                    PhoneNumber.setError("Enter phone number here!");
                    PhoneNumber.requestFocus();
                } else
                {
                    alertDialog.dismiss();
                    Thread UpdateThread = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            String result;
                            //if(Code.equals("91"))
                                result = Code + PhoneNo;
                            /*else
                                result = "00" + Code + PhoneNo;*/
                            ChangePhone(result);
                        }
                    });
                    if (!EA.isRunning)
                        StartAnimation();
                    UpdateThread.start();
                }
            }
        });
        ((Button)ABView.findViewById(R.id.VerificationCancelButton)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void ChangePhone(final String num)
    {
        final SharedPreferences SP = getSharedPreferences(CommonData.SP,MODE_PRIVATE);
        try
        {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.ChangePhoneAddress).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            bufferedWriter.write("username=" + SP.getString("username",null) + "&phone_number=" + num);
            bufferedWriter.flush();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK)
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String Line;
                while((Line = bufferedReader.readLine()) != null)
                    stringBuilder.append(Line);
                final String Result = stringBuilder.toString();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (EA.isRunning)
                            StopAnimation();
                        if(Result.equals("PhoneNumberExist"))
                        {
                            new AlertDialog.Builder(PhoneVerificationActivity.this)
                                    .setTitle("Failed")
                                    .setMessage("Phone number updation failed since this number is registered with another account!")
                                    .setPositiveButton("Continue", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {}
                                    })
                                    .create().show();
                        }
                        else
                        {
                            SP.edit().putString("phone_number",num).apply();
                            verification = SendOtpVerification.createSmsVerification(SendOtpVerification.config(
                                    getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("phone_number",null))
                                    .context(PhoneVerificationActivity.this)
                                    .autoVerification(false)
                                    .message("ASIET Alumni\nYour verification code is ##OTP##")
                                    .otplength("6")
                                    .build(),PhoneVerificationActivity.this);
                            verification.initiate();
                            Toast.makeText(PhoneVerificationActivity.this, "Verification code is send to new number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (EA.isRunning)
                            StopAnimation();
                        Toast.makeText(PhoneVerificationActivity.this, "Cannot connect to server. Try again after sometime!", Toast.LENGTH_SHORT).show();
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
                    if (EA.isRunning)
                        StopAnimation();
                    Toast.makeText(PhoneVerificationActivity.this, "Cannot connect to server. Try again after sometime!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}