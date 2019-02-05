package com.inzapp.foodguider.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.inzapp.foodguider.R;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class PermissionManager {

    private Activity mainContext;

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

        ArrayList<Integer> permissionStateList = getPermissionStateList();

        if (!isAllPermissionGranted(permissionStateList)) {
            alertPermissionWarning();
            requestPermission(permissionStateList);
        }
    }

    private ArrayList<Integer> getPermissionStateList() {
        ArrayList<Integer> permissionStateList = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++)
            permissionStateList.add(ContextCompat.checkSelfPermission(mainContext, permissions[i]));

        return permissionStateList;
    }

    private boolean isAllPermissionGranted(ArrayList<Integer> permissionStateList) {

        for (int curState : permissionStateList) {
            if (curState == PackageManager.PERMISSION_DENIED)
                return false;
        }

        return true;
    }

    // 부여되지 않은 권한 요청
    private void requestPermission(ArrayList<Integer> permissionStateList) {

        for (int i = 0; i < permissionStateList.size(); i++) {
            if (permissionStateList.get(i) == PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(mainContext, new String[]{permissions[i]}, 0);
        }
    }

    // 접근권한이 없으면 앱이 제대로 작동하지 않는다는 팝업창 출력
    private void alertPermissionWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainContext);
        builder.setTitle(null);
        builder.setMessage(R.string.ALERT_PERMISSION);
        builder.setPositiveButton("확인", (dialog, which) -> {});

        builder.show();
    }
}
