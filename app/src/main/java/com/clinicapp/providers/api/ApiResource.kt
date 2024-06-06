package com.clinicapp.providers.api

import com.clinicapp.models.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*


interface ApiResource {
    @POST("clinic/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("patient/register")
    fun addPatient(@Body request: AddPatientRequest): Call<AddPatientResponse>

    @POST("patient/search")
    fun searchPatient(@Body request: AddPatientRequest): Call<SearchPatientsResponse>

    @POST("hair_analysis/create")
    fun createHairAnalysis(@Body request: HairAnalysisRequest): Call<HairAnalysisResponse>

    @GET("hair_analysis/camera-guide/{id}")
    fun getCameraMeasurement( @Path("id") id: Long): Call<CameraMeasurementResponse>

    @POST("hair_analysis/camera-guide/{id}")
    fun setCameraMeasurement(
            @Path("id") id: Long,
            @Body request: CameraMeasurementRequest): Call<CameraMeasurementResponse>

    @Multipart
    @POST("hair_analysis/{analysisId}/images/upload")
    fun uploadImage(@Part mediaTye:MultipartBody.Part, @Part subType:MultipartBody.Part, @Part image:MultipartBody.Part,@Path( "analysisId") analysisID:Long): Call<UploadImageResponse>


    // Glo

    @POST("face_analysis/create")
    fun createFaceAnalysis(@Body request: FaceAnalysisRequest): Call<FaceAnalysisResponse>

    @Multipart
    @POST("face_analysis/{analysisId}/upload-image")
    fun uploadFaceGlobalImages(@Part type:MultipartBody.Part, @Part position:MultipartBody.Part,
                               @Part image:MultipartBody.Part,@Part x_axis:MultipartBody.Part,
                               @Part y_axis:MultipartBody.Part,
                               @Path("analysisId") analysisID:Long): Call<UploadFaceImageResponse>

    @Multipart
    @POST("face_analysis/{analysisId}/upload-image")
    fun uploadFaceCloseupImages(@Part type:MultipartBody.Part, @Part position:MultipartBody.Part,
                                @Part area:MultipartBody.Part,@Part x_axis:MultipartBody.Part,
                                @Part y_axis:MultipartBody.Part,@Part is_brown_spot:MultipartBody.Part,
                                @Part is_red_spot:MultipartBody.Part,@Part is_pores:MultipartBody.Part,
                                @Part image:MultipartBody.Part,@Path( "analysisId") analysisID:Long): Call<UploadFaceImageResponse>

}