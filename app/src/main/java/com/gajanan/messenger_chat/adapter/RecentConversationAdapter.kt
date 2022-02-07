package com.gajanan.messenger_chat.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gajanan.messenger_chat.databinding.ItemlistRecentUserBinding
import com.gajanan.messenger_chat.listeners.ConversationListener
import com.gajanan.messenger_chat.models.ChatMessage
import com.gajanan.messenger_chat.models.User

class RecentConversationAdapter(
    var list: List<ChatMessage>,
    var conversationListener: ConversationListener
) :
    RecyclerView.Adapter<RecentConversationAdapter.RecentViewHolder>() {
    class RecentViewHolder(
        var binding: ItemlistRecentUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ChatMessage) {
            binding.apply {
                ivProfileImg.setImageBitmap(getConversationImage(chatMessage.conversationImg!!))
                tvRecentMsg.text = chatMessage.message
                tvUserName.text = chatMessage.conversationName
            }
        }

        private fun getConversationImage(encodedImage: String): Bitmap {
            val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentViewHolder = RecentViewHolder(
        ItemlistRecentUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        holder: RecentViewHolder,
        position: Int
    ) {
       val listItem = list[position]
        holder.bind(listItem)
        holder.itemView.setOnClickListener {
            val user = User()
            user.id = listItem.conversationId
            user.name = listItem.conversationName
            user.image = listItem.conversationImg

            conversationListener.onConversationClicked(user)
        }
    }

    override fun getItemCount(): Int = list.size


}