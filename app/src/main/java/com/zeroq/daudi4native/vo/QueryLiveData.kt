package com.zeroq.daudi4native.vo


import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.zeroq.daudi4native.data.models.Model
import java.util.*


class QueryLiveData<T : Model>(private val query: Query, private val type: Class<T>) : LiveData<Resource<List<T>>>(),
    EventListener<QuerySnapshot> {
    private var registration: ListenerRegistration? = null

    override fun onEvent(snapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            value = Resource(e)
            return
        }
        value = Resource(documentToList(snapshots!!))
    }


    override fun onActive() {
        super.onActive()
        registration = query.addSnapshotListener(this)
    }

    override fun onInactive() {
        super.onInactive()
        if (registration != null) {
            registration!!.remove()
            registration = null
        }
    }

    @NonNull
    private fun documentToList(snapshots: QuerySnapshot): List<T> {
        val retList = ArrayList<T>()
        if (snapshots.isEmpty) {
            return retList
        }

        for (document in snapshots.documents) {
            retList.add(document.toObject(type)!!.withSnapshotId(document.id))
        }

        return retList
    }
}
