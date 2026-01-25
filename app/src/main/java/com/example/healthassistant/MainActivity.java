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
    private Uri photoUri; // 用于存储拍摄照片的Uri

    // 1. 权限请求启动器 (推荐使用新API)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // 权限授予，可以尝试启动相机
                    dispatchTakePictureIntent();
                } else {
                    // 权限被拒绝
                    Toast.makeText(this, "相机权限被拒绝，无法拍摄。", Toast.LENGTH_LONG).show();
                    textViewStatus.setText("状态: 缺少相机权限");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // 如果不需要边缘布局处理，可以保留或移除
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 保持系统栏处理不变
            return insets;
        });

        // 2. 初始化 UI 控件
        imageView = findViewById(R.id.imageView_photo);
        textViewStatus = findViewById(R.id.textView_status);
        textViewResult = findViewById(R.id.textView_result);
        buttonCapture = findViewById(R.id.button_capture);

        // 3. 设置按钮点击事件，点击时检查权限
        buttonCapture.setOnClickListener(v -> checkAndRequestPermissions());
        
        // 初始状态
        textViewStatus.setText("状态: 准备就绪，点击拍摄");
    }

    /**
     * 检查所需权限，如果缺失则请求
     */
    private void checkAndRequestPermissions() {
        // 只需要相机权限来启动 Intent 即可，存储权限在 getExternalFilesDir 时系统会帮忙处理或在 Android Q+ 后不再需要运行时权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            // 请求相机权限
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * 启动相机 Intent
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
                textViewStatus.setText("状态: 文件创建失败");
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
            Toast.makeText(this, "设备上没有可用的相机应用", Toast.LENGTH_SHORT).show();
            textViewStatus.setText("状态: 未检测到相机应用");
        }
    }
    
    /**
     * 创建一个用于存储照片的临时文件
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        
        // 使用 getExternalFilesDir 存储在应用私有/半私有区域
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            throw new IOException("无法获取外部存储目录");
        }
        
        File image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        );
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                // 1. 图片已保存到 photoUri 指定的位置
                textViewStatus.setText("状态: 图片已拍摄完成，正在加载预览...");
                
                // 2. 使用 Glide 加载图片预览 (独立验证点)
                Glide.with(this)
                        .load(photoUri)
                        .centerCrop()
                        .into(imageView);
                
                textViewStatus.setText("状态: 图片预览成功，准备上传...");

                // 3. 占位：调用发送到后端的逻辑
                sendImageToServer(photoUri);
            } else if (resultCode == RESULT_CANCELED) {
                textViewStatus.setText("状态: 拍摄被取消");
                Toast.makeText(this, "拍摄被取消", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 占位函数：用于替换为 Retrofit 网络请求
     */
    private void sendImageToServer(Uri uri) {
        Log.d(TAG, "图片URI: " + uri.toString());
        
        textViewStatus.setText("状态: 等待后端响应 (OCR/LLM处理中)...");
        
        // 模拟网络延迟和结果
        textViewResult.setText("模拟LLM返回的JSON结构:\n{\n  \"diagnosis\": \"普通感冒\",\n  \"medication_plan\": [\n    {\"name\": \"感冒灵\", \"dosage\": \"一次一包\", \"frequency\": \"一日三次\"},\n    {\"name\": \"维生素C\", \"dosage\": \"500mg\", \"frequency\": \"一日一次\"}\n  ],\n  \"notes\": \"请多喝热水，注意休息。\" \n}");
        textViewStatus.setText("状态: AI处理完成！");
        Toast.makeText(this, "结果已返回，请查看下方", Toast.LENGTH_LONG).show();
    }
}