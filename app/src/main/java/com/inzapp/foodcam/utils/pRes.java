package com.inzapp.foodcam.utils;


import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;

public abstract class pRes {
    public static final String IP = "172.30.1.20"; // 서버아이피
    public static final int PORT = 12332; // 서버포트
    public static final int IMAGE_COMPRESSION_VAL = 100; // 이미지 압축률 : 100에 가까울수록 원본 화질로 전송된다
    public static final int TIMEOUT_CONNECTION = 1000 * 10; // 이 시간동안 서버에 연결하지 못하면 연결을 중지
    public static ExecutorService clientThreadPool; // 데이터전송을 위한 스레드풀

    public static void toast(Context context, Handler mainLooperHandler, int strId) {
        mainLooperHandler.post(() -> Toast.makeText(context, strId, Toast.LENGTH_SHORT).show());
    }
}
