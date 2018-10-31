package in.ac.adishankara.alumni.asietalumni;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.io.File;

import static android.content.Context.MODE_PRIVATE;

class CommonData
{
    static final String SP = "in.ac.adishankara.alumni.asietalumni";
    static final String CheckMailAddress = "https://squareskipper.000webhostapp.com/CheckEmail.php";
    static final String CheckPhoneAddress = "https://squareskipper.000webhostapp.com/CheckPhone.php";
    static final String InsertDetailsAddress = "https://squareskipper.000webhostapp.com/InsertData.php";
    static final String CheckPasswordAddress = "https://squareskipper.000webhostapp.com/CheckPassword.php";
    static final String UploadImageAddress = "https://squareskipper.000webhostapp.com/UploadImage.php";
    static final String DownloadImageAddress = "https://squareskipper.000webhostapp.com/DownloadImage.php";
    static final String RemoveImageAddress = "https://squareskipper.000webhostapp.com/RemoveImage.php";
    static final String CheckTokenAddress = "https://squareskipper.000webhostapp.com/CheckToken.php";
    static final String UpdateProfileAddress = "https://squareskipper.000webhostapp.com/UpdateProfile.php";
    static final String GetPollAddress = "https://squareskipper.000webhostapp.com/GetPoll.php";
    static final String SetPollAddress = "https://squareskipper.000webhostapp.com/SetPoll.php";
    static final String PhoneVerifiedAddress = "https://squareskipper.000webhostapp.com/VerifiedPhone.php";

    static void Logout(Context context)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences(CommonData.SP, MODE_PRIVATE).edit();
        editor.putBoolean("LoggedIn", false);
        editor.putString("username", "");
        editor.putString("name", "");
        editor.putString("email", "");
        editor.putString("password", "");
        editor.putString("phone_number","");
        editor.putBoolean("phone_number_verified",false);
        editor.putString("token", "");
        editor.putBoolean("profile_completed", false);
        editor.putString("current_job", "");
        editor.putString("area_of_expertise", "");
        editor.putString("course_time_frame", "");
        editor.putString("university_roll_no", "");
        editor.putString("passout_year", "");
        editor.putString("department", "");
        editor.apply();
        if(ActivityCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            File Image = new File(context.getExternalCacheDir().getAbsolutePath() + "/ProPic.webp");
            if (Image.exists())
                Image.delete();
        }
        NotificationActivity.ClearList();
    }

    static class NotificationData
    {
        String NotificationId;
        String NotificationText;

        NotificationData(String id, String text)
        {
            NotificationId = id;
            NotificationText = text;
        }
    }
}
