package com.inzapp.foodcam.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.inzapp.foodcam.R;
import com.inzapp.foodcam.core.ImageSender;
import com.inzapp.foodcam.utils.PermissionManager;
import com.inzapp.foodcam.utils.pRes;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;

/**
 * 메인 인텐트 클래스
 */
public class main extends AppCompatActivity {

    private ProgressBar progressBar;
    private Handler mainLooperHandler;

    private Bitmap bitmap;
    private Uri imgUri;

    private final int REQUEST_GET_IMG = 22; // 카메라를 이용해 사진을 얻어오는 요청코드
    private final int REQUEST_CROP_IMG = 24; // 갤러리 사진 요청 후 얻어온 사진을 1:1 비율로 수정하기 위한 요청코드
    private final int REQUEST_BROWSER = 25; // 서버 응답에 의한 브라우저 링크 실행에 대한 요청코드

    private ArrayList<String> foodLinkListByRank; // 사진요청 후 서버로부터 수신받는 소개링크 리스트
    private int linkIdx; // 서버로부터 전송받은 소개링크리스트의 인덱스

    // 직접 촬영 버튼
    public void takePicBtClick(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imgUri = getNewImgUri();
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri); // 해당 uri 에 이미지를 임시로 저장해 원본 화질을 유지한다
        startActivityForResult(cameraIntent, REQUEST_GET_IMG);
    }

    // 갤러리 사진 선택 버튼
    public void galleryBtClick(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.putExtra("crop", "true");
        galleryIntent.putExtra("aspectX", 1);
        galleryIntent.putExtra("aspectY", 1);
        galleryIntent.putExtra("scale", true);
        imgUri = getNewImgUri();
        galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(galleryIntent, REQUEST_CROP_IMG);
    }

    // 촬영한 파일이 임시로 저장될 uri 생성
    private Uri getNewImgUri() {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return Uri.fromFile(new File(storageDir, imageFileName));
    }

    // 요청 액티비티 수행 후 결과값에 따른 동작 정의
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GET_IMG:
                if (resultCode != Activity.RESULT_OK)
                    return;
                sendToCropIntent();
                break;

            case REQUEST_CROP_IMG:
                if (resultCode != Activity.RESULT_OK)
                    return;
                requestPic();
                break;

            case REQUEST_BROWSER:
                if (resultCode != Activity.RESULT_CANCELED)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("FoodCam")
                .setMessage(R.string.Q_SATISFIED)
                .setIcon(R.drawable.app_icon)
                .setPositiveButton("Yes", (event, inWhich) -> {})
                .setNegativeButton("No", (event, inWhich) -> executeNextLink());
                AlertDialog dialog = builder.create();
                dialog.show();
            default:
                break;
        }

    }

    // 촬영된 사진을 1:1 비율로 수정
    private void sendToCropIntent() {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(imgUri, "image/*");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("return-data", true);

        imgUri = getNewImgUri();
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(cropIntent, REQUEST_CROP_IMG);
    }

    // 1:1 비율로 수정된 이미지를 서버에 분석 요청
    private void requestPic() {
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
        } catch (Exception e) {
            e.printStackTrace();
            pRes.toast(main.this, mainLooperHandler, R.string.FAIL_TO_GET_BITMAP);

            return;
        }

        if (bitmap == null) {
            pRes.toast(main.this, mainLooperHandler, R.string.BITMAP_IS_NULL);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        pRes.toast(main.this, mainLooperHandler, R.string.IMAGE_REQUEST); // 이미지 분석 사용자 알림

        // 네트워크 처리를 위한 스레드풀을 이용해 이미지를 전송하고 서버로부터 받은 순위링크리스트를 저장한다
        pRes.thdPool.execute(() -> {
            ImageSender imageSender = new ImageSender();
            foodLinkListByRank = imageSender.sendImage(bitmap, main.this, mainLooperHandler);
            mainLooperHandler.post(() -> progressBar.setVisibility(View.INVISIBLE));

            if (this.foodLinkListByRank == null)
                return;

            // 기본적으로 1순위로 수신된 링크를 실행한다. 2, 3순위의 실행은 1순위를 보여준 후 사용자의 응답 여부에 따라 결정된다
            linkIdx = -1;
            executeNextLink();
        });
    }

    // 내장 브라우저를 이용해 링크를 실행한다
    private void executeNextLink() {
        ++linkIdx;
        if(foodLinkListByRank.size() <= linkIdx){
            pRes.toast(this, mainLooperHandler, R.string.FOOD_NOT_FOUND);
            return;
        }
        Intent intent = new Intent(main.this, Browser.class);
        intent.putExtra("link", foodLinkListByRank.get(linkIdx));
        startActivityForResult(intent, REQUEST_BROWSER);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar ab = getSupportActionBar();
        ab.hide();

        progressBar = findViewById(R.id.progressBar);

        // 네트워크 상태 처리를 위한 싱글 스레드풀 생성
        pRes.thdPool = Executors.newSingleThreadExecutor();

        // 네트워크 스레드에서 ui 에 접근하기 위한 메인 루퍼 생성
        mainLooperHandler = new Handler(Looper.getMainLooper());

        // 안드로이드 "누가"버전부터 파일 URI 에 대해 제한된 접근권한을 풀어주기 위함
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // 접근권한 요청
        PermissionManager permissionManager = new PermissionManager(main.this);
        permissionManager.permissionCheck();
    }
}
