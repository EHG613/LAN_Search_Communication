package com.gongw.mobile;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.gongw.mobile.databinding.ActivityMainBinding;
import com.gongw.remote.Device;
import com.gongw.remote.communication.host.Command;
import com.gongw.remote.communication.host.CommandSender;
import com.gongw.remote.search.DeviceSearcher;
import com.lijian.screencast.ScreencastService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gongw on 2018/10/16.
 */

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private List<Device> deviceList = new ArrayList<>();
    private SimpleAdapter<Device> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        Intent screen = new Intent(this, ScreencastService.class);
        bindService(screen, mScreenMirror, Context.BIND_AUTO_CREATE);
        //开始搜索局域网中的设备
        startSearch();
    }

    ScreencastService.ScreenBinder msScreenBinder;
    /**
     * 投屏连接
     */
    private ServiceConnection mScreenMirror = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            msScreenBinder = (ScreencastService.ScreenBinder) service;
            msScreenBinder.setmOnScreen(new ScreencastService.OnScreen() {


                @Override
                public void connectSuccess(MediaProjectionManager mediaProjectionManager) {
                    startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),
                            PERMISSION_CODE);
                }

                @Override
                public void onStart() {
                }

                @Override
                public void onStop() {
//                    ToastUtil.show(MainActivity.this, "投屏结束");
                }

                @Override
                public void onError() {

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    public final static int PERMISSION_CODE = 0x0a1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PERMISSION_CODE) {
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "User denied screen sharing permission", Toast.LENGTH_SHORT).show();
            return;
        }
        msScreenBinder.startScreen(resultCode, data);
        if (deviceList.size() > 0) {
            sendCommand(deviceList.get(0), ScreencastService.mStream);
//            sendCommand(deviceList.get(0), "rtmp://10.5.223.100:1935/live/screen");
        }
    }


    private void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        adapter = new SimpleAdapter<Device>(deviceList, R.layout.item_device, BR.device) {
            @Override
            public void addListener(View root, final Device itemData, int position) {
                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //点击列表项时发送命令
                        sendCommand(itemData, " Are you OK!");
                    }
                });
            }
        };
        binding.rvDevices.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.rvDevices.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.VERTICAL));
        binding.rvDevices.setAdapter(adapter);
        binding.srlRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startSearch();
            }
        });
    }

    /**
     * 开始异步搜索局域网中的设备
     */
    private void startSearch() {
        DeviceSearcher.search(new DeviceSearcher.OnSearchListener() {
            @Override
            public void onSearchStart() {
                binding.srlRefreshLayout.setRefreshing(true);
                deviceList.clear();
            }

            @Override
            public void onSearchedNewOne(Device device) {
                binding.srlRefreshLayout.setRefreshing(false);
                deviceList.add(device);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onSearchFinish() {
                binding.srlRefreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void sendCommand(Device device, final String text) {
        //发送命令，命令内容为"hello!"
        Command command = new Command(text, new Command.Callback() {
            @Override
            public void onRequest(String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Request:" + text, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSuccess(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Success:" + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Error:" + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onEcho(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Echo：" + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        command.setDestIp(device.getIp());
        CommandSender.addCommand(command);
    }

    public void res1(View view) {
        if (deviceList.size() > 0) {
            sendCommand(deviceList.get(0), "http://men-res.codyy.cn/a.mp4");
        }
    }

    public void res2(View view) {
        if (deviceList.size() > 0) {
            sendCommand(deviceList.get(0), "rtmp://10.5.31.218:1935/dms/lijian");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mScreenMirror);
    }

    public void res3(View view) {
        if (msScreenBinder != null)
            msScreenBinder.connect();
    }

    public void res4(View view) {
        if (msScreenBinder != null)
            msScreenBinder.stopScreen();
    }
}
