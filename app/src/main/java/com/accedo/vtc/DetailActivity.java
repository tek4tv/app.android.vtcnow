package com.accedo.vtc;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.accedo.vtc.common.Common;
import com.accedo.vtc.model.Detail;
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
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.VideoPlayerView;
import com.google.gson.Gson;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener , AdEvent.AdEventListener, AdErrorEvent.AdErrorListener {
    public VideoPlayerView videoPlayer;
    public RelativeLayout rlVideoPlayer;
    public HiveProgressView loadingView;
    public RelativeLayout rlMediaController;
    private ImageView imgPlay, imgZoomOut, img_pause_detail;
    private TextView tvCurrentTime, tvDuration;
    private AppCompatSeekBar progress_bar_video;
    public LinearLayout failedView;
    private FrameLayout flShimmer;
    public WebView wv, wvheader;
    public String mUrl = "";
    private boolean isLive = false;
    private boolean isFAV = false;
    public static DetailActivity instance = null;
    private boolean isLandcape = false;
    public  boolean isBACK = false;
    private int type = 0;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_new);

        instance = this;
        rlVideoPlayer = (RelativeLayout) findViewById(R.id.rlVideoPlayer);
//        rlVideoPlayer.setOnTouchListener(this);
        videoPlayer = (VideoPlayerView) findViewById(R.id.videoPlayer);
        loadingView = (HiveProgressView) findViewById(R.id.loadingView);
        wvheader = (WebView) findViewById(R.id.wvheader);
        wv = (WebView) findViewById(R.id.webDetail);
        rlMediaController = (RelativeLayout) findViewById(R.id.rlMediaController);
        imgPlay = (ImageView) findViewById(R.id.imgPlay);
        imgPlay.setOnClickListener(this);
        imgZoomOut = (ImageView) findViewById(R.id.imgZoomOut);
        imgZoomOut.setOnClickListener(this);
        tvCurrentTime = (TextView) findViewById(R.id.tvCurrentTime);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        progress_bar_video = (AppCompatSeekBar) findViewById(R.id.progress_bar_video);
        failedView = (LinearLayout) findViewById(R.id.failedView);
        failedView.setOnClickListener(this);

        // code phan quang cao

        mVideoPlayer = (VideoPlayerAd) findViewById(R.id.sampleVideoPlayer);
        mAdUiContainer = (ViewGroup) findViewById(R.id.videoPlayerWithAdPlayback);
        // Create an AdsLoader.
        loadAds();


        // controller
        Bundle mBundle = this.getIntent().getBundleExtra("bundle");
        if (mBundle != null) {
            mUrl = mBundle.getString("url", "");
//            Toast.makeText(this, mUrl, Toast.LENGTH_SHORT).show();
            isLive = mBundle.getBoolean("isLive", false);
            String urlDetail = mBundle.getString("urlDetail", "");
            String urlWvheader = mBundle.getString("urlWvHeader", "");
            int hightww = mBundle.getInt("height", 0);
            int type = mBundle.getInt("type", 0);

            if (urlWvheader != null && !urlWvheader.equals("")) {
                wvheader.setVisibility(View.VISIBLE);
                setUpWV(urlWvheader, wvheader);
                if (hightww != 0) {
                    if (wvheader.getLayoutParams() != null) {
                        wvheader.getLayoutParams().height = Common.dpToPx(hightww);
                    }
                }
            } else {
                wvheader.setVisibility(View.GONE);
            }
            setUpWV(urlDetail, wv);
        }
        if(mUrl != null && !mUrl.equals("")){
            // load quang cao len
            mAdUiContainer.setVisibility(View.VISIBLE);
            requestAds(getString(R.string.ad_tag_url));
//            bindData();
//            if (!isLive) {
//                videoPlayer.setUseController(true);
//            }
        }else{
            rlVideoPlayer.setVisibility(View.GONE);
        }

    }

    private void loadAds(){
        // Create an AdsLoader.
        mSdkFactory = ImaSdkFactory.getInstance();
        AdDisplayContainer adDisplayContainer = mSdkFactory.createAdDisplayContainer();
        adDisplayContainer.setAdContainer(mAdUiContainer);
        ImaSdkSettings settings = mSdkFactory.createImaSdkSettings();
        mAdsLoader = mSdkFactory.createAdsLoader(DetailActivity.this, settings, adDisplayContainer);

        // Add listeners for when ads are loaded and for errors.

        mAdsLoader.addAdErrorListener(this);
        mAdsLoader.addAdsLoadedListener(new AdsLoader.AdsLoadedListener() {
            @Override
            public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
                // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
                // events for ad playback and errors.
                mAdsManager = adsManagerLoadedEvent.getAdsManager();

                // Attach event and error event listeners.
                mAdsManager.addAdErrorListener(DetailActivity.this);
                mAdsManager.addAdEventListener(DetailActivity.this);
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

    public void bindData() {
        failedView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        videoPlayer.setVideoUri(mUrl, "");
        videoPlayer.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    loadingView.setVisibility(View.GONE);
                    videoPlayer.setVisibility(View.VISIBLE);
                }else if(!isLive && !isBACK && playbackState == Player.STATE_ENDED){
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        Log.d("vao day","vao day");
                        wv.evaluateJavascript("nextPlayList();", null);
                    } else {
                        Log.d("vao day","vao day");
                        wv.loadUrl("javascript:nextPlayList();");
                    }
                }
            }


        });
        videoPlayer.getPlayer().setPlayWhenReady(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            wvheader.setVisibility(View.GONE);
            rlVideoPlayer.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            rlVideoPlayer.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            imgZoomOut.setImageResource(R.drawable.exo_controls_fullscreen_exit);
            imgZoomOut.setTag("1");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            rlVideoPlayer.getLayoutParams().width = 0;
            rlVideoPlayer.getLayoutParams().height = 0;
            imgZoomOut.setImageResource(R.drawable.exo_controls_fullscreen_enter);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (!isFAV) {
                wvheader.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAdsManager != null && mIsAdDisplayed) {
            mAdsManager.resume();
        } else {
//            if (isLive) {
//                if(mUrl != null && !mUrl.equals("") && rlVideoPlayer.getVisibility() == View.VISIBLE){
//                    videoPlayer.setUseController(true);
//                    bindData();
//                }
//            }
            if (!isFAV) {
                if(rlVideoPlayer.getVisibility() == View.VISIBLE){
                    videoPlayer.getPlayer().setPlayWhenReady(true);
                }else{
                    videoPlayer.getPlayer().setPlayWhenReady(false);

                }
            } else {
                videoPlayer.getPlayer().setPlayWhenReady(false);
            }
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdsManager != null && mIsAdDisplayed) {
            mAdsManager.pause();
        } else {
            videoPlayer.getPlayer().setPlayWhenReady(false);
            mVideoPlayer.pause();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdsManager != null && mIsAdDisplayed) {
            mAdsManager.pause();
        } else {
            videoPlayer.getPlayer().setPlayWhenReady(false);
            mVideoPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayer.releasePlayer();
        instance = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            videoPlayer.getPlayer().seekTo(data.getLongExtra("currentPosition", videoPlayer.getPlayer().getDuration()));

        }
    }

    private void setUpWV(String url, WebView wvpr) {

        wvpr.getSettings().setLoadsImagesAutomatically(true);
        wvpr.getSettings().setJavaScriptEnabled(true);
        wvpr.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        wvpr.setScrollbarFadingEnabled(true);
        wvpr.setScrollContainer(false);
        wvpr.setWebChromeClient(new HomeWebViewClient());
        wvpr.addJavascriptInterface(new DetaiWebViewJavaScriptInterface(this), "DetailActivity");
        wvpr.clearCache(true);
        if (url != null && !url.equals("")) {
            if (!Common.checkInternet(this)) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(DetailActivity.this);
                alertDialog.setTitle("");
                alertDialog.setMessage(getString(R.string.eror_network));
                alertDialog.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        DetailActivity.this.finish();
                    }
                });
                alertDialog.show();
            } else {
                wvpr.loadUrl(url);
            }

        }

    }

    @Override
    public void onBackPressed() {
        isBACK = true;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imgZoomOut.setImageResource(R.drawable.ic_expand);
            imgZoomOut.setTag("");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }
        super.onBackPressed();
    }

    private void goToFullScreen(String url) {
        Intent intent = new Intent(this, FullActivity.class);
        intent.putExtra("currentPosition", videoPlayer.getPlayer().getCurrentPosition());
        intent.putExtra("detailUrl", url);
        intent.putExtra("isLive", isLive);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgZoomOut:
                if (view.getTag() != null && "1".equals(view.getTag().toString())) {
                    imgZoomOut.setImageResource(R.drawable.exo_controls_fullscreen_enter);
                    imgZoomOut.setTag("");
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
            case R.id.failedView:
                bindData();
                break;

        }
    }

    public void playVideo(String url) {
        mUrl = url;
        bindData();
    }

    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        Log.e(LOGTAG, "Ad Error: " + adErrorEvent.getError().getMessage());
//        mVideoPlayer.play(); load quang cao loi
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
                if (!isLive) {
                    videoPlayer.setUseController(true);
                }
                break;
            default:
                break;
        }

    }

    private class HomeWebViewClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            result.confirm();
            Log.d("TAG", "message" + message);
            if (message != null && !message.equals("")) {
                videoPlayer.getPlayer().setPlayWhenReady(false);
                try {
                    Detail detail = new Gson().fromJson(message, Detail.class);
                    if (detail != null) {
                        isLive = detail.isLive();
                        if(isLive){
                            videoPlayer.setUseController(false);
                        }else{
                            videoPlayer.setUseController(true);
                        }
                        if (detail.getUrl_favorites() != null && !detail.getUrl_favorites().equals("")) {
                            isFAV = true;
                            wvheader.setVisibility(View.GONE);
                            videoPlayer.getPlayer().setPlayWhenReady(false);
                            rlVideoPlayer.setVisibility(View.GONE);
                            wv.loadUrl(wv.getUrl() + detail.getUrl_favorites());
                        } else {
                            isFAV = false;
                            wvheader.setVisibility(View.VISIBLE);
                            if (detail.getUrlwv_header() != null && !detail.getUrlwv_header().equals("")) {
                                wvheader.loadUrl(detail.getUrlwv_header());
                                if (detail.getHeight() != 0) {
                                    wvheader.getLayoutParams().height = Common.dpToPx(detail.getHeight());
                                }
                            }
                            if (detail.getUrl() != null && !detail.getUrl().equals("")) {
                                mSdkFactory = null;
                                rlVideoPlayer.setVisibility(View.VISIBLE);
                                videoPlayer.setVisibility(View.GONE);
                                if (mAdsManager != null) {
                                    mAdsManager.destroy();
                                    mAdsManager = null;
                                }
                                // doan nay load lai quang cao
                                mUrl = detail.getUrl();
                                type = detail.getType();
                                loadAds();
                                mAdUiContainer.setVisibility(View.VISIBLE);
                                requestAds(getString(R.string.ad_tag_url));
//                                playVideo(detail.getUrl());
//                                videoPlayer.getPlayer().setPlayWhenReady(true);
                            }else{
                                videoPlayer.getPlayer().setPlayWhenReady(false);
                                rlVideoPlayer.setVisibility(View.GONE);
                            }
                            if (detail.getUrlwv() != null && !detail.getUrlwv().equals("")) {
                                wv.setVisibility(View.VISIBLE);
                                wv.loadUrl(detail.getUrlwv());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
    }

    public String saveData(String json) {
        Context ctx = getApplicationContext();
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
        // Put the json format string to SharedPreferences object.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(this.getPackageName(), json);
        editor.commit();
        return "true";
    }

    public String loadData() {
        Context ctx = getApplicationContext();
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
        // Get saved string data in it.
        String userInfoListJsonString = sharedPreferences.getString(this.getPackageName(), "");
        return userInfoListJsonString;
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
        public void onBackPress() {
            isBACK = true;
            DetailActivity.this.finish();
        }

        @JavascriptInterface
        public String saveDataFromWeb(String json) {
            return saveData(json);
        }

        @JavascriptInterface
        public String loadDataFromApp() {
            return loadData();
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
}
