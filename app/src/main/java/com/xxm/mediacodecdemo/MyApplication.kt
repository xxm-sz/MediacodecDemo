package com.xxm.mediacodecdemo

import android.app.Application
import android.content.Context
import android.widget.Toast
import kotlin.properties.Delegates

/**
 *Time:2023/9/22
 *Author:zhangwenshuan
 *Description:
 *since version
 *
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        var context: Context by Delegates.notNull()
    }
}

fun Context.showToast(msg:String){
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}