package com.inzapp.foodcam.utils;


import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;

/**
 * 프로젝트 내부 설정클래스
 * 프로젝트 내부적으로 사용되는 값들을 모아둠
 */
public abstract class pRes {
    public static final String IP = "172.30.125.167"; // 서버아이피
    public static final int PORT = 12332; // 서버포트
    public static final int IMAGE_COMPRESSION_VAL = 100; // 이미지 압축률 : 100에 가까울수록 원본 화질로 전송된다
    public static final int TIMEOUT_CONNECTION = 1000 * 10; // 이 시간동안 서버에 연결하지 못하면 연결을 중지
    public static ExecutorService thdPool; // 데이터전송을 위한 스레드풀

    // 사용자 알림을 위한 토스트
    public static void toast(Context context, Handler mainLooperHandler, int strId) {
        mainLooperHandler.post(() -> Toast.makeText(context, strId, Toast.LENGTH_SHORT).show());
    }
}
