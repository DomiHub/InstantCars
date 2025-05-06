package com.domingo.instantcars

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ChatListAdapter(
    private val context: Context, private val chatList: List<ChatPreview>
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.chat_name)
        val lastMessage: TextView = itemView.findViewById(R.id.chat_last_message)
        val time: TextView = itemView.findViewById(R.id.chat_time)
        val avatar: ImageView = itemView.findViewById(R.id.chat_avatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_select_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount() = chatList.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        val otherUserId = if (chat.user1 == currentUserId) chat.user2 else chat.user1

        holder.lastMessage.text = chat.lastMessage
        holder.time.text = chat.timestamp?.toDate()?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
        } ?: ""

        // Carga nombre del otro usuario
        FirebaseFirestore.getInstance().collection("users").document(otherUserId).get()
            .addOnSuccessListener {
                val nombre = it.getString("nombre") ?: "Usuario"
                holder.name.text = nombre
                //Imagen base64 Avatar
            }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("chatId", chat.chatId)
            intent.putExtra("receiverId", otherUserId)
            context.startActivity(intent)
        }
    }
}
