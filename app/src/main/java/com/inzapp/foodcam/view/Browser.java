package com.inzapp.foodcam.view;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.inzapp.foodcam.R;

/**
 * ACTION_VIEW, Uri.parse() 의 RESULT_CODE 는 정상적으로 반환되지 않기에
 * 임의의 액티비티를 만들어 url 를 실행하고 액티비티의 RESULT_CODE 를 리턴받는다
 * 해당 액티비티는 url 을 실행하고 바로 종료되어 main 액티비티에 RESULT_CODE 를 리턴하게 된다
 */
public class Browser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        String link = (String) getIntent().getExtras().get("link");
        Intent browser = new Intent(Intent.ACTION_VIEW);
        browser.setData(Uri.parse(link));
        startActivity(browser);
        finish();
    }
}
