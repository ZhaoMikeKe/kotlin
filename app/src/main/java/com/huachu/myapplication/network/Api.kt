package com.huachu.myapplication.network

import com.huachu.myapplication.bean.ResultBean
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


interface CallAdapterApiService {
    @GET("data/iOS/2/1")
    fun getIOSGank(): Deferred<ResultBean>

    @GET("data/Android/2/1")
    fun getAndroidGank(): Deferred<ResultBean>
}

class ApiSource {
    companion object {
        @JvmField
        val callAdapterInstance = Retrofit.Builder()
                .baseUrl("http://gank.io/api/")
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(CallAdapterApiService::class.java)

    }
}