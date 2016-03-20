package com.future.nkwlan;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private int status;
    private ImageView wifiImg;
    private Button loginBtn;
    private TextView messageTxt, uidTxt, ssidTxt, isNkuTxt, loginInfoTxt;
    private String ssid = "to be getting";
    private String isNKU = "是";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        User user = NkCache.getAccount(this);
        if (user.uid.length() == 0) {
            startActivity(new Intent(this, AccountActivity.class));
            return;
        }

        messageTxt = (TextView) findViewById(R.id.main_message_txt);
        uidTxt = (TextView) findViewById(R.id.main_uid_txt);
        loginInfoTxt = (TextView) findViewById(R.id.main_login_info_txt);
        ssidTxt = (TextView) findViewById(R.id.main_ssid_txt);
        isNkuTxt = (TextView) findViewById(R.id.main_isnku_txt);

        wifiImg = (ImageView) findViewById(R.id.main_wifi_image);
        loginBtn = (Button) findViewById(R.id.main_login_btn);
        loginBtn.setOnClickListener(this);
//        wifiImg.setOnClickListener(this);
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        ssid = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
        new UpdateStatus().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_login_btn:
                User user = NkCache.getAccount(this);
                if (status == NetworkInfo.UN_LOGIN) {
                    Toast.makeText(getApplicationContext(), "正在登录...",
                            Toast.LENGTH_LONG).show();
                    new Login().execute(user.uid, user.psw);
                }
                if (status == NetworkInfo.ONLINE) {
                    Toast.makeText(getApplicationContext(), "正在注销...",
                            Toast.LENGTH_LONG).show();
                    new Logout().execute();
                }

                break;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_user:
                if (NkCache.getAccount(this).uid.isEmpty())
                    startActivity(new Intent(getApplicationContext(), AccountActivity.class));
                else
                    currentAccountDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void currentAccountDialog() {
//        Toast.makeText(this, "显示当前账户", Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("提示")
                .setMessage(Html.fromHtml(
                        String.format(getString(R.string.main_logout_or_not),
                                NkCache.getAccount(this).uid))).setNegativeButton("切换账号", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NkCache.setAccount(getApplicationContext(),"","",false);
                        startActivity(new Intent(getApplicationContext(), AccountActivity.class));
                    }
                }).setPositiveButton("不更换",null);
        builder.show();
    }

    private void loginInfoDialog(){
        View v = getLayoutInflater().inflate(R.layout.dialog_main_login_info,null);
        uidTxt = (TextView) v.findViewById(R.id.main_uid_txt);
        loginInfoTxt = (TextView) v.findViewById(R.id.main_login_info_txt);
        ssidTxt = (TextView) v.findViewById(R.id.main_ssid_txt);
        isNkuTxt = (TextView) v.findViewById(R.id.main_isnku_txt);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(v).setPositiveButton("好的",null);

    }

    private class UpdateStatus extends AsyncTask<String, Long, NetworkInfo> {

        @Override
        protected NetworkInfo doInBackground(String... params) {
            return NkNetwork.getLogStatus();
        }

        @Override
        protected void onPostExecute(NetworkInfo info) {
            updateUI(info);
            switch (info.status) {
                case NetworkInfo.UNCONNECTED:
                    Toast.makeText(getApplicationContext(), "Not in NKU network", Toast.LENGTH_SHORT).show();
                    break;
                case NetworkInfo.UN_LOGIN://登录&保存
                    break;
                case NetworkInfo.ONLINE:
                    break;
            }
        }
    }


    private void updateUI(NetworkInfo info) {
        status = info.status;
        isNKU = (info.status == NetworkInfo.UNCONNECTED) ? "否" : "是";

        switch (info.status) {
            case NetworkInfo.ONLINE:
                wifiImg.setImageResource(R.drawable.wifi_online);
                ssidTxt.setVisibility(View.GONE);
                isNkuTxt.setVisibility(View.GONE);
                loginInfoTxt.setVisibility(View.VISIBLE);
                uidTxt.setVisibility(View.VISIBLE);

                uidTxt.setText(Html.fromHtml(
                        String.format(
                                getResources().getString(R.string.main_user_info),
                                ssid, "是", info.uid
                        )));
                loginInfoTxt.setText(Html.fromHtml(String.format(
                        getResources().getString(R.string.main_network_info),
                        info.time, info.fee, info.flow)
                ));
                loginBtn.setText("注销");
                break;
            case NetworkInfo.UN_LOGIN:
                wifiImg.setImageResource(R.drawable.wifi_offline);
                loginInfoTxt.setVisibility(View.GONE);
                uidTxt.setVisibility(View.GONE);
                ssidTxt.setVisibility(View.VISIBLE);
                isNkuTxt.setVisibility(View.VISIBLE);

                ssidTxt.setText(Html.fromHtml(String.format(
                        getResources().getString(R.string.main_ssid),
                        ssid)
                ));
                isNkuTxt.setText(Html.fromHtml(String.format(
                        getResources().getString(R.string.main_isnku),
                        isNKU)
                ));
                loginBtn.setText("登录");
                break;
            case NetworkInfo.UNCONNECTED:
                uidTxt.setVisibility(View.GONE);
                ssidTxt.setVisibility(View.GONE);
                loginInfoTxt.setVisibility(View.GONE);
                loginBtn.setText("无法登录");
                isNkuTxt.setText("不是NKU_WLAN环境");
                wifiImg.setImageResource(R.drawable.wifi_disconnected);
                break;
        }
    }

    private class Login extends AsyncTask<String, Long, NetworkInfo> {

        @Override
        protected NetworkInfo doInBackground(String... params) {

            return NkNetwork.login(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(NetworkInfo info) {
            updateUI(info);
            switch (status) {
                case NetworkInfo.ONLINE:
                    Toast.makeText(getApplicationContext(), info.uid + "登录成功", Toast.LENGTH_SHORT).show();
                    break;
                case NetworkInfo.UN_LOGIN:
                    Toast.makeText(getApplicationContext(),
                            "登录失败，请确认账号密码正确，且余额充足", Toast.LENGTH_SHORT).show();
                    NkCache.setAccount(getApplicationContext(), "", "", false);
                    startActivity(new Intent(getApplicationContext(), AccountActivity.class));
                    break;
                case NetworkInfo.UNCONNECTED:
                    Toast.makeText(getApplicationContext(),
                            "登录失败，请稍后再试", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private class Logout extends AsyncTask<Void, Long, NetworkInfo> {

        @Override
        protected NetworkInfo doInBackground(Void... params) {
            return NkNetwork.logout();
        }

        @Override
        protected void onPostExecute(NetworkInfo info) {
            updateUI(info);
            switch (info.status) {
                case NetworkInfo.ONLINE:
                    Toast.makeText(getApplicationContext(),
                            "注销失败，请稍后再试", Toast.LENGTH_SHORT).show();
                    break;
                case NetworkInfo.UN_LOGIN:
                    Toast.makeText(getApplicationContext(), "注销成功", Toast.LENGTH_SHORT).show();
                    break;
                case NetworkInfo.UNCONNECTED:
                    Toast.makeText(getApplicationContext(),
                            "请确保连接了校园网，并重试", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }

    private class ConfigConnect extends AsyncTask<Void, Long, String> {

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url("http://token.futureer.net/hello.html").build();
            Call call = client.newCall(request);
            String result;
            try {
                result = call.execute().body().string();
                Log.e("wlan", result);
            } catch (IOException e) {
                e.printStackTrace();
                result = "f**k";
            }
            return result;
        }


        @Override
        protected void onPostExecute(String o) {
        }
    }

}
