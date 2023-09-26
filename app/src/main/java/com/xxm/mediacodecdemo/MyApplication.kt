package com.xxm.mediacodecdemo

import android.app.Application
import android.content.Context
import kotlin.properties.Delegates

/**
 *Time:2023/9/22
 *Author:zhangwenshuan
 *Description:
 *since version
 *
 */
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        context=applicationContext
    }
    companion object{
        var context: Context by Delegates.notNull()
    }
}