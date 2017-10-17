package com.nilhcem.kidsroom

import android.arch.lifecycle.ViewModel
import android.util.Log

class MainViewModel : ViewModel() {

    companion object {
        private val TAG = MainViewModel::class.java.simpleName!!
    }

    val rc522LiveData by lazy { Rc522LiveData() }

    override fun onCleared() {
        Log.i(TAG, "onCleared")
    }
}
