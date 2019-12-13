package com.zeroq.daudi4native.data.models

import androidx.annotation.NonNull
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties


/**
 * A Base Model to be extended by other models to add ids.
 */

@IgnoreExtraProperties
open class Model {

    @Exclude
    var snapshotid: String? = null

    fun <T : Model> withSnapshotId(@NonNull id: String): T {
        snapshotid = id
        return this as T
    }
}