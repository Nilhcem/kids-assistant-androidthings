package com.nilhcem.kidsroom

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName!!
    }

    private val viewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        viewModel.rc522LiveData.observe(this, Observer { uid ->
            Log.i(TAG, "Uid=$uid")
        })
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
    }
}
