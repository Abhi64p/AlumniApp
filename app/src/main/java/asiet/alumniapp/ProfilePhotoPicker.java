package asiet.alumniapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ProfilePhotoPicker extends AppCompatActivity
{
    private int GalleryRequestCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_photo_picker);

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This feature needs storage permission for selecting profile picture from phone storage.")
                    .setCancelable(false)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
                        }
                    })
                    .create().show();
        }
    }

    public void GalleryClicked(View view)
    {
        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,GalleryRequestCode);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == GalleryRequestCode)
            if (resultCode == RESULT_OK)
            {
                Intent returnIntent = new Intent();
                setResult(RESULT_OK,returnIntent);
                returnIntent.putExtra("FilePath",new UriPathResolver(getContentResolver()).GetRealPath(this,data.getData()));
                this.finish();
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0)
        {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Denied")
                        .setMessage("This feature won't work without storage permission!")
                        .setCancelable(false)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                Intent returnIntent = new Intent();
                                setResult(RESULT_CANCELED,returnIntent);
                                ProfilePhotoPicker.this.finish();
                            }
                        })
                        .create().show();
            }
        }
    }
}
