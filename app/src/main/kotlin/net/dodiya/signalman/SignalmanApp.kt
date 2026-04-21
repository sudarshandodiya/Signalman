package net.dodiya.signalman

import android.app.Application
import net.dodiya.signalman.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SignalmanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@SignalmanApp)
            modules(appModule)
        }
    }
}
