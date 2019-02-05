package com.inzapp.foodguider.core;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;

import com.inzapp.foodguider.R;
import com.inzapp.foodguider.utils.Cmd;
import com.inzapp.foodguider.utils.pRes;

import org.json.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

class ServerConnector {

    Socket socket;
    ObjectOutputStream oos;
    ObjectInputStream ois;

    // 서버접속 성공시 true 리턴
    boolean connectServer(Context context, Handler mainLooperHandler) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(pRes.IP, pRes.PORT), pRes.TIMEOUT_CONNECTION);

            if (!getStream())
                throw new Exception("GetStreamFailException");

            if (!sendServerKey())
                throw new Exception("ServerKeySendFailException");

            if (isServerBusy()) {
                alertServerIsBusy(context, mainLooperHandler);
                disconnect();
                return false;
            }

            return true;
        } catch (Exception e) {
            pRes.toast(context, mainLooperHandler, R.string.CONNECTION_TIMEOUT);
            disconnect();

            e.printStackTrace();
            return false;
        }
    }

    private boolean getStream() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean sendServerKey() {
        try {
            JSONObject json = new JSONObject();
            json.put("key", Cmd.SERVER_KEY);
            oos.writeObject(json.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isServerBusy() {
        try {
            JSONObject json = new JSONObject((String) ois.readObject());
            String serverStatement = (String) json.get("serverStatus");

            return serverStatement.equals(Cmd.SERVER_IS_BUSY);
        } catch (Exception e) {
            return true;
        }
    }

    private void alertServerIsBusy(Context context, Handler mainLooperHandler) {
        mainLooperHandler.post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(null);
            builder.setMessage(R.string.SERVER_IS_BUSY);
            builder.setPositiveButton("확인", (dialog, which) -> {
                /*empty*/
            });
            builder.show();
        });
    }

    void disconnect() {
        try {
            socket.getOutputStream().close();
        } catch (Exception e) {
            // empty
        }
    }
}
