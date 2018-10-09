package asiet.alumniapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity
{

    private TextView mTextMessage;
    private ConstraintLayout LL;
    private int PhotoPickerRequestCode = 0;
    private BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    HomePressed();
                    return true;
                case R.id.navigation_account:
                    mTextMessage.setText(R.string.title_dashboard);
                    AccountPressed();
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    NotificationsPressed();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mTextMessage = (TextView) findViewById(R.id.message);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        LL = findViewById(R.id.ProfileLayout);
    }

    private void Logout()
    {
        SharedPreferences.Editor editor = getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit();
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

    private void HomePressed()
    {
        LL.removeAllViewsInLayout();
        LL.addView(LayoutInflater.from(this).inflate(R.layout.profile_home,LL,false));
    }

    private void AccountPressed()
    {
        LL.removeAllViewsInLayout();
        LL.addView(LayoutInflater.from(this).inflate(R.layout.profile_account,LL,false));
    }

    private void NotificationsPressed()
    {
        LL.removeAllViewsInLayout();
        LL.addView(LayoutInflater.from(this).inflate(R.layout.profile_notifications,LL,false));
    }

    public void ProfilePhotoClicked(View view)
    {
        startActivityForResult(new Intent(this,ProfilePhotoPicker.class),PhotoPickerRequestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PhotoPickerRequestCode)
        {
            if (resultCode == RESULT_OK)
            {
                Toast.makeText(this, "Result OK", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(this, "Result Not OK", Toast.LENGTH_SHORT).show();
        }
    }

}
