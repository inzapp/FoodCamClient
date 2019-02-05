package com.inzapp.foodguider.core;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;

import com.inzapp.foodguider.R;
import com.inzapp.foodguider.utils.pRes;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public final class ImageSender extends ServerConnector {

    public void sendImage(Bitmap bitmap, Context context, Handler mainLooperHandler) {
        if (!connectServer(context, mainLooperHandler))
            return;

        try {
            sendImageToServer(bitmap);
        } catch (Exception e) {
            pRes.toast(context, mainLooperHandler, R.string.IMAGE_SEND_FAILURE);
            disconnect();

            e.printStackTrace();
            return;
        }

        JSONObject result = getServerResult();
        if (result == null)
            return;

        alertResult(context, result);
        disconnect();
    }

    private void sendImageToServer(Bitmap bitmap) throws Exception {
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

    private boolean sendByteArrToServer(byte[] bitmapByteArr) {
        try {
            oos.writeObject(bitmapByteArr);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private JSONObject getServerResult() {
        try {
            return new JSONObject((String) ois.readObject());
        } catch (Exception e) {
            return null;
        }
    }

    private void alertResult(Context context, JSONObject result) {
        try {
            String link = (String) result.get("link");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
