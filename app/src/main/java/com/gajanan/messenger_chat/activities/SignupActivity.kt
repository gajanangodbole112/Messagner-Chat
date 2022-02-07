package com.gajanan.messenger_chat.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.gajanan.messenger_chat.databinding.ActivitySignupBinding
import com.gajanan.messenger_chat.utils.Constants.DB_COLLECTION_KEY
import com.gajanan.messenger_chat.utils.Constants.KEY_EMAIL
import com.gajanan.messenger_chat.utils.Constants.KEY_IMAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_IS_SIGNED_IN
import com.gajanan.messenger_chat.utils.Constants.KEY_NAME
import com.gajanan.messenger_chat.utils.Constants.KEY_PASSWORD
import com.gajanan.messenger_chat.utils.Constants.KEY_USER_ID
import com.gajanan.messenger_chat.utils.PreferenceManager
import com.gajanan.messenger_chat.utils.showToast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SignupActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignupBinding
    private var encodedImage: String? = null
    lateinit var prefManager: PreferenceManager
    val db = Firebase.firestore
    val pickImage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null) {
                val imgUri: Uri? = result.data?.data
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(imgUri!!)
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.ivProfile.setImageBitmap(bitmap)
                    encodedImage = getEncodedImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager = PreferenceManager(this)
        setOnClickMethods()
    }

    private fun setOnClickMethods() {
        binding.apply {
            tvSignin.setOnClickListener {
                onBackPressed()

            }
            btnSignup.setOnClickListener {
                if (isValidSignupDetail()) {
                    signup()
                }
            }

            ivProfile.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                pickImage.launch(intent)
            }
        }
    }

    private fun signup() {
        loading(true)
        binding.apply {
            val users = hashMapOf(
                KEY_NAME to etNameSignup.text.toString(),
                KEY_EMAIL to etEmailSignup.text.toString(),
                KEY_PASSWORD to etPasswordSignup.text.toString(),
                KEY_IMAGE to encodedImage
            )

            db.collection(DB_COLLECTION_KEY)
                .add(users)
                .addOnSuccessListener { dr ->
                    loading(false)
                    prefManager.putBoolean(KEY_IS_SIGNED_IN,true)
                    prefManager.putString(KEY_USER_ID,dr.id)
                    prefManager.putString(KEY_NAME,etNameSignup.text.toString())
                    prefManager.putString(KEY_IMAGE,encodedImage!!)
                    val intent = Intent(applicationContext, MainActivity::class.java)
                       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or
                       Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { error ->
                    loading(false)
                    showToast(error.message.toString())
                }
        }

    }

    private fun getEncodedImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val prewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap: Bitmap = Bitmap.createScaledBitmap(
            bitmap,
            previewWidth,
            prewHeight,
            false
        )

        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val byte: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byte, Base64.DEFAULT)
    }


    private fun isValidSignupDetail(): Boolean {
        binding.apply {
            if (encodedImage == null) {
                showToast("select profile Image")
                return false
            } else if (etNameSignup.text.toString().isEmpty()) {
                showToast("Enter Name")
                return false
            } else if (etEmailSignup.text.toString().trim().isEmpty()) {
                showToast("Enter Email")
                return false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(etEmailSignup.text.toString()).matches()) {
                showToast("Enter Valid Email")
                return false
            } else if (etPasswordSignup.text.toString().trim().isEmpty()) {
                showToast("Enter Password")
                return false
            } else if (etRePasswordSignup.text.toString().trim().isEmpty()) {
                showToast("Enter Comfirm Password")
                return false
            } else if (!etPasswordSignup.text.toString().trim()
                    .equals(etRePasswordSignup.text.toString().trim())
            ) {
                showToast("Please enter same password")
                return false
            } else {
                return true
            }
        }

    }

    private fun loading(isloading: Boolean) {
        if (isloading) {
            binding.btnSignup.visibility = View.INVISIBLE
            binding.progressbar.visibility = View.VISIBLE
        } else {
            binding.btnSignup.visibility = View.VISIBLE
            binding.progressbar.visibility = View.INVISIBLE
        }
    }
}