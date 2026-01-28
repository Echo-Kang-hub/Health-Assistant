package com.example.healthassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanActivity extends AppCompatActivity {
    private Uri photoUri;
    private File currentPhotoFile;
    private Button buttonStart;
    private TextView textManual;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "需要相机权限才能识别", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && currentPhotoFile != null) {
                    uploadImage(currentPhotoFile);
                } else {
                    Toast.makeText(this, "拍照已取消", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        buttonStart = findViewById(R.id.button_start_scan);
        textManual = findViewById(R.id.text_scan_manual);

        buttonStart.setOnClickListener(v -> checkAndRequestPermissions());
        textManual.setOnClickListener(v -> {
            startActivity(new Intent(this, RecognitionConfirmActivity.class));
        });
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void dispatchTakePictureIntent() {
        try {
            currentPhotoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(this, "com.example.healthassistant.fileprovider", currentPhotoFile);
            takePictureLauncher.launch(photoUri);
        } catch (IOException ex) {
            Toast.makeText(this, "文件创建失败", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    private void uploadImage(File file) {
        buttonStart.setEnabled(false);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        RequestBody barcodeText = RequestBody.create(MediaType.parse("text/plain"), "");
        RetrofitClient.getApiService().createRecognition(UUID.randomUUID().toString(), body, barcodeText)
                .enqueue(new Callback<ApiResponse<RecognitionStartResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<RecognitionStartResponse>> call, Response<ApiResponse<RecognitionStartResponse>> response) {
                        buttonStart.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            int recognitionId = response.body().getData().getRecognitionId();
                            Intent intent = new Intent(ScanActivity.this, RecognitionProcessingActivity.class);
                            intent.putExtra("recognition_id", recognitionId);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ScanActivity.this, "识别失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<RecognitionStartResponse>> call, Throwable t) {
                        buttonStart.setEnabled(true);
                        Toast.makeText(ScanActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
