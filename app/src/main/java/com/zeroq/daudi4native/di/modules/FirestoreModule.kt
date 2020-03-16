package com.zeroq.daudi4native.di.modules

import com.google.firebase.firestore.CollectionReference
import dagger.Module
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton


@Module
class FirestoreModule {


    @Singleton
    @Provides
    fun providesFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun providesFireStoreStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    fun providesStorageReference(storage: FirebaseStorage): StorageReference {
        return storage.reference
    }


    @Singleton
    @Provides
    @Named("admins")
    fun providesAdmins(fireStore: FirebaseFirestore): CollectionReference {
        return fireStore.collection("admins")
    }

    @Singleton
    @Provides
    @Named("depots")
    fun providesDepots(fireStore: FirebaseFirestore): CollectionReference {
        return fireStore.collection("depots")
    }


    @Singleton
    @Provides
    @Named("omc")
    fun providesOmc(fireStore: FirebaseFirestore): CollectionReference {
        return fireStore.collection("omc")
    }
}