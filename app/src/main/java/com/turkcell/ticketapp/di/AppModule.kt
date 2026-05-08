package com.turkcell.ticketapp.di

import com.turkcell.ticketapp.presentation.auth.login.LoginViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// app katmanına ait DI bildirimleri (ViewModel'lar vb).
// AuthRepository binding'i dataModule'da tanımlandığı için
// LoginViewModel constructor'ına Koin tarafından otomatik enjekte edilir.
val appModule = module {
    viewModel { LoginViewModel(authRepository = get()) }
}
