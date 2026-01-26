package com.example.healthassistant;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
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
    private TextView textViewSafetyReport;
    private TextView textViewReportContent; // 每日报告内容
    private TextView textViewReportDate;    // 每日报告日期
    private FloatingActionButton buttonSettings; 
    private Uri photoUri;
    private File currentPhotoFile;

    private AppDatabase db;
    private MedicineAdapter adapter;

    private SafetyReport currentSafetyReport;
    private UserProfile currentUserProfile;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) dispatchTakePictureIntent();
            });

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "提醒通知权限已开启", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "通知权限未开启，提醒功能将无法正常工作", Toast.LENGTH_LONG).show();
                }
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
        textViewSafetyReport = findViewById(R.id.textView_safety_report); 
        textViewReportContent = findViewById(R.id.textView_report_content); 
        textViewReportDate = findViewById(R.id.textView_report_date);       
        buttonSettings = findViewById(R.id.button_settings);
        Button buttonCapture = findViewById(R.id.button_capture);
        RecyclerView recyclerView = findViewById(R.id.recyclerView_medicines);

        adapter = new MedicineAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(this::showEditDialog);
        adapter.setOnItemLongClickListener(medicine -> {
            new AlertDialog.Builder(this)
                    .setTitle("删除记录")
                    .setMessage("确定要删除这条用药计划吗？")
                    .setPositiveButton("删除", (dialog, which) -> deleteMedicine(medicine))
                    .setNegativeButton("取消", null)
                    .show();
        });

        adapter.setOnReminderClickListener(this::handleReminderClick);

        buttonCapture.setOnClickListener(v -> checkAndRequestPermissions());
        buttonSettings.setOnClickListener(v -> showProfileSettingsDialog());
        
        refreshMedicineList();
        updateSafetyReportDisplay();
        fetchDailyReport(); // 自动获取每日报告

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void fetchDailyReport() {
        RetrofitClient.getApiService().getDailyReport().enqueue(new Callback<DailyReportResponse>() {
            @Override
            public void onResponse(Call<DailyReportResponse> call, Response<DailyReportResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DailyReportResponse report = response.body();
                    if (report.isSuccess()) {
                        // 修正：调用 getSummary() 而非 getContent()
                        textViewReportContent.setText(report.getSummary());
                        textViewReportDate.setText(report.getReportDate());
                    }
                }
            }
            @Override
            public void onFailure(Call<DailyReportResponse> call, Throwable t) {
                textViewReportContent.setText("今日暂无更新提示。");
            }
        });
    }

    private void handleReminderClick(Medicine medicine) {
        if (medicine.isReminderEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("关闭提醒")
                    .setMessage("是否要关闭 " + medicine.getName() + " 的服药提醒？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        medicine.setReminderEnabled(false);
                        ReminderManager.cancelReminder(this, medicine);
                        updateMedicine(medicine);
                        Toast.makeText(this, "提醒已关闭", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                String time = String.format(Locale.CHINA, "%02d:%02d", hourOfDay, minute);
                medicine.setReminderEnabled(true);
                medicine.setReminderTime(time);
                ReminderManager.setReminder(this, medicine);
                updateMedicine(medicine);
                Toast.makeText(this, "已设置提醒: " + time, Toast.LENGTH_SHORT).show();
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }
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
                if (medicine.isReminderEnabled()) {
                    ReminderManager.setReminder(this, medicine);
                }
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
    }

    private void deleteMedicine(Medicine medicine) {
        new Thread(() -> {
            ReminderManager.cancelReminder(this, medicine);
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
            File processedFile = processImage(currentPhotoFile);
            Glide.with(this).load(processedFile).centerCrop().into(imageView);
            uploadImage(processedFile);
        }
    }

    private File processImage(File imageFile) {
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) matrix.postRotate(90);
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) matrix.postRotate(180);
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) matrix.postRotate(270);
            
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            
            File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "upright_" + imageFile.getName());
            FileOutputStream out = new FileOutputStream(outputFile);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return outputFile;
        } catch (Exception e) {
            return imageFile;
        }
    }

    private void uploadImage(File file) {
        textViewStatus.setText("状态: AI 正在云端分析药单...");
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
        
        MultipartBody.Part profilePart = null; 
        if (currentUserProfile != null) {
            Gson gson = new Gson();
            String profileJson = gson.toJson(currentUserProfile);
            RequestBody profileBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), profileJson);
            profilePart = MultipartBody.Part.createFormData("user_profile", "user_profile.json", profileBody);
        }
        
        RetrofitClient.getApiService().processPrescription(body, profilePart).enqueue(new Callback<HealthResponse>() {
            @Override
            public void onResponse(Call<HealthResponse> call, Response<HealthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HealthResponse result = response.body();
                    
                    if (result.isSuccess()) {
                        textViewStatus.setText("状态: AI 分析完成！");
                        textViewDiagnosis.setText("诊断结论: " + (result.getDiagnosis() != null ? result.getDiagnosis() : "未明确"));
                        textViewNotes.setText("健康建议: " + (result.getNotes() != null ? result.getNotes() : "无额外说明"));

                        currentSafetyReport = result.getSafetyReport();
                        updateSafetyReportDisplay();

                        if (result.getMedicationPlan() != null) {
                            for (MedicinePlan plan : result.getMedicationPlan()) {
                                saveToDatabase(plan.getName(), plan.getDosage(), plan.getFrequency());
                            }
                        }
                    } else {
                        String errorMessage = result.getError() != null && !result.getError().isEmpty() ? result.getError() : "未能识别处方信息";
                        textViewStatus.setText("状态: 识别失败");
                        textViewDiagnosis.setText("诊断结论: 识别失败");
                        textViewNotes.setText("错误信息: " + errorMessage);
                        textViewSafetyReport.setVisibility(View.GONE); 
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    textViewStatus.setText("状态: 服务器错误 (" + response.code() + ")");
                    textViewDiagnosis.setText("诊断结论: 请求失败");
                    textViewSafetyReport.setVisibility(View.GONE); 
                }
            }
            @Override
            public void onFailure(Call<HealthResponse> call, Throwable t) {
                textViewStatus.setText("状态: 网络请求失败");
                textViewSafetyReport.setVisibility(View.GONE); 
            }
        });
    }

    private void updateSafetyReportDisplay() {
        if (currentSafetyReport != null) {
            if (!currentSafetyReport.isSafe()) {
                String overallMessage = currentSafetyReport.getOverallMessage();
                if (overallMessage != null && !overallMessage.isEmpty()) {
                    textViewSafetyReport.setText("安全警告: " + overallMessage);
                    textViewSafetyReport.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_dark)); 
                    textViewSafetyReport.setVisibility(View.VISIBLE);
                } else {
                    textViewSafetyReport.setVisibility(View.GONE);
                }
            } else {
                textViewSafetyReport.setText("安全报告: ✅ 未发现严重风险 (点击查看详情)");
                textViewSafetyReport.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_green_dark));
                textViewSafetyReport.setVisibility(View.VISIBLE);
            }
        } else {
            textViewSafetyReport.setVisibility(View.GONE);
        }
    }

    private void showSafetyReportDetails() {
        if (currentSafetyReport == null) {
            Toast.makeText(this, "暂无安全报告信息可供查看", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("详细安全报告");
        StringBuilder reportDetails = new StringBuilder();
        if (!currentSafetyReport.isSafe()) {
            reportDetails.append("\n风险等级: 存在风险\n");
            if (currentSafetyReport.getOverallMessage() != null && !currentSafetyReport.getOverallMessage().isEmpty()) {
                reportDetails.append("总体警告: ").append(currentSafetyReport.getOverallMessage()).append("\n");
            }
        } else {
            reportDetails.append("\n风险等级: 安全\n");
        }
        List<Interaction> interactions = currentSafetyReport.getInteractions();
        if (interactions != null && !interactions.isEmpty()) {
            reportDetails.append("\n--- 药物相互作用 ---\n");
            for (Interaction interaction : interactions) {
                reportDetails.append("- ").append(interaction.getDrugA()).append(" & ").append(interaction.getDrugB());
                reportDetails.append(" (").append(interaction.getSeverity()).append("): ").append(interaction.getWarning()).append("\n");
            }
        }
        List<String> duplicates = currentSafetyReport.getDuplicates();
        if (duplicates != null && !duplicates.isEmpty()) {
            reportDetails.append("\n--- 重复用药 ---\n");
            for (String drug : duplicates) reportDetails.append("- ").append(drug).append("\n");
        }
        List<Risk> personalizedRisks = currentSafetyReport.getPersonalizedRisks();
        if (personalizedRisks != null && !personalizedRisks.isEmpty()) {
            reportDetails.append("\n--- 个性化风险 ---\n");
            for (Risk risk : personalizedRisks) {
                reportDetails.append("- ").append(risk.getRiskType()).append(" (").append(risk.getDrug()).append("): ").append(risk.getWarning()).append("\n");
            }
        }
        builder.setMessage(reportDetails.toString());
        builder.setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showProfileSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("配置健康档案 (个性化分析)");
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_profile_settings, null); 
        final EditText inputAllergies = viewInflated.findViewById(R.id.edit_allergies);
        final EditText inputWeight = viewInflated.findViewById(R.id.edit_weight);
        final EditText inputStatus = viewInflated.findViewById(R.id.edit_status); 
        if (currentUserProfile != null) {
            inputAllergies.setText(currentUserProfile.getAllergies() != null ? String.join(", ", currentUserProfile.getAllergies()) : "");
            if (currentUserProfile.getWeightKg() != null) inputWeight.setText(String.valueOf(currentUserProfile.getWeightKg()));
            inputStatus.setText(currentUserProfile.getPregnancyStatus() != null ? currentUserProfile.getPregnancyStatus() : "NONE");
        }
        builder.setView(viewInflated);
        builder.setPositiveButton("保存并启用分析", (dialog, which) -> {
            try {
                List<String> allergiesList = Arrays.asList(inputAllergies.getText().toString().split("\\s*,\\s*"));
                Double weight = inputWeight.getText().toString().isEmpty() ? null : Double.parseDouble(inputWeight.getText().toString());
                String status = inputStatus.getText().toString().trim();
                currentUserProfile = new UserProfile(weight, null, allergiesList.isEmpty() || (allergiesList.size() == 1 && allergiesList.get(0).isEmpty()) ? null : allergiesList, null, status.isEmpty() ? "NONE" : status);
                Toast.makeText(this, "用户档案已更新", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "输入无效", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
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