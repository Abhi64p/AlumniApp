package in.ac.adishankara.alumni.asietalumni;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class BackgroundFetch extends Worker
{
    private String ChannelId = "in.ac.adishankara.alumni.asietalumni.notif";

    public BackgroundFetch(@NonNull Context context, @NonNull WorkerParameters params)
    {
        super(context, params);
    }

    @Override
    @NonNull
    public Result doWork()
    {
        Context context = getApplicationContext();
        String username = context.getSharedPreferences(CommonData.SP, Context.MODE_PRIVATE).getString("username", "...");
        CreateNotificationChannel(context);
        String NotificationResult = LoadNotification(username);

        if (NotificationResult != null)
        {
            String[] Notifications = NotificationResult.split(";");
            int i = 2;
            for (String Item : Notifications)
                if (!Item.isEmpty())
                    ShowNotification(context, Item, i++);
        }

        String NewPollResult = LoadNewPoll(username);
        if (NewPollResult != null)
            if (NewPollResult.equals("NewPoll"))
                ShowPollNotification(context);

        return Result.success();
    }

    private void ShowNotification(Context context, String Text, int i)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "test123")
                .setSmallIcon(R.drawable.notif_icon)
                .setContentTitle("ASIET Alumni")
                .setContentText(Text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(i, builder.build());
    }

    private void ShowPollNotification(Context context)
    {
        Intent intent = new Intent(context,ProfileActivity.class);
        intent.setAction("ShowPolls");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ChannelId)
                .setSmallIcon(R.drawable.notif_icon)
                .setContentTitle("ASIET Alumni")
                .setContentText("New poll is available")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());
    }

    private String LoadNewPoll(String Username)
    {
        try
        {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.NewPollAddress).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
            bufferedWriter.write("username=" + Username);
            bufferedWriter.flush();
            if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK)
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8));
                final StringBuilder stringBuilder = new StringBuilder();
                String Line;
                while((Line = bufferedReader.readLine()) != null)
                    stringBuilder.append(Line);
                return stringBuilder.toString();
            }
            return null;
        }
        catch(final Exception ex)
        {
            return null;
        }
    }

    private String LoadNotification(String Username)
    {
        StringBuilder Result = new StringBuilder();
        try
        {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.GetNotification).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
            bufferedWriter.write("username=" + Username);
            bufferedWriter.flush();
            if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK)
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8));
                StringBuilder stringBuilder = new StringBuilder();
                String Line;
                while((Line = bufferedReader.readLine()) != null)
                    stringBuilder.append(Line);
                String Output = stringBuilder.toString();

                JSONArray jsonArray = new JSONArray(Output);
                int Size = jsonArray.length(), i;
                for(i=0;i<Size;i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Result.append(jsonObject.getString("text"));
                    Result.append(";");
                }
                return Result.toString();
            }
            return null;
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    private void CreateNotificationChannel(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ChannelId, "Alumni Asiet", importance);
            channel.setDescription("Shows notifications about events and polls");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }
}