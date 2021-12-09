package com.ruben.composeanimation.data

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * Created by Ruben Quadros on 08/12/21
 **/
interface DownloadService {

    @Streaming
    @GET
    fun downloadAsset(@Url url: String): Call<ResponseBody>
}