package com.example.healthassistant;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    /**
     * 上传图片并获取 AI 处理结果
     */
    @Multipart
    @POST("/api/process_prescription")
    Call<HealthResponse> processPrescription(
            @Part MultipartBody.Part image,
            @Part MultipartBody.Part userProfile
    );

    /**
     * 获取每日健康更新报告
     * 后端指示: 实现 GET /api/daily_report 接口
     */
    @GET("/api/daily_report")
    Call<DailyReportResponse> getDailyReport();
}