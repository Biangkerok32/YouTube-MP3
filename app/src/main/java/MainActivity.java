package io.github.dannybritto96.youtubemp3;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    protected Button pauseBtn;
    public TextView textView;
    protected Button btn;
    public Button playBtn;
    public Button replayBtn;
    protected String youtube_id;
    protected String filename;
    public String audio_url;
    protected TextView textView2;
    protected MediaPlayer mplayer = null;
    protected Uri audio_uri;
    protected ProgressBar spinner;
    public EditText text;
    public SeekBar seek_bar;
    public SeekBar seek_bar2;
    public Handler seekHandler = new Handler();
    public TextView title;
    public String song_title = null;
    public String image_url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.audioStreamBtn);
        pauseBtn = findViewById(R.id.button3);
        playBtn = findViewById(R.id.button2);
        spinner = findViewById(R.id.progressBar1);
        seek_bar = findViewById(R.id.seekBar);
        replayBtn = findViewById(R.id.button4);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView3);
        textView.setVisibility(View.GONE);
        textView2.setVisibility(View.GONE);
        seek_bar2 = findViewById(R.id.seekBar2);
        seek_bar.bringToFront();
        seek_bar.getThumb().mutate().setAlpha(0);
        seek_bar.setVisibility(View.GONE);
        seek_bar2.setVisibility(View.GONE);
        playBtn.setEnabled(false);
        pauseBtn.setEnabled(false);
        replayBtn.setEnabled(false);
        playBtn.setVisibility(View.INVISIBLE);
        pauseBtn.setVisibility(View.INVISIBLE);
        replayBtn.setVisibility(View.INVISIBLE);
        title = findViewById(R.id.textView4);
        title.setVisibility(View.GONE);
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
                    audio_url = "http://XX.XXX.XXX.XX/mp3-" + youtube_id + ".mp3";
                    new PostDataAsyncTask().execute();
                    Log.d("Audio_URL", "" + audio_url);
                    image_url = "https://img.youtube.com/vi"+youtube_id+"/maxresdefault.jpg";
                    audio_uri = Uri.parse(audio_url);
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

        seek_bar.setOnSeekBarChangeListener(new yourListener());
        replayBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mplayer.setLooping(true);
                Toast.makeText(MainActivity.this,"Looping",Toast.LENGTH_SHORT).show();
                /*try{
                    mplayer.release();
                    mplayer = null;
                } catch(Exception e){
                    Log.e("Exception at replay",Log.getStackTraceString(e));
                    Toast.makeText(MainActivity.this,"Check URL",Toast.LENGTH_SHORT).show();
                }
                try{
                    startPlaying();
                } catch(Exception e){
                    Log.e("Exception at replay",Log.getStackTraceString(e));
                    Toast.makeText(MainActivity.this,"Check URL",Toast.LENGTH_SHORT).show();
                }*/
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            boolean mStartPlaying = true;

            @Override
            public void onClick(View view) {
                stopPlaying();
                text.setText("");
                playBtn.setBackgroundResource(R.drawable.ic_play);
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
        seek_bar2.setProgress(mplayer.getCurrentPosition());
        seekHandler.postDelayed(run,1000);
        String time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(mplayer.getCurrentPosition()),
                TimeUnit.MILLISECONDS.toSeconds(mplayer.getCurrentPosition()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mplayer.getCurrentPosition()))
        );
        textView.setText(time);
    }
    public class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mplayer.pause();
                playBtn.setBackgroundResource(R.drawable.ic_play);

            }
        }
    }
    final IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    final BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();

    MediaSessionCompat.Callback callback = new
            MediaSessionCompat.Callback() {
                @Override
                public void onPlay() {
                    registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
                    Log.d("Receiver Register","Receiver Registered");
                }

                @Override
                public void onStop() {
                    unregisterReceiver(myNoisyAudioStreamReceiver);
                }
            };

    @SuppressLint("SetTextI18n")
    private void startPlaying(){
        if(mplayer != null && mplayer.isPlaying()){
            mplayer.pause();
            playBtn.setBackgroundResource(R.drawable.ic_play);
            //playBtn.setCompoundDrawablesWithIntrinsicBounds(drawable_play,0,0,0);

        }else if(mplayer != null){
            mplayer.start();
            playBtn.setBackgroundResource(R.drawable.ic_pause);
            //playBtn.setCompoundDrawablesWithIntrinsicBounds(drawable_pause,0,0,0);

        }else{
            mplayer = new MediaPlayer();
            try{
                mplayer.stop();
                mplayer.setDataSource(audio_url);
                mplayer.prepare();
                seek_bar.setMax(mplayer.getDuration());
                seek_bar2.setMax(mplayer.getDuration());
                Log.d("getDuration",""+mplayer.getDuration());
                int getDuration = mplayer.getDuration();
                String time = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(getDuration),
                        TimeUnit.MILLISECONDS.toSeconds(getDuration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getDuration))
                );
                callback.onPlay();
                seekUpdation();
                textView2.setText(time);
                mplayer.start();
                textView.setVisibility(View.VISIBLE);
                textView2.setVisibility(View.VISIBLE);
                mplayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                        seek_bar.setSecondaryProgress((seek_bar.getMax() /100) * i);
                    }
                });
                playBtn.setBackgroundResource(R.drawable.ic_pause);
                //playBtn.setCompoundDrawablesWithIntrinsicBounds(drawable_pause,0,0,0);
            }catch (Exception e){
                Log.e("Exception",Log.getStackTraceString(e));
            }
        }
    }
    private void stopPlaying(){
        try{
            btn.setVisibility(View.VISIBLE);
            title.setVisibility(View.GONE);
            seek_bar.setVisibility(View.GONE);
            seek_bar2.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            textView2.setVisibility(View.GONE);
            seekHandler.removeCallbacks(run);
            callback.onStop();
            mplayer.stop();
            mplayer.release();
            mplayer = null;
            btn.setEnabled(true);
            audio_url = "";
            playBtn.setEnabled(false);
            pauseBtn.setEnabled(false);
            replayBtn.setEnabled(false);
            playBtn.setVisibility(View.INVISIBLE);
            pauseBtn.setVisibility(View.INVISIBLE);
            replayBtn.setVisibility(View.INVISIBLE);
        } catch (Exception e){
            Log.d("Stop Exception",Log.getStackTraceString(e));
        }

    }
    private class yourListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekbar,int progress, boolean fromUser){
            if(fromUser){
                int secProgress = seek_bar.getSecondaryProgress();
                if (secProgress > progress){
                    mplayer.seekTo(progress);
                }
                else {
                    seekbar.setProgress(seek_bar.getProgress());
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar){

        }

        public void onStopTrackingTouch(SeekBar seekBar){

        }
    }

    public class PostDataAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                String url = "http://XX.XX.XXX.XX:5000/hello";
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
                        song_title = responseStr;
                        Log.v("Response: ",""+responseStr);
                        httpClient.getConnectionManager().shutdown();
                    }
                }
                catch(Exception e){
                    Log.e("Exception",Log.getStackTraceString(e));
                    Toast.makeText(MainActivity.this,"Couldn't establish connection to server",Toast.LENGTH_LONG).show();
                }

            } catch (Exception x) {
                x.printStackTrace();
                //Toast.makeText(MainActivity.this,"Couldn't establish connection to server",Toast.LENGTH_LONG).show();
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(String... text){
            Toast.makeText(MainActivity.this, "Please Wait...", Toast.LENGTH_LONG).show();
        }
        @Override
        protected void onPostExecute(String lengthOfFile) {
            spinner.setVisibility(View.GONE);
            SpannableString spannableString = new SpannableString(song_title);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD),0,spannableString.length(),0);
            title.setVisibility(View.VISIBLE);
            title.setText(spannableString);
            playBtn.setVisibility(View.VISIBLE);
            pauseBtn.setVisibility(View.VISIBLE);
            replayBtn.setVisibility(View.VISIBLE);
            btn.setVisibility(View.INVISIBLE);
            playBtn.setEnabled(true);
            pauseBtn.setEnabled(true);
            replayBtn.setEnabled(true);
            btn.setEnabled(false);
            playBtn.performClick();
            seek_bar2.setVisibility(View.VISIBLE);
            seek_bar.setVisibility(View.VISIBLE);
        }

    }
}
