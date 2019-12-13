package com.zeroq.daudi4native.data.repository

import com.google.firebase.firestore.CollectionReference
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.vo.CompletionLiveData
import com.zeroq.daudi4native.vo.DocumentLiveData
import javax.inject.Inject
import javax.inject.Named

class AdminRepository @Inject
constructor(@Named("admins") val admins: CollectionReference) {


    fun getAdmin(id: String): DocumentLiveData<UserModel> {
        val adminRef = admins.document(id)
        val data = DocumentLiveData(adminRef, UserModel::class.java)
        adminRef.addSnapshotListener(data)

        return data
    }

    fun postFcmToken(adminId: String, token: String): CompletionLiveData {
        val adminRef = admins.document(adminId)
        val completion = CompletionLiveData()

        adminRef.update("fcmtokens.apk", token)
            .addOnCompleteListener(completion)

        return completion
    }
}