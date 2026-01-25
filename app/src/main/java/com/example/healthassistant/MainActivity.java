package com.example.healthassistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// ⚠️ 简化起见，暂时不引入 Retrofit/Room，仅实现“拍照”和“结果显示”占位。

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final String TAG = "HealthAssistant";

    private ImageView imageView;
    private TextView textViewStatus;
    private TextView textViewResult;
    private Button buttonCapture;
    private Uri photoUri; // 用于存储拍摄照片的Uri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 保持系统栏处理不变
            return insets;
        });

        // 1. 初始化 UI 控件
        imageView = findViewById(R.id.imageView_photo);
        textViewStatus = findViewById(R.id.textView_status);
        textViewResult = findViewById(R.id.textView_result);
        buttonCapture = findViewById(R.id.button_capture);

        // 2. 设置按钮点击事件
        buttonCapture.setOnClickListener(v -> dispatchTakePictureIntent());

        // 3. 权限检查和 Room 初始化（后续步骤）
        // checkCameraPermissionAndRoom(); 
    }

    /**
     * 启动相机 Intent
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 确保有应用可以处理这个Intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // 创建一个用于存储照片的临时文件 (为简化，使用 FileProvider 是更安全的选择，但这里先使用简单路径)
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                // ⚠️ 生产环境中，需要使用 FileProvider 来生成 content:// URI
                // 为了简化演示，我们暂时使用 FileProvider 生成一个 Uri
                photoUri = FileProvider.getUriForFile(this,
                        "com.example.healthassistant.fileprovider", // <-- ⚠️ 确保你在 AndroidManifest.xml 中定义了 fileprovider
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "设备上没有可用的相机应用", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 创建一个用于存储照片的临时文件
     */
    private File createImageFile() throws IOException {
        // 文件命名格式: Health_YYYYMMDD_HHMMSS
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        
        // 存储在外部公共目录下 (需要权限)
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );
        
        // 保存路径供后续上传使用
        // photoPath = image.getAbsolutePath(); 
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // 1. 图片已保存到 photoUri 指定的位置
            textViewStatus.setText("状态: 图片已拍摄完成，正在上传并发送给AI处理...");
            
            // 2. 使用 Glide 加载图片预览
            // Glide.with(this).load(photoUri).into(imageView); 
            // ⚠️ 暂时注释，因为还没有导入 Glide 相关的 import

            // 3. 占位：调用发送到后端的逻辑
            sendImageToServer(photoUri);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED) {
            textViewStatus.setText("状态: 拍摄被取消");
            Toast.makeText(this, "拍摄被取消", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 占位函数：用于替换为 Retrofit 网络请求
     */
    private void sendImageToServer(Uri uri) {
        Log.d(TAG, "图片URI: " + uri.toString());
        // --- 核心逻辑：这里将使用 Retrofit 将图片以 Base64 或 Multipart 形式发送到 Python 后端 ---
        
        // 模拟网络延迟和结果
        textViewStatus.setText("状态: 等待后端响应 (OCR/LLM处理中)...");
        
        // 4天时间，我们暂时模拟一个 JSON 结果
        textViewResult.setText("模拟LLM返回的JSON结构:\n{\n  \"diagnosis\": \"普通感冒\",\n  \"medication_plan\": [\n    {\"name\": \"感冒灵\", \"dosage\": \"一次一包\", \"frequency\": \"一日三次\"},\n    {\"name\": \"维生素C\", \"dosage\": \"500mg\", \"frequency\": \"一日一次\"}\n  ],\n  \"notes\": \"请多喝热水，注意休息。\" \n}");
        textViewStatus.setText("状态: AI处理完成！");
        Toast.makeText(this, "结果已返回，请查看下方", Toast.LENGTH_LONG).show();
        
        // 接着需要调用 Room 数据库保存 medication_plan
        // saveToDatabase();
    }
    
    // private void saveToDatabase() { ... } // Room 存储步骤 (Day 4)
}