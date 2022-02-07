package com.gajanan.messenger_chat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gajanan.messenger_chat.databinding.ActivitySigninBinding
import com.gajanan.messenger_chat.utils.Constants.DB_COLLECTION_KEY
import com.gajanan.messenger_chat.utils.Constants.KEY_EMAIL
import com.gajanan.messenger_chat.utils.Constants.KEY_IMAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_IS_SIGNED_IN
import com.gajanan.messenger_chat.utils.Constants.KEY_NAME
import com.gajanan.messenger_chat.utils.Constants.KEY_PASSWORD
import com.gajanan.messenger_chat.utils.Constants.KEY_USER_ID
import com.gajanan.messenger_chat.utils.PreferenceManager
import com.gajanan.messenger_chat.utils.showToast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SigninActivity : AppCompatActivity() {
    lateinit var binding: ActivitySigninBinding
    lateinit var prefManager: PreferenceManager
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefManager = PreferenceManager(applicationContext)
        if (prefManager.getBoolean(KEY_IS_SIGNED_IN)) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOnClickMethods()
    }

    private fun setOnClickMethods() {
        binding.apply {
            tvCreateAccount.setOnClickListener {
                startActivity(Intent(this@SigninActivity, SignupActivity::class.java))
            }
            btnSignin.setOnClickListener {
                if (isValidSigninDetails()) {
                    signin()
                }
            }
        }
    }

    private fun signin() {
        binding.apply {
            loading(true)
            db.collection(DB_COLLECTION_KEY)
                .whereEqualTo(KEY_EMAIL, etEmailSignin.text.toString())
                .whereEqualTo(KEY_PASSWORD, etPasswordSignin.text.toString())
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful &&
                        task.result != null &&
                        task.result.documents.size > 0
                    ) {

                        val ds: DocumentSnapshot = task.result.documents[0]
                        prefManager.putBoolean(KEY_IS_SIGNED_IN, true)
                        prefManager.putString(KEY_USER_ID, ds.id)
                        prefManager.putString(KEY_NAME, ds.getString(KEY_NAME)!!)
                        prefManager.putString(KEY_IMAGE, ds.getString(KEY_IMAGE)!!)

                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                        startActivity(intent)
                        finish()
                    } else {
                        loading(false)
                        showToast("unable to signIn")
                    }

                }

        }
    }

    private fun loading(isloading: Boolean) {
        binding.apply {
            if (isloading) {
                btnSignin.visibility = View.INVISIBLE
                progressbar.visibility = View.VISIBLE
            } else {
                btnSignin.visibility = View.VISIBLE
                progressbar.visibility = View.INVISIBLE
            }
        }
    }

    private fun isValidSigninDetails(): Boolean {
        binding.apply {
            if (etEmailSignin.text.toString().trim().isEmpty()) {
                showToast("Enter Email")
                return false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(etEmailSignin.text.toString()).matches()) {
                showToast("Enter Valid Email")
                return false
            } else if (etPasswordSignin.text.toString().trim().isEmpty()) {
                showToast("Enter Password")
                return false
            } else {
                return true
            }
        }
    }
}