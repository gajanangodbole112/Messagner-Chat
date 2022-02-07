package com.gajanan.messenger_chat.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View

import com.gajanan.messenger_chat.adapter.ChatAdapter
import com.gajanan.messenger_chat.databinding.ActivityChatRoomActivtyBinding
import com.gajanan.messenger_chat.firebase.StorageUtils
import com.gajanan.messenger_chat.models.ChatMessage
import com.gajanan.messenger_chat.models.ImageMessage
import com.gajanan.messenger_chat.models.User

import com.gajanan.messenger_chat.network.GetApi
import com.gajanan.messenger_chat.utils.Constants.DB_COLLECTION_KEY
import com.gajanan.messenger_chat.utils.Constants.KEY_AVAILABILITY
import com.gajanan.messenger_chat.utils.Constants.KEY_COLLECTION_CHAT
import com.gajanan.messenger_chat.utils.Constants.KEY_COLLECTION_CONVERSATION
import com.gajanan.messenger_chat.utils.Constants.KEY_FCM_TOKEN
import com.gajanan.messenger_chat.utils.Constants.KEY_IMAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_LAST_MESSAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_MESSAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_NAME
import com.gajanan.messenger_chat.utils.Constants.KEY_RECEIVER_ID
import com.gajanan.messenger_chat.utils.Constants.KEY_RECEIVER_IMAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_RECEIVER_NAME
import com.gajanan.messenger_chat.utils.Constants.KEY_SENDER_ID
import com.gajanan.messenger_chat.utils.Constants.KEY_SENDER_IMAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_SENDER_NAME
import com.gajanan.messenger_chat.utils.Constants.KEY_SEND_IMAGE
import com.gajanan.messenger_chat.utils.Constants.KEY_TIMESTAMP
import com.gajanan.messenger_chat.utils.Constants.KEY_USER
import com.gajanan.messenger_chat.utils.Constants.KEY_USER_ID
import com.gajanan.messenger_chat.utils.Constants.REMOTE_MSG_DATA
import com.gajanan.messenger_chat.utils.Constants.REMOTE_MSG_REGISTRATION_IDS
import com.gajanan.messenger_chat.utils.PreferenceManager
import com.gajanan.messenger_chat.utils.getRemoteMsgHeaders
import com.gajanan.messenger_chat.utils.showToast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ChatRoomActivty : BaseActivty() {

    private lateinit var binding: ActivityChatRoomActivtyBinding
    private lateinit var receiveUser: User
    lateinit var chatlist: ArrayList<ChatMessage>
    lateinit var chatAdapter: ChatAdapter
    private val db = Firebase.firestore
    lateinit var prefManager: PreferenceManager
    var conversationId: String? = null
    var isReceiverAvailable: Boolean = false

    private val REQUEST_CODE =121




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomActivtyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager = PreferenceManager(applicationContext)
        loadReceiverDetails()
        onClickMethods()


        chatlist = ArrayList()
        chatAdapter = ChatAdapter(
            chatlist,
            getReceiverImgEncoded(receiveUser.image!!),
            prefManager.getString(KEY_USER_ID)
        )

        binding.apply {
            rvChatRoom.adapter = chatAdapter

        }

        listenerMsg()
    }

    private fun sendMessages() {
        val message = HashMap<String, Any>()
        message[KEY_SENDER_ID] = prefManager.getString(KEY_USER_ID)
        message[KEY_RECEIVER_ID] = receiveUser.id!!
        message[KEY_MESSAGE] = binding.etMsg.text.toString()
        message[KEY_TIMESTAMP] = Date()

        db.collection(KEY_COLLECTION_CHAT)
            .add(message)
        if (conversationId != null) {
            updateConversation(binding.etMsg.text.toString())
        } else {
            val conersation: HashMap<String, Any> = HashMap()
            conersation[KEY_SENDER_ID] = prefManager.getString(KEY_USER_ID)
            conersation[KEY_SENDER_NAME] = prefManager.getString(KEY_NAME)
            conersation[KEY_SENDER_IMAGE] = prefManager.getString(KEY_IMAGE)
            conersation[KEY_RECEIVER_ID] = receiveUser.id!!
            conersation[KEY_RECEIVER_NAME] = receiveUser.name!!
            conersation[KEY_RECEIVER_IMAGE] = receiveUser.image!!
            conersation[KEY_LAST_MESSAGE] = binding.etMsg.text.toString()
            conersation[KEY_TIMESTAMP] = Date()

            addConversations(conersation)
        }
        if (!isReceiverAvailable) {
            try {
                val tokens = JSONArray()
                tokens.put(receiveUser.token)

                val data = JSONObject()
                data.put(KEY_USER_ID, prefManager.getString(KEY_USER_ID))
                data.put(KEY_NAME, prefManager.getString(KEY_NAME))
                data.put(KEY_FCM_TOKEN, prefManager.getString(KEY_FCM_TOKEN))
                data.put(KEY_MESSAGE, binding.etMsg.text.toString())

                val body = JSONObject()
                body.put(REMOTE_MSG_DATA, data)
                body.put(REMOTE_MSG_REGISTRATION_IDS, tokens)

                sendNotifications(body.toString())


            } catch (e: Exception) {
                showToast("FCM1 ${e.message.toString()}")
            }
        }
        binding.etMsg.text = null
    }

    private fun sendNotifications(message: String) {
        GetApi.retrofitService.sendMessage(
            getRemoteMsgHeaders(),
            message
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.isSuccessful) {
                    try {
                        if (response.body() != null) {
                            val responseJson = JSONObject(response.body()!!)
                            val result: JSONArray = responseJson.getJSONArray("result")
                            if (responseJson.getInt("failure") == 1) {
                                val error: JSONObject = result[0] as JSONObject
                                showToast("FCM2 ${error.getString("error")}")
                                return
                            }
                        }
                        showToast("Notification send successfully!!")

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    showToast("error ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                showToast(t.message.toString())
            }

        })
    }

    private fun listenAvailablityOfReceiver() {
        db.collection(DB_COLLECTION_KEY)
            .document(receiveUser.id!!)
            .addSnapshotListener { value, error ->

                error?.let {
                    return@addSnapshotListener
                }
                value?.let { result ->
                    result.getLong(KEY_AVAILABILITY)?.let {
                        val availablity: Int? = Objects.requireNonNull(
                            result.getLong(KEY_AVAILABILITY)
                        )?.toInt()

                        isReceiverAvailable = availablity == 1
                    }
                    receiveUser.token = value.getString(KEY_FCM_TOKEN)
                }
                if (isReceiverAvailable) {
                    binding.tvAvailability.visibility = View.VISIBLE
                } else {
                    binding.tvAvailability.visibility = View.GONE
                }


            }

    }

    private fun listenerMsg() {
        db.collection(KEY_COLLECTION_CHAT)
            .whereEqualTo(KEY_SENDER_ID, prefManager.getString(KEY_USER_ID))
            .whereEqualTo(KEY_RECEIVER_ID, receiveUser.id)
            .addSnapshotListener(eventlistener)
        db.collection(KEY_COLLECTION_CHAT)
            .whereEqualTo(KEY_SENDER_ID, receiveUser.id)
            .whereEqualTo(KEY_RECEIVER_ID, prefManager.getString(KEY_USER_ID))
            .addSnapshotListener(eventlistener)
    }

    private val eventlistener: EventListener<QuerySnapshot> = EventListener { value, error ->
        error?.let {
            return@EventListener
        }
        value?.let {
            val count = chatlist.size
            for (ds in value.documentChanges) {
                if (ds.type == DocumentChange.Type.ADDED) {
                    val chatMessage = ChatMessage()
                    chatMessage.senderId = ds.document.getString(KEY_SENDER_ID)!!
                    chatMessage.receiverId = ds.document.getString(KEY_RECEIVER_ID)!!
                    chatMessage.message = ds.document.getString(KEY_MESSAGE)!!
                    chatMessage.time = getDateTime(ds.document.getDate(KEY_TIMESTAMP)!!)
                    chatMessage.date = ds.document.getDate(KEY_TIMESTAMP)!!


                    chatlist.add(chatMessage)
                }
            }
            chatlist.sortWith { o1, o2 -> o1.date!!.compareTo(o2.date) }
            if (count == 0) {
                chatAdapter.notifyDataSetChanged()
            } else {
                chatAdapter.notifyItemRangeInserted(chatlist.size, chatlist.size)
                binding.rvChatRoom.smoothScrollToPosition(chatlist.size - 1)
            }

            binding.rvChatRoom.visibility = View.VISIBLE
        }

        binding.progressbar.visibility = View.GONE
        if (conversationId == null) {
            checkForConversation()
        }
    }

    private fun getReceiverImgEncoded(img: String): Bitmap {
        val bytes: ByteArray = Base64.decode(img, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadReceiverDetails() {
        receiveUser = intent.getSerializableExtra(KEY_USER) as User
        binding.apply {
            tvReceiverName.text = receiveUser.name
        }
    }

    private fun onClickMethods() {
        binding.apply {
            ivBack.setOnClickListener {
                onBackPressed()
            }
            layoutSend.setOnClickListener {
                sendMessages()
            }
            viewImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_CODE)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){

                val selectImagePath = data?.data
            val selectImgBitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectImagePath)
            val outputStream = ByteArrayOutputStream()
            selectImgBitmap.compress(Bitmap.CompressFormat.JPEG,90,outputStream)
            val selectImgBytes = outputStream.toByteArray()

            StorageUtils.uploadMessageImg(selectImgBytes){
                 imagePath->

                val sendImg = HashMap<String, Any>()
                sendImg[KEY_SENDER_ID] = prefManager.getString(KEY_USER_ID)
                sendImg[KEY_RECEIVER_ID] = receiveUser.id!!
                sendImg[KEY_SEND_IMAGE] = imagePath
                sendImg[KEY_TIMESTAMP] = Date()

                db.collection(KEY_COLLECTION_CHAT)
                    .document("image")
                    .collection("chatImage")
                    .add(sendImg)

            }
        }
    }
    private fun getDateTime(date: Date): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }

    private fun addConversations(conversation: HashMap<String, Any>) {

        db.collection(KEY_COLLECTION_CONVERSATION)
            .add(conversation)
            .addOnSuccessListener { dr ->
                conversationId = dr.id

            }
    }

    private fun updateConversation(message: String) {
        db.collection(KEY_COLLECTION_CONVERSATION)
            .document(conversationId!!)
            .update(
                KEY_LAST_MESSAGE,
                message,
                KEY_TIMESTAMP,
                Date()
            )
    }

    private fun checkForConversation() {
        if (chatlist.size != 0) {
            checkForConversationRemotely(
                prefManager.getString(KEY_USER_ID),
                receiveUser.id!!
            )
            checkForConversationRemotely(
                receiveUser.id!!,
                prefManager.getString(KEY_USER_ID)
            )
        }
    }

    private fun checkForConversationRemotely(senderId: String, receiverId: String) {
        db.collection(KEY_COLLECTION_CONVERSATION)
            .whereEqualTo(KEY_SENDER_ID, senderId)
            .whereEqualTo(KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(conversationOnCompleteListener)
    }

    private val conversationOnCompleteListener = OnCompleteListener<QuerySnapshot> { task ->
        if (task.isSuccessful && task.result != null &&
            task.result.documents.size > 0
        ) {
            val ds: DocumentSnapshot = task.result.documents.get(0)
            conversationId = ds.id
        }
    }

    override fun onResume() {
        super.onResume()
        listenAvailablityOfReceiver()
    }

}