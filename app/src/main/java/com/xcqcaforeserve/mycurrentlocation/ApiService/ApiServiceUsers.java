package com.xcqcaforeserve.mycurrentlocation.ApiService;

import com.xcqcaforeserve.mycurrentlocation.Models.ImageResponse;

import io.reactivex.Single;
import retrofit2.http.GET;

public interface ApiServiceUsers {

    @GET("random")
    Single<ImageResponse> getImageResponse();

}