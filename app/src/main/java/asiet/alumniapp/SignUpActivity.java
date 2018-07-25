package asiet.alumniapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Regions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SignUpActivity extends Activity
{
    private EditText SignUpPassword1ET, SignUpPassword2ET;
    private Thread AnimationThread;
    private EntryAnimation EA;
    private ScrollView SignUpScrollView;
    private String Email, Name, Password1, Phone;
    private Button ContinueButton;
    private static final int VerificationRequest = 1;
    private EditText SignUpPhoneET;

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

        Name = SignUpNameET.getText().toString();
        Phone = SignUpPhoneET.getText().toString();
        Password1 = SignUpPassword1ET.getText().toString();
        final String Password2 = SignUpPassword2ET.getText().toString();

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
                        if(EA.isRunning)
                            StopAnimation();

                        if (LoginResponse.equals("PhoneNumberExist"))
                        {
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
                            writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                            writer.write("email=" + Email+"&phone_number=" + Phone + "&name=" + Name + "&password=" + Password1 + "&phone_number=" + Phone);
                            writer.flush();
                            if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK)
                            {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if(!EA.isRunning)
                                            StartAnimation();
                                    }
                                });
                                CognitoSignUp();
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

    private void CognitoSignUp()
    {
        CognitoUserPool userPool = new CognitoUserPool(getApplicationContext(), CommonData.UserPoolId, CommonData.ClientId, CommonData.ClientSecret, Regions.AP_SOUTH_1);

        CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        userAttributes.addAttribute("given_name", Name);
        userAttributes.addAttribute("phone_number", Phone);
        userAttributes.addAttribute("email", Email);

        SignUpHandler signupCallback = new SignUpHandler()
        {
            @Override
            public void onSuccess(CognitoUser cognitoUser, boolean userConfirmed, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails)
            {
                CommonData.cognitoUser = cognitoUser;
                if (!userConfirmed)
                {
                    SharedPreferences.Editor editor = getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit();
                    editor.putString("email",Email);
                    editor.putString("password",Password1);
                    editor.putBoolean("LoggedIn",true);
                    editor.apply();
                    if(EA.isRunning)
                        StopAnimation();
                    startActivityForResult(new Intent(SignUpActivity.this,VerifyPhoneActivity.class).putExtra("Phone",Phone),VerificationRequest);
                }
            }

            @Override
            public void onFailure(Exception exception)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(SignUpActivity.this, "Bad Internet Connection. Try again", Toast.LENGTH_SHORT).show();
                        if(EA.isRunning)
                            StopAnimation();
                        ContinueButton.setEnabled(true);
                    }
                });
            }

        };

        userPool.signUpInBackground(Email, Password1, userAttributes, null, signupCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == VerificationRequest)
        {
            Intent returnIntent = new Intent();
            if (resultCode == Activity.RESULT_OK)
            {
                returnIntent.putExtra("Status","Verified");
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
            else if(resultCode == Activity.RESULT_CANCELED)
            {
                returnIntent.putExtra("Status","NotVerified");
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
