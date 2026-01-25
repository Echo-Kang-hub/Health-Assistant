package com.example.healthassistant;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    /**
     * 上传图片并获取 AI 处理结果
     * @param image 对应后端的 'image' 字段
     */
    @Multipart
    @POST("/api/process_prescription")
    Call<HealthResponse> processPrescription(@Part MultipartBody.Part image);
}