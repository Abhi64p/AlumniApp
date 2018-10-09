package asiet.alumniapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ProfilePhotoPicker extends AppCompatActivity
{
    private int GalleryRequestCode = 0;
    private EntryAnimation EA;
    private Thread AnimationThread;
    private TextView ProgressTV;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE,WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE);
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

        EA = findViewById(R.id.UploadAnim);
        ProgressTV = findViewById(R.id.ProgressTextView);
    }

    public void GalleryClicked(View view)
    {
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,GalleryRequestCode);
    }

    private void UploadFile(final Uri uri)
    {
        Thread UploadThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String boundary =  "*****"+Long.toString(System.currentTimeMillis())+"*****";
                    int bytesRead, bytesAvailable, bufferSize;
                    byte[] buffer;
                    File Image = new File(uri.getPath());
                    FileInputStream fileInputStream = new FileInputStream(Image);
                    HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.UploadImageAddress).openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes("--" + boundary + "\r\n");
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "img_upload" + "\"; filename=\"" + "UserId.jpg" +"\"" + "\r\n");
                    outputStream.writeBytes("Content-Type: image/jpeg" + "\r\n");
                    outputStream.writeBytes("Content-Transfer-Encoding: binary" + "\r\n");
                    outputStream.writeBytes("\r\n");

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, 1048576);
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while(bytesRead > 0) {
                        outputStream.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, 1048576);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                    outputStream.writeBytes("\r\n");
                    outputStream.writeBytes("--" + boundary + "--" + "\r\n");

                    int status = connection.getResponseCode();
                    if (status == HttpsURLConnection.HTTP_OK)
                    {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        connection.disconnect();
                        fileInputStream.close();
                        outputStream.flush();
                        outputStream.close();

                        final String Result = response.toString();
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(Result.equals("Success"))
                                    Toast.makeText(ProfilePhotoPicker.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(ProfilePhotoPicker.this, "Uploading failed\n" + Result, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ProfilePhotoPicker.this, "Profile picture updation failed!\n"+ ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                StopAnimation();
            }
        });
        StartAnimation();
        UploadThread.start();
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

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == GalleryRequestCode)
            if (resultCode == RESULT_OK)
                UploadFile(data.getData());
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
