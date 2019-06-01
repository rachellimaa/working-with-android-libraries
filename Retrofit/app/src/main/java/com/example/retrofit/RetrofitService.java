package com.example.retrofit;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RetrofitService {

    @GET("generate-data?")
    Call<RespostaServidor> getURL(@Query("token") String token);

    @Multipart
    @POST("submit-solution?")
    Call<ResponseBody> postFile(@Query("token") String token, @Part MultipartBody.Part file);
}
