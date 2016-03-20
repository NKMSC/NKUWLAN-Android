package com.future.nkwlan;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by future on 2016/3/09 0009.
 */
public class NkCache {

    private static final String account_tag = "NKU_USER";
    private static final String account_tag_uid = "NKU_USER_id";
    private static final String account_tag_pwd = "NKU_USER_pwd";
    private static final String account_tag_validate = "NKU_USER_validate";

    public static void setAccount(Context context, String id, String psw, boolean validate) {
        SharedPreferences sp = context.getSharedPreferences(account_tag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(account_tag_uid, id);
        editor.putString(account_tag_pwd, psw);
        editor.putBoolean(account_tag_validate, validate);
        editor.apply();
    }

    public static User getAccount(Context context) {
        SharedPreferences sp = context.getSharedPreferences(account_tag, Context.MODE_PRIVATE);
        return new User(sp.getString(account_tag_uid, ""), sp.getString(account_tag_pwd, ""));
    }

}
