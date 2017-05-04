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
 *    This document is Bruce's individual learning the android demo, wherein the use of the code from the Internet, only to use as a learning exchanges.
 *    And where any person can download and use, but not for commercial purposes.
 *    Author does not assume the resulting corresponding disputes.
 *    If you have good suggestions for the code, you can contact BurrceHurrican@foxmail.com
 *    本文件为Bruce's个人学习android的作品, 其中所用到的代码来源于互联网，仅作为学习交流使用。
 *    任和何人可以下载并使用, 但是不能用于商业用途。
 *    作者不承担由此带来的相应纠纷。
 *    如果对本代码有好的建议，可以联系BurrceHurrican@foxmail.com
 */

package bruce.kk.wdemo.utils;

import android.content.Context;
import android.text.TextUtils;

import com.bruceutils.utils.logdetails.LogDetails;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import bruce.kk.wdemo.model.WifiInfo2;

public class NetWorkUtils {
    public static List<WifiInfo2> read() {
        List<WifiInfo2> wifiInfos = new ArrayList<WifiInfo2>();
        Process process = null;
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
//        StringBuffer wifiConf = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataInputStream = new DataInputStream(process.getInputStream());
            dataOutputStream.writeBytes("cat /data/misc/wifi/*.conf\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            String pwd = null;
            while ((line = bufferedReader.readLine()) != null) {
//                wifiConf.append(line);
                WifiInfo2 wifiInfo = new WifiInfo2();
                if (line.trim().startsWith("ssid=")) {
                    LogDetails.d("ssid: " + line.trim().replace("ssid=",""));
                    wifiInfo.ssid = line.trim().replace("ssid=","");
                    if (wifiInfo.ssid.contains("\"")) {
                        wifiInfo.ssid = wifiInfo.ssid.replace("\"","");
                    }
                    ssid: while ((pwd = bufferedReader.readLine()) != null) {
                        if (pwd.trim().endsWith("}")) {
                            break ssid;
                        }
                        if (pwd.trim().startsWith("psk=")) {
                            LogDetails.d("psk: " + pwd.trim().replace("psk=",""));
                            wifiInfo.password = pwd.trim().replace("psk=","");
                            if (wifiInfo.password.contains("\"")) {
                                wifiInfo.password = wifiInfo.password.replace("\"","");
                            }
                            break ssid;
                        }
                    }
                }
                if (!TextUtils.isEmpty(wifiInfo.ssid)) {
                    wifiInfos.add(wifiInfo);
                }
            }
            bufferedReader.close();
            inputStreamReader.close();
            process.waitFor();
        } catch (Exception e) {
            LogDetails.e(e);
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                LogDetails.e(e);
            }
        }


//        Pattern network = Pattern.compile("network=\\{([^\\}]+)\\}", Pattern.DOTALL);
//        Matcher networkMatcher = network.matcher(wifiConf.toString());
//        while (networkMatcher.find()) {
//            String networkBlock = networkMatcher.group();
//            Pattern ssid = Pattern.compile("ssid=\"([^\"]+)\"");
//            Matcher ssidMatcher = ssid.matcher(networkBlock);
//
//            if (ssidMatcher.find()) {
//                WifiInfo2 wifiInfo = new WifiInfo2();
//                wifiInfo.ssid = ssidMatcher.group(1);
//                Pattern psk = Pattern.compile("psk=\"([^\"]+)\"");
//                Matcher pskMatcher = psk.matcher(networkBlock);
//                if (pskMatcher.find()) {
//                    wifiInfo.password = pskMatcher.group(1);
//                } else {
//                    wifiInfo.password = "无密码";
//                }
//                LogDetails.d(wifiInfo);
//                wifiInfos.add(wifiInfo);
//            }
//
//        }

        return wifiInfos;
    }

    public static List<WifiInfo2> testRead(Context context) {
        List<WifiInfo2> wifiInfos = new ArrayList<WifiInfo2>();
        BufferedReader bufferedReader = null;
        InputStream inputStream = null;
        try {
//            File file1 = Environment.getExternalStorageDirectory();
//            bufferedReader = new BufferedReader(new FileReader(file1.getPath() + "/test1.txt"));
            inputStream = context.getAssets().open("test.txt");
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            String pwd = null;
            while ((line = bufferedReader.readLine()) != null) {
                WifiInfo2 wifiInfo = new WifiInfo2();
                if (line.trim().startsWith("ssid=")) {
                    LogDetails.d("ssid: " + line.trim().replace("ssid=",""));
                    wifiInfo.ssid = line.trim().replace("ssid=","");
                    if (wifiInfo.ssid.contains("\"")) {
                        wifiInfo.ssid = wifiInfo.ssid.replace("\"","");
                    }
                    ssid: while ((pwd = bufferedReader.readLine()) != null) {
                        if (pwd.trim().endsWith("}")) {
                            break ssid;
                        }
                        if (pwd.trim().startsWith("psk=")) {
                            LogDetails.d("psk: " + pwd.trim().replace("psk=",""));
                            wifiInfo.password = pwd.trim().replace("psk=","");
                            if (wifiInfo.password.contains("\"")) {
                                wifiInfo.password = wifiInfo.password.replace("\"","");
                            }
                            break ssid;
                        }
                    }
                }
                if (!TextUtils.isEmpty(wifiInfo.ssid)) {
                    wifiInfos.add(wifiInfo);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return wifiInfos;
    }
}
