package com.example.ecampus

import android.app.Application
import com.example.ecampus.utils.ServiceLocator
import com.tencent.mmkv.MMKV

class ECampusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        ServiceLocator.init(this)
    }
}
