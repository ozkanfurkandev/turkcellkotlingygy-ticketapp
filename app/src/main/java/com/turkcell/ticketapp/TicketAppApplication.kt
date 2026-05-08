package com.turkcell.ticketapp

import android.app.Application
import com.turkcell.data.di.dataModule
import com.turkcell.ticketapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

// Uygulama başladığında Activity'lerden önce oluşturulur.
// Singleton (Tek bir instance olarak memory'de kalır)
// Uygulama kapanana kadar yok edilmez.
class TicketAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TicketAppApplication)
            // Tüm modülleri Koin grafına ekliyoruz.
            // dataModule -> Retrofit, AuthApi, AuthRepository
            // appModule  -> ViewModel'lar
            modules(dataModule, appModule)
        }
    }
}
