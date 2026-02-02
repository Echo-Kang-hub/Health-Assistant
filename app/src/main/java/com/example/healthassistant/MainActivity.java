package com.example.healthassistant;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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
    private static final String PREFS_NAME = "HealthAssistantPrefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    private ImageView imageView;
    private TextView textViewStatus;
    private TextView textViewDiagnosis;
    private TextView textViewNotes;
    private TextView textViewSafetyReport;
    private TextView textViewReportContent; 
    private TextView textViewReportDate;    
    private View buttonSettings; 
    private ImageView buttonThemeToggle; // 修改为 ImageView 以便设置 src
    private Uri photoUri;
    private File currentPhotoFile;

    // 统计面板 View
    private TextView textMedicineCount;
    private View layoutEmptyState;
    private View headerBackground; // 头部背景 View
    private View rootView; // 根布局，用于做颜色动画

    // 选择模式相关 View
    private MaterialCardView cardSelectionActions;
    private CheckBox checkboxAll;
    private TextView textSelectionCount;
    private Button buttonDeleteSelected;
    private ImageButton buttonCloseSelection;
    private MaterialCardView cardPhotoPreview;
    private ExtendedFloatingActionButton buttonCapture;

    private AppDatabase db;
    private MedicineAdapter adapter;
    private RecyclerView recyclerView;

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
        // 1. 在 super.onCreate 之前应用保存的主题设置
        applySavedTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        db = AppDatabase.getDatabase(this);
        rootView = findViewById(android.R.id.content); // 获取根视图
        imageView = findViewById(R.id.imageView_photo);
        textViewStatus = findViewById(R.id.textView_status);
        textViewDiagnosis = findViewById(R.id.textView_diagnosis);
        textViewNotes = findViewById(R.id.textView_notes);
        textViewSafetyReport = findViewById(R.id.textView_safety_report); 
        textViewReportContent = findViewById(R.id.textView_report_content); 
        textViewReportDate = findViewById(R.id.textView_report_date);       
        buttonSettings = findViewById(R.id.button_settings);
        buttonThemeToggle = findViewById(R.id.button_theme_toggle); // 绑定明暗切换按钮
        buttonCapture = findViewById(R.id.button_capture);
        recyclerView = findViewById(R.id.recyclerView_medicines);
        
        // 新增 UI 元素绑定
        textMedicineCount = findViewById(R.id.text_medicine_count);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        cardPhotoPreview = findViewById(R.id.card_photo_preview);
        headerBackground = findViewById(R.id.view_header_background); // 需要在 XML 中给头部 View 加 ID

        // 初始化选择模式 View
        cardSelectionActions = findViewById(R.id.card_selection_actions);
        checkboxAll = findViewById(R.id.checkbox_all);
        textSelectionCount = findViewById(R.id.text_selection_count);
        buttonDeleteSelected = findViewById(R.id.button_delete_selected);
        buttonCloseSelection = findViewById(R.id.button_close_selection);

        adapter = new MedicineAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 设置 RecyclerView 动画
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(animation);

        adapter.setOnItemClickListener(this::showEditDialog);
        adapter.setOnItemLongClickListener(medicine -> {
            // 长按不再弹出单个删除对话框，逻辑已移动到适配器进入选择模式
        });

        adapter.setOnReminderClickListener(this::handleReminderClick);

        // 设置选择模式监听
        adapter.setOnSelectionModeListener(new MedicineAdapter.OnSelectionModeListener() {
            @Override
            public void onSelectionModeChanged(boolean enabled) {
                if (enabled) {
                    cardSelectionActions.setVisibility(View.VISIBLE);
                    cardSelectionActions.setAlpha(0f);
                    cardSelectionActions.animate().alpha(1f).setDuration(300).start();
                } else {
                    cardSelectionActions.animate().alpha(0f).setDuration(300).withEndAction(() -> 
                        cardSelectionActions.setVisibility(View.GONE)
                    ).start();
                    checkboxAll.setChecked(false);
                }
            }

            @Override
            public void onSelectionCountChanged(int count) {
                textSelectionCount.setText("已选 " + count + " 项");
                buttonDeleteSelected.setEnabled(count > 0);
            }
        });

        // 全选复选框逻辑
        checkboxAll.setOnClickListener(v -> {
            adapter.selectAll(checkboxAll.isChecked());
        });

        // 删除选中项逻辑
        buttonDeleteSelected.setOnClickListener(v -> {
            List<Medicine> selected = adapter.getSelectedItems();
            if (selected.isEmpty()) return;

            new AlertDialog.Builder(this)
                    .setTitle("批量删除")
                    .setMessage("确定要删除选中的 " + selected.size() + " 条记录吗？")
                    .setPositiveButton("删除", (dialog, which) -> deleteMultipleMedicines(selected))
                    .setNegativeButton("取消", null)
                    .show();
        });

        // 退出选择模式逻辑
        buttonCloseSelection.setOnClickListener(v -> {
            adapter.setSelectionMode(false);
        });

        buttonCapture.setOnClickListener(v -> checkAndRequestPermissions());
        buttonSettings.setOnClickListener(v -> showProfileSettingsDialog());
        
        // 明暗切换逻辑
        updateThemeToggleIcon(); // 初始化图标
        buttonThemeToggle.setOnClickListener(v -> toggleThemeWithAnimation());

        // 点击安全报告查看详情
        textViewSafetyReport.setOnClickListener(v -> showSafetyReportDetails());

        refreshMedicineList();
        updateSafetyReportDisplay();
        fetchDailyReport(); 

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int mode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private void updateThemeToggleIcon() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            buttonThemeToggle.setImageResource(R.drawable.ic_sun); // 暗色模式显示太阳（切换到亮色）
        } else {
            buttonThemeToggle.setImageResource(R.drawable.ic_moon); // 亮色模式显示月亮（切换到暗色）
        }
    }

    private void toggleThemeWithAnimation() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        int newMode = (currentMode == AppCompatDelegate.MODE_NIGHT_YES) 
                ? AppCompatDelegate.MODE_NIGHT_NO 
                : AppCompatDelegate.MODE_NIGHT_YES;

        // 保存新状态
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_THEME_MODE, newMode);
        editor.apply();

        // 关键修改：不使用 recreate()，而是手动触发颜色动画
        // 注意：AppCompatDelegate.setDefaultNightMode 实际上会触发 Activity 重建
        // 为了实现"同屏平滑变色"，我们需要拦截重建，或者使用自定义的颜色动画覆盖
        // 但 Android 原生主题切换机制决定了必须重建 Activity 才能应用所有资源（如 drawable, styles）
        // 真正的"同屏平滑变色"通常需要自定义 View 系统或 Compose
        // 这里我们采用折中方案：使用 Window 动画让重建过程看起来像是在同屏发生
        
        // 1. 获取当前屏幕截图作为遮罩 (可选，这里简化为淡出)
        // 2. 设置 Window 动画为淡入淡出
        getWindow().setWindowAnimations(android.R.style.Animation_Activity);
        
        // 3. 切换模式
        AppCompatDelegate.setDefaultNightMode(newMode);
        
        // 4. 重启 Activity (必须步骤)
        // 使用 overridePendingTransition 实现淡入淡出，模拟"原地变色"
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(getIntent());
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * 动态更新主题颜色
     * @param riskLevel 0: Safe (Green), 1: Low Risk (Blue), 2: Medium Risk (Yellow), 3: High Risk (Orange)
     */
    private void updateThemeColor(int riskLevel) {
        int startColorRes, endColorRes, primaryColorRes;

        switch (riskLevel) {
            case 1: // Low Risk (Blue)
                startColorRes = R.color.risk_low;
                endColorRes = R.color.risk_low_end;
                primaryColorRes = R.color.risk_low;
                break;
            case 2: // Medium Risk (Yellow)
                startColorRes = R.color.risk_medium;
                endColorRes = R.color.risk_medium_end;
                primaryColorRes = R.color.risk_medium;
                break;
            case 3: // High Risk (Orange)
                startColorRes = R.color.risk_high;
                endColorRes = R.color.risk_high_end;
                primaryColorRes = R.color.risk_high;
                break;
            case 0: // Safe (Green) - Default
            default:
                startColorRes = R.color.risk_safe;
                endColorRes = R.color.risk_safe_end;
                primaryColorRes = R.color.risk_safe;
                break;
        }

        int newStartColor = ContextCompat.getColor(this, startColorRes);
        int newEndColor = ContextCompat.getColor(this, endColorRes);
        int newPrimaryColor = ContextCompat.getColor(this, primaryColorRes);

        // 1. 更新头部渐变背景
        if (headerBackground != null) {
            // 使用 ValueAnimator 实现颜色平滑过渡
            // 注意：这里简化处理，直接设置新背景，如果需要更平滑，可以对 GradientDrawable 的颜色属性做动画
            GradientDrawable gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{newStartColor, newEndColor});
            gradientDrawable.setCornerRadii(new float[]{0, 0, 0, 0, 32 * getResources().getDisplayMetrics().density, 32 * getResources().getDisplayMetrics().density, 32 * getResources().getDisplayMetrics().density, 32 * getResources().getDisplayMetrics().density});
            
            // 简单的淡入过渡
            headerBackground.animate().alpha(0.5f).setDuration(150).withEndAction(() -> {
                headerBackground.setBackground(gradientDrawable);
                headerBackground.animate().alpha(1f).setDuration(150).start();
            }).start();
        }

        // 2. 更新按钮颜色
        if (buttonCapture != null) {
            buttonCapture.setBackgroundTintList(ColorStateList.valueOf(newPrimaryColor));
        }
        
        // 3. 更新选择模式卡片颜色
        if (cardSelectionActions != null) {
            cardSelectionActions.setCardBackgroundColor(newPrimaryColor);
        }
    }

    private void deleteMultipleMedicines(List<Medicine> medicines) {
        new Thread(() -> {
            for (Medicine m : medicines) {
                ReminderManager.cancelReminder(this, m);
                db.medicineDao().delete(m);
            }
            runOnUiThread(() -> {
                adapter.setSelectionMode(false);
                refreshMedicineList();
                Toast.makeText(this, "已删除选中记录", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void fetchDailyReport() {
        RetrofitClient.getApiService().getDailyReport().enqueue(new Callback<DailyReportResponse>() {
            @Override
            public void onResponse(Call<DailyReportResponse> call, Response<DailyReportResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DailyReportResponse report = response.body();
                    if (report.isSuccess()) {
                        textViewReportContent.setText(report.getSummary());
                        textViewReportDate.setText(report.getReportDate());
                        
                        // 简单的淡入动画
                        textViewReportContent.setAlpha(0f);
                        textViewReportContent.animate().alpha(1f).setDuration(500).start();
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
            cardPhotoPreview.setVisibility(View.VISIBLE); // 拍照后显示预览卡片
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

//    private void uploadImage(File file) {
//        textViewStatus.setText("状态: AI 正在云端分析药单...");
//        // 重置为默认颜色 (青绿)
//        updateThemeColor(0);
//
//        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
//        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
//
//        MultipartBody.Part profilePart = null;
//        if (currentUserProfile != null) {
//            Gson gson = new Gson();
//            String profileJson = gson.toJson(currentUserProfile);
//            RequestBody profileBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), profileJson);
//            profilePart = MultipartBody.Part.createFormData("user_profile", "user_profile.json", profileBody);
//        }
//
//        RetrofitClient.getApiService().processPrescription(body, profilePart).enqueue(new Callback<HealthResponse>() {
//            @Override
//            public void onResponse(Call<HealthResponse> call, Response<HealthResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    HealthResponse result = response.body();
//
//                    if (result.isSuccess()) {
//                        textViewStatus.setText("状态: AI 分析完成！");
//                        textViewDiagnosis.setText("诊断结论: " + (result.getDiagnosis() != null ? result.getDiagnosis() : "未明确"));
//                        textViewNotes.setText("健康建议: " + (result.getNotes() != null ? result.getNotes() : "无额外说明"));
//
//                        currentSafetyReport = result.getSafetyReport();
//                        updateSafetyReportDisplay();
//
//                        if (result.getMedicationPlan() != null) {
//                            for (MedicinePlan plan : result.getMedicationPlan()) {
//                                saveToDatabase(plan.getName(), plan.getDosage(), plan.getFrequency());
//                            }
//                        }
//                    } else {
//                        String errorMessage = result.getError() != null && !result.getError().isEmpty() ? result.getError() : "未能识别处方信息";
//                        textViewStatus.setText("状态: 识别失败");
//                        textViewDiagnosis.setText("诊断结论: 识别失败");
//                        textViewNotes.setText("错误信息: " + errorMessage);
//                        textViewSafetyReport.setVisibility(View.GONE);
//                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
//                    }
//                } else {
//                    textViewStatus.setText("状态: 服务器错误 (" + response.code() + ")");
//                    textViewDiagnosis.setText("诊断结论: 请求失败");
//                    textViewSafetyReport.setVisibility(View.GONE);
//                }
//            }
//            @Override
//            public void onFailure(Call<HealthResponse> call, Throwable t) {
//                textViewStatus.setText("状态: 网络请求失败");
//                textViewSafetyReport.setVisibility(View.GONE);
//            }
//        });
//    }

    private void uploadImage(File file) {
        textViewStatus.setText("状态: AI 正在云端分析药单...");
        // 重置为默认颜色 (青绿)
        updateThemeColor(0);

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

                        // ================== 修改开始：批量保存到数据库 ==================
                        List<MedicinePlan> plans = result.getMedicationPlan();
                        if (plans != null && !plans.isEmpty()) {
                            new Thread(() -> {
                                int count = 0;
                                for (MedicinePlan plan : plans) {
                                    // 1. 空值检查，防止 crash 或存入 null
                                    String name = plan.getName();
                                    // 只有当药名有效时才保存
                                    if (name != null && !name.trim().isEmpty()) {
                                        String dosage = plan.getDosage() != null ? plan.getDosage() : "未明确";
                                        String frequency = plan.getFrequency() != null ? plan.getFrequency() : "遵医嘱";

                                        // 2. 插入数据库
                                        db.medicineDao().insert(new Medicine(name, dosage, frequency, System.currentTimeMillis()));
                                        count++;
                                    }
                                }

                                // 3. 只有当确实有数据插入时，才刷新 UI
                                if (count > 0) {
                                    int finalCount = count;
                                    runOnUiThread(() -> {
                                        refreshMedicineList(); // 刷新列表
                                        Toast.makeText(MainActivity.this,
                                                "已自动将 " + finalCount + " 种药品加入药箱",
                                                Toast.LENGTH_LONG).show();
                                    });
                                }
                            }).start();
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this, "分析完成，但在处方中未发现明确的药品信息", Toast.LENGTH_SHORT).show()
                            );
                        }
                        // ================== 修改结束 ==================

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
                t.printStackTrace(); // 建议打印错误堆栈以便调试
                Toast.makeText(MainActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSafetyReportDisplay() {
        if (currentSafetyReport != null) {
            if (!currentSafetyReport.isSafe()) {
                String overallMessage = currentSafetyReport.getOverallMessage();
                if (overallMessage != null && !overallMessage.isEmpty()) {
                    textViewSafetyReport.setText("⚠️ 安全警告: " + overallMessage);
                    textViewSafetyReport.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.error)); 
                    textViewSafetyReport.setBackgroundResource(R.drawable.bg_warning_light);
                    textViewSafetyReport.setVisibility(View.VISIBLE);
                    
                    // 存在风险，根据严重程度变色
                    // 这里简单逻辑：只要不安全就视为高风险(橙色)，如果有更细粒度数据可调整
                    // 假设：有相互作用或过敏风险 -> 高风险 (3)
                    // 仅有重复用药 -> 中风险 (2)
                    // 仅有剂量异常 -> 低风险 (1)
                    
                    int riskLevel = 3; // 默认为高风险
                    if (currentSafetyReport.getInteractions() == null || currentSafetyReport.getInteractions().isEmpty()) {
                         if (currentSafetyReport.getPersonalizedRisks() == null || currentSafetyReport.getPersonalizedRisks().isEmpty()) {
                             // 没有相互作用和个性化风险，可能是重复用药
                             riskLevel = 2;
                         }
                    }
                    updateThemeColor(riskLevel);
                    
                } else {
                    textViewSafetyReport.setVisibility(View.GONE);
                    updateThemeColor(0); // 默认安全
                }
            } else {
                textViewSafetyReport.setText("✅ 安全报告: 未发现严重风险 (点击查看详情)");
                textViewSafetyReport.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.primary));
                textViewSafetyReport.setBackgroundResource(R.drawable.bg_tag_light);
                textViewSafetyReport.setVisibility(View.VISIBLE);
                updateThemeColor(0); // 安全
            }
            // 简单的淡入动画
            textViewSafetyReport.setAlpha(0f);
            textViewSafetyReport.animate().alpha(1f).setDuration(500).start();
        } else {
            textViewSafetyReport.setVisibility(View.GONE);
            updateThemeColor(0); // 默认
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
            runOnUiThread(() -> {
                adapter.setMedicines(list);
                // 更新统计数字
                textMedicineCount.setText(String.valueOf(list.size()));
                // 空状态处理
                if (list.isEmpty()) {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    layoutEmptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.scheduleLayoutAnimation();
                }
            });
        }).start();
    }
}