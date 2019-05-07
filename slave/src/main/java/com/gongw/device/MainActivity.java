package com.gongw.device;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codyy.robinsdk.RBPlayAndroid;
import com.codyy.robinsdk.impl.RBManagerAndroid;
import com.gongw.device.databinding.ActivityMainBinding;
import com.gongw.remote.communication.CommunicationKey;
import com.gongw.remote.communication.slave.CommandReceiver;
import com.gongw.remote.search.DeviceSearchResponser;

import static com.codyy.robinsdk.impl.RBManagerAndroid.RB_MEDIA_ALL;
import static com.codyy.robinsdk.impl.RBManagerAndroid.RB_RENDER_ASPECT_RATIO;
import static com.codyy.robinsdk.impl.RBManagerAndroid.RB_RENDER_FULL_SCREEN;

/**
 * Created by gongw on 2018/10/16.
 */

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Main";
    /**
     * 是否开启了搜索响应和通信响应
     */
    private boolean isOpen;
    ActivityMainBinding binding;
    private RBManagerAndroid mManager = null;
    private RBPlayAndroid mPlayer1 = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.btnOpenResponser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    //停止响应搜索
                    DeviceSearchResponser.close();
                    isOpen = false;
                    binding.btnOpenResponser.setText("打开应答");
                    //停止接收通信命令
                    CommandReceiver.close();
                    Toast.makeText(MainActivity.this, "已经关闭响应程序！", Toast.LENGTH_SHORT).show();
                } else {
                    //开始响应搜索
                    DeviceSearchResponser.open();
                    isOpen = true;
                    binding.btnOpenResponser.setText("关闭应答");
                    //开始接受通信命令
                    CommandReceiver.open(new CommandReceiver.CommandListener() {
                        @Override
                        public String onReceive(final String command) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (command.startsWith("http")) {
                                        binding.videoView.setVisibility(View.VISIBLE);
                                        binding.surfaceView.setVisibility(View.GONE);
                                        if (binding.videoView.isPlaying()) {
                                            binding.videoView.stopPlayback();
                                        }
                                        binding.videoView.setVideoPath(command);
                                        binding.videoView.start();
                                    } else if (command.startsWith("rtmp://")) {
                                        binding.videoView.setVisibility(View.GONE);
                                        binding.surfaceView.setVisibility(View.VISIBLE);
//                                        binding.gsyPlayer.setUp(command,true,command);
//                                        binding.gsyPlayer.startPlayLogic();
                                        mPlayer1.setUri(command);
                                        mPlayer1.start();
                                    }
                                    Toast.makeText(MainActivity.this, "Receive:" + command, Toast.LENGTH_SHORT).show();
                                }
                            });
                            return CommunicationKey.RESPONSE_OK +
                                    "I am OK!" +
                                    CommunicationKey.EOF;
                        }
                    });
                    Toast.makeText(MainActivity.this, "已经打开响应程序！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.setIsOpen(isOpen);
        binding.executePendingBindings();
        binding.videoView.setVisibility(View.GONE);
        initRobin();
    }


    private void initRobin() {
        mManager = RBManagerAndroid.getSingleton();
        mManager.enableLog();
        mManager.init();
        mPlayer1 = mManager.createPlayer();
        mPlayer1.init();
        mPlayer1.setVideoHardwareDec(true);
        mPlayer1.setVideoRenderMode(RB_RENDER_ASPECT_RATIO);
        mPlayer1.setMediaMode(RB_MEDIA_ALL, binding.surfaceView);
//        mPlayer1.setUri("rtmp://localhost:1935/live/screen");
//        mPlayer1.setUri("rtmp://10.5.31.218:1935/dms/lijian");
//        mPlayer1.setUri("rtmp://10.5.223.100:1935/live/screen");
//        mPlayer1.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer(mManager, mPlayer1);
    }

    @Override
    public void onBackPressed() {
        //释放所有
        super.onBackPressed();
    }

    private void releasePlayer(RBManagerAndroid manager, RBPlayAndroid player) {
        try {
            if (manager != null && player != null) {
                player.stop();
                while (player.release() != 0) {
                    Thread.sleep(10);
                }
                manager.deletePlayer(player);
            }
        } catch (InterruptedException ex) {
            Log.e(TAG, "releasePlayer: Interrupted Exception is " + ex);
        }
    }
}
