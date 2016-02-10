package com.nd.android.sdp.common.photoviewpager;

import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.nd.android.sdp.common.photoviewpager.downloader.ExtraDownloader;
import com.nd.android.sdp.common.photoviewpager.downloader.PhotoViewConfirmDownloadCallback;
import com.nd.android.sdp.common.photoviewpager.downloader.PhotoViewDownloaderCallback;
import com.nd.android.sdp.common.photoviewpager.pojo.Info;
import com.nd.android.sdp.common.photoviewpager.pojo.VideoInfo;
import com.nd.android.sdp.common.photoviewpager.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 视频页面
 */
public class VideoPagerFragment extends BasePagerFragment {

    private ViewStub mVideoStub;
    private TextureView mVideoView;
    private View mBtnPlay;
    private MediaPlayer mMediaPlayer;
    private ExtraDownloader mExtraDownloader;
    private Subscription mDownloadFullVideoSubscription;
    private Surface mSurface;
    private FrameLayout mFlVideo;
    private VideoInfo mVideoInfo;

    @Override
    protected File getShowFileCache() {
        return mConfiguration.getPicDiskCache(mInfo.getUrl());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photo_viewpager_fragment_video_page, container, false);
    }

    @Override
    protected void findView(View view) {
        super.findView(view);
        mVideoStub = ((ViewStub) view.findViewById(R.id.vstubVideo));
    }

    @Override
    protected void loadFileCache(File fileCache, boolean needAnimate) {
        super.loadFileCache(fileCache, needAnimate);
        initVideo();
        mBtnPlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        if (mSurface != null) {
            mSurface.release();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mExtraDownloader != null) {
            mExtraDownloader.cancelCallBack(mInfo.getOrigUrl());
        }
        if (mDownloadFullVideoSubscription != null) {
            mDownloadFullVideoSubscription.unsubscribe();
        }
    }

    @Nullable
    @Override
    protected ViewPropertyAnimator fadeOutContentViewAnimate() {
        return mFlVideo.animate();
    }

    @Override
    protected boolean animateFinish() {
        return false;
    }

    private void initVideo() {
        mFlVideo = (FrameLayout) mVideoStub.inflate();
        mVideoView = ((TextureView) mFlVideo.findViewById(R.id.vd));
        mBtnPlay = mFlVideo.findViewById(R.id.btnPlay);
        mBtnPlay.setOnClickListener(mVideoPlayClickListener);
        mVideoView.setFocusable(false);
        mVideoView.setFocusableInTouchMode(false);
        mView.requestFocus();
        mVideoView.setVisibility(View.GONE);
    }

    private void initMediaPlayer(final File picDiskCache) {
        mPb.setVisibility(View.GONE);
        mIvReal.setVisibility(View.GONE);
        mIvTemp.setVisibility(View.GONE);
        mIvPreview.setVisibility(View.GONE);
        mVideoView.setVisibility(View.VISIBLE);
        mVideoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                try {
                    boolean needPlay = true;
                    if (mSurface != null) {
                        mSurface.release();
                    }
                    mSurface = new Surface(surface);
                    if (mMediaPlayer != null) {
                        mMediaPlayer.release();
                        needPlay = false;
                    }
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setDataSource(getContext(), Uri.fromFile(picDiskCache));
                    mMediaPlayer.setSurface(mSurface);
                    mMediaPlayer.prepare();
                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int videoWidth, int videoHeight) {
                            double aspectRatio = (double) videoHeight / videoWidth;
                            int newWidth, newHeight;
                            final int viewHeight = mView.getHeight();
                            final int viewWidth = mView.getWidth();
                            if (viewHeight > (int) (viewWidth * aspectRatio)) {
                                // limited by narrow width; restrict height
                                newWidth = viewWidth;
                                newHeight = (int) (viewWidth * aspectRatio);
                            } else {
                                // limited by short height; restrict width
                                newWidth = (int) (viewHeight / aspectRatio);
                                newHeight = viewHeight;
                            }
                            int xoff = (viewWidth - newWidth) / 2;
                            int yoff = (viewHeight - newHeight) / 2;
                            Matrix txform = new Matrix();
                            txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
                            txform.postTranslate(xoff, yoff);
                            mVideoView.setTransform(txform);
                            mVideoView.setOnClickListener(mFinishClickListener);
                        }
                    });
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mBtnPlay.setVisibility(View.VISIBLE);
                        }
                    });
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    if (needPlay) {
                        mMediaPlayer.start();
                    } else {
                        mMediaPlayer.seekTo(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    public void setExtraDownloader(ExtraDownloader extraDownloader) {
        mExtraDownloader = extraDownloader;
    }

    private View.OnClickListener mVideoPlayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final File diskCache = mConfiguration.getPicDiskCache(mVideoInfo.videoUrl);
            if (!diskCache.exists()) {
                if (mExtraDownloader != null) {
                    mExtraDownloader.confirmDownload(new PhotoViewConfirmDownloadCallback() {
                        @Override
                        public void confirm() {
                            if (initStartDownload()) return;
                            v.setVisibility(View.GONE);
                            final Observable<Integer> download = downloadByExtraDownloader(mExtraDownloader, mVideoInfo.videoUrl, diskCache);
                            downloadFullVideo(download, diskCache);
                        }

                        @Override
                        public void dismiss() {

                        }
                    });
                } else {
                    if (initStartDownload()) return;
                    v.setVisibility(View.GONE);
                    assert mInfo.getOrigUrl() != null;
                    final Observable<Integer> download = Utils.download(getActivity(), mInfo.getOrigUrl(), diskCache);
                    downloadFullVideo(download, diskCache);
                }
            } else {
                v.setVisibility(View.GONE);
                if (mMediaPlayer == null) {
                    // 初始化
                    initMediaPlayer(diskCache);
                } else {
                    // 已经初始化
                    if (!mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                    }
                }
            }
        }
    };

    private void downloadFullVideo(Observable<Integer> observable, final File diskCache) {
        mDownloadFullVideoSubscription = observable
                .throttleLast(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer progress) {
                        if (progress > 0 && mPb.isIndeterminate()) {
                            mPb.setIndeterminate(false);
                        }
                        mPb.setProgress(progress);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        mIvPreview.setVisibility(View.GONE);
                        mIvReal.setVisibility(View.GONE);
                        mPb.setVisibility(View.GONE);
                        mTvError.setVisibility(View.VISIBLE);
                        mTvError.setOnClickListener(mFinishClickListener);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        initMediaPlayer(diskCache);
                    }
                });
    }

    @Override
    public void onParentScroll() {
        if (mMediaPlayer != null
                && mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(0);
            mMediaPlayer.pause();
            mBtnPlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayer != null
                && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mBtnPlay.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    private Observable<Integer> downloadByExtraDownloader(final ExtraDownloader extraDownloader,
                                                          @NonNull final String url,
                                                          @NonNull final File file) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        extraDownloader.startDownload(url, file,
                                mVideoInfo.videoUrl,
                                new PhotoViewDownloaderCallback() {
                                    @Override
                                    public void updateProgress(String url, long current, long total) {
                                        if (total > 0) {
                                            final int progress = (int) ((current * 100) / total);
                                            subscriber.onNext(progress);
                                        } else {
                                            subscriber.onNext(0);
                                        }
                                    }

                                    @Override
                                    public void cancelDownload(String url) {
                                    }

                                    @Override
                                    public void onComplete(String url) {
                                        subscriber.onCompleted();
                                    }

                                    @Override
                                    public void onError(String url, int httpCode) {
                                        subscriber.onError(new IOException());
                                    }

                                    @Override
                                    public void onPause(String url) {

                                    }

                                    @Override
                                    public void onCancel(String url) {

                                    }
                                });
                    }
                });
            }
        });
    }

    @Override
    protected void afterGetImg() {
        initVideo();
    }

    /**
     * New instance base pager fragment.
     *
     * @param arguments the arguments
     * @return the base pager fragment
     */
    public static VideoPagerFragment newVideoInstance(Bundle arguments) {
        VideoPagerFragment viewPagerFragment = new VideoPagerFragment();
        if (arguments == null) {
            arguments = new Bundle();
        }
        viewPagerFragment.setArguments(arguments);
        return viewPagerFragment;
    }

    protected boolean initStartDownload() {
        mPb.setVisibility(View.VISIBLE);
        mIvReal.setVisibility(View.GONE);
        mIvTemp.setVisibility(View.GONE);
        ((RelativeLayout.LayoutParams) mFlPreview.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT, 1);
        mFlPreview.requestLayout();
        mIvPreview.setDrawableRadius(mFrameSize / 2);
        mIvPreview.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public void setInfo(Info picInfo) {
        super.setInfo(picInfo);
        mVideoInfo = (VideoInfo) picInfo;
    }
}
