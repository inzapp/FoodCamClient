package com.inzapp.foodcam.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.inzapp.foodcam.R;

import java.util.ArrayList;

/**
 * 앱 이용에 필요한 권한을 관리하는 클래스
 * 초기 접속 시 앱 실행에 필요한 권한이 부여되었는지를 확인 후
 * 부여되지 않은 권한에 대한 권한을 요청한다
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class PermissionManager {

    private Activity mainContext;

    // 앱 사용에 필요한 권한들
    private String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    public PermissionManager(Activity mainContext) {
        this.mainContext = mainContext;
    }

    // 앱에 필요한 권한부여 여부에 따라 필요시 권한 요청
    public void permissionCheck() {
        String[] unGrantedPermissions = getUnGrantedPermissions();
        if(0 < unGrantedPermissions.length) {
            ActivityCompat.requestPermissions(mainContext, unGrantedPermissions, 0);
        }
    }

    // 부여되지 않은 권한들 리턴
    private String[] getUnGrantedPermissions() {
        ArrayList<String> unGrantedPermissionList = new ArrayList<>();
        for(int i=0; i<permissions.length; ++i) {
            int checkResult = ContextCompat.checkSelfPermission(mainContext, permissions[i]);
            if(checkResult == PackageManager.PERMISSION_DENIED) {
                unGrantedPermissionList.add(permissions[i]);
            }
        }

        return unGrantedPermissionList.toArray(new String[0]);
    }
}
