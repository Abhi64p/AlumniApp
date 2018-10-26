package asiet.alumniapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Poll
{
    private String jsonPolls;
    private String jsonResponse;
    private String Username;
    private Context context;

    public Poll(String Username, Context context)
    {
        this.Username = Username;
        this.context = context;
    }

    public View[] getPollViews()
    {
       if(!LoadPoll(Username))
           return null;
       return GenerateView();
    }

    private boolean LoadPoll(String Username)
    {
        try
        {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.GetPollAddress).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),"UTF-8"));
            bufferedWriter.write("username=" + Username);
            bufferedWriter.flush();
            if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK)
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String Line;
                while((Line = bufferedReader.readLine()) != null)
                    stringBuilder.append(Line);
                String[] arr = stringBuilder.toString().split(";");
                jsonPolls = arr[0];
                jsonResponse = arr[1];
                return true;
            }
            return false;
        }
        catch(Exception ex)
        {
            return false;
        }
    }

    private View[] GenerateView()
    {
        try
        {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            int ResponseSize = jsonArray.length();
            String[] PollIdArray = new String[ResponseSize];
            String[] ResponseArray = new String[ResponseSize];
            for(int i=0; i<ResponseSize; i++)
            {
                PollIdArray[i] = jsonArray.getJSONObject(i).getString("poll_id");
                ResponseArray[i] = jsonArray.getJSONObject(i).getString("response");
            }

            jsonArray = new JSONArray(jsonPolls);
            View[] viewArray = new View[jsonArray.length()];
            int PollSize = jsonArray.length();
            for(int i=0; i<PollSize; i++)
            {
                View cardView = LayoutInflater.from(context).inflate(R.layout.poll_layout,null,false);

                boolean Flag = false;
                String tmpPollId = jsonArray.getJSONObject(i).getString("poll_id");
                int j=0;
                for(j=0; j<ResponseSize; j++)
                    if(PollIdArray[j].equals(tmpPollId))
                    {
                        Flag = true;
                        break;
                    }

                 if(!Flag)
                 {
                     ((TextView)cardView.findViewById(R.id.PollMessageTV)).setText(jsonArray.getJSONObject(i).getString("text"));
                     ((CustomButton) cardView.findViewById(R.id.PollButton1)).setText(jsonArray.getJSONObject(i).getString("button1"));
                     ((CustomButton) cardView.findViewById(R.id.PollButton2)).setText(jsonArray.getJSONObject(i).getString("button2"));
                     ((CustomButton) cardView.findViewById(R.id.PollButton3)).setText(jsonArray.getJSONObject(i).getString("button3"));
                     ((CustomButton) cardView.findViewById(R.id.PollButton1)).setPollId(jsonArray.getJSONObject(i).getString("poll_id"));
                     ((CustomButton) cardView.findViewById(R.id.PollButton2)).setPollId(jsonArray.getJSONObject(i).getString("poll_id"));
                     ((CustomButton) cardView.findViewById(R.id.PollButton3)).setPollId(jsonArray.getJSONObject(i).getString("poll_id"));
                     ((CustomButton) cardView.findViewById(R.id.PollButton1)).setView(cardView);
                     ((CustomButton) cardView.findViewById(R.id.PollButton2)).setView(cardView);
                     ((CustomButton) cardView.findViewById(R.id.PollButton3)).setView(cardView);
                 }
                 else
                 {
                     ((TextView)cardView.findViewById(R.id.PollMessageTV)).setText(jsonArray.getJSONObject(i).getString("text") + "\n\nYour response was '"+ ResponseArray[j]+"'");
                 }
                viewArray[i] = cardView;
            }
            return viewArray;
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    public void UpdatePoll(final String Response,final String PollId)
    {
        Thread UploadThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                UploadResponse(Response,PollId);
            }
        });
        UploadThread.start();
    }

    private void UploadResponse(String Response,String PollId)
    {
        try
        {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(CommonData.SetPollAddress).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(false);
            connection.setRequestMethod("POST");
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),"UTF-8"));
            bufferedWriter.write("username=" + Username + "&response=" + Response + "&poll_id=" + PollId);
            bufferedWriter.flush();
            bufferedWriter.close();
            connection.getResponseCode();
        }
        catch(Exception ex)
        { }
    }
}
