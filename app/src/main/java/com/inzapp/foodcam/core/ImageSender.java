package com.inzapp.foodcam.core;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;

import com.inzapp.foodcam.R;
import com.inzapp.foodcam.utils.pRes;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * 이미지가 어떤 음식인지 서버에 분석 요청하는 클래스
 * 이미지를 전송 한 후로 서버로부터
 * 해당 음식을 소개하는 웹 페이지의 링크를 Json 형태로 수신받는다
 */
public final class ImageSender extends ServerConnector {

    // 이미지 전송
    public void sendImage(Bitmap bitmap, Context context, Handler mainLooperHandler) {
        if (!connectServer(context, mainLooperHandler))
            return;

        try {
            sendByteArrayToServer(bitmap);
        } catch (Exception e) {
            pRes.toast(context, mainLooperHandler, R.string.IMAGE_SEND_FAILURE);
            disconnect();

            e.printStackTrace();
            return;
        }

        JSONObject result = getServerResult();
        if (result == null)
            return;

        alertResult(context, mainLooperHandler, result);
        disconnect();
    }

    // 비트맵 이미지를 바이트형 배열로 변환 후 서버에 전송
    private void sendByteArrayToServer(Bitmap bitmap) throws Exception {
        byte[] bitmapByteArr = getByteArr(bitmap);
        if (bitmapByteArr == null)
            throw new Exception("GetByteArrException");

        if (!sendByteArrToServer(bitmapByteArr))
            throw new Exception("SendByteArrToServerException");
    }

    // 바이트스트림 클래스를 사용해 선택된 이미지를 압축 후 바이트 배열형태로 변환
    private byte[] getByteArr(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, pRes.IMAGE_COMPRESSION_VAL, byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }

    // 바이트형 배열을 서버에 전송
    private boolean sendByteArrToServer(byte[] bitmapByteArr) {
        try {
            oos.writeObject(bitmapByteArr);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 서버 응답 수신
    private JSONObject getServerResult() {
        try {
            return new JSONObject((String) ois.readObject());
        } catch (Exception e) {
            return null;
        }
    }

    // 서버로부터 수신받은 응답에 대한 사용자 알림 : 웹 브라우저를 통해 링크를 실행한다
    private void alertResult(Context context, Handler mainLooperHandler, JSONObject result) {
        try {
            String link = (String) result.get("link");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            context.startActivity(intent);
        } catch (Exception e) {
            pRes.toast(context, mainLooperHandler, R.string.FOOD_NOT_FOUND);
            e.printStackTrace();
        }
    }
}
