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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final String TAG = "HealthAssistant";

    private ImageView imageView;
    private TextView textViewStatus;
    private TextView textViewResult;
    private Button buttonCapture;
    private Uri photoUri;

    // Room 数据库实例
    private AppDatabase db;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "相机权限被拒绝，无法拍摄。", Toast.LENGTH_LONG).show();
                    textViewStatus.setText("状态: 缺少相机权限");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化数据库
        db = AppDatabase.getDatabase(this);

        imageView = findViewById(R.id.imageView_photo);
        textViewStatus = findViewById(R.id.textView_status);
        textViewResult = findViewById(R.id.textView_result);
        buttonCapture = findViewById(R.id.button_capture);

        buttonCapture.setOnClickListener(v -> checkAndRequestPermissions());
        textViewStatus.setText("状态: 准备就绪，点击拍摄");
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
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "com.example.healthassistant.fileprovider", 
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            // ⚠️ 模拟器没有相机时，为了演示，我们直接模拟“拍摄成功并上传”
            Toast.makeText(this, "模拟器未检测到相机，正在模拟 AI 识别流程...", Toast.LENGTH_LONG).show();
            sendImageToServer(null);
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
            sendImageToServer(photoUri);
        }
    }

    /**
     * 发送到后端（目前是模拟）并存入本地数据库
     */
    private void sendImageToServer(Uri uri) {
        textViewStatus.setText("状态: AI 正在识别中...");
        
        // 模拟 2 秒延迟
        new android.os.Handler().postDelayed(() -> {
            String mockResult = "模拟识别结果:\n- 感冒灵: 一次一包, 一日三次\n- 维生素C: 500mg, 一日一次";
            textViewResult.setText(mockResult);
            textViewStatus.setText("状态: 识别成功，已存入用药计划！");

            // --- 核心：将识别到的药品存入 Room 数据库 ---
            saveToDatabase("感冒灵", "一次一包", "一日三次");
            saveToDatabase("维生素C", "500mg", "一日一次");

            Toast.makeText(this, "计划已保存至本地数据库", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    /**
     * 将药品保存到数据库（在子线程中操作）
     */
    private void saveToDatabase(String name, String dosage, String freq) {
        new Thread(() -> {
            Medicine medicine = new Medicine(name, dosage, freq, System.currentTimeMillis());
            db.medicineDao().insert(medicine);
            Log.d(TAG, "已保存到数据库: " + name);
        }).start();
    }
}