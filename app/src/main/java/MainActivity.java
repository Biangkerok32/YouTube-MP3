package io.github.dannybritto96.youtubemp3;


import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class MainActivity extends AppCompatActivity {
    protected Button pauseBtn;
    protected Button btn;
    public Button playBtn;
    protected String youtube_id;
    protected String filename;
    public String audio_url;
    protected MediaPlayer mplayer = null;
    protected Uri audio_uri;
    int drawable_pause = R.drawable.ic_pause;
    int drawable_play = R.drawable.ic_play;
    protected ProgressBar spinner;
    public EditText text;
    public SeekBar seek_bar;
    Handler seekHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.audioStreamBtn);
        pauseBtn = findViewById(R.id.button3);
        playBtn = findViewById(R.id.button2);
        spinner = findViewById(R.id.progressBar1);
        seek_bar = findViewById(R.id.seekBar);
        playBtn.setEnabled(false);
        pauseBtn.setEnabled(false);
        try{
            text = findViewById(R.id.youtube_url);
        } catch (Exception e){
            Log.e("Exception",Log.getStackTraceString(e));
        }
        try{
            Bundle extras = getIntent().getExtras();
            String value1 = (String) extras.get(Intent.EXTRA_TEXT);
            text.setText(value1);
        } catch (Exception e){
            Log.e("Exception",Log.getStackTraceString(e));

        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlString = text.getText().toString();
                String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
                Pattern compiledPattern = Pattern.compile(pattern);
                Matcher matcher = compiledPattern.matcher(urlString);
                if (matcher.find()) {
                    spinner.setVisibility(View.VISIBLE);
                    youtube_id = matcher.group();
                    filename = youtube_id + ".mp3";
                    audio_url = "http://35.229.150.101/mp3-" + youtube_id + ".mp3";
                    new PostDataAsyncTask().execute();
                    Log.d("Audio_URL", "" + audio_url);
                    audio_uri = Uri.parse(audio_url);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Please Wait...", Toast.LENGTH_LONG).show();
                            playBtn.setEnabled(true);
                            pauseBtn.setEnabled(true);
                        }
                    }, 3000);

                } else {
                    Toast.makeText(MainActivity.this, "Invalid URL", Toast.LENGTH_LONG).show();
                }


            }
        });


        playBtn.setOnClickListener(new View.OnClickListener() {
            boolean mStartPlaying = true;

            @Override
            public void onClick(View view) {
                startPlaying();
                mStartPlaying = !mStartPlaying;
            }
        });
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            boolean mStartPlaying = true;

            @Override
            public void onClick(View view) {
                stopPlaying();
                text.setText("");
                playBtn.setText(R.string.play);
                playBtn.setCompoundDrawablesWithIntrinsicBounds(drawable_play,0,0,0);
            }
        });

    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };
    public void seekUpdation(){
        seek_bar.setProgress(mplayer.getCurrentPosition());
        seekHandler.postDelayed(run,1000);
    }

    @SuppressLint("SetTextI18n")
    private void startPlaying(){
        if(mplayer != null && mplayer.isPlaying()){
            mplayer.pause();
            playBtn.setText(R.string.play);
            playBtn.setCompoundDrawablesWithIntrinsicBounds(drawable_play,0,0,0);
        }else if(mplayer != null){
            mplayer.start();
            playBtn.setText(R.string.pause);
            playBtn.setCompoundDrawablesWithIntrinsicBounds(drawable_pause,0,0,0);

        }else{
            mplayer = new MediaPlayer();
            btn.setEnabled(false);
            try{
                mplayer.setDataSource(audio_url);
                mplayer.prepare();
                seek_bar.setMax(mplayer.getDuration());
                mplayer.start();
                seekUpdation();
                playBtn.setText(R.string.pause);
                playBtn.setCompoundDrawablesWithIntrinsicBounds(drawable_pause,0,0,0);
            }catch (Exception e){
                Log.e("Exception",Log.getStackTraceString(e));
            }
        }


    }

    private void stopPlaying(){
        mplayer.release();
        mplayer = null;
        btn.setEnabled(true);

    }


    /*public void sendNotification(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,"1").setSmallIcon(R.drawable.ic_music).setContentTitle("Youtube MP3").setContentText("Ready to Play");
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(channel_1);
        mNotificationManager.notify(1,mBuilder.build());
    }*/

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
        protected void onProgressUpdate(String... text){
        }
        @Override
        protected void onPostExecute(String lengthOfFile) {
            spinner.setVisibility(View.GONE);
            //sendNotification();
        }

    }


}
