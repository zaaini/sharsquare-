package com.example.ss;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface YourApiService {

    @Multipart
    @POST("uploadA")
    Call<UploadResponse> uploadFile(
            @Part MultipartBody.Part file,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude
    );
}
