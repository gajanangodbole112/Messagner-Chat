package com.gajanan.messenger_chat.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gajanan.messenger_chat.databinding.ItemlistUserBinding
import com.gajanan.messenger_chat.listeners.UserListener
import com.gajanan.messenger_chat.models.User
import java.util.*

class UserAdapter(
    var list : List<User>,
   var onItemClick : UserListener
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {


    class UserViewHolder(var binding : ItemlistUserBinding)
        : RecyclerView.ViewHolder(binding.root) {

            fun bind(user : User) {

                binding.apply {
                    tvUserName.text = user.name
                    tvEmail.text = user.email
                    ivProfileImg.setImageBitmap(getUserImage(user.image!!))

                }

            }

       private fun getUserImage(encodedImage : String):Bitmap{
            val bytes : ByteArray =Base64.decode(encodedImage,Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes,0,bytes.size)

        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):UserViewHolder =
        UserViewHolder(
            ItemlistUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val userlist = list[position]

        holder.bind(userlist)
        holder.itemView.setOnClickListener {
            onItemClick.onUserClicked(userlist)
        }
    }

    override fun getItemCount(): Int  = list.size
}