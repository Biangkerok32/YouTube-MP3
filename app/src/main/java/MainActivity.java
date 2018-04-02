package io.github.dannybritto96.youtubemp3;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import com.google.android.gms.ads.internal.gmsg.HttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Entity;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    private Button btn;
    protected Button pauseBtn;
    protected Button playBtn;
    protected String youtube_id;
    protected String filename;
    public String audio_url;
    private MediaPlayer player;
    protected Uri audio_uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.audioStreamBtn);
        pauseBtn = findViewById(R.id.button3);
        playBtn = findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = (EditText)findViewById(R.id.youtube_url);
                String urlString = text.getText().toString();
                String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
                Pattern compiledPattern = Pattern.compile(pattern);
                Matcher matcher = compiledPattern.matcher(urlString);
                if (matcher.find()) {
                    youtube_id = matcher.group();
                    filename = youtube_id+".mp3";
                    audio_url = "http://35.229.150.101/mp3-"+youtube_id+".mp3";
                    new PostDataAsyncTask().execute();
                    Toast.makeText(MainActivity.this, "Please Wait..", Toast.LENGTH_LONG).show();
                    Log.d("Audio_URL",""+audio_url);
                    audio_uri = Uri.parse(audio_url);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"Loading, Please Wait...",Toast.LENGTH_LONG).show();
                        }
                    },6000);
                }
                else{
                    Toast.makeText(MainActivity.this,"Invalid URL",Toast.LENGTH_LONG).show();
                    text.setText("");
                }


            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackgroundSound mBackgroundSound = new BackgroundSound();
                mBackgroundSound.execute();
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player!=null){
                    player.pause();
                }
            }
        });
    }
    public class BackgroundSound extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... strings){
            player = MediaPlayer.create(MainActivity.this,audio_uri);
            AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
            float actualVolume = (float)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            float maxVolume = (float)audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float volume = actualVolume;
            player.setLooping(false);
            player.setVolume(volume,volume);
            player.start();
            return null;
        }
    }
    public class PostDataAsyncTask extends AsyncTask<String, String, String> {



        @Override
        protected String doInBackground(String... strings) {
            try {
                String url = "http://35.229.150.101:5000/hello";
                Log.d("URL",""+url);
                Log.d("ID",""+youtube_id);
                org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                nameValuePairs.add(new BasicNameValuePair("id",""+youtube_id));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity resEntity = response.getEntity();
                try {
                    if (resEntity != null) {
                        String responseStr = EntityUtils.toString(resEntity).trim();
                        Log.v("Response: ",""+responseStr);
                    }
                }
                catch(Exception e){
                    Log.e("Exception",Log.getStackTraceString(e));
                    Toast.makeText(MainActivity.this,"Couldn't establish connection to server",Toast.LENGTH_LONG).show();
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception x) {
                x.printStackTrace();
                Toast.makeText(MainActivity.this,"Couldn't establish connection to server",Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String lengthOfFile) {
            // do stuff after posting data

        }

    }


}
