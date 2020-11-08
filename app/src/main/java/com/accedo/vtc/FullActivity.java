package com.accedo.vtc;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.accedo.vtc.common.Common;
import com.accedo.vtc.playerads.VideoPlayerAd;
import com.comix.overwatch.HiveProgressView;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.VideoPlayerView;

public class FullActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener,
        AdEvent.AdEventListener, AdErrorEvent.AdErrorListener  {
    VideoPlayerView videoPlayer;
    private WebView wv;
    private String url = "";
    private String urlOverlay = "";
    private String isOriention;

    long currentPosition = 0;
    public HiveProgressView loadingView;

    public RelativeLayout rlMediaController;
    private ImageView imgPlay, imgZoomOut, img_pause_detail;
    private TextView tvCurrentTime, tvDuration;
    private ProgressBar progress_bar_video;

    private FrameLayout rlVideoPlayer;
    private boolean isLive;
    public static FullActivity instance = null;


    // define quang cao
    private static String LOGTAG = "ImaAds";

    // The video player.
    private VideoPlayerAd mVideoPlayer;

    // The container for the ad's UI.
    private ViewGroup mAdUiContainer;

    // Factory class for creating SDK objects.
    private ImaSdkFactory mSdkFactory;

    // The AdsLoader instance exposes the requestAds method.
    private AdsLoader mAdsLoader;

    // AdsManager exposes methods to control ad playback and listen to ad events.
    private AdsManager mAdsManager;

    // Whether an ad is displayed.
    private boolean mIsAdDisplayed;
    private int type = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance= this;
//        getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN
//        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_full_screen_new);

        if (!Common.checkInternet(this)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(FullActivity.this);
            alertDialog.setTitle("");
            alertDialog.setMessage(getString(R.string.eror_network));
            alertDialog.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    FullActivity.this.finish();
                }
            });
            alertDialog.show();
        }
        rlVideoPlayer = (FrameLayout) findViewById(R.id.rlVideoPlayer);
        rlVideoPlayer.setOnTouchListener(this);
        rlMediaController = (RelativeLayout) findViewById(R.id.rlMediaController);
        imgPlay = (ImageView) findViewById(R.id.imgPlay);
        imgPlay.setOnClickListener(this);
        imgZoomOut = (ImageView) findViewById(R.id.imgZoomOut);
        imgZoomOut.setOnClickListener(this);
        tvCurrentTime = (TextView) findViewById(R.id.tvCurrentTime);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        progress_bar_video = (ProgressBar) findViewById(R.id.progress_bar_video);
        videoPlayer = (VideoPlayerView) findViewById(R.id.vtvView);
        loadingView = (HiveProgressView) findViewById(R.id.loadingView);

        wv = (WebView) findViewById(R.id.webView);
        wv.setBackgroundColor(getResources().getColor(R.color.colortransperent));

        // code phan quang cao

        mVideoPlayer = (VideoPlayerAd) findViewById(R.id.sampleVideoPlayer);
        mAdUiContainer = (ViewGroup) findViewById(R.id.videoPlayerWithAdPlayback);
        // Create an AdsLoader.
        loadAds();

        url = getIntent().getStringExtra("detailUrl");
        currentPosition = getIntent().getLongExtra("currentPosition", 0);
        isLive = getIntent().getBooleanExtra("isLive", false);
        Bundle mbundle = this.getIntent().getBundleExtra("bundle");
        if (url == null || url.equals("")) {
            if (mbundle != null) {
                url = mbundle.getString("url", "");
                urlOverlay = mbundle.getString("urlOverlay", "");
                setUpWV(urlOverlay);
                isOriention = mbundle.getString("isOriention", "");
                isLive = mbundle.getBoolean("isLive", false);
                if (isOriention != null && !isOriention.equals("")) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }

            }
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
//        bindData();
        if (!isLive) {
            videoPlayer.setUseController(true);
        }

    }
    private void loadAds(){
        // Create an AdsLoader.
        mSdkFactory = ImaSdkFactory.getInstance();
        AdDisplayContainer adDisplayContainer = mSdkFactory.createAdDisplayContainer();
        adDisplayContainer.setAdContainer(mAdUiContainer);
        ImaSdkSettings settings = mSdkFactory.createImaSdkSettings();
        mAdsLoader = mSdkFactory.createAdsLoader(FullActivity.this, settings, adDisplayContainer);

        // Add listeners for when ads are loaded and for errors.

        mAdsLoader.addAdErrorListener(this);
        mAdsLoader.addAdsLoadedListener(new AdsLoader.AdsLoadedListener() {
            @Override
            public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
                // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
                // events for ad playback and errors.
                mAdsManager = adsManagerLoadedEvent.getAdsManager();

                // Attach event and error event listeners.
                mAdsManager.addAdErrorListener(FullActivity.this);
                mAdsManager.addAdEventListener(FullActivity.this);
                mAdsManager.init();
            }
        });

        // Add listener for when the content video finishes.
        mVideoPlayer.addVideoCompletedListener(new VideoPlayerAd.OnVideoCompletedListener() {
            @Override
            public void onVideoCompleted() {
                // Handle completed event for playing post-rolls.
                if (mAdsLoader != null) {
                    mAdsLoader.contentComplete();
                }
            }
        });
    }

    /**
     * Request video ads from the given VAST ad tag.
     * @param adTagUrl URL of the ad's VAST XML
     */
    private void requestAds(String adTagUrl) {
        if(type != 0){
            adTagUrl = adTagUrl + "?p=" + type;
        }
        // Create the ads request.
        AdsRequest request = mSdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setContentProgressProvider(new ContentProgressProvider() {
            @Override
            public VideoProgressUpdate getContentProgress() {
                if (mIsAdDisplayed || mVideoPlayer == null || mVideoPlayer.getDuration() <= 0) {
                    return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
                }
                return new VideoProgressUpdate(mVideoPlayer.getCurrentPosition(),
                        mVideoPlayer.getDuration());
            }
        });

        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        mAdsLoader.requestAds(request);
    }
    private void bindData() {
        if (!isLive) {
            videoPlayer.setUseController(true);
        }
        loadingView.setVisibility(View.VISIBLE);
        videoPlayer.setVideoUri(url, "");
        videoPlayer.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    loadingView.setVisibility(View.GONE);
                    videoPlayer.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
//        if(isLive){

        if(url != null && !url.equals("")){
            // load quang cao len
            mAdUiContainer.setVisibility(View.VISIBLE);
            requestAds(getString(R.string.ad_tag_url));
        }else{
            rlVideoPlayer.setVisibility(View.GONE);
        }
//        bindData();
//        if (currentPosition != 0) {
//            videoPlayer.getPlayer().seekTo(currentPosition);
//        }
//        videoPlayer.getPlayer().setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdsManager != null && mIsAdDisplayed) {
            mAdsManager.pause();
        } else {
            currentPosition = videoPlayer.getPlayer().getCurrentPosition();
            videoPlayer.getPlayer().setPlayWhenReady(false);
            imgPlay.setImageResource(R.drawable.ic_play);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdsManager != null && mIsAdDisplayed) {
            mAdsManager.pause();
        } else {
            videoPlayer.getPlayer().setPlayWhenReady(false);
            imgPlay.setImageResource(R.drawable.ic_play);
            mVideoPlayer.pause();
        }

    }

    @Override
    public void onBackPressed() {
        if (currentPosition != 0) {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("currentPosition", videoPlayer.getPlayer().getCurrentPosition());
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        videoPlayer.releasePlayer();
        super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        videoPlayer.restartPlayer();
    }

    @Override
    protected void onDestroy() {
        instance = null;
        super.onDestroy();
        videoPlayer.releasePlayer();
    }

    private void setUpWV(String url) {

        wv.getSettings().setLoadsImagesAutomatically(true);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        wv.setScrollbarFadingEnabled(true);
        wv.setScrollContainer(false);

        if (url != null) {
            wv.loadUrl(url);
        }
//        wv.loadUrl("file:///android_asset/test.html");

//        wv.setWebChromeClient(new HomeWebViewClient());
        wv.addJavascriptInterface(new DetaiWebViewJavaScriptInterface(this), "FullActivity");
        wv.clearCache(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgPlay:
                if (videoPlayer != null) {
                    isPausePlayVideo();
                    new Handler().postDelayed(hideControllerRunnable, 2000);
                }
                break;
            case R.id.imgZoomOut:
//                if (currentPosition != 0) {
                    Intent intent = new Intent(this, DetailActivity.class);
                    intent.putExtra("currentPosition", videoPlayer.getPlayer().getCurrentPosition());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
//                }
                videoPlayer.releasePlayer();

                break;
            case R.id.failedView:
                bindData();
                break;

        }
    }

    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        mAdUiContainer.setVisibility(View.GONE);
        if (mAdsManager != null) {
            mAdsManager.destroy();
            mAdsManager = null;
        }

        mAdUiContainer.setVisibility(View.GONE);
        rlVideoPlayer.setVisibility(View.VISIBLE);
        bindData();
        if (!isLive) {
            videoPlayer.setUseController(true);
        }

    }

    @Override
    public void onAdEvent(AdEvent adEvent) {
        Log.i(LOGTAG, "Event: " + adEvent.getType());

        // These are the suggested event types to handle. For full list of all ad event
        // types, see the documentation for AdEvent.AdEventType.
        switch (adEvent.getType()) {
            case LOADED:
                rlVideoPlayer.setVisibility(View.VISIBLE);
                videoPlayer.setVisibility(View.VISIBLE);
                // AdEventType.LOADED will be fired when ads are ready to be played.
                // AdsManager.start() begins ad playback. This method is ignored for VMAP or
                // ad rules playlists, as the SDK will automatically start executing the
                // playlist.
                mAdsManager.start();
                break;
            case CONTENT_PAUSE_REQUESTED:
                // AdEventType.CONTENT_PAUSE_REQUESTED is fired immediately before a video
                // ad is played.
                mIsAdDisplayed = true;
                mVideoPlayer.pause();
                break;
            case CONTENT_RESUME_REQUESTED:
                // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad is completed
                // and you should start playing your content.
                mIsAdDisplayed = false;
                mVideoPlayer.play();
                break;
            case ALL_ADS_COMPLETED:

                if (mAdsManager != null) {
                    mAdsManager.destroy();
                    mAdsManager = null;
                }

                mAdUiContainer.setVisibility(View.GONE);
                rlVideoPlayer.setVisibility(View.VISIBLE);
                bindData();
                if (currentPosition != 0) {
                    videoPlayer.getPlayer().seekTo(currentPosition);
                }
                videoPlayer.getPlayer().setPlayWhenReady(true);
                if (!isLive) {
                    videoPlayer.setUseController(true);
                }
                break;
            default:
                break;
        }
    }

    public class DetaiWebViewJavaScriptInterface {

        private Context context;

        /*
         * Need a reference to the context in order to sent a post message
         */
        public DetaiWebViewJavaScriptInterface(Context context) {
            this.context = context;
        }

        /*
         * This method can be called from Android. @JavascriptInterface
         * required after SDK version 17.
         */
        @JavascriptInterface
        public void playDetail(String url, String urlDetail) {
            Bundle mBundle = new Bundle();
            Intent intent = new Intent(context, FullActivity.class);
            mBundle.putString("url", url);
            mBundle.putString("urlDetail", urlDetail);

            intent.putExtra("bundle", mBundle);
            startActivity(intent);
            finish();
        }

        @JavascriptInterface
        public void onBackPress() {
            FullActivity.this.finish();
        }

        @JavascriptInterface
        public void shareApp(String message) {
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "VTC Now");
                String shareMessage = "";
                shareMessage = shareMessage + "\n" + message;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, ""));
            } catch (Exception e) {
            }
        }

    }

    private void isPausePlayVideo() {
        if (isPlaying()) {
            imgPlay.setImageResource(R.drawable.ic_play);
            videoPlayer.getPlayer().setPlayWhenReady(false);
        } else {
            imgPlay.setImageResource(R.drawable.ic_pause);
            videoPlayer.getPlayer().setPlayWhenReady(true);
        }
        new Handler().removeCallbacks(hideControllerRunnable);
    }

    private Runnable hideControllerRunnable = new Runnable() {
        @Override
        public void run() {
            hideController();
        }
    };

    private void hideController() {
        rlMediaController.setVisibility(View.GONE);
    }

    private void showController() {
        rlMediaController.setVisibility(View.VISIBLE);
    }

    private boolean isPlaying() {
        if (videoPlayer != null) {
            if (videoPlayer.getPlayer() != null) {
                return (videoPlayer.getPlayer() != null
                        && videoPlayer.getPlayer().getPlaybackState() != Player.STATE_ENDED
                        && videoPlayer.getPlayer().getPlaybackState() != Player.STATE_IDLE
                        && videoPlayer.getPlayer().getPlayWhenReady());
            }
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == R.id.rlVideoPlayer) {
            if (rlMediaController.getVisibility() == View.VISIBLE) {
                hideController();
            } else {
                showController();
                new Handler().postDelayed(hideControllerRunnable, 2000);
            }
        }

        return false;
    }
}
