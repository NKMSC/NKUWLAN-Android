package com.future.nkwlan;

/**
 * Created by future on 2016/3/11 0011.
 */
public class NetworkInfo {

    public String uid;
    public float fee;
    public float flow;
    public float time;
    public int status;

    public static final int ONLINE = 0;//0:正常
    public static final int WRONG_PSW = 1;//1：账号密码错误
    public static final int UNCONNECTED = 2;//2：网络连接失败
    public static final int UN_LOGIN = 3;//3：未登录
    public static final int SUCCESS_UNKNOWN = 4;//登录成功，但是未获取到流量等信息。

    public NetworkInfo(String uid, String fee, String flow_, String time) {
        this.uid = uid;
        this.fee = Float.parseFloat(fee);
        this.fee = (this.fee - this.fee % 100) / 10000;
        this.flow = Float.parseFloat(flow_)/(1024*1024);
        this.time = Float.parseFloat(time);
    }

    public NetworkInfo(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "NetworkInfo{" +
                "fee=" + fee +
                ", flow=" + flow +
                ", time=" + time +
                ", status=" + status +
                '}';
    }
}
