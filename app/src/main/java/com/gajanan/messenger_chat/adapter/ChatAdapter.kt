package com.gajanan.messenger_chat.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gajanan.messenger_chat.databinding.ItemReceiveMsgBinding
import com.gajanan.messenger_chat.databinding.ItemSendImageBinding
import com.gajanan.messenger_chat.databinding.ItemSendMessageBinding
import com.gajanan.messenger_chat.models.ChatMessage
import com.gajanan.messenger_chat.models.ImageMessage

class ChatAdapter(
    var chatlist : List<ChatMessage>,
    var receiverImg : Bitmap,
    var senderId : String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val  VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVE = 2
    private val VIEW_IMAGE_SENT = 3
    private val VIEW_IMAGE_RECEIVE = 4
    class SendMsgViewHolder(var sendBinding :ItemSendMessageBinding )
        : RecyclerView.ViewHolder(sendBinding.root){
            fun sendData(chatMsg : ChatMessage){
                sendBinding.apply {
                    tvSendMsg.text = chatMsg.message
                    tvTime.text = chatMsg.time
                }
            }
        }

    class ReceiveMsgViewHolder(var receiveBinding : ItemReceiveMsgBinding )
        : RecyclerView.ViewHolder(receiveBinding.root){
        fun receiveData(chatMsg : ChatMessage, receiveImage: Bitmap){
            receiveBinding.apply {
                tvReceiveMsg.text = chatMsg.message
                tvTime.text = chatMsg.time
                ivReceiverImg.setImageBitmap(receiveImage)
            }
        }
    }
class SendImage(var sendImgBinding : ItemSendImageBinding)
    : RecyclerView.ViewHolder(sendImgBinding.root){
        fun sendImgData(imgMsg : ImageMessage){


        }
    }
    override fun getItemViewType(position: Int): Int {
            if (chatlist[position].senderId.equals(senderId)){
                return VIEW_TYPE_SENT
            }else{
                return VIEW_TYPE_RECEIVE
            }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      if (viewType == VIEW_TYPE_SENT){
          return SendMsgViewHolder(
              ItemSendMessageBinding.inflate(
                  LayoutInflater.from(parent.context),
                  parent,
                  false
              )
          )
      }else{
          return ReceiveMsgViewHolder(
              ItemReceiveMsgBinding.inflate(
                  LayoutInflater.from(parent.context),
                  parent,
                  false
              )
          )
      }
        if (viewType == VIEW_IMAGE_SENT){
            return ReceiveMsgViewHolder(
                ItemReceiveMsgBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       if (getItemViewType(position) == VIEW_TYPE_SENT){
           (holder as SendMsgViewHolder).sendData(chatlist[position])
       }else{
           (holder as ReceiveMsgViewHolder).receiveData(chatlist[position],receiverImg)
       }
    }

    override fun getItemCount(): Int = chatlist.size
}