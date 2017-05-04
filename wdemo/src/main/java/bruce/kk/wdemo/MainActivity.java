/*
 * BruceHurrican
 * Copyright (c) 2016.
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    This document is Bruce's individual learning the android demo, wherein the use of the code from the Internet,
 *    only to use as a learning exchanges.
 *    And where any person can download and use, but not for commercial purposes.
 *    Author does not assume the resulting corresponding disputes.
 *    If you have good suggestions for the code, you can contact BurrceHurrican@foxmail.com
 *    本文件为Bruce's个人学习android的作品, 其中所用到的代码来源于互联网，仅作为学习交流使用。
 *    任和何人可以下载并使用, 但是不能用于商业用途。
 *    作者不承担由此带来的相应纠纷。
 *    如果对本代码有好的建议，可以联系BurrceHurrican@foxmail.com
 */

package bruce.kk.wdemo;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bruceutils.base.BaseActivity;
import com.bruceutils.utils.LogUtils;
import com.bruceutils.utils.ProgressDialogUtils;
import com.bruceutils.utils.PublicUtil;
import com.bruceutils.utils.logdetails.LogDetails;

import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import bruce.kk.wdemo.model.UploadResult;
import bruce.kk.wdemo.model.WFinfo;
import bruce.kk.wdemo.model.WFinfoUpload;
import bruce.kk.wdemo.model.WifiInfo2;
import bruce.kk.wdemo.net.IUploadData;
import bruce.kk.wdemo.utils.LocationUtils;
import bruce.kk.wdemo.utils.NetWorkUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    public static final int NOTIFICATION_ID = 101;
    private String[] permissions = {
            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
    };
    private static final int PERMISSION_CODE = 0;
    private List<WFinfo> dataList = new ArrayList<>(20);
    @Bind(R.id.tv_ssid_title)
    TextView tvSsidTitle;
    @Bind(R.id.tv_device)
    TextView tvDevice;
    @Bind(R.id.tv_uploaded_num)
    TextView tvUploadedNum;
    @Bind(R.id.btn_upload)
    Button btnUpload;
    @Bind(R.id.btn_test_data)
    Button btnTestData;
    @Bind(R.id.btn_test_clear)
    Button btnTestClear;
    @Bind(R.id.rv_container)
    RecyclerView rvContainer;
    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    private SSIDInfoAdapter adapter;
    private long exitFlag;
    private String deviceID;
    private List<WFinfoUpload.HostInfo> uploadDataList;
    private List<WifiInfo2> wifiList;
    private String hostMacAddress;

    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationClientOption;
    private NetWorkAvailableReceiver netWorkAvailableReceiver = new NetWorkAvailableReceiver();
    private NotificationManager notificationManager;
    private Notification notification;
    private WifiManager wifiManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        wifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(WIFI_SERVICE);

        checkPermissions(permissions);
        startLoc();
        regisNetWorkChanged();

        dataList = DataSupport.findAll(WFinfo.class);

        adapter = new SSIDInfoAdapter();
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvContainer.setLayoutManager(manager);
        rvContainer.setItemAnimator(new DefaultItemAnimator());
        LogDetails.d(dataList);

        adapter.setDataList(dataList);
        rvContainer.setAdapter(adapter);

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceID = telephonyManager.getDeviceId();
        tvDevice.setText("当前设备: " + deviceID);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LogDetails.d("刷新完毕");
                dataList = DataSupport.findAll(WFinfo.class);
                LogDetails.d(dataList);
                adapter.setDataList(dataList);
                adapter.notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
                int flag = 0;
                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).isUploaded) {
                        flag++;
                    }
                }
                tvUploadedNum.setText(String.format("总共%s,已经上传 %s",dataList.size(), flag));
            }
        });


        ProgressDialogUtils.initProgressBar(MainActivity.this, "数据操作中...", R.mipmap.ic_launcher);

        createNotification();
        int flag = 0;
        if (dataList.size() > 0) {
            for (int i = 0; i < dataList.size(); i++) {
                if (dataList.get(i).isUploaded) {
                    flag++;
                }
            }
        }
        tvUploadedNum.setText(String.format("总共%s,已经上传 %s", dataList.size(), flag));

        ProgressDialogUtils.showProgressDialog();
        tvUploadedNum.postDelayed(new Runnable() {
            @Override
            public void run() {
                repeatCount();
            }
        }, 1500);

        tvSsidTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                btnTestData.setVisibility(View.VISIBLE);
                btnTestClear.setVisibility(View.VISIBLE);
                return false;
            }
        });
    }

    /**
     * 数据去重逻辑
     */
    private void repeatCount() {
        if (locationClient != null) {
            wifiList = NetWorkUtils.read();
            LogDetails.d(dataList);
            LogDetails.i(wifiList);
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            hostMacAddress = wifiManager.getConnectionInfo().getBSSID();
            LogDetails.d("hostMacAddress: %s\nbssid: %s\nssid: %s", hostMacAddress, wifiManager.getConnectionInfo().getBSSID(), wifiManager.getConnectionInfo().getSSID());
            String flag = wifiManager.getConnectionInfo().getSSID().replace("\"","");
            List<WFinfo> tempList = new ArrayList<WFinfo>();
            for (int i = 0; i < wifiList.size(); i++) {
                WFinfo wFinfo = new WFinfo();
                wFinfo.ssid = wifiList.get(i).ssid;
//                LogDetails.i("ssid: %s, wifi list ssid: %s", wifiManager.getConnectionInfo().getSSID().replace("\"",""), wifiList.get(i).ssid);
//                LogDetails.i(flag.equals(wifiList.get(i).ssid));
                if (flag.equals(wifiList.get(i).ssid)) {
                    wFinfo.host_mac = hostMacAddress;
                }
                if (locationClient.getLastKnownLocation() != null) {
                    wFinfo.latitude = locationClient.getLastKnownLocation().getLatitude() + "";
                    wFinfo.longitude = locationClient.getLastKnownLocation().getLongitude() + "";
                }
                wFinfo.password = wifiList.get(i).password;
                tempList.add(wFinfo);
            }
            LogDetails.d(tempList);
            LogDetails.d(dataList);
            LogDetails.d(tempList.removeAll(dataList));
            for (int i = 0; i < tempList.size(); i++) {
                tempList.get(i).delete();
            }
            LogDetails.d(tempList);
            dataList.addAll(tempList);
//                    DataSupport.deleteAll(WFinfo.class,"");
            for (int i = 0; i < dataList.size(); i++) {
                if (flag.equals(dataList.get(i).ssid)) {
                    dataList.get(i).host_mac = hostMacAddress;
                }
                dataList.get(i).save();
            }
            LogDetails.d(dataList);
            adapter.notifyDataSetChanged();
            LogDetails.d("去重操作完成");
            ProgressDialogUtils.cancelProgressDialog();
        }
    }

    private void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
        builder.setContentTitle("app 运行中,请不要关闭")
               .setAutoCancel(false)
               .setSmallIcon(R.mipmap.ic_launcher)
               .setWhen(System.currentTimeMillis())
               .build();
        notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(101, notification);
    }

    private void regisNetWorkChanged() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkAvailableReceiver, filter);
    }

    private void startLoc() {
        locationClient = new AMapLocationClient(getApplicationContext());
        locationClientOption = new AMapLocationClientOption();
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
                            .setGpsFirst(false)
                            .setInterval(5000)
                            .setNeedAddress(true)
                            .setOnceLocation(false)
                            .setOnceLocationLatest(false);
        locationClientOption.setSensorEnable(true);
        locationClientOption.setHttpTimeOut(30000);
        locationClientOption.setNeedAddress(true);
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);
        locationClientOption.setWifiScan(true);
        locationClientOption.setLocationCacheEnable(true);
        locationClient.setLocationOption(locationClientOption);
        locationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (null != aMapLocation) {
                    String result = LocationUtils.getLocationStr(aMapLocation);
                    LogDetails.i(result);
                } else {
                    LogDetails.d("定位失败");
                }
            }
        });
        locationClient.startLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != locationClient) {
            locationClient.stopLocation();
        }
    }

    @Override
    protected void onDestroy() {
        notificationManager.cancel(NOTIFICATION_ID);
        super.onDestroy();
        if (null != locationClient) {
            locationClient.onDestroy();
            locationClient = null;
            locationClient = null;
        }
    }

    @OnClick({R.id.btn_upload, R.id.btn_test_data, R.id.btn_test_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_upload:
                LogDetails.d("上传数据");
                ProgressDialogUtils.showProgressDialog();
                WFinfoUpload wFinfoUpload = new WFinfoUpload();
                wFinfoUpload.client_id = deviceID;

                uploadDataList = new ArrayList<>();
                for (int i = 0; i < dataList.size(); i++) {
                    if (!dataList.get(i).isUploaded) {
                        WFinfoUpload.HostInfo hostInfo = new WFinfoUpload.HostInfo();
                        hostInfo.ssid = dataList.get(i).ssid;
                        hostInfo.password = dataList.get(i).password;
                        hostInfo.host_mac = dataList.get(i).host_mac;
                        hostInfo.latitude = dataList.get(i).latitude;
                        hostInfo.longitude = dataList.get(i).longitude;
                        uploadDataList.add(hostInfo);
                    }
                }
                wFinfoUpload.host_list = uploadDataList;
                try {
                    doRxUpload(wFinfoUpload.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_test_data:
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.disconnect();
                }
                localTest();
                Properties properties = new Properties();
                try {
                    properties.load(new InputStreamReader(getAssets().open("webconfig.txt")));
                    LogDetails.d(properties.getProperty("net"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_test_clear:
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.disconnect();
                }
                dataList.clear();
                wifiList.clear();
                adapter.notifyDataSetChanged();
                break;
        }
    }

    private void localTest() {
        LogDetails.d("just for test");
        wifiList.clear();
        dataList.clear();
        wifiList = NetWorkUtils.testRead(MainActivity.this);
        LogDetails.d(dataList);
        LogDetails.i(wifiList);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        hostMacAddress = wifiManager.getConnectionInfo().getBSSID();
        LogDetails.d("hostMacAddress: %s\nbssid: %s\nssid: %s", hostMacAddress, wifiManager.getConnectionInfo().getBSSID(), wifiManager.getConnectionInfo().getSSID());
        String flag = wifiManager.getConnectionInfo().getSSID().replace("\"","");
        List<WFinfo> tempList = new ArrayList<WFinfo>();
        for (int i = 0; i < wifiList.size(); i++) {
            WFinfo wFinfo = new WFinfo();
            wFinfo.ssid = wifiList.get(i).ssid;
            if (flag.equals(wifiList.get(i).ssid)) {
                wFinfo.host_mac = hostMacAddress;
            }
            wFinfo.password = wifiList.get(i).password;
            tempList.add(wFinfo);
        }
        LogDetails.d(tempList);
        LogDetails.d(dataList);
        LogDetails.d(tempList.removeAll(dataList));
        for (int i = 0; i < tempList.size(); i++) {
            tempList.get(i).delete();
        }
        LogDetails.d(tempList);
        dataList.addAll(tempList);
//        for (int i = 0; i < dataList.size(); i++) {
//            if (flag.equals(dataList.get(i).ssid)) {
//                dataList.get(i).host_mac = hostMacAddress;
//            }
//            dataList.get(i).save();
//        }
        LogDetails.d(dataList);
        adapter.notifyDataSetChanged();
        LogDetails.d("去重操作完成");
        ProgressDialogUtils.cancelProgressDialog();
    }

    private void doRxUpload(final String data) throws JSONException {
        String url = "";
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(getAssets().open("webconfig.txt")));
            url = properties.getProperty("net");
            LogDetails.d("请求 url: " + url);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String url = "http://10.180.184.52:8080";
//        String url = "http://10.180.184.52:8000";
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create())
                                                  .addCallAdapterFactory(RxJavaCallAdapterFactory.create()).client
                        (new OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()).build();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), data);
        LogDetails.d(body);
        IUploadData apiService = retrofit.create(IUploadData.class);
        apiService.upLoadData(body).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe
                (new Subscriber<UploadResult>() {
            @Override
            public void onCompleted() {
//                LogDetails.d("上传数据成功");
//                ProgressDialogUtils.cancelProgressDialog();
//                showToastShort("上传数据成功");
//                int flag = 0;
//                for (int i = 0; i < dataList.size(); i++) {
//                    if (dataList.get(i).isUploaded) {
//                        flag++;
//                    }
//                }
//                tvUploadedNum.setText(String.format("总共%s,已经上传 %s",dataList.size(), flag));
            }

            @Override
            public void onError(Throwable e) {
                LogDetails.d("上传数据失败");
                LogDetails.d(e);
                dataList = DataSupport.findAll(WFinfo.class);
                adapter.setDataList(dataList);
                adapter.notifyDataSetChanged();
                ProgressDialogUtils.cancelProgressDialog();
                showToastShort("上传数据失败:" + e);
            }

            @Override
            public void onNext(UploadResult result) {
                ProgressDialogUtils.cancelProgressDialog();
                LogDetails.d(result);
                if (result == null) {
                    LogDetails.d("服务器数据异常");
                    showToastShort("服务器数据异常result: " + result);
                    return;
                }
                if (200 != Integer.valueOf(result.code)) {
                    LogDetails.d("上传数据失败");
                    showToastShort(String.format("上传数据失败 code: %s,msg: %s", result.code, result.msg));
                    return;
                }
                LogDetails.d(uploadDataList);
                if (!TextUtils.isEmpty(result.saved) && Integer.valueOf(result.saved) > 0) {
                    for (int i = 0; i < uploadDataList.size(); i++) {
                        for (int j = 0; j < dataList.size(); j++) {
                            if (!TextUtils.isEmpty(uploadDataList.get(i).host_mac) && uploadDataList.get(i).host_mac.equals(dataList.get(j).host_mac)) {
                                dataList.get(j).isUploaded = true;
                                dataList.get(j).save();
                            }
                        }
                    }
                }
                if (!TextUtils.isEmpty(result.saved) && Integer.valueOf(result.saved) == 0) {
                    for (int i = 0; i < dataList.size(); i++) {
                        dataList.get(i).isUploaded = true;
                        dataList.get(i).save();
                    }
                }
                LogDetails.d(result.msg);
                showToastShort(result.msg);
                int flag = 0;
                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).isUploaded) {
                        flag++;
                    }
                }
                tvUploadedNum.setText(String.format("总共%s,已经上传 %s",dataList.size(), flag));
                adapter.setDataList(dataList);
                adapter.notifyDataSetChanged();
                showToastShort("操作成功");

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (!verifyPermissions(grantResults)) {
                showMissingPermissionDialog();
            }
        }
    }

    /**
     * 检测是否说有的权限都已经授权
     * @param grantResults
     * @return
     * @since 2.5.0
     *
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("程序正常使用需要相关功能");

        // 拒绝, 退出应用
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        builder.setPositiveButton("设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    /**
     *  启动应用的设置
     *
     * @since 2.5.0
     *
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void checkPermissions(String... permissions) {
        List<String> needRequestPermissonList = findDeniedPermissions(permissions);
        if (null != needRequestPermissonList
                && needRequestPermissonList.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    needRequestPermissonList.toArray(
                            new String[needRequestPermissonList.size()]),
                    PERMISSION_CODE);
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     *
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, perm)) {
                needRequestPermissonList.add(perm);
            }
        }
        return needRequestPermissonList;
    }

    @Override
    public void onBackPressed() {
        if (Math.abs(exitFlag - System.currentTimeMillis()) < 2000 && exitFlag > 0) {
            super.onBackPressed();
        } else {
            showToastShort("为了保证数据采集,请不要退出...");
            exitFlag = System.currentTimeMillis();
            return;
        }
        super.onBackPressed();
    }

    private class NetWorkAvailableReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.i("网络状态发生改变 ");
            if (PublicUtil.isNetWorkAvailable(MainActivity.this)) {
                LogUtils.d("当前设备已经联网");
                ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                boolean isWifiOK = cm.getNetworkInfo(1).isConnectedOrConnecting();
                LogUtils.i("isWifiOK -->" + isWifiOK);
                if (isWifiOK) {
                    repeatCount();
//                    wifiList = NetWorkUtils.read();
//                    if (wifiList.get(0) == null) {
//                        LogDetails.d("wifi list 首项为 null");
//                        return;
//                    }
//                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                    hostMacAddress = wifiManager.getConnectionInfo().getMacAddress();
//                    WFinfo wFinfo = new WFinfo();
//                    // 取新增第一项数据
//                    wFinfo.ssid = wifiList.get(0).ssid;
//                    wFinfo.host_mac = hostMacAddress;
//                    if (locationClient.getLastKnownLocation() != null) {
//                        wFinfo.latitude = locationClient.getLastKnownLocation().getLatitude() + "";
//                        wFinfo.longitude = locationClient.getLastKnownLocation().getLongitude() + "";
//                    }
//                    wFinfo.password = wifiList.get(0).password;
//                    boolean isRepeat = false;
//                    for (int i = 0; i < dataList.size(); i++) {
//                        isRepeat = dataList.get(i).ssid.equals(wifiList.get(0).ssid);
//                    }
//                    if (!isRepeat) {
//                        wFinfo.save();
//                        dataList.add(0, wFinfo);
//                        adapter.notifyDataSetChanged();
//                    } else {
//                        LogDetails.d("重复数据");
//                    }
                    LogDetails.d("保存 wifi 位置信息成功");
                }
            }
        }
    }
}
