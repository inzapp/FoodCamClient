package com.inzapp.foodcam.core;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;

import com.inzapp.foodcam.R;
import com.inzapp.foodcam.utils.Cmd;
import com.inzapp.foodcam.utils.pRes;

import org.json.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 소켓을 이용해 서버 접속을 담당하는 클래스
 * 서버 연결 후 공중키(pRes)를 보내 지정된 앱에서 접속한다는 것을 알림
 * 그 후 서버로부터 서버의 부하여부(busy)를 수신받아 접속제한여부를 체크
 * 부하상태가 아니라면 정상 접속 진행
 */
class ServerConnector {

    Socket socket;
    ObjectOutputStream oos;
    ObjectInputStream ois;

    // 서버 연결 : 성공시 true
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

    // 서버와 데이터 통신을 위한 스트림 생성
    private boolean getStream() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 서버의 Gate를 통과하기 위한 서버키 전송
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

    // 서버의 부하상태 여부 : true 시 서버 접속을 중지한다
    private boolean isServerBusy() {
        try {
            JSONObject json = new JSONObject((String) ois.readObject());
            String serverStatement = (String) json.get("serverStatus");

            return serverStatement.equals(Cmd.SERVER_IS_BUSY);
        } catch (Exception e) {
            return true;
        }
    }

    // 서버가 부하상태라면 사용자에게 서버가 부하되어 접속이 지연됨을 알림
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

    // 서버 연결 종료
    void disconnect() {
        try {
            socket.getOutputStream().close();
        } catch (Exception e) {
            // empty
        }
    }
}
