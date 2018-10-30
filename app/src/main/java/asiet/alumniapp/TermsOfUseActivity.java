package asiet.alumniapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class TermsOfUseActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_of_service);
        WebView webView = findViewById(R.id.TOSWebView);
        webView.loadUrl("file:///android_res/raw/terms_of_service.html");
    }
}
