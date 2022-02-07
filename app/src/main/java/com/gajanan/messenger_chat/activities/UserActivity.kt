package com.gajanan.messenger_chat.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gajanan.messenger_chat.adapter.UserAdapter
import com.gajanan.messenger_chat.databinding.ActivityUserBinding
import com.gajanan.messenger_chat.listeners.UserListener
import com.gajanan.messenger_chat.models.User
import com.gajanan.messenger_chat.utils.Constants.DB_COLLECTION_KEY
import com.gajanan.messenger_chat.utils.Constants.KEY_EMAIL
import com.gajanan.messenger_chat.utils.Constants.KEY_FCM_TOKEN
import com.gajanan.messenger_chat.utils.Constants.KEY_IMAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_NAME
import com.gajanan.messenger_chat.utils.Constants.KEY_USER
import com.gajanan.messenger_chat.utils.Constants.KEY_USER_ID
import com.gajanan.messenger_chat.utils.PreferenceManager
import com.gajanan.messenger_chat.utils.showToast
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserActivity :BaseActivty(),UserListener {

    lateinit var binding: ActivityUserBinding
    lateinit var prefManager: PreferenceManager
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PreferenceManager(applicationContext)

        onClickMethods()
        getAllUsers()
    }

    private fun onClickMethods() {
        binding.apply {
            ivBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun getAllUsers() {
        loading(true)
        db.collection(DB_COLLECTION_KEY)
            .get()
            .addOnCompleteListener { task ->
                loading(false)
                val userid = prefManager.getString(KEY_USER_ID)
                if (task.isSuccessful && task.result != null) {
                    val userlist = arrayListOf<User>()
                    for (qs: QueryDocumentSnapshot in task.result) {
                        if (userid == qs.id) {
                            continue
                        }
                        userlist.add(
                            User(
                                id = qs.id,
                                name = qs.getString(KEY_NAME),
                                email = qs.getString(KEY_EMAIL),
                                image = qs.getString(KEY_IMAGE),
                                token = qs.getString(KEY_FCM_TOKEN)
                            )
                        )
                    }
                    if (userlist.size > 0) {
                        val adapter = UserAdapter(userlist,this)
                        binding.rvAllUser.adapter = adapter
                        binding.rvAllUser.visibility = View.VISIBLE

                    } else {
                        showError()
                    }
                }else{
                    showError()
                }
            }

    }

    private fun showError() {
        binding.tvErrorMsg.text = String.format("%s", "No User available")
        binding.tvErrorMsg.visibility = View.VISIBLE
    }

    private fun loading(isloading: Boolean) {
        binding.apply {
            progressbar.visibility = if (isloading)
                View.VISIBLE
            else
                View.INVISIBLE

        }


    }

    override fun onUserClicked(user : User) {
        val intent = Intent(applicationContext,ChatRoomActivty::class.java)
       intent.putExtra(KEY_USER,user)
        startActivity(intent)
        finish()
    }
}