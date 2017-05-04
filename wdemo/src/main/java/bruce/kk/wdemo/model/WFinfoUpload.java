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

package bruce.kk.wdemo.model;

import java.util.List;

/**
 * 数据上传模型
 * Created by BruceHurrican on 17/4/26.
 */

public class WFinfoUpload {
    /*
    {
    "client_id": 124,
    "host_list": [
        {
            "ssid": "海岸城",
            "password": "123456",
            "host_mac": "ABCDEFE",
            "latitude": "21.123",
            "longitude": "123.321"
        },
        {
            "ssid": "abc_free",
            "password": "123456",
            "host_mac": "ABCDEFE",
            "latitude": "21.123",
            "longitude": "123.321"
        }
    ]
}
    * */

    public String client_id;
    public List<HostInfo> host_list;

    public static class HostInfo {

        public String ssid;
        public String password;
        public String host_mac;
        public String latitude;
        public String longitude;

        @Override
        public String toString() {
            return "{" + "\"ssid\":\"" + ssid + "\"" + ", \"password\":\"" + password + "\"" + ", \"host_mac\":\"" +
                    host_mac + "\"" + ", \"latitude\":\"" + latitude + "\"" + ", \"longitude\":\"" + longitude + "\"" + "}";
        }
    }

    @Override
    public String toString() {
        return "{" + "\"client_id\":\"" + client_id + "\"" + ", \"host_list\":" + host_list + "}";
    }
}
