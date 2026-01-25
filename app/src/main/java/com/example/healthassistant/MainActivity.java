package com.example.healthassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final String TAG = "HealthAssistant";

    private ImageView imageView;
    private TextView textViewStatus;
    private Button buttonCapture;
    private Uri photoUri;
    private File currentPhotoFile; // 记录当前拍摄的文件

    private AppDatabase db;
    private MedicineAdapter adapter;
    private RecyclerView recyclerView;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) dispatchTakePictureIntent();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        db = AppDatabase.getDatabase(this);
        imageView = findViewById(R.id.imageView_photo);
        textViewStatus = findViewById(R.id.textView_status);
        buttonCapture = findViewById(R.id.button_capture);
        recyclerView = findViewById(R.id.recyclerView_medicines);

        adapter = new MedicineAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        buttonCapture.setOnClickListener(v -> checkAndRequestPermissions());
        refreshMedicineList();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                currentPhotoFile = createImageFile();
                photoUri = FileProvider.getUriForFile(this, "com.example.healthassistant.fileprovider", currentPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException ex) {
                Toast.makeText(this, "文件创建失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "未检测到相机，请在真机测试", Toast.LENGTH_LONG).show();
        }
    }
    
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Glide.with(this).load(photoUri).centerCrop().into(imageView);
            uploadImage(currentPhotoFile); // 开始真实上传
        }
    }

    /**
     * 核心：使用 Retrofit 上传图片到后端
     */
    private void uploadImage(File file) {
        textViewStatus.setText("状态: AI 正在云端分析药单 (请稍候)...");

        // 构造 MultipartBody
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        // 发起请求
        RetrofitClient.getApiService().processPrescription(body).enqueue(new Callback<HealthResponse>() {
            @Override
            public void onResponse(Call<HealthResponse> call, Response<HealthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HealthResponse result = response.body();
                    textViewStatus.setText("状态: AI 分析完成！");
                    
                    // 将返回的用药计划存入数据库
                    if (result.getMedicationPlan() != null) {
                        for (MedicinePlan plan : result.getMedicationPlan()) {
                            saveToDatabase(plan.getName(), plan.getDosage(), plan.getFrequency());
                        }
                        Toast.makeText(MainActivity.this, "识别到 " + result.getMedicationPlan().size() + " 种药品", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    textViewStatus.setText("状态: 服务器响应错误 (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<HealthResponse> call, Throwable t) {
                textViewStatus.setText("状态: 网络请求失败 - " + t.getMessage());
                Log.e(TAG, "Upload failed", t);
            }
        });
    }

    private void saveToDatabase(String name, String dosage, String freq) {
        new Thread(() -> {
            db.medicineDao().insert(new Medicine(name, dosage, freq, System.currentTimeMillis()));
            runOnUiThread(this::refreshMedicineList);
        }).start();
    }

    private void refreshMedicineList() {
        new Thread(() -> {
            List<Medicine> list = db.medicineDao().getAllMedicines();
            runOnUiThread(() -> adapter.setMedicines(list));
        }).start();
    }
}