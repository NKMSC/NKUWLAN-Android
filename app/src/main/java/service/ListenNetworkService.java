package service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by future on 2016/3/28 0028.
 */
public class ListenNetworkService extends Service {
    private ConnectivityManager connectivityManager;
    private NetworkInfo info;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class ListenNetworkService1 extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d("wlan", "网络状态已经改变");
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    String name = info.getTypeName();
                    Log.d("wlan", "当前网络名称：" + name);
                } else {
                    Log.d("wlan", "没有可用网络");
                }
            }
        }
    }

    ListenNetworkService1 mReceiver;

    public void onCreate() {
        mReceiver = new ListenNetworkService1();
        super.onCreate();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, mFilter);
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }


}
