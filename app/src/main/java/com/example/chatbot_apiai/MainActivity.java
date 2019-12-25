package com.example.chatbot_apiai;


import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;



import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;

import android.widget.ListView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


import ai.api.AIListener;

import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import android.speech.tts.TextToSpeech;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements AIListener {

    private static final String TAG = "ChatActivity";



    //private static final int MY_PERMISSIONS_REQUEST_INTERNET = 35;

    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    // private EditText chatText;
    // private FloatingActionButton sendButton;
    private FloatingActionButton listenButton;
    private AIService aiService;
    private Animation pop_in_anim;
    private Animation pop_out_anim;

    private boolean rightSide = true; //true if you want message on right rightSide

    TextToSpeech t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listenButton =  findViewById(R.id.btn_mic);
        listView = findViewById(R.id.msgview);

        // chatText =  findViewById(R.id.msg);

        pop_in_anim = AnimationUtils.loadAnimation(this, R.anim.pop_in);
        pop_out_anim = AnimationUtils.loadAnimation(this, R.anim.pop_out);
        listenButton.setAnimation(pop_out_anim);
        listenButton.setAnimation(pop_in_anim);

        listenButton.clearAnimation();

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);

        listView.setAdapter(chatArrayAdapter);

        t1=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status!=TextToSpeech.ERROR){
                    t1.setLanguage(Locale.US);

                    t1.setSpeechRate(1/2);
                }
            }
        });

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {

            makeRequest();
        }



        final ai.api.android.AIConfiguration config = new ai.api.android.AIConfiguration("d9080e3bc4a948b5902a7a74ca29be02",
                ai.api.android.AIConfiguration.SupportedLanguages.English,
                ai.api.android.AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);





        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });




    }
    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {


                } else {

                }
                return;
            }
        }
    }

    public void onclickbtn(View v){

        aiService.startListening();
    }

    public void openabtdev(View v){
        Intent i=new Intent(MainActivity.this,about_dev.class);
        startActivity(i);
    }

    private boolean sendResponse(String text) {
        if (text.length() == 0)
            return false;
        chatArrayAdapter.add(new ChatMessage(!rightSide, text ));

        t1.speak(text,TextToSpeech.QUEUE_FLUSH,null);
        return true;
    }
    private boolean sendChatMessage(String text) {
        if (text.length() == 0)
            return false;
        chatArrayAdapter.add(new ChatMessage(rightSide, text));
        //  chatText.setText("");
        return true;
    }



    @Override
    public void onResult(final AIResponse response) {
        Result result = response.getResult();
        Log.i(TAG, "Action: " + result.getAction());
        // process response object
        sendChatMessage(response.getResult().getResolvedQuery());

        sendResponse(result.getFulfillment().getSpeech());

    }

    @Override
    public void onError(AIError error) {
        sendResponse("Network error  please check your internet connection and try again");
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
}

