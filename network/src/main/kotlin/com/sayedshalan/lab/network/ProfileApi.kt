package com.sayedshalan.lab.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

data class Profile(
    val name: String,
)

interface ProfileApi {
    @GET("profile")
    suspend fun profile(): Profile
}

fun profileApi(
    baseUrl: String,
    client: OkHttpClient = OkHttpClient(),
): ProfileApi {
    val moshi =
        Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    return Retrofit
        .Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(ProfileApi::class.java)
}
