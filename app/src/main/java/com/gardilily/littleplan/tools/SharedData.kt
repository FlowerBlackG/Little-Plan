package com.gardilily.littleplan.tools

import android.app.Application
import android.content.Context

/**
 * Shared Data is a class to store data that should be shared across all activities in the project
 */
class SharedData : Application() {

    /**
     * To store the data of tasks
     */
    lateinit var store: TodoStore

    /**
     * Determine if Main Activity should reload all task cards.
     * "True" means Main Activity should clear all cards and create them again.
     */
    var localDataLegacy = true

    /**
     * Suggested to be called as soon as the application created.
     */
    fun initiate(c: Context) {
        store = TodoStore(c)
        store.initiate()
    }
}
