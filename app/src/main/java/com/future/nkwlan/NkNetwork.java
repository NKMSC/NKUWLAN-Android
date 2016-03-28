package com.future.nkwlan;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;

/**
 * Created by future on 2016/3/11 0011.
 */

public class NkNetwork {

    private static String[] hosts = {"http://202.113.18.110", "http://202.113.18.210"};
    private static String login_path = ":801/eportal/?c=ACSetting&a=Login";
    private static String logout_path = ":801/eportal/?c=ACSetting&a=Logout";
    private static OkHttpClient client = new OkHttpClient();

    /**
     * 获取NKU校园网登陆状态
     *
     * @return null 当前非校园网
     * NetworkInfo，uid为空，未登录
     * NetworkInfo，uid不为空，uid为当前登陆用户
     */
    public static NetworkInfo getLogStatus() {

        for (String host : hosts) {
            final Request request = new Request.Builder()
                    .url(host).build();
            Call call = client.newCall(request);

            try {
                String result = call.execute().body().string();

                int tmp = result.indexOf("uid=");//没有UID说明没登陆

                String uid = find(result, "uid='");
                if (uid == null) continue;
                String flow = find(result, "flow='");
                String fee = find(result, "fee='");
                String time = find(result, "time='");

                Log.e("wlan", String.format("%s,%s,%s,%s", uid, flow, time, fee));
                return new NetworkInfo(uid, fee, flow, time);

            } catch (IOException e) {
                e.printStackTrace();
                return new NetworkInfo(NetworkInfo.UNCONNECTED); //打不开这个网页说明没连内网
            }
        }
        return new NetworkInfo(NetworkInfo.UN_LOGIN);
    }


    public static NetworkInfo login(String uid, String psw) {
        NetworkInfo tmp = getLogStatus();
//        Log.e("wlan", String.format("status check before login:%s,%s,%s",tmp.status,uid,psw));
        if (tmp.status != NetworkInfo.UN_LOGIN)
            return tmp;
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded"),
                "DDDDD=" + uid + "&upass=" + psw
        );

        Request request = new Request.Builder().post(requestBody).url(hosts[0] + login_path).build();
        try {
            String result = client.newCall(request).execute().body().string();
            Log.e("wlan", result);
        } catch (IOException e) {
            Log.e("wlan", e.toString());
            e.printStackTrace();
        }
        return getLogStatus();
    }

    public static boolean checkInternet() {
        final Request request = new Request.Builder()
                .url("http://token.futureer.net/hello.html").build();
        Call call = client.newCall(request);
        String result;
        try {
            result = call.execute().body().string();//正确内容为 hello future
            Log.e("wlan", result);
        } catch (IOException e) {
            result = "f**k";
            e.printStackTrace();
        }
        return result.length() > 5;
    }

    public static NetworkInfo logout() {
        NetworkInfo info = getLogStatus();
        if (info.status != NetworkInfo.ONLINE)
            return info;

        for (int i = 0; i < 3; i++) {
            Request request = new Request.Builder().url(hosts[0] + logout_path).build();
            try {
                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            NetworkInfo info1 = getLogStatus();
            if (info1.status == NetworkInfo.UN_LOGIN)
                return info1;
        }
        return info;
    }


    private static String find(String raw, String tag) {
        if (!raw.contains(tag)) return null;
        String raw1 = raw.substring(raw.indexOf(tag) + tag.length());
        String raw2 = raw1.substring(0, raw1.indexOf("'"));
        return raw2.trim();
    }

    public static boolean isWifi(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifiNetworkInfo =
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo.isConnected();

    }
}
