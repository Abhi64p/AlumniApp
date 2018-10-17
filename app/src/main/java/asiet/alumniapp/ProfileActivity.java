package asiet.alumniapp;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ProfileActivity extends AppCompatActivity
{
    private ConstraintLayout LL;
    private int GalleryDialogRequestCode = 0;
    private int CameraDialogRequestCode = 1;
    private int ProfilePhotoAutoLoadPermissionRequestCode = 2;
    private int ProfileComplitionActivityRequestCode = 3;
    private EntryAnimation EA;
    private Thread AnimationThread;
    private ImageView ProPicView;
    private String ProPicPath;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.navigation_home:
                    HomePressed();
                    return true;
                case R.id.navigation_account:
                    AccountPressed();
                    return true;
                case R.id.navigation_notifications:
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

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        LL = findViewById(R.id.ProfileLayout);
        ProPicPath = getExternalCacheDir().getAbsolutePath() + "/ProPic.webp";

        if(!getSharedPreferences(CommonData.SP,MODE_PRIVATE).getBoolean("profile_completed",false))
        {
            new AlertDialog.Builder(this)
                    .setMessage("We need more basic info about you!")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            Intent intent = new Intent(ProfileActivity.this,ProfileCompletionActivity.class);
                            startActivityForResult(intent,ProfileComplitionActivityRequestCode);
                        }
                    })
                    .setCancelable(false)
                    .create().show();
        }
    }

    private void Logout()
    {
        SharedPreferences.Editor editor = getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit();
        editor.putString("email","...");
        editor.putString("password","...");
        editor.putString("name","...");
        editor.putString("token","...");
        editor.putBoolean("LoggedIn",false);
        editor.putString("username","...");
        editor.putBoolean("profile_completed",false);
        editor.apply();
        File Image = new File(ProPicPath);
        if(Image.exists())
            Image.delete();
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
        TextView ProfileNameTV = findViewById(R.id.ProfileNameTV);
        ProfileNameTV.setText(getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("name",""));
        EA = findViewById(R.id.ProPicUpdateAnim);
        ProPicView = findViewById(R.id.ProPicView);
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            LoadImageToView();
        else
        {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("App needs storage permission for showing profile picture from phone's storage.")
                    .setCancelable(false)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},ProfilePhotoAutoLoadPermissionRequestCode);
                        }
                    })
                    .create().show();
        }
    }

    private void NotificationsPressed()
    {
        LL.removeAllViewsInLayout();
        LL.addView(LayoutInflater.from(this).inflate(R.layout.profile_notifications,LL,false));
    }

    public void ProfilePhotoClicked(View view)
    {
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
        else
        {
            final AlertDialog PhotoPicker = new AlertDialog.Builder(this)
                    .setView(R.layout.profile_photo_picker_dialog)
                    .create();
            PhotoPicker.show();
            PhotoPicker.findViewById(R.id.GalleryButton).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, GalleryDialogRequestCode);
                    PhotoPicker.dismiss();
                }
            });
            PhotoPicker.findViewById(R.id.CameraButton).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    try
                    {
                        File Image = new File(ProPicPath + "_tmp");
                        if (Image.exists())
                            Image.delete();
                        Image.createNewFile();
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        Uri photoURI = FileProvider.getUriForFile(ProfileActivity.this,"asiet.alumniapp.fileprovider", Image);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(intent, CameraDialogRequestCode);
                        PhotoPicker.dismiss();
                    }
                    catch(Exception ex)
                    {
                        Toast.makeText(ProfileActivity.this, "Can't Load Image!\n" + ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            PhotoPicker.findViewById(R.id.RemoveButton).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Thread BGThread = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(CommonData.RemoveImageAddress).openConnection();
                                urlConnection.setRequestMethod("POST");
                                urlConnection.setDoOutput(true);
                                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                                writer.write("username=" + getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("username",""));
                                writer.flush();
                                if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK)
                                {
                                    File Image = new File(ProPicPath);
                                    if(Image.exists())
                                        Image.delete();
                                    runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            if(EA.isRunning)
                                                StopAnimation();
                                            ProPicView.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.blank_pro_pic));
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
                                            Toast.makeText(ProfileActivity.this, "Bad Internet Connection!", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(ProfileActivity.this, "Bad Internet Connection!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                    if(!EA.isRunning)
                        StartAnimation();
                    BGThread.start();
                    PhotoPicker.dismiss();
                }
            });
        }

    }

    private void UploadFile(final String Path)
    {
        try
        {
            String username = getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("username",null);
            String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            final File Image = new File(Path+"_tmp");
            FileInputStream fileInputStream = new FileInputStream(Image);
            HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.UploadImageAddress).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes("--" + boundary + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"img_upload\"; filename=\"" + username + ".webp" + "\"" + "\r\n");
            outputStream.writeBytes("Content-Type: image/jpeg" + "\r\n");
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + "\r\n");
            outputStream.writeBytes("\r\n");

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, 1048576);
            buffer = new byte[bufferSize];
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0)
            {
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
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                {
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
                        if (EA.isRunning)
                            StopAnimation();
                        if (Result.equals("Success"))
                        {
                            File NewImage = new File(ProPicPath);
                            if(NewImage.exists())
                                NewImage.delete();
                            Image.renameTo(NewImage);
                            ProPicView.setImageBitmap(BitmapFactory.decodeFile(ProPicPath));
                        }
                        else
                            Toast.makeText(ProfileActivity.this, "Bad Internet Connection!", Toast.LENGTH_LONG).show();
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
                        if (EA.isRunning)
                            StopAnimation();
                        Toast.makeText(ProfileActivity.this, "Bad Internet Connection!", Toast.LENGTH_LONG).show();
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
                    if (EA.isRunning)
                        StopAnimation();
                    Toast.makeText(ProfileActivity.this, "Bad Internet Connection!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void DownloadImage()
    {
        try
        {
            byte[] buffer = new byte[4096];
            int BytesRead;
            String username = getSharedPreferences(CommonData.SP,MODE_PRIVATE).getString("username",null);
            HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.DownloadImageAddress).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write("email=" + username);
            writer.flush();
            int ConnectionResponse = connection.getResponseCode();
            if (ConnectionResponse == HttpsURLConnection.HTTP_OK)
            {
                File Image = new File(ProPicPath);
                Image.createNewFile();
                FileOutputStream ImageOutput = new FileOutputStream(Image);
                InputStream Input = connection.getInputStream();
                while((BytesRead = Input.read(buffer,0,4096))>0)
                    ImageOutput.write(buffer,0,BytesRead);
                ImageOutput.flush();
                ImageOutput.close();
                Input.close();
                connection.disconnect();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ProPicView.setImageBitmap(BitmapFactory.decodeFile(ProPicPath));
                    }
                });
            }
            else if(ConnectionResponse != HttpsURLConnection.HTTP_NOT_FOUND)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(ProfileActivity.this, "Bad Internet Connection!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(ProfileActivity.this, "Bad Internet Connection!", Toast.LENGTH_LONG).show();
                }
            });
        }
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(EA.isRunning)
                    StopAnimation();
            }
        });
    }

    private void StoreImage(Bitmap bitmap)
    {
        try
        {
            int Width = bitmap.getWidth();
            int Height = bitmap.getHeight();
            int CenterX = Width/2;
            int CenterY = Height/2;
            if(Width > Height)
                Width = Height;
            else
                Height = Width;
            bitmap = Bitmap.createBitmap(bitmap,CenterX-Width/2,CenterY-Height/2,Width,Height);
            bitmap = Bitmap.createScaledBitmap(bitmap,800,800,true);
            File Image = new File(ProPicPath+"_tmp");
            if (Image.exists())
                Image.delete();
            Image.createNewFile();
            OutputStream ImageOut = new FileOutputStream(Image);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 30,ImageOut);
            ImageOut.flush();
            ImageOut.close();
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
                    Toast.makeText(ProfileActivity.this, "Can't load image!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void LoadImageToView()
    {
        File Image = new File(ProPicPath);
        if (Image.exists())
            ProPicView.setImageBitmap(BitmapFactory.decodeFile(ProPicPath));
        else
        {
            Thread DownloadImageThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    DownloadImage();
                }
            });
            if (!EA.isRunning)
                StartAnimation();
            DownloadImageThread.start();
        }
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
    public void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        if (requestCode == GalleryDialogRequestCode)
        {
            if (resultCode == RESULT_OK)
            {
                if (!EA.isRunning)
                    StartAnimation();
                Thread BGThread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Bitmap bitmap = BitmapFactory.decodeFile(new UriPathResolver(getContentResolver()).GetRealPath(ProfileActivity.this, data.getData()));
                        StoreImage(bitmap);
                        UploadFile(ProPicPath);
                    }
                });
                BGThread.start();
            }
        }
        else if(requestCode == CameraDialogRequestCode)
        {
            if(resultCode == RESULT_OK)
            {
                if (!EA.isRunning)
                    StartAnimation();
                Thread BGThread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            StoreImage(BitmapFactory.decodeFile(ProPicPath+"_tmp"));
                            UploadFile(ProPicPath);
                        }
                        catch (final Exception ex)
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(ProfileActivity.this, "Can't load image!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
                BGThread.start();
            }
        }
        else if(requestCode == ProfileComplitionActivityRequestCode)
        {
            if(resultCode == RESULT_OK)
                getSharedPreferences(CommonData.SP,MODE_PRIVATE).edit().putBoolean("profile_completed",true).apply();
            else
            {
                new AlertDialog.Builder(this)
                        .setTitle("Incomplete Profile")
                        .setMessage("You must complete your profie to continue!")
                        .setPositiveButton("Okey", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                Intent intent = new Intent(ProfileActivity.this,ProfileCompletionActivity.class);
                                startActivityForResult(intent,ProfileComplitionActivityRequestCode);
                            }
                        })
                        .setNegativeButton("Logout", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                Logout();
                            }
                        })
                        .setCancelable(false)
                        .create().show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ProfilePhotoAutoLoadPermissionRequestCode)
        {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Denied")
                        .setMessage("Can't load profile photo without storage permission!")
                        .create().show();
            }
            else
                LoadImageToView();
        }
    }

}
