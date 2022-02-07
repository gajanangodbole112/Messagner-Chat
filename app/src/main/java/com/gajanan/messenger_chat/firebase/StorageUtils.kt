package com.gajanan.messenger_chat.firebase

import com.gajanan.messenger_chat.utils.Constants.KEY_USER_ID
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

object StorageUtils {

    private val storageIntance = FirebaseStorage.getInstance()
    private val currentRef : StorageReference
    get() = storageIntance.reference
        .child(KEY_USER_ID)
    fun uploadMessageImg(imageBytes: ByteArray,
                         onSuccess : (imagePath : String) -> Unit){

        val ref = currentRef.child("messages/${UUID.nameUUIDFromBytes(imageBytes)}")

            ref.putBytes(imageBytes)
                .addOnSuccessListener {
                    onSuccess(ref.path)
                }

    }

}