package com.inzapp.foodguider.view;

import android.app.Activity;
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

import com.inzapp.foodguider.R;
import com.inzapp.foodguider.core.ImageSender;
import com.inzapp.foodguider.utils.PermissionManager;
import com.inzapp.foodguider.utils.pRes;

import java.io.File;
import java.util.concurrent.Executors;


public class main extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView imageView;
    private Handler mainLooperHandler;

    private final int REQUEST_TAKE_IMAGE_FROM_CAMERA = 22; // 직접 사진을 찍기 위한 요청 코드
    private final int REQUEST_GET_IMAGE_FROM_GALLERY = 23; // 갤러리에서 사진을 가져오기 위한 요청코드

    private Bitmap bitmap;
    private Uri newFileUri;

    // 촬영한 파일이 임시로 저장될 uri 생성
    private Uri getNewFileUri() {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return Uri.fromFile(new File(storageDir, imageFileName));
    }

    // 직접 촬영 버튼
    public void takePicBtClick(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        newFileUri = getNewFileUri();
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, newFileUri); // 해당 uri 에 이미지를 임시로 저장해 원본 화질을 유지한다
        startActivityForResult(cameraIntent, REQUEST_TAKE_IMAGE_FROM_CAMERA);
    }

    // 갤러리 사진 선택 버튼
    public void galleryBtClick(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        galleryIntent.putExtra("crop", "true");
        galleryIntent.putExtra("aspectX", 1);
        galleryIntent.putExtra("aspectY", 1);
        galleryIntent.putExtra("scale", true);

        newFileUri = getNewFileUri();
        galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, newFileUri);

        startActivityForResult(galleryIntent, REQUEST_GET_IMAGE_FROM_GALLERY);
    }

    public void sendBtClick(View view) {
        if (bitmap == null) {

            pRes.toast(main.this, mainLooperHandler, R.string.BITMAP_IS_NULL);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        pRes.toast(main.this, mainLooperHandler, R.string.IMAGE_REQUEST); // 이미지 분석 사용자 알림

        pRes.clientThreadPool.execute(() -> {
            ImageSender imageSender = new ImageSender();
            imageSender.sendImage(bitmap, main.this, mainLooperHandler);
            mainLooperHandler.post(() -> progressBar.setVisibility(View.INVISIBLE));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {
                case REQUEST_TAKE_IMAGE_FROM_CAMERA:
                    // 카메라인텐트를 생성할 때 새로운 파일이 임시로 저장될 uri 를 같이 넣어 보냈으므로 여기선 아무것도 안 함
                    break;

                case REQUEST_GET_IMAGE_FROM_GALLERY:
//                    newFileUri = data.getData(); // 선택된 사진의 uri 를 저장
//                    imageView.setRotation(270);
                    break;

                default:
                    break;
            }

            try {
                // 해당 uri 를 이용한 원본화질의 비트맵 객체 생성
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), newFileUri);
            } catch (Exception e) {
                e.printStackTrace();
                pRes.toast(main.this, mainLooperHandler, R.string.FAIL_TO_GET_BITMAP);
            }

            // 사용자에게 선택된 이미지를 보여줌
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar ab = getSupportActionBar();
        ab.hide();

        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.imageView);

        // 네트워크 상태 처리를 위한 싱글 스레드풀 생성
        pRes.clientThreadPool = Executors.newSingleThreadExecutor();

        // 네트워크 스레드에서 ui 에 접근하기 위한 메인 루퍼 생성
        mainLooperHandler = new Handler(Looper.getMainLooper());

        // 안드로이드 "누가"버전부터 파일 URI 에 대해 제한된 접근권한을 풀어주기 위함
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // 접근권한 요청
        PermissionManager perMissionManager = new PermissionManager(main.this);
        perMissionManager.permissionCheck();
    }
}
