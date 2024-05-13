package com.samsung.android.sdk.spay.sample.issuer

import android.app.Application
import android.util.Log
import java.lang.reflect.InvocationTargetException

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.IS_LEAK_CANARY_ENABLED) {
            var clazz: Class<*>? = null
            try {
                clazz = Class.forName("com.squareup.leakcanary.LeakCanary")
                val method = clazz.getDeclaredMethod("install", Application::class.java)
                method.invoke(null, this)
            } catch (e: ClassNotFoundException) {
                Log.d(TAG, "${e.message}")
            } catch (e: NoSuchMethodException) {
                Log.d(TAG, "${e.message}")
            } catch (e: IllegalAccessException) {
                Log.d(TAG, "${e.message}")
            } catch (e: InvocationTargetException) {
                Log.d(TAG, "${e.message}")
            }
        }
    }

    companion object {
        private val TAG = MainApplication::class.java.simpleName
    }
}