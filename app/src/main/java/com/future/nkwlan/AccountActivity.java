package com.future.nkwlan;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText uidEdit;
    private EditText pswEdit;
    private Button saveBtn;
    private int status = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        uidEdit = (EditText) findViewById(R.id.account_uid_edit);
        pswEdit = (EditText) findViewById(R.id.account_psw_edit);
        saveBtn = (Button) findViewById(R.id.account_save_btn);
        saveBtn.setOnClickListener(this);
        TextView despTxt = (TextView) findViewById(R.id.account_desp_txt);
        despTxt.setText(Html.fromHtml(getResources().getString(R.string.app_description)));
        new Connect().execute();
    }

    @Override
    public void onClick(View v) {
        String uid = uidEdit.getEditableText().toString();
        String psw = pswEdit.getEditableText().toString();
        switch (v.getId()) {
            case R.id.account_save_btn:
                if (uid.isEmpty() | psw.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "用户名和密码不能为空",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (status == NetworkInfo.UN_LOGIN) {
                    Toast.makeText(getApplicationContext(), "登录中...",
                            Toast.LENGTH_LONG).show();
                    new Login().execute(uid,psw);
                    return;
                }

                NkCache.setAccount(getApplicationContext(), uid,
                        psw, status == NetworkInfo.UN_LOGIN);

                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
        }
    }

    private class Connect extends AsyncTask<String, Long, NetworkInfo> {

        @Override
        protected NetworkInfo doInBackground(String... params) {
            return NkNetwork.getLogStatus();
        }

        @Override
        protected void onPostExecute(NetworkInfo info) {

            switch (info.status) {

                case NetworkInfo.UNCONNECTED:
                    Toast.makeText(getApplicationContext(), "Not in NKU network", Toast.LENGTH_SHORT).show();
                    break;
                case NetworkInfo.UN_LOGIN://登录&保存
                    saveBtn.setText("登录");
                    Toast.makeText(getApplicationContext(), "Not log in NKU_WLAN yet", Toast.LENGTH_SHORT).show();
                    break;
                case NetworkInfo.ONLINE:
                    Toast.makeText(getApplicationContext(), info.uid + "online", Toast.LENGTH_SHORT).show();
                    saveBtn.setText("保存");
                    uidEdit.setText(info.uid);
            }
            status = info.status;
        }
    }

    private class Login extends AsyncTask<String, Long, NetworkInfo> {

        @Override
        protected NetworkInfo doInBackground(String... params) {

            return NkNetwork.login(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(NetworkInfo info) {
            Log.e("wlan",info.toString());
            switch (info.status){
                case NetworkInfo.SUCCESS_UNKNOWN:

                case NetworkInfo.ONLINE:
                    Toast.makeText(getApplicationContext(),
                            info.uid + " online", Toast.LENGTH_SHORT).show();
                    NkCache.setAccount(getApplicationContext(), info.uid,
                            pswEdit.getEditableText().toString(),
                            status == NetworkInfo.UN_LOGIN);
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    break;
                case NetworkInfo.WRONG_PSW:
                    Toast.makeText(getApplicationContext(),
                            "账号密码不对，请核对后再试", Toast.LENGTH_SHORT).show();
                    break;
                case NetworkInfo.UNCONNECTED:
                    Toast.makeText(getApplicationContext(),
                            "登录失败，请稍后再试", Toast.LENGTH_SHORT).show();
                    break;
                case NetworkInfo.UN_LOGIN:
                    Toast.makeText(getApplicationContext(),
                            "登录失败，请确认账号密码正确，且余额充足", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
