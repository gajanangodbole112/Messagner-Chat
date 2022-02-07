package com.gajanan.messenger_chat.activities

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.gajanan.messenger_chat.utils.Constants.DB_COLLECTION_KEY
import com.gajanan.messenger_chat.utils.Constants.KEY_AVAILABILITY
import com.gajanan.messenger_chat.utils.Constants.KEY_USER_ID
import com.gajanan.messenger_chat.utils.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

open class BaseActivty :AppCompatActivity() {

    lateinit var dr : DocumentReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         val prefManager = PreferenceManager(applicationContext)
        val db = Firebase.firestore

      dr =   db.collection(DB_COLLECTION_KEY)
            .document(prefManager.getString(KEY_USER_ID))
    }

    override fun onPause() {
        super.onPause()
        dr.update(KEY_AVAILABILITY,0)
    }

    override fun onResume() {
        super.onResume()
        dr.update(KEY_AVAILABILITY,1)

    }

}