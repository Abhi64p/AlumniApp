package asiet.alumniapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

class Poll
{
    private String jsonPolls;
    private String jsonResponse;
    private String Username;
    private Context context;

    Poll(String Username, Context context)
    {
        this.Username = Username;
        this.context = context;
    }

    View[] getPollViews()
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
            int[] PollIdArray = new int[ResponseSize];
            String[] ResponseArray = new String[ResponseSize];
            for(int i=0; i<ResponseSize; i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                PollIdArray[i] = jsonObject.getInt("poll_id");
                ResponseArray[i] = jsonObject.getString("response");
            }

            jsonArray = new JSONArray(jsonPolls);
            View[] viewArray = new View[jsonArray.length()];
            int PollSize = jsonArray.length();
            for(int i=0; i<PollSize; i++)
            {
                View view = LayoutInflater.from(context).inflate(R.layout.poll_layout,null,false);
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                boolean Flag = false;
                int tmpPollId = jsonObject.getInt("poll_id");
                int j;
                for(j=0; j<ResponseSize; j++)
                    if(PollIdArray[j] == tmpPollId)
                    {
                        Flag = true;
                        break;
                    }

                 if(!Flag)
                 {
                     ((TextView)view.findViewById(R.id.PollMessageTV)).setText(jsonObject.getString("text"));
                     ((Button)view.findViewById(R.id.PollButton1)).setText(jsonObject.getString("button1"));
                     ((Button)view.findViewById(R.id.PollButton2)).setText(jsonObject.getString("button2"));
                     ((Button)view.findViewById(R.id.PollButton3)).setText(jsonObject.getString("button3"));
                     PollCardView pollCardView = view.findViewById(R.id.pollcardview);
                     pollCardView.setPollId(jsonObject.getInt("poll_id"));
                     pollCardView.setOtherText(jsonObject.getString("other"));
                     view.findViewById(R.id.PollResponseTV).setVisibility(View.GONE);

                 }
                 else
                 {
                     ((TextView) view.findViewById(R.id.PollMessageTV)).setText(jsonObject.getString("text"));
                     ((TextView) view.findViewById(R.id.PollResponseTV)).setText("Your response was '" + ResponseArray[j] + "'");
                     view.findViewById(R.id.PollButton1).setVisibility(View.GONE);
                     view.findViewById(R.id.PollButton2).setVisibility(View.GONE);
                     view.findViewById(R.id.PollButton3).setVisibility(View.GONE);
                     view.findViewById(R.id.PollButton4).setVisibility(View.GONE);
                     view.findViewById(R.id.PollSubmitButton).setVisibility(View.GONE);
                 }
                viewArray[i] = view;
            }
            return viewArray;
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    void UpdatePoll(final String Response,final String PollId)
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
