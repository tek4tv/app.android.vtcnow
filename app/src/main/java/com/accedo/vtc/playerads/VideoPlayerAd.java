package com.accedo.vtc.playerads;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerAd extends VideoView {

    /**
     * Interface for alerting caller of video completion.
     */
    public interface OnVideoCompletedListener {

        /**
         * Called when the current video has completed playback to the end of the video.
         */
        void onVideoCompleted();
    }

    private final List<OnVideoCompletedListener> mOnVideoCompletedListeners = new ArrayList<>(1);

    public VideoPlayerAd(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public VideoPlayerAd(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoPlayerAd(Context context) {
        super(context);
        init();
    }

    private void init() {
        MediaController mediaController = new MediaController(getContext());
        mediaController.setAnchorView(this);

        // Set OnCompletionListener to notify our listeners when the video is completed.
        super.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Reset the MediaPlayer.
                // This prevents a race condition which occasionally results in the media
                // player crashing when switching between videos.
                mediaPlayer.reset();
                mediaPlayer.setDisplay(getHolder());

                for (OnVideoCompletedListener listener : mOnVideoCompletedListeners) {
                    listener.onVideoCompleted();
                }
            }
        });

        // Set OnErrorListener to notify our listeners if the video errors.
        super.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                // Returning true signals to MediaPlayer that we handled the error. This will
                // prevent the completion handler from being called.
                return true;
            }
        });
    }

    @Override
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        // The OnCompletionListener can only be implemented by SampleVideoPlayer.
        throw new UnsupportedOperationException();
    }

    public void play() {
        start();
    }

    public void addVideoCompletedListener(OnVideoCompletedListener listener) {
        mOnVideoCompletedListeners.add(listener);
    }
}
