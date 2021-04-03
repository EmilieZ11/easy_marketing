package uk.easys.easymarketing;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

class MultiDexApplication : Application() {
    override protected fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
