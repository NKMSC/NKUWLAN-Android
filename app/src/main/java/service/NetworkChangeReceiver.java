package service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.future.nkwlan.NkCache;
import com.future.nkwlan.NkNetwork;
import com.future.nkwlan.User;


/**
 * 接收网络状况变化的广播
 * Created by future on 2016/3/28 0028.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private String userId;
    private String pwd;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("wlan", "onReceive");
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo.isConnected()) {
            new UpdateStatus().execute();
        }
        User user = NkCache.getAccount(context);
        userId = user.uid;
        pwd = user.psw;
    }

    private class UpdateStatus extends AsyncTask<String, Long, com.future.nkwlan.NetworkInfo> {

        @Override
        protected com.future.nkwlan.NetworkInfo doInBackground(String... params) {

            return NkNetwork.getLogStatus();
        }

        @Override
        protected void onPostExecute(com.future.nkwlan.NetworkInfo info) {
            switch (info.status) {
                case com.future.nkwlan.NetworkInfo.UNCONNECTED:

                    Log.e("wlan", "Receiver：UNCONNECTED, nothing to do");
                    break;
                case com.future.nkwlan.NetworkInfo.UN_LOGIN://登录&保存
                    if (userId == null)
                        Log.e("wlan", "Receiver：UNLOGIN, and no user saved");
                    else {
                        Log.e("wlan", "Receiver：UNLOGIN, perfoming login" + userId + pwd);
                        new Login().execute(userId, pwd);
                    }
                    break;
                case com.future.nkwlan.NetworkInfo.ONLINE:
                    Log.e("wlan", "Receiver：online, nothing to do");
                    break;
            }
        }
    }

    private class Login extends AsyncTask<String, Long, com.future.nkwlan.NetworkInfo> {

        @Override
        protected com.future.nkwlan.NetworkInfo doInBackground(String... params) {

            return NkNetwork.login(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(com.future.nkwlan.NetworkInfo info) {

            switch (info.status) {
                case com.future.nkwlan.NetworkInfo.ONLINE:
                    Log.e("wlan", "Receiver.login: login success. notify user this good news!");
                    break;
                case com.future.nkwlan.NetworkInfo.UN_LOGIN:

                    Log.e("wlan", "Receiver.login: 登录失败，请确认账号密码正确，且余额充足");
                    break;
                case com.future.nkwlan.NetworkInfo.UNCONNECTED:
                    Log.e("wlan", "Receiver.login: 网络连接失败,弹一个通知让用户进APP内部操作");
                    break;
            }
        }
    }
}
