package in.ac.adishankara.alumni.asietalumni;

import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PasswordResetActivity extends AppCompatActivity
{
    private String email;
    private int VCode;
    private EntryAnimation EA;
    private Thread AnimationThread;
    private TextView VcodeTV;
    private EditText VcodeET,VcodeET1;
    private Boolean Verified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        email = getIntent().getStringExtra("email");
        EA = findViewById(R.id.VCodeAnim);
        VcodeTV = findViewById(R.id.VCodeTV);
        VcodeET = findViewById(R.id.VcodeET);
        VcodeET1 = findViewById(R.id.Vcode1ET);

        Thread GetVCodeThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                GetVerificationCode();
            }
        });
        if(!EA.isRunning)
            StartAnimation();
        GetVCodeThread.start();
    }

    private void GetVerificationCode()
    {
        try
        {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.GetPasswordResetVcodeAddress).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),"UTF-8"));
            bufferedWriter.write("email=" + email);
            bufferedWriter.flush();
            if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK)
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String Line;
                while((Line = bufferedReader.readLine()) != null)
                    stringBuilder.append(Line);
                VCode = Integer.parseInt(stringBuilder.toString().trim());
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        VcodeTV.setText("Enter the verification code send to your registered email address");
                        if(EA.isRunning)
                            StopAnimation();
                        VcodeET.setEnabled(true);
                        findViewById(R.id.VcodeButton).setEnabled(true);
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
                        Toast.makeText(PasswordResetActivity.this, "Cannot connect to server. Try again after sometime!", Toast.LENGTH_SHORT).show();
                        PasswordResetActivity.this.finish();
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
                    if(EA.isRunning)
                        StopAnimation();
                    VcodeTV.setText(ex.getMessage());
                    //Toast.makeText(PasswordResetActivity.this, "Cannot connect to server. Try again after sometime!", Toast.LENGTH_LONG).show();
                    //PasswordResetActivity.this.finish();
                }
            });
        }
    }

    public void VcodeButtonPressed(View view)
    {
        if(!Verified)
        {
            String TmpVcode = VcodeET.getText().toString();
            if(TmpVcode.isEmpty())
            {
                VcodeET.setError("Enter verification code here!");
                VcodeET.requestFocus();
            }
            else
            {
                if(TmpVcode.equals(String.valueOf(VCode)))
                {
                    Verified = true;
                    ChangeLayout();
                }
                else
                    Toast.makeText(this, "Incorrect Verification Code", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            final String Pass1, Pass2;
            Pass1 = VcodeET1.getText().toString();
            Pass2 = VcodeET.getText().toString();
            if(Pass1.isEmpty())
            {
                VcodeET1.setError("Enter password here!");
                VcodeET1.requestFocus();
            }
            else if(Pass2.isEmpty())
            {
                VcodeET.setError("Enter password here!");
                VcodeET.requestFocus();
            }
            else if(Pass1.length() < 6)
            {
                VcodeET1.setError("Password length should be 6 or more");
                VcodeET1.requestFocus();
            }
            else if(!Pass1.equals(Pass2))
            {
                VcodeET.setError("Password doesn't match!");
                VcodeET.requestFocus();
            }
            else
            {
                Thread PassswordUpdate = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        UpdatePassword(Pass1);
                    }
                });
                if(!EA.isRunning)
                    StartAnimation();
                PassswordUpdate.start();
            }
        }
    }

    private void ChangeLayout()
    {
        VcodeTV.setText("Enter new password");
        VcodeET1.setVisibility(View.VISIBLE);
        VcodeET.setText("");
        VcodeET.setHint("Confirm Password");
        ((Button)findViewById(R.id.VcodeButton)).setText("Change Password");
        VcodeET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        VcodeET1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        ImageButton MaskButton = findViewById(R.id.VcodeMask);
        MaskButton.setVisibility(View.VISIBLE);
        MaskButton.setOnTouchListener(MaskTouchListener);

        ViewTranslationAnimation anim = new ViewTranslationAnimation(VcodeET1);
        anim.setNewValue(VcodeET.getY() - getDP(50));
        anim.setDuration(400);

        VcodeET1.setY(VcodeET.getY());
        VcodeET1.startAnimation(anim);
        VcodeET1.requestFocus();
    }

    private void UpdatePassword(final String Pass)
    {
        try
        {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.SetNewPasswordAddress).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            bufferedWriter.write("email=" + email + "&password=" + Pass);
            bufferedWriter.flush();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (EA.isRunning)
                            StopAnimation();
                        getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit().putString("password",Pass).apply();
                        Toast.makeText(PasswordResetActivity.this, "Password Changed! Use new password to login.", Toast.LENGTH_LONG).show();
                        PasswordResetActivity.this.finish();
                    }
                });
            } else
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(PasswordResetActivity.this, "Cannot connect to server. Try again after sometime!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(PasswordResetActivity.this, "Cannot connect to server. Try again after sometime!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private View.OnTouchListener MaskTouchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent)
        {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            {
                VcodeET.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                VcodeET1.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else if(motionEvent.getAction() == MotionEvent.ACTION_UP)
            {
                VcodeET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                VcodeET1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            return  true;
        }
    };

    public void ResendPressed(View view)
    {
        VcodeTV.setText("Resending Code. Please Wait.");
        Thread GetVCodeThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                GetVerificationCode();
            }
        });
        if(!EA.isRunning)
            StartAnimation();
        GetVCodeThread.start();
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

    private float getDP(final int pixel)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, getResources().getDisplayMetrics());
    }

}
