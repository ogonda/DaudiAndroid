package com.zeroq.daudi4native.vo

import androidx.annotation.NonNull
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.OnCompleteListener
import androidx.lifecycle.LiveData


open class CompletionLiveData : LiveData<Resource<Boolean>>(), OnCompleteListener<Void> {

    override fun onComplete(@NonNull task: Task<Void>) {
        value = if (task.isSuccessful) {
            Resource(true)
        } else {
            Resource(task.exception!!)
        }
    }
}