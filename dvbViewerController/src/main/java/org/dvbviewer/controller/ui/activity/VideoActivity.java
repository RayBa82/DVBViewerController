package org.dvbviewer.controller.ui.activity;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.io.HTTPUtil;
import org.dvbviewer.controller.player.DMSExtractorsFactory;
import org.dvbviewer.controller.player.DMSRenderersFactory;

import static com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES;
import static com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS;


/**
 * Created by rbaun on 31.03.18.
 */

public class VideoActivity extends AppCompatActivity implements VideoRendererEventListener {

    private static final String TAG = VideoActivity.class.getSimpleName();
    public static final String EXTRA_VIDEO_URL = TAG + "_EXTRA_VIDEO_URL";
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private ExtractorsFactory mExtractorsFactory;
    private PowerManager.WakeLock screenLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        screenLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "screenLock");

// 1. Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

// 2. Create a default LoadControl

        DefaultAllocator allocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(15000, 50000, 250, 2000)
                .setAllocator(allocator)
                .setTargetBufferBytes(C.LENGTH_UNSET)
                .createDefaultLoadControl();

// 3. Create the player
        RenderersFactory renderersFactory = new DMSRenderersFactory(getBaseContext());
        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
        playerView = findViewById(R.id.player_view);

//Set media controller
        playerView.setUseController(true);
        playerView.requestFocus();

// Bind the player to the view.
        playerView.setPlayer(player);

        Uri videoUri =Uri.parse(getIntent().getStringExtra(EXTRA_VIDEO_URL));

//Measures bandwidth during playback. Can be null if not required.
        final DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
//Produces DataSource instances through which media data is loaded.
        //DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "DMSClient"), bandwidthMeterA);

        final OkHttpDataSourceFactory dataSourceFactory = new OkHttpDataSourceFactory(
                HTTPUtil.getHttpClient(),
                Util.getUserAgent(this, "DMSClient"),
                bandwidthMeterA
        );

        mExtractorsFactory = new DMSExtractorsFactory();
        final MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(mExtractorsFactory)
                .createMediaSource(videoUri);


// Prepare the player with the source.
        player.prepare(videoSource);

        player.addListener(new Player.DefaultEventListener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.v(TAG, "Listener-onPlayerStateChanged..." + playbackState);
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    // media actually playing
                    screenLock.acquire();
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                } else if (screenLock.isHeld()){
                    // player paused in any state
                    screenLock.release();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
            }


            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.v(TAG, "Listener-onPlayerError...");
                player.stop();
                player.prepare(videoSource);
                player.setPlayWhenReady(true);
                if (screenLock.isHeld()){
                    screenLock.release();
                }
            }

        });

        player.setPlayWhenReady(true); //run file/link when ready to play.
        player.addVideoDebugListener(this); //for listening to resolution change and  outputing the resolution
    }//End of onCreate

    @Override
    public void onVideoEnabled(DecoderCounters counters) {

    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onVideoInputFormatChanged(Format format) {

    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        Log.v(TAG, "onVideoSizeChanged ["  + " width: " + width + " height: " + height + "]");
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {

    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {

    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (screenLock.isHeld()){
            screenLock.release();
        }
        Log.v(TAG, "onPause()...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()...");
        player.release();
    }

}
