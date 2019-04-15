package in.ac.adishankara.alumni.asietalumni;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.work.WorkManager;

import java.io.File;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

class CommonData
{
    static final String SP = "in.ac.adishankara.alumni.asietalumni";
    private static final String Server = "https://squareskipper.000webhostapp.com/";
    static final String CheckMailAddress = Server + "CheckEmail.php";
    static final String CheckPhoneAddress = Server + "CheckPhone.php";
    static final String InsertDetailsAddress = Server + "InsertData.php";
    static final String CheckPasswordAddress = Server + "CheckPassword.php";
    static final String UploadImageAddress = Server + "UploadImage.php";
    static final String DownloadImageAddress = Server + "DownloadImage.php";
    static final String RemoveImageAddress = Server + "RemoveImage.php";
    static final String CheckTokenAddress = Server + "CheckToken.php";
    static final String UpdateProfileAddress = Server + "UpdateProfile.php";
    static final String GetPollAddress = Server + "GetPoll.php";
    static final String SetPollAddress = Server + "SetPoll.php";
    static final String PhoneVerifiedAddress = Server + "VerifiedPhone.php";
    static final String GetPasswordResetVcodeAddress = Server + "GetPasswordResetVcode.php";
    static final String SetNewPasswordAddress = Server + "SetNewPassword.php";
    static final String ChangePhoneAddress = Server + "ChangePhone.php";
    static final String GetNotification = Server + "GetNotification.php";
    static final String NewPollAddress = Server + "IsNewPoll.php";

    static void Logout(Context context)
    {
        SharedPreferences SP = context.getSharedPreferences(CommonData.SP, MODE_PRIVATE);
        SharedPreferences.Editor editor = SP.edit();
        UUID id = java.util.UUID.fromString(SP.getString("UUID","...."));
        WorkManager.getInstance().cancelWorkById(id);
        editor.clear();
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
