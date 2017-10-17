package com.nilhcem.kidsroom.device

import android.arch.lifecycle.ViewModel
import android.util.Log
import com.nilhcem.kidsroom.device.components.ButtonsLiveData
import com.nilhcem.kidsroom.device.components.Rc522LiveData

class MainViewModel : ViewModel() {

    companion object {
        private val TAG = MainViewModel::class.java.simpleName!!
    }

    val rc522LiveData by lazy { Rc522LiveData() }
    val buttonsLiveData by lazy { ButtonsLiveData() }

    override fun onCleared() {
        Log.i(TAG, "onCleared")
    }
}
