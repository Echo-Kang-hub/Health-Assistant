package com.example.healthassistant;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Auth
    @POST("api/v1/auth/register")
    Call<ApiResponse<TokenPair>> register(@Body AuthRequest request);

    @POST("api/v1/auth/login")
    Call<ApiResponse<TokenPair>> login(@Body AuthRequest request);

    @POST("api/v1/auth/refresh")
    Call<ApiResponse<TokenPair>> refresh(@Body RefreshRequest request);

    @POST("api/v1/auth/logout")
    Call<ApiResponse<Object>> logout(@Body RefreshRequest request);

    // Profile
    @GET("api/v1/me")
    Call<ApiResponse<UserProfile>> getProfile();

    @PUT("api/v1/me")
    Call<ApiResponse<UserProfile>> updateProfile(@Body UserProfile profile);

    @GET("api/v1/me/allergies")
    Call<ApiResponse<List<Map<String, Object>>>> getAllergies();

    @PUT("api/v1/me/allergies")
    Call<ApiResponse<List<Map<String, Object>>>> updateAllergies(@Body List<Map<String, Object>> allergies);

    // Medications
    @GET("api/v1/medications")
    Call<ApiResponse<PagedResponse<Medication>>> getMedications(
            @Query("query") String query,
            @Query("status") String status,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @POST("api/v1/medications")
    Call<ApiResponse<Medication>> createMedication(@Body MedicationRequest request);

    @GET("api/v1/medications/{id}")
    Call<ApiResponse<Medication>> getMedication(@Path("id") int id);

    @PUT("api/v1/medications/{id}")
    Call<ApiResponse<Medication>> updateMedication(@Path("id") int id, @Body MedicationRequest request);

    @DELETE("api/v1/medications/{id}")
    Call<ApiResponse<Object>> deleteMedication(@Path("id") int id);

    // Recognition
    @Multipart
    @POST("api/v1/recognitions")
    Call<ApiResponse<RecognitionStartResponse>> createRecognition(
            @Header("Idempotency-Key") String idempotencyKey,
            @Part MultipartBody.Part image,
            @Part("barcodeText") RequestBody barcodeText
    );

    @GET("api/v1/recognitions/{id}")
    Call<ApiResponse<Recognition>> getRecognition(@Path("id") int id);

    @POST("api/v1/recognitions/{id}/confirm")
    Call<ApiResponse<RecognitionConfirmResponse>> confirmRecognition(
            @Path("id") int id,
            @Body RecognitionConfirmRequest request
    );

    // Plans
    @GET("api/v1/plans")
    Call<ApiResponse<PagedResponse<Plan>>> getPlans(
            @Query("active") Boolean active,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @POST("api/v1/plans")
    Call<ApiResponse<Plan>> createPlan(@Body PlanRequest request);

    @GET("api/v1/plans/{id}")
    Call<ApiResponse<Plan>> getPlan(@Path("id") int id);

    @PUT("api/v1/plans/{id}")
    Call<ApiResponse<Plan>> updatePlan(@Path("id") int id, @Body PlanRequest request);

    @POST("api/v1/plans/{id}/occurrences/next")
    Call<ApiResponse<OccurrenceResponse>> getNextOccurrences(
            @Path("id") int id,
            @Body OccurrenceRequest request
    );

    // Dose logs
    @GET("api/v1/dose-logs")
    Call<ApiResponse<PagedResponse<DoseLog>>> getDoseLogs(
            @Query("from") String from,
            @Query("to") String to,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @POST("api/v1/dose-logs")
    Call<ApiResponse<DoseLog>> createDoseLog(@Body DoseLogRequest request);

    // Risks
    @GET("api/v1/risks")
    Call<ApiResponse<PagedResponse<Risk>>> getRisks(
            @Query("active") Boolean active,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    @GET("api/v1/risks/{id}")
    Call<ApiResponse<Risk>> getRisk(@Path("id") int id);

    // Metrics
    @GET("api/v1/metrics")
    Call<ApiResponse<PagedResponse<Metric>>> getMetrics(
            @Query("type") String type,
            @Query("from") String from,
            @Query("to") String to,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    // Data sources
    @GET("api/v1/data-sources")
    Call<ApiResponse<List<DataSource>>> getDataSources();

    @PUT("api/v1/data-sources/{id}")
    Call<ApiResponse<DataSource>> updateDataSource(@Path("id") int id, @Body DataSourceUpdateRequest request);

    // Reports
    @POST("api/v1/reports/generate")
    Call<ApiResponse<ReportJob>> generateReport(@Header("Idempotency-Key") String idempotencyKey,
                                                @Body ReportGenerateRequest request);

    @GET("api/v1/reports/jobs/{id}")
    Call<ApiResponse<ReportJob>> getReportJob(@Path("id") int id);

    @GET("api/v1/reports")
    Call<ApiResponse<PagedResponse<Report>>> getReports(
            @Query("range") String range,
            @Query("from") String from,
            @Query("to") String to,
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );

    // Demo mode
    @POST("api/v1/demo/enable")
    Call<ApiResponse<Object>> enableDemo();

    @POST("api/v1/demo/disable")
    Call<ApiResponse<Object>> disableDemo();

    // AI
    @POST("api/v1/ai/chat")
    Call<ApiResponse<ChatResponse>> chat(@Body ChatRequest request);

    @POST("api/v1/ai/recognitions/reparse")
    Call<ApiResponse<RecognitionReparseResponse>> reparseRecognition(@Body RecognitionReparseRequest request);

    @POST("api/v1/ai/plans/recommend-times")
    Call<ApiResponse<PlanRecommendResponse>> recommendTimes(@Body PlanRecommendRequest request);

    @POST("api/v1/ai/adherence/tips")
    Call<ApiResponse<AdherenceTipsResponse>> adherenceTips(@Body AdherenceTipsRequest request);

    @POST("api/v1/ai/daily-tips")
    Call<ApiResponse<DailyTipsResponse>> dailyTips(@Body DailyTipsRequest request);

    @POST("api/v1/ai/reports/summary")
    Call<ApiResponse<ReportSummaryResponse>> reportSummary(@Body ReportSummaryRequest request);

    @POST("api/v1/ai/risks/explain")
    Call<ApiResponse<RiskExplainResponse>> riskExplain(@Body RiskExplainRequest request);
}
