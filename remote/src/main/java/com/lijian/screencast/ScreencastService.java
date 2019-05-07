package com.lijian.screencast;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.codyy.robinsdk.RBPublishAndroid;
import com.codyy.robinsdk.impl.RBManagerAndroid;

import static com.codyy.robinsdk.impl.RBManagerAndroid.RB_MEDIA_ALL;

public class ScreencastService extends Service {
    public final static String TAG = "ScreencastService-----:";
    /**
     * 原始
     */
    public final static int QUALITY_NORMAL = 0x01;
    /**
     * 中等
     * 原始图像的0.75
     */
    public final static int QUALITY_MIDDLE = 0x02;
    /**
     * 低分辨率
     */
    public final static int QUALITY_LOW = 0x03;
    /**
     * 自定义
     */
    public final static int QUALITY_CUSTOMIZE = 0x04;
    /**
     * 地址搜索
     */
    private final static int FIND_IP = 0x001;
    /**
     * 停止
     */
    private final static int SCREEN_STOP = 0x002;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private RBPublishAndroid mRBPublishAndroid;
    /**
     * 屏幕宽
     */
    private int mScreenWidth = 1280;
    /**
     * 屏幕高
     */
    private int mScreenHeight = 720;
    private int mScreenDensity;
    /**
     * 帧率
     */
    private int mScreenFrameRate = 30;
    private boolean mScreen;
    private ScreenBinder mScreenBinder;
//    public static String mStream = "rtmp://10.5.31.218:1935/dms/screen";
    public static String mStream = "rtmp://10.5.223.25:1935/live/screen";
//    public static String mStream = "rtmp://localhost:1935/live/screen";

    public ScreencastService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(metrics);
        }
        mScreenDensity = metrics.densityDpi;
//        mScreenWidth = metrics.widthPixels;
//        mScreenHeight = metrics.heightPixels;
        mScreenBinder = new ScreenBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mScreenBinder;
    }

    // case SCREEN_STOP: {
//        Log.d(TAG, "SCREEN_STOP");
//        mScreen = false;
//        mRBPublishAndroid.release();
//        RBManagerAndroid.getSingleton().deletePublisher(mRBPublishAndroid);
//        RBManagerAndroid.getSingleton().release();
//        if (mScreenBinder != null && mScreenBinder.mOnScreen != null) {
//            mScreenBinder.mOnScreen.onStop();
//        }
//    }
    private void publishInit() {
        RBManagerAndroid.getSingleton().enableLog();
        RBManagerAndroid.getSingleton().init();
        mRBPublishAndroid = RBManagerAndroid.getSingleton().createPublisher();
        mRBPublishAndroid.init();
        mRBPublishAndroid.setPublisherListener(new RBPublishAndroid.PublisherListener() {
            @Override
            public void OnStateChange(int i) {
                if (RBManagerAndroid.RB_STATUS_STOP == i) {
                    Log.d("PlayerListener", "-------RB_STATUS_STOP");
//                    mHandler.sendEmptyMessage(SCREEN_STOP);
                }
            }

            @Override
            public void OnErrorGet(int i, String s) {

            }

            @Override
            public void OnNoticeGet(int i, String s) {

            }

            @Override
            public void OnMediaModeChange(int i) {

            }

            @Override
            public void OnVideoIdChange(int newVideoId) {

            }

        });
        mRBPublishAndroid.setUri(mStream);
        int videoDeviceNum = (int) mRBPublishAndroid.getVideoDeviceNum();
        for (int i = 0; i < videoDeviceNum; i++) {
            if ("ScreenCapture".equals(mRBPublishAndroid.getVideoDeviceDescribe(i))||"Screen Capture".equals(mRBPublishAndroid.getVideoDeviceDescribe(i))) {
                Log.d("----", "Screen Capture");
                mRBPublishAndroid.setVideoDevice(i, RBManagerAndroid.RB_LANDSCAPE_LEFT);
                break;
            }
        }
        int audioDeviceNum = (int) mRBPublishAndroid.getAudioDeviceNum();
        for (int i = 0; i < audioDeviceNum; i++) {
            if ("AppAudioPcm".equals(mRBPublishAndroid.getAudioDeviceDescribe(i))||"AppAudio".equals(mRBPublishAndroid.getAudioDeviceDescribe(i))) {
                Log.d("----", "AppAudio");
                mRBPublishAndroid.setAudioDevice(i);
                break;
            }
        }
        mRBPublishAndroid.setMediaProjection(mMediaProjection, 1);
        mRBPublishAndroid.setVideoResolution(1080, 1920);
//        mRBPublishAndroid.setVideoFrameRate(10);
        mRBPublishAndroid.setVideoHardwareEnc(true);
        mRBPublishAndroid.setVideoBitRate(2000);
        mRBPublishAndroid.setMediaMode(RB_MEDIA_ALL, null);
    }

    public class ScreenBinder extends Binder {
        OnScreen mOnScreen;

        public OnScreen getmOnScreen() {
            return mOnScreen;
        }

        public void setmOnScreen(OnScreen mOnScreen) {
            this.mOnScreen = mOnScreen;
        }

        public void startScreen(int resultCode, Intent data) {
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            mMediaProjection.registerCallback(new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    super.onStop();
                }
            }, null);
            publishInit();
            mRBPublishAndroid.start();
            if (mScreenBinder != null && mScreenBinder.mOnScreen != null) {
                mScreenBinder.mOnScreen.onStart();
            }
        }

        public void stopScreen() {
            if (mRBPublishAndroid != null) {
                mRBPublishAndroid.stop();
            }
            mScreen = false;
        }

        public void connect() {
            if (mScreen) {
                return;
            }
            mScreen = true;
            if (mScreenBinder.mOnScreen != null) {
                mScreenBinder.mOnScreen.connectSuccess(mProjectionManager);
            }
        }
    }

    public interface OnScreen {
        void connectSuccess(MediaProjectionManager mediaProjection);

        void onStart();

        void onStop();

        void onError();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePublisher(RBManagerAndroid.getSingleton(), mRBPublishAndroid);
    }

    private void releasePublisher(RBManagerAndroid manager, RBPublishAndroid publisher) {
        try {
            if (manager != null && publisher != null) {
                publisher.stop();
                while (publisher.release() != 0) {
                    Thread.sleep(10);
                }
                manager.deletePublisher(publisher);
            }
        } catch (InterruptedException ex) {
            Log.e(TAG, "releasePublisher: Interrupted Exception is " + ex);
        }
    }
}
