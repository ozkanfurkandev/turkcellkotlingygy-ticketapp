package com.turkcell.data.di

import com.turkcell.core.domain.AuthRepository
import com.turkcell.data.remote.AuthApi
import com.turkcell.data.repository.AuthRepositoryImpl
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

// Koin'in nesneleri nasıl üreteceğini öğrendiği yer.
// single  -> uygulama yaşam süresi boyunca tek instance (singleton)
// factory -> her get() çağrısında yeni instance
val dataModule = module {

    // JSON parser. Retrofit converter buradan beslenecek.
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    // İstek/yanıt loglarını Logcat'te görmek için interceptor.
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // OkHttp client. get() ile yukarıdaki HttpLoggingInterceptor'ı çekiyoruz.
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    // Retrofit. Uzak (remote) Tickets API'si.
    // Swagger: https://tickets-api.halitkalayci.com/docs/#/
    single {
        val json = get<Json>()
        Retrofit.Builder()
            .baseUrl("https://tickets-api.halitkalayci.com/")
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // AuthApi'yi Retrofit'ten üretiyoruz.
    single<AuthApi> { get<Retrofit>().create(AuthApi::class.java) }

    // AuthRepositoryImpl'i, AuthRepository interface'ine bağlıyoruz.
    // İstek geldiğinde AuthApi'yi otomatik olarak get() ile çözüp constructor'a verir.
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}
