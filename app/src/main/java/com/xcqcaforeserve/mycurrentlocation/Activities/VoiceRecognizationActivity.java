package com.xcqcaforeserve.mycurrentlocation.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.xcqcaforeserve.mycurrentlocation.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class VoiceRecognizationActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private ImageView iv_mic;
    private TextView tv_Speech_to_text, translatedLanguage_tv;
    private AppCompatButton translate_Btn, showPieChart_Btn;
    private String speechText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recognization);
        try {
            mContext = this;
            getLayoutUiIdFindInit();

        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("On create method error :- " + exp.getStackTrace());
        }
    }

    private void getLayoutUiIdFindInit() {
        try {
            iv_mic = (ImageView) findViewById(R.id.iv_mic);
            tv_Speech_to_text = (TextView) findViewById(R.id.tv_speech_to_text);
            translatedLanguage_tv = (TextView) findViewById(R.id.translatedLanguage_tv);
            translate_Btn = (AppCompatButton) findViewById(R.id.translate_Btn);
            showPieChart_Btn = (AppCompatButton) findViewById(R.id.showPieChart_Btn);

            iv_mic.setOnClickListener(this);
            translate_Btn.setOnClickListener(this);
            showPieChart_Btn.setOnClickListener(this);

        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Layout UI Id :- " + exp.getStackTrace());
        }
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    try {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            ArrayList<String> resultText = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                            tv_Speech_to_text.setText(capitalize(Objects.requireNonNull(resultText).get(0)));

                        }
                    } catch (Exception exp) {
                        exp.getStackTrace();
                        System.out.println("Activity result :- " + exp.getStackTrace());
                    }
                }
            });

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_mic:
                try {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "I am listening...");
                    someActivityResultLauncher.launch(intent);
                } catch (Exception e) {
                    System.out.println("Event of Speak to text :- " + e.getStackTrace());
                    Toast.makeText(mContext, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.translate_Btn:
                try {
                    speechText = tv_Speech_to_text.getText().toString().trim();
                    if (speechText.equalsIgnoreCase("") && speechText.isEmpty()) {
                        Toast.makeText(mContext, "Please tap mic and speek.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (isOnline()) {
                            downloadModal(speechText);
                        }
                    }
                } catch (Exception exp) {
                    exp.getStackTrace();
                    System.out.println("Event of Speak to text translate :- " + exp.getStackTrace());
                }
                break;

            case R.id.showPieChart_Btn:
                startActivity(new Intent(mContext, PieChartActivity.class));
                break;
        }
    }

    private void downloadModal(String speechText) {
//        HttpRequest httpRequest = new HttpRequest(mContext, translatedLanguage_tv);
//        httpRequest.execute(speechText);
//        Toast.makeText(mContext, speechText, Toast.LENGTH_SHORT).show();
//        Translate.setHttpReferrer("http://android-er.blogspot.com/");
        startActivity(new Intent(mContext, NoteBookActivity.class));

    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else {
            Toast.makeText(mContext, "Check your network connection.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}