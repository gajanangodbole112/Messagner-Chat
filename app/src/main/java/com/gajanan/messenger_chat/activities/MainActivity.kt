package com.gajanan.messenger_chat.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gajanan.messenger_chat.adapter.RecentConversationAdapter
import com.gajanan.messenger_chat.databinding.ActivityMainBinding
import com.gajanan.messenger_chat.listeners.ConversationListener
import com.gajanan.messenger_chat.models.ChatMessage
import com.gajanan.messenger_chat.models.User
import com.gajanan.messenger_chat.utils.Constants.DB_COLLECTION_KEY
import com.gajanan.messenger_chat.utils.Constants.KEY_COLLECTION_CONVERSATION
import com.gajanan.messenger_chat.utils.Constants.KEY_FCM_TOKEN
import com.gajanan.messenger_chat.utils.Constants.KEY_IMAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_LAST_MESSAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_NAME
import com.gajanan.messenger_chat.utils.Constants.KEY_RECEIVER_ID
import com.gajanan.messenger_chat.utils.Constants.KEY_RECEIVER_IMAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_RECEIVER_NAME
import com.gajanan.messenger_chat.utils.Constants.KEY_SENDER_ID
import com.gajanan.messenger_chat.utils.Constants.KEY_SENDER_IMAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_SENDER_NAME
import com.gajanan.messenger_chat.utils.Constants.KEY_TIMESTAMP
import com.gajanan.messenger_chat.utils.Constants.KEY_USER
import com.gajanan.messenger_chat.utils.Constants.KEY_USER_ID
import com.gajanan.messenger_chat.utils.PreferenceManager
import com.gajanan.messenger_chat.utils.showToast
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : BaseActivty(),ConversationListener {

    lateinit var binding: ActivityMainBinding
    private lateinit var prefmanager: PreferenceManager
    private val db = Firebase.firestore
 var  recentlist : ArrayList<ChatMessage> = ArrayList()
   lateinit var chatMessage: ChatMessage
    lateinit var recentConversationAdapter: RecentConversationAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefmanager = PreferenceManager(applicationContext)

        loadUserDetails()
        getToken()
        onClickMethods()
        listenerConversation()

        recentConversationAdapter = RecentConversationAdapter(recentlist,this)
        binding.rvRecentConversation.adapter = recentConversationAdapter
    }

    private fun onClickMethods() {
        binding.apply {
            ivSignout.setOnClickListener {
                signOut()
            }
            fbAdd.setOnClickListener {
                startActivity(Intent(applicationContext, UserActivity::class.java))
            }
        }

    }


    private fun loadUserDetails() {
        binding.apply {
            tvUsername.text = prefmanager.getString(KEY_NAME)

            val bytes: ByteArray? = Base64.decode(
                prefmanager.getString(KEY_IMAGE),
                Base64.DEFAULT
            )
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes!!.size
            )
            ivProfileImg.setImageBitmap(bitmap)
        }
    }

    private fun listenerConversation(){
        db.collection(KEY_COLLECTION_CONVERSATION)
            .whereEqualTo(KEY_SENDER_ID,prefmanager.getString(KEY_USER_ID))
            .addSnapshotListener(eventlistener)

        db.collection(KEY_COLLECTION_CONVERSATION)
            .whereEqualTo(KEY_RECEIVER_ID,prefmanager.getString(KEY_USER_ID))
            .addSnapshotListener(eventlistener)
    }
    private val eventlistener: EventListener<QuerySnapshot> = EventListener { value, error ->

        error?.let {
            return@EventListener
        }
         value?.let {
            for (ds : DocumentChange in it.documentChanges) {
                if (ds.type == DocumentChange.Type.ADDED) {
                    val senderId = ds.document.getString(KEY_SENDER_ID)
                    val receiverId = ds.document.getString(KEY_RECEIVER_ID)
                    chatMessage = ChatMessage()
                       chatMessage. senderId = senderId!!
                      chatMessage.  receiverId = receiverId!!


                    if (prefmanager.getString(KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversationImg = ds.document.getString(KEY_RECEIVER_IMAGE)!!
                        chatMessage.conversationName = ds.document.getString(KEY_RECEIVER_NAME)!!
                        chatMessage.conversationId = ds.document.getString(KEY_RECEIVER_ID)!!

                    } else {
                        chatMessage.conversationImg = ds.document.getString(KEY_SENDER_IMAGE)!!
                        chatMessage.conversationName = ds.document.getString(KEY_SENDER_NAME)!!
                        chatMessage.conversationId = ds.document.getString(KEY_SENDER_ID)!!
                    }
                    chatMessage.message = ds.document.getString(KEY_LAST_MESSAGE)!!
                    chatMessage.date = ds.document.getDate(KEY_TIMESTAMP)!!

                        recentlist.add(chatMessage)

                } else if (ds.type == DocumentChange.Type.MODIFIED) {
                    for (i in recentlist.indices) {
                        val senderId: String = ds.document.getString(KEY_SENDER_ID)!!
                        val receiverId: String = ds.document.getString(KEY_RECEIVER_ID)!!
                        if (recentlist[i].senderId.equals(senderId) &&
                            recentlist[i].receiverId.equals(receiverId)
                        ) {
                            recentlist[i].message = ds.document.getString(KEY_LAST_MESSAGE)!!
                            recentlist[i].date = ds.document.getDate(KEY_TIMESTAMP)!!
                            break
                        }
                    }
                }
            }

            //this 1 line of code can sort list with date
            recentlist.sortWith { o1, o2 -> o1.date!!.compareTo(o2.date) }

            recentConversationAdapter.notifyDataSetChanged()
            binding.rvRecentConversation.smoothScrollToPosition(0)
            binding.rvRecentConversation.visibility = View.VISIBLE
            binding.progressbar.visibility = View.GONE

        }

    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener(this::updateToken)
            .addOnFailureListener { }
    }

    private fun updateToken(token: String) {
        prefmanager.putString(KEY_FCM_TOKEN,token)
        val dr = db.collection(DB_COLLECTION_KEY)
            .document(prefmanager.getString(KEY_USER_ID))

        dr.update(KEY_FCM_TOKEN, token)
            .addOnFailureListener {
                showToast("unable to update token")
            }
    }

    private fun signOut() {
        showToast("SignOut")
        val userUpdate: HashMap<String, Any> = HashMap()
        userUpdate[KEY_FCM_TOKEN] = FieldValue.delete()
        db.collection(DB_COLLECTION_KEY)
            .document(
                prefmanager.getString(KEY_USER_ID)
            ).update(userUpdate)
            .addOnSuccessListener {
                prefmanager.clear()
                startActivity(Intent(applicationContext, SigninActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                showToast("unable to signout")
            }

    }

    override fun onConversationClicked(user: User) {
       val intent = Intent(applicationContext,ChatRoomActivty::class.java)
        intent.putExtra(KEY_USER,user)
        startActivity(intent)
    }
}

