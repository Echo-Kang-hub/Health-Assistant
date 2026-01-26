package com.example.healthassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
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

    private ImageView imageView;
    private TextView textViewStatus;
    private TextView textViewDiagnosis;
    private TextView textViewNotes;
    private Uri photoUri;
    private File currentPhotoFile;

    private AppDatabase db;
    private MedicineAdapter adapter;

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
        textViewDiagnosis = findViewById(R.id.textView_diagnosis);
        textViewNotes = findViewById(R.id.textView_notes);
        Button buttonCapture = findViewById(R.id.button_capture);
        RecyclerView recyclerView = findViewById(R.id.recyclerView_medicines);

        adapter = new MedicineAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 设置点击修改监听
        adapter.setOnItemClickListener(this::showEditDialog);

        // 设置长按删除监听
        adapter.setOnItemLongClickListener(medicine -> {
            new AlertDialog.Builder(this)
                    .setTitle("删除记录")
                    .setMessage("确定要删除这条用药计划吗？")
                    .setPositiveButton("删除", (dialog, which) -> deleteMedicine(medicine))
                    .setNegativeButton("取消", null)
                    .show();
        });

        buttonCapture.setOnClickListener(v -> checkAndRequestPermissions());
        refreshMedicineList();
    }

    private void showEditDialog(Medicine medicine) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改用药信息");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_edit_medicine, null);
        final EditText inputName = viewInflated.findViewById(R.id.edit_name);
        final EditText inputDosage = viewInflated.findViewById(R.id.edit_dosage);
        final EditText inputFrequency = viewInflated.findViewById(R.id.edit_frequency);

        inputName.setText(medicine.getName());
        inputDosage.setText(medicine.getDosage());
        inputFrequency.setText(medicine.getFrequency());

        builder.setView(viewInflated);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String newName = inputName.getText().toString().trim();
            String newDosage = inputDosage.getText().toString().trim();
            String newFreq = inputFrequency.getText().toString().trim();
            
            if (!newName.isEmpty()) {
                medicine.setName(newName);
                medicine.setDosage(newDosage);
                medicine.setFrequency(newFreq);
                updateMedicine(medicine);
            } else {
                Toast.makeText(this, "药品名称不能为空", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateMedicine(Medicine medicine) {
        new Thread(() -> {
            db.medicineDao().update(medicine);
            runOnUiThread(this::refreshMedicineList);
        }).start();
        Toast.makeText(this, "修改已保存", Toast.LENGTH_SHORT).show();
    }

    private void deleteMedicine(Medicine medicine) {
        new Thread(() -> {
            db.medicineDao().delete(medicine);
            runOnUiThread(this::refreshMedicineList);
        }).start();
        Toast.makeText(this, "记录已删除", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "未检测到相机", Toast.LENGTH_LONG).show();
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
            uploadImage(currentPhotoFile);
        }
    }

    private void uploadImage(File file) {
        textViewStatus.setText("状态: AI 正在云端分析药单...");
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        RetrofitClient.getApiService().processPrescription(body).enqueue(new Callback<HealthResponse>() {
            @Override
            public void onResponse(Call<HealthResponse> call, Response<HealthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HealthResponse result = response.body();
                    
                    if (result.isSuccess()) {
                        // ✅ 识别成功 (HTTP 200, success: true)
                        textViewStatus.setText("状态: AI 分析完成！");
                        
                        // 更新诊断和建议
                        textViewDiagnosis.setText("诊断结论: " + (result.getDiagnosis() != null ? result.getDiagnosis() : "未明确"));
                        textViewNotes.setText("健康建议: " + (result.getNotes() != null ? result.getNotes() : "无额外说明"));

                        if (result.getMedicationPlan() != null) {
                            for (MedicinePlan plan : result.getMedicationPlan()) {
                                saveToDatabase(plan.getName(), plan.getDosage(), plan.getFrequency());
                            }
                        }
                    } else {
                        // ❌ 识别失败 (HTTP 200, but success: false)
                        String errorMessage = result.getError() != null && !result.getError().isEmpty() 
                                            ? result.getError() 
                                            : "未能识别到处方中的药品信息，请提供清晰的处方图片";
                        
                        textViewStatus.setText("状态: 识别失败");
                        textViewDiagnosis.setText("诊断结论: 识别失败");
                        textViewNotes.setText("错误信息: " + errorMessage);
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // 💥 服务器错误 (HTTP 400, 500 etc.)
                    textViewStatus.setText("状态: 服务器错误 (" + response.code() + ")");
                    textViewDiagnosis.setText("诊断结论: 请求失败");
                    textViewNotes.setText("错误信息: 无法连接或服务器异常");
                }
            }
            @Override
            public void onFailure(Call<HealthResponse> call, Throwable t) {
                textViewStatus.setText("状态: 网络请求失败");
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