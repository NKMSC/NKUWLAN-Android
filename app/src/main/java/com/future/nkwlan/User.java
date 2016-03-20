package com.future.nkwlan;

/**
 * Created by future on 2016/3/09 0009.
 */
public class User {
    public String uid;
    public String psw;
    public boolean validate = false;

    public User(String ID, String psw) {
        this.uid = ID;
        this.psw = psw;
    }

    public User() {
    }

}
