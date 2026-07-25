package com.example.sepotify

import android.app.Application
import com.example.sepotify.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SepotifyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SepotifyApplication)
            modules(appModule)
        }
    }
}