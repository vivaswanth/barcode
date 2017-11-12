package com.harika.vvh;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

public class WebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        WebView mysample = (WebView) findViewById(R.id.webview);
        mysample.loadUrl("http://www.barcodelookup.com/"+getIntent().getStringExtra("code"));
    }
}
