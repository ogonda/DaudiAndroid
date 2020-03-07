package com.zeroq.daudi4native.data.repository

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.zeroq.daudi4native.data.models.OrderModel
import com.zeroq.daudi4native.vo.CompletionLiveData
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.*
import javax.inject.Inject

class UploadRepository @Inject constructor(
    val firestoreStorage: FirebaseStorage
) {

    fun uploadNote(bitmap: Bitmap, order: OrderModel): Pair<UploadTask, StorageReference> {
        val storageRef = firestoreStorage.reference
        // my 1st sample data
        val ImageRef =
            storageRef.child("deliverynote/${order.Id}/${order.QbConfig!!.InvoiceId}_${Date().time}.jpeg")


        val baos = ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()



        return Pair(ImageRef.putBytes(data), ImageRef);
    }
}